package org.namelessrom.devicecontrol.proprietary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Constants {

    private static final String DONATE_URL =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZSN2SW53JJQJY";

    public static boolean startExternalDonation(final Context context) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(DONATE_URL));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

}
