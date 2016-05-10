package org.namelessrom.devicecontrol.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.models.WebServerConfig;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.HtmlHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.amartinz.hardware.device.Device;
import at.amartinz.hardware.utils.HwIoUtils;
import timber.log.Timber;

/**
 * A wrapper for the AsyncHttpServer
 */
public class ServerWrapper {
    public static final String ACTION_CONNECTED = "---CONNECTED---";
    public static final String ACTION_TERMINATING = "---TERMINATING---";

    public boolean isStopped = false;

    private static final ArrayList<WebSocket> _sockets = new ArrayList<>();

    private AsyncHttpServer mServer;
    private AsyncServerSocket mServerSocket;

    private final WebServerService mService;

    private WebServerConfig webServerConfig;

    public ServerWrapper(final WebServerService service) {
        mService = service;
    }

    public void stopServer() {
        unregisterReceivers();

        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
        if (mServerSocket != null) {
            mServerSocket.stop();
            mServerSocket = null;
        }
        for (final WebSocket socket : _sockets) {
            if (socket == null) { continue; }
            socket.send(ACTION_TERMINATING);
            socket.close();
        }
        _sockets.clear();

        isStopped = true;
    }

    public void createServer() {
        /*Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                createServerAsync();
            }
        });
        thread.start();*/
        createServerAsync();
    }

    private void createServerAsync() {
        if (mServer != null) {
            return;
        }
        webServerConfig = WebServerConfig.get();

        mServer = new AsyncHttpServer();
        Timber.v("[!] Server created");

        setupFonts();
        Timber.v("[!] Setup fonts");

        setupWebSockets();
        Timber.v("[!] Setup websockets");

        setupApi();
        Timber.v("[!] Setup api");

        mServer.directory(App.get(), "/license", "license.html");
        Timber.v("[!] Setup route: /license");

        mServer.get("/files", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {
                res.redirect("/files/");
            }
        });
        mServer.get("/files/(?s).*", filesCallback);
        Timber.v("[!] Setup route: /files/(?s).*");

        mServer.get("/information", informationCallback);
        Timber.v("[!] Setup route: /information");

        // should be always the last, matches anything that the stuff above did not
        mServer.get("/(?s).*", mainCallback);
        Timber.v("[!] Setup route: /");

        mServerSocket = mServer.listen(WebServerConfig.get().port);

        mService.setNotification(null);

