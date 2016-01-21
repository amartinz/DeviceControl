package org.namelessrom.devicecontrol.modules.info.hardware;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.views.CardTitleView;

import alexander.martinz.libs.hardware.security.Fingerprinter;

/**
 * Created by amartinz on 21.01.16.
 */
public class FingerprintView extends CardTitleView {
    private Fingerprinter fingerprinter;

    private TextView statusView;

    private boolean isSupported;

    public FingerprintView(Context context) {
        super(context);
    }

    public FingerprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FingerprintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public FingerprintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void init(@Nullable AttributeSet attrs) {
        super.init(attrs);
        statusView = new TextView(getContext());

        final FrameLayout content = getContentView();
        content.addView(statusView);
        statusView.setText(R.string.fingerprint_press_to_authenticate);

        fingerprinter = new Fingerprinter(getContext(), new Fingerprinter.FingerprinterCallback() {
            @Override public void onAuthenticationError(int errMsgId, CharSequence errString) {
                statusView.setText(errString);
            }

            @Override public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                statusView.setText(helpString);
            }

            @Override public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                statusView.setText(R.string.fingerprint_success);
            }

            @Override public void onAuthenticationFailed() {
                statusView.setText(R.string.fingerprint_failed);
            }
        });
    }

    public void setSupported(boolean isSupported) {
        this.isSupported = isSupported;
        if (!this.isSupported) {
            setNotAvailable();
        }
    }

    public void onResume() {
        final int setup = fingerprinter.hasFingerprintsSetup();
        if (setup == Fingerprinter.SETUP_OK) {
            if (!fingerprinter.init()) {
                setNotAvailable();
            }
        } else {
            switch (setup) {
                case Fingerprinter.SETUP_NO_HARDWARE: {
                    setNotAvailable();
                    break;
                }
                case Fingerprinter.SETUP_NO_SECURE_LOCK_SCREEN: {
                    statusView.setText(R.string.fingerprint_no_secure_lock_screen);
                    break;
                }
                case Fingerprinter.SETUP_NO_FINGERPRINTS: {
                    statusView.setText(R.string.fingerprint_no_fingerprints);
                }
            }
        }

        if (isSupported) {
            fingerprinter.startListening();
        }
    }

    public void setNotAvailable() {
        statusView.setText(R.string.fingerprint_not_available);
    }

    public void onPause() {
        if (isSupported) {
            fingerprinter.stopListening();
        }
    }

    public void onDestroy() {
        if (isSupported) {
            fingerprinter.onDestroy();
        }
    }
}
