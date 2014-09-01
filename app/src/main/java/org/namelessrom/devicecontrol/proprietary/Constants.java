package org.namelessrom.devicecontrol.proprietary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Constants {

    private static final String DONATE_URL =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZSN2SW53JJQJY";

    /**
     * You do not have to be a genius to break that.
     * Well, its a public key so the world does not die if someone gets it.
     * Still, add some pseudo obfuscation for proguard as well as make it
     * not just a copy-paste action to get it.
     */
    public static class Iab {
        private static final String IAB_PREF = "iab_pref";

        private static final String a = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgK";
        private static final String b = "CAQEAw20oxkT9x/QZJKYArXPneHGkFYmk0Hd5PI1A0R89Ns3GAKKp";
        private static final String c = "HTkTy2eLggt1bfUq67IXkNzv3/GNPrSypFvuaxW4RL/kX";
        private static final String d = "GWGffWgMm7ohoG1MQKmzLbrVP4MsQ9Gji2olo";
        private static final String e = "43B3K5+Oku0GzjZfj/BTWu0N";
        private static final String f = "MkxcPh9BIEaqwBLfwO81IFNBDnYjC";
        private static final String g = "+K64fpxvdWG0w3SrYQRFVYFVd5D3WgZtjMWHF22ehOt0wN8U7TsT";
        private static final String h = "f+fZV/XkZJVlE+P5onxqxaKUCqYZaNbXFKN/";
        private static final String i = "R+oT8ybucbRPjKv3knc5/BRw8JassdEoe";
        private static final String j = "xCfHhciU00K9UaD+D+n0TH9zDhfcduuzNfz4FQIDAQAB";

        public static String getKey() {
            String key = "<<";
            @SuppressWarnings("StringBufferReplaceableByString")
            final StringBuilder sb = new StringBuilder(a + b);
            sb.append(c).append(d);
            sb.append(f).append(e);
            sb.append(g).append(h).append(i);
            sb.append(j);
            key = key + sb.toString() + ">1>";
            String tmp = key.replace("<<", "");
            key = tmp.replace('>' + String.valueOf(1) + '>', "");

            return key;
        }

        public static String getPref() { return IAB_PREF; }

    }

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