        registerReceivers();
    }

    private final HttpServerRequestCallback mainCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {
            if (!shouldPass(req, res)) {
                return;
            }
            Timber.v("[+] Received connection from: %s", req.getHeaders().get("User-Agent"));
            final String path = remapPath(req.getPath());
            res.getHeaders().set("Content-Type", ContentTypes.getInstance().getContentType(path));

            final InputStream is = HtmlHelper.loadPath(path);
            if (is != null) {
                try {
                    res.sendStream(is, is.available());
                    return;
                } catch (IOException ioe) {
                    Timber.e(ioe, "Error!");
                } finally {
                    HwIoUtils.closeQuietly(is);
                }
            }
            res.send(HtmlHelper.loadPathAsString(path));
        }
    };

    private final HttpServerRequestCallback filesCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {
            if (!shouldPass(req, res)) {
                return;
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                res.send("sdcard not mounted!");
                return;
            }
            boolean isDirectory = true;
            final String filePath = HtmlHelper.urlDecode(req.getPath()).replace("/files/", "");
            Timber.v("req.getPath(): %s", req.getPath());
            Timber.v("filePath: %s", filePath);
            File file;
            String sdRoot;
            if (webServerConfig.root) {
                file = new File("/");
                sdRoot = "";
            } else {
                file = Environment.getExternalStorageDirectory();
                sdRoot = file.getAbsolutePath();
            }
            if (filePath != null && !filePath.isEmpty()) {
                file = new File(file, filePath);
                if (file.exists()) {
                    isDirectory = file.isDirectory();
                } else {
                    res.send("File or directory does not exist!");
                    return;
                }
            }
            if (isDirectory) {
                final File[] fs = file.listFiles();
                if (fs == null) {
                    res.send("An error occured!");
                    return;
                }
                final List<File> directories = new ArrayList<>();
                final List<File> files = new ArrayList<>();
                for (final File f : fs) {
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            directories.add(f);
                        } else {
                            files.add(f);
                        }
                    }
                }

                final ArrayList<FileEntry> fileEntries = new ArrayList<>();
                if (directories.size() > 0) {
                    Collections.sort(directories, SortHelper.sFileComparator);
                    for (final File f : directories) {
                        fileEntries.add(new FileEntry(f.getName(), f.getAbsolutePath().replace(sdRoot, ""), true));
                    }
                }
                if (files.size() > 0) {
                    Collections.sort(files, SortHelper.sFileComparator);
                    for (final File f : files) {
                        fileEntries.add(new FileEntry(f.getName(), f.getAbsolutePath().replace(sdRoot, ""), false));
                    }
                }

                res.send(new Gson().toJson(fileEntries));
            } else {
                final String contentType = ContentTypes.getInstance().getContentType(file.getAbsolutePath());
                Timber.v("Requested file: %s", file.getName());
                Timber.v("Content-Type: %s", contentType);
                res.setContentType(contentType);
                res.sendFile(file);
            }
        }
    };

    private static final HttpServerRequestCallback informationCallback = new HttpServerRequestCallback() {
        @Override public void onRequest(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {

        }
    };

    private boolean shouldPass(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {
        if (isStopped) {
            res.code(404);
            res.end();
            return false;
        }
        if (!isAuthenticated(req)) {
            res.getHeaders().add("WWW-Authenticate", "Basic realm=\"DeviceControl\"");
            res.code(401);
            res.end();
            return false;
        }
        return true;
    }

    private String remapPath(final String path) {
        if (TextUtils.equals("/", path)) {
            return "index.html";
        }
        return path;
    }

    private void setupFonts() {
        final Context context = mService.getApplicationContext();

        // Bootstrap glyphicons
        mServer.directory(context, "/fonts/glyphicons-halflings-regular.eot", "fonts/glyphicons-halflings-regular.eot");
        mServer.directory(context, "/fonts/glyphicons-halflings-regular.svg", "fonts/glyphicons-halflings-regular.svg");
        mServer.directory(context, "/fonts/glyphicons-halflings-regular.ttf", "fonts/glyphicons-halflings-regular.ttf");
        mServer.directory(context, "/fonts/glyphicons-halflings-regular.woff", "fonts/glyphicons-halflings-regular.woff");

        // FontAwesome
        mServer.directory(context, "/fonts/FontAwesome.otf", "fonts/FontAwesome.otf");
        mServer.directory(context, "/fonts/fontawesome-webfont.eot", "fonts/fontawesome-webfont.eot");
        mServer.directory(context, "/fonts/fontawesome-webfont.svg", "fonts/fontawesome-webfont.svg");
        mServer.directory(context, "/fonts/fontawesome-webfont.ttf", "fonts/fontawesome-webfont.ttf");
        mServer.directory(context, "/fonts/fontawesome-webfont.woff", "fonts/fontawesome-webfont.woff");
    }

    private void setupWebSockets() {
        mServer.websocket("/live", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override public void onConnected(final WebSocket socket, AsyncHttpServerRequest req) {
                _sockets.add(socket);
                if (_sockets.size() == 1) {
                    // first client connected, register receivers
                    registerReceivers();
                }

                socket.setClosedCallback(new CompletedCallback() {
                    @Override public void onCompleted(final Exception ex) {
                        _sockets.remove(socket);
                        if (_sockets.size() == 0) {
                            // No client left, unregister to save battery
                            unregisterReceivers();
                        }
                    }
                });

                socket.setStringCallback(new WebSocket.StringCallback() {
                    @Override public void onStringAvailable(final String s) {
                        Timber.v(s);
                        //noinspection StatementWithEmptyBody
                        if (ACTION_CONNECTED.equals(s)) {
                            //TODO: initializing
                        }
                    }
                });

                socket.send(ACTION_CONNECTED);
            }
        });
    }

    private void setupApi() {
        mServer.get("/api", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) {
                res.redirect("/api/device");
            }
        });

        mServer.get("/api/device", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req, final AsyncHttpServerResponse res) {
                final String result = Device.get(mService).update().toString();
                Timber.v(result);
                res.send(result);
            }
        });
    }

    private boolean isAuthenticated(final AsyncHttpServerRequest req) {
        final boolean isAuth = !webServerConfig.useAuth;
        final String authHeader = req.getHeaders().get("Authorization");
        if (!isAuth && !TextUtils.isEmpty(authHeader)) {
            final String[] parts = new String(Base64.decode(authHeader.replace("Basic", "").trim(), Base64.DEFAULT)).split(":");
            return parts[0] != null
                   && parts[1] != null
                   && parts[0].equals(webServerConfig.username)
                   && parts[1].equals(webServerConfig.password);
        }
        return isAuth;
    }

    private void registerReceivers() {
        if (mService == null) {
            Timber.wtf("mService is null!");
            return;
        }
        final Intent sticky = mService.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // try to preload battery level
        if (sticky != null) {
            mBatteryReceiver.onReceive(mService, sticky);
        }
    }

    private void unregisterReceivers() {
        if (mService == null) {
            Timber.wtf("mService is null!");
            return;
        }
        try {
            mService.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) { }
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            final String level = String.format("batteryLevel|%s", intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1));
            final String charging = String.format("batteryCharging|%s",
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0 ? "1" : "0");
            for (final WebSocket socket : _sockets) {
                socket.send(level);
                socket.send(charging);
            }
        }
    };

    public AsyncHttpServer getServer() { return mServer; }

    public AsyncServerSocket getServerSocket() { return mServerSocket; }

    private class FileEntry {
        public final String name;
        public final String path;
        public final boolean isDirectory;

        public FileEntry(final String name, final String path, final boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
        }
    }

}
