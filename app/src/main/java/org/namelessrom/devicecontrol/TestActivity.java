package org.namelessrom.devicecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.libcore.RequestHeaders;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by alex on 27.05.14.
 */
public class TestActivity extends Activity {

    @InjectView(R.id.test_textview) TextView testTextView;

    private AsyncHttpServer asyncHttpServer;

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);

        asyncHttpServer = new AsyncHttpServer();

        asyncHttpServer.setErrorCallback(new CompletedCallback() {
            @Override public void onCompleted(Exception e) {
                Log.e("TestActivity", e.getMessage());
            }
        });

        asyncHttpServer.websocket("/ws", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override public void onConnected(WebSocket webSocket, RequestHeaders headers) {
                webSocket.send("hello via websocket");
            }
        });
        asyncHttpServer.directory(Application.applicationContext, "/live", "websocket.html");
        asyncHttpServer.directory(Application.applicationContext, "/license", "license.html");
        asyncHttpServer.get("/", new HttpServerRequestCallback() {
            @Override public void onRequest(final AsyncHttpServerRequest request,
                    final AsyncHttpServerResponse response) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        testTextView.setText(testTextView.getText() + "\n" + request.getMethod());
                    }
                });
                response.send("Test");
            }
        });

        asyncHttpServer.listen(8080);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        asyncHttpServer.stop();
    }
}
