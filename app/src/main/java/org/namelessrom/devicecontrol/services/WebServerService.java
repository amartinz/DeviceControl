package org.namelessrom.devicecontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class WebServerService extends Service {

    public static final String ACTION_START = "action_start";

    private final WebServerBinder mBinder = new WebServerBinder();

    private AsyncHttpServer mServer;
    private StringBuilder   mStringBuilder;

    public WebServerService() { }

    @Override public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (mServer != null) mServer.stop();
    }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null || intent.getAction() == null || intent.getAction().isEmpty()) {
            logDebug("intent or action is null or empty!");
            stopSelf();
            return START_NOT_STICKY;
        }
        final String action = intent.getAction();
        logDebug("action: " + action);

        if (ACTION_START.equals(action)) {
            logDebug("creating server!");
            createServer();
            logDebug("created server!");
            return START_STICKY;
        } else {
            logDebug("stopping service!");
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void createServer() {
        if (mServer != null) return;

        mStringBuilder = new StringBuilder();
        mServer = new AsyncHttpServer();
        mStringBuilder.append("[!] Server created!\n");

        mServer.directory(this, "/license", "license.html");
        mStringBuilder.append("[!] Setup route: /license\n");

        mServer.get("/", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest request,
                    final AsyncHttpServerResponse response) {
                mStringBuilder.append("[+] Received connection from: ")
                        .append(request.getHeaders().getUserAgent())
                        .append('\n');
                response.send("More to come soon");
            }
        });
        mStringBuilder.append("[!] Setup route: /\n");

        mServer.listen(8080);
    }

    public String getWebServerLog() {
        if (mStringBuilder == null) return "";
        final String log = mStringBuilder.toString();              // save
        if (!log.isEmpty()) mStringBuilder = new StringBuilder();  // clear
        return log;                                                // return
    }

    public class WebServerBinder extends Binder {
        public WebServerService getService() {
            return WebServerService.this;
        }
    }
}
