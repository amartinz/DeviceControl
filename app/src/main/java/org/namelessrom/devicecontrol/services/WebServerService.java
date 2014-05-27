package org.namelessrom.devicecontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class WebServerService extends Service {

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";

    private final WebServerBinder mBinder = new WebServerBinder();

    private AsyncHttpServer   mServer;
    private AsyncServerSocket mServerSocket;
    private StringBuilder     mStringBuilder;

    public WebServerService() { }

    @Override public IBinder onBind(final Intent intent) { return mBinder; }

    @Override public void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null || intent.getAction() == null || intent.getAction().isEmpty()) {
            logDebug("intent or action is null or empty!");
            stopServer();
            return START_NOT_STICKY;
        }
        final String action = intent.getAction();
        logDebug("action: " + action);

        if (ACTION_START.equals(action)) {
            logDebug("creating server!");
            createServer();
        } else {
            logDebug("stopping service!");
            stopServer();
        }
        return START_NOT_STICKY;
    }

    private void stopServer() {
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
        if (mServerSocket != null) {
            mServerSocket.stop();
            mServerSocket = null;
        }
        stopSelf();
    }

    private void createServer() {
        if (mServer != null) return;

        mStringBuilder = new StringBuilder();
        mServer = new AsyncHttpServer();
        mStringBuilder.append("[!] Server created!\n");

        mServer.directory(this, "/license", "license.html");
        mStringBuilder.append("[!] Setup route: /license\n");

        mServer.get("/sdcard", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest request,
                    final AsyncHttpServerResponse response) {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    response.send("SDCARD not mounted!");
                    return;
                }
                boolean isDirectory = true;
                final String filePath = request.getQuery().getString("file");
                File file = Environment.getExternalStorageDirectory();
                final String sdRoot = file.getAbsolutePath();
                if (filePath != null && !filePath.isEmpty()) {
                    file = new File(file, filePath);
                    if (file.exists()) {
                        isDirectory = file.isDirectory();
                    } else {
                        response.send("File or directory does not exist!");
                        return;
                    }
                }
                if (isDirectory) {
                    final File[] fs = file.listFiles();
                    if (fs == null) {
                        response.send("An error occured!");
                        return;
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.append("<a href=\"/sdcard\">Back to root</a><br /><br />");
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

                    Collections.sort(directories, SortHelper.sFileComparator);
                    Collections.sort(files, SortHelper.sFileComparator);

                    for (final File f : directories) {
                        sb.append("Directory: <a href=\"/sdcard?file=")
                                .append(f.getAbsolutePath().replace(sdRoot, "")).append("\">")
                                .append(f.getName()).append("</a><br />");
                    }
                    for (final File f : files) {
                        sb.append("File: <a href=\"/sdcard?file=")
                                .append(f.getAbsolutePath().replace(sdRoot, "")).append("\">")
                                .append(f.getName()).append("</a><br />");
                    }
                    response.send(sb.toString());
                } else {
                    final String contentType = ContentTypes.getInstance()
                            .getContentType(file.getAbsolutePath());
                    mStringBuilder.append("Requested file: ").append(file.getName()).append('\n');
                    mStringBuilder.append("Content-Type: ").append(contentType).append('\n');
                    response.setContentType(contentType);
                    response.sendFile(file);
                }
            }
        });
        mStringBuilder.append("[!] Setup route: /sdcard\n");

        mServer.get("/*", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest request,
                    final AsyncHttpServerResponse response) {
                mStringBuilder.append("[+] Received connection from: ")
                        .append(request.getHeaders().getUserAgent())
                        .append('\n');
                response.send("More to come soon");
            }
        });
        mStringBuilder.append("[!] Setup route: /\n");

        mServerSocket = mServer.listen(8080);
    }

    public String getWebServerLog() {
        if (mStringBuilder == null) return "";
        final String log = mStringBuilder.toString();              // save
        if (!log.isEmpty()) mStringBuilder = new StringBuilder();  // clear
        return log;                                                // return
    }

    public class WebServerBinder extends Binder {
        public WebServerService getService() { return WebServerService.this; }
    }

}
