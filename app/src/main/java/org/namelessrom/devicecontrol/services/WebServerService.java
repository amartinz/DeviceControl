/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.HtmlHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class WebServerService extends Service {

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";

    private AsyncHttpServer   mServer;
    private AsyncServerSocket mServerSocket;
    private StringBuilder     mStringBuilder;

    private boolean isStopped = false;

    @Override public IBinder onBind(final Intent intent) { return new WebServerBinder(); }

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
        isStopped = true;
        stopSelf();
    }

    private void createServer() {
        if (mServer != null) return;

        mStringBuilder = new StringBuilder();
        mServer = new AsyncHttpServer();
        mStringBuilder.append("[!] Server created!\n");

        mServer.directory(this, "/license", "license.html");
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
                final String filePath = URLDecoder.decode(req.getPath().replace("/files", ""));
                Log.e("SERVER", filePath);
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
                    sb.append("<a href=\"/files/\">Back to root</a><br /><br />");
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

                    if (directories.size() > 0) {
                        Collections.sort(directories, SortHelper.sFileComparator);
                        for (final File f : directories) {
                            sb.append("Directory: <a href=\"/files")
                                    .append(Html.escapeHtml(
                                            f.getAbsolutePath().replace(sdRoot, "")))
                                    .append("\">")
                                    .append(f.getName()).append("</a><br />");
                        }
                    }
                    if (files.size() > 0) {
                        Collections.sort(files, SortHelper.sFileComparator);
                        for (final File f : files) {
                            sb.append("File: <a href=\"/files")
                                    .append(Html
                                            .escapeHtml(f.getAbsolutePath().replace(sdRoot, "")))
                                    .append("\">")
                                    .append(f.getName()).append("</a><br />");
                        }
                    }

                    if (directories.size() == 0 && files.size() == 0) {
                        sb.append("Empty :(<br />");
                    }

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
                                "Sdcard file manager: " +
                                "<a href=\"/files\">CLICK HERE</a><br /><br />" +
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

    public String getWebServerLog() {
        if (mStringBuilder == null) return "";
        final String log = mStringBuilder.toString();              // save
        if (!log.isEmpty()) mStringBuilder = new StringBuilder();  // clear
        return log;                                                // return
    }

    public AsyncHttpServer getServer() { return mServer; }

    public AsyncServerSocket getSocket() { return mServerSocket; }

    public class WebServerBinder extends Binder {
        public WebServerService getService() { return WebServerService.this; }
    }

}
