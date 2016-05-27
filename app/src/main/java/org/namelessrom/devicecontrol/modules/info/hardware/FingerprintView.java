/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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
import org.namelessrom.devicecontrol.views.CardTitleView;

import at.amartinz.hardware.security.Fingerprinter;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setSupported(true);
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
        } else {
            setSupported(false);
        }
    }

    public void setSupported(boolean isSupported) {
        this.isSupported = isSupported;
        if (!this.isSupported) {
            setNotAvailable();
        }
    }

    public void onResume() {
        if (fingerprinter == null) {
            return;
        }

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
        if (isSupported && fingerprinter != null) {
            fingerprinter.stopListening();
        }
    }

    public void onDestroy() {
        if (isSupported && fingerprinter != null) {
            fingerprinter.onDestroy();
        }
    }
}
