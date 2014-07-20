package org.namelessrom.devicecontrol.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Base64;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.libcore.RequestHeaders;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.HtmlHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper for the AsyncHttpServer
 */
public class ServerWrapper {
    public static final String ACTION_CONNECTED   = "---CONNECTED---";
    public static final String ACTION_TERMINATING = "---TERMINATING---";

    public boolean isStopped = false;
    public StringBuilder mStringBuilder;

    private static final ArrayList<WebSocket> _sockets = new ArrayList<WebSocket>();

    private AsyncHttpServer   mServer;
    private AsyncServerSocket mServerSocket;

    private final WebServerService mService;

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
            if (socket == null) continue;
            socket.send(ACTION_TERMINATING);
            socket.close();
        }
        _sockets.clear();

        isStopped = true;
    }

    public void createServer() {
        if (mServer != null) return;

        registerReceivers();

        mStringBuilder = new StringBuilder();
        mServer = new AsyncHttpServer();
        mStringBuilder.append("[!] Server created!\n");

        setupStaticFiles();
        mStringBuilder.append("[!] Setup static files\n");

        setupWebSockets();
        mStringBuilder.append("[!] Setup websockets\n");

        mServer.directory(Application.applicationContext, "/license", "license.html");
        mStringBuilder.append("[!] Setup route: /license\n");

        mServer.get("/files", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) { res.redirect("/files/"); }
        });
        mServer.get("/files/(?s).*", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) {
                if (isStopped) {
                    res.responseCode(404);
                    res.end();
                    return;
                }
                if (!isAuthenticated(req)) {
                    res.getHeaders().getHeaders().add("WWW-Authenticate",
                            "Basic realm=\"DeviceControl\"");
                    res.responseCode(401);
                    res.end();
                    return;
                }
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    res.send("SDCARD not mounted!");
                    return;
                }
                boolean isDirectory = true;
                final String filePath = HtmlHelper.urlDecode(req.getPath()).replace("/files/", "");
                Logger.v(this, "req.getPath(): " + req.getPath());
                Logger.v(this, "filePath: " + filePath);
                File file;
                String sdRoot;
                if (PreferenceHelper.getBoolean("wfm_root", false)) {
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
                    final StringBuilder sb = new StringBuilder();
                    final List<File> directories = new ArrayList<File>();
                    final List<File> files = new ArrayList<File>();
                    for (final File f : fs) {
                        if (f.exists()) {
                            if (f.isDirectory()) {
                                directories.add(f);
                            } else {
                                files.add(f);
                            }
                        }
                    }

                    sb.append(HtmlHelper.getBreadcrumbs(filePath));
                    final boolean isEmpty = (directories.size() == 0 && files.size() == 0);
                    sb.append("<ul>");
                    if (isEmpty) sb.append("<li>Empty :(</li>");

                    if (directories.size() > 0) {
                        Collections.sort(directories, SortHelper.sFileComparator);
                        for (final File f : directories) {
                            sb.append(HtmlHelper.getDirectoryLine(
                                    HtmlHelper.escapeHtml(f.getAbsolutePath().replace(sdRoot, "")),
                                    f.getName()));
                        }
                    }
                    if (files.size() > 0) {
                        Collections.sort(files, SortHelper.sFileComparator);
                        for (final File f : files) {
                            sb.append(HtmlHelper.getFileLine(
                                    HtmlHelper.escapeHtml(f.getAbsolutePath().replace(sdRoot, "")),
                                    f.getName()));
                        }
                    }

                    sb.append("</ul>");

                    res.send(HtmlHelper.getHtmlContainer("File Manager", sb.toString()));
                } else {
                    final String contentType = ContentTypes.getInstance()
                            .getContentType(file.getAbsolutePath());
                    mStringBuilder.append("Requested file: ").append(file.getName()).append('\n');
                    mStringBuilder.append("Content-Type: ").append(contentType).append('\n');
                    res.setContentType(contentType);
                    res.sendFile(file);
                }
            }
        });
        mStringBuilder.append("[!] Setup route: /files/(?s).*\n");

        mServer.get("/(?s).*", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest req,
                    final AsyncHttpServerResponse res) {
                if (isStopped) {
                    res.responseCode(404);
                    res.end();
                }
                if (!isAuthenticated(req)) {
                    res.getHeaders().getHeaders().add("WWW-Authenticate",
                            "Basic realm=\"DeviceControl\"");
                    res.responseCode(401);
                    res.end();
                    return;
                }
                mStringBuilder.append("[+] Received connection from: ")
                        .append(req.getHeaders().getUserAgent())
                        .append('\n');
                res.send(HtmlHelper.getHtmlContainer("Device Control Web Server",
                        "Welcome to Device Control's web server!<br /><br />" +
                                "This feature is highly experimental at the current stage, " +
                                "you are warned!<br />" +
                                "More to come soon!"
                ));
            }
        });
        mStringBuilder.append("[!] Setup route: /\n");

        final String portString = PreferenceHelper.getString("wfm_port", "8080");
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            port = 8080;
        }
        mServerSocket = mServer.listen(port);

        mService.setNotification(null);
    }

    private void setupStaticFiles() {
        mServer.directory(Application.applicationContext, "/css/bootstrap.min.css",
                "css/bootstrap.min.css");
        mServer.directory(Application.applicationContext, "/css/font-awesome.min.css",
                "css/font-awesome.min.css");
        mServer.directory(Application.applicationContext, "/css/main.css",
                "css/main.css");
        mServer.directory(Application.applicationContext, "/fonts/FontAwesome.otf",
                "fonts/FontAwesome.otf");
        mServer.directory(Application.applicationContext, "/fonts/fontawesome-webfont.eot",
                "fonts/fontawesome-webfont.eot");
        mServer.directory(Application.applicationContext, "/fonts/fontawesome-webfont.svg",
                "fonts/fontawesome-webfont.svg");
        mServer.directory(Application.applicationContext, "/fonts/fontawesome-webfont.ttf",
                "fonts/fontawesome-webfont.ttf");
        mServer.directory(Application.applicationContext, "/fonts/fontawesome-webfont.woff",
                "fonts/fontawesome-webfont.woff");
        mServer.directory(Application.applicationContext, "/js/bootstrap.min.js",
                "js/bootstrap.min.js");
        mServer.directory(Application.applicationContext, "/js/jquery.min.js",
                "js/jquery.min.js");
        mServer.directory(Application.applicationContext, "/js/navigation.js",
                "js/navigation.js");
        mServer.directory(Application.applicationContext, "/js/websocket.js",
                "js/websocket.js");
    }

    private void setupWebSockets() {
        mServer.websocket("/live", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override public void onConnected(final WebSocket webSocket, RequestHeaders headers) {
                _sockets.add(webSocket);
                if (_sockets.size() == 1) {
                    // first client connected, register receivers
                    registerReceivers();
                }

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override public void onCompleted(final Exception ex) {
                        _sockets.remove(webSocket);
                        if (_sockets.size() == 0) {
                            // No client left, unregister to save battery
                            unregisterReceivers();
                        }
                    }
                });

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override public void onStringAvailable(final String s) {
                        Logger.v(this, s);
                        if (ACTION_CONNECTED.equals(s)) {
                            //TODO: initializing
                        }
                    }
                });

                webSocket.send(ACTION_CONNECTED);
            }
        });
    }

    private boolean isAuthenticated(final AsyncHttpServerRequest req) {
        final boolean isAuth = !PreferenceHelper.getBoolean("wfm_auth", true);
        if (req.getHeaders().hasAuthorization() && !isAuth) {
            final String auth = req.getHeaders().getHeaders().get("Authorization");
            if (auth != null && !auth.isEmpty()) {
                final String[] parts = new String(Base64.decode(auth.replace("Basic", "").trim(),
                        Base64.DEFAULT)).split(":");
                return parts[0] != null
                        && parts[0].equals(PreferenceHelper.getString("wfm_username", "root"))
                        && parts[1] != null
                        && parts[1].equals(PreferenceHelper.getString("wfm_password", "toor"));
            }
        }
        return isAuth;
    }

    private void registerReceivers() {
        Application.applicationContext.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void unregisterReceivers() {
        try {
            Application.applicationContext.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) { }
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            final String batteryLevel = String.format("batteryLevel|%s",
                    intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1));
            final String batteryCharging = String.format("batteryCharging|%s",
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0 ? "1" : "0");
            for (final WebSocket socket : _sockets) {
                socket.send(batteryLevel);
                socket.send(batteryCharging);
            }
        }
    };

    public AsyncHttpServer getServer() { return mServer; }

    public AsyncServerSocket getServerSocket() { return mServerSocket; }

}
