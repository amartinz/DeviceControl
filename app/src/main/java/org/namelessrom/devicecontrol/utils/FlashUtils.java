package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.Collections;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Created by alex on 26.06.14.
 */
public class FlashUtils implements DeviceConstants {

    private static final String CWM_PATH = "/cache/recovery/extendedcommand";
    private static final String OR_PATH  = "/cache/recovery/openrecoveryscript";

    private static String createOpenRecoveryScript(final List<String> files) {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("echo \"set tw_signed_zip_verify 0\" > %s;\n", OR_PATH));

        for (String file : files) {
            if (file.startsWith("/sdcard")) file = file.replaceFirst("/sdcard", "/sdcard/0");
            sb.append(String.format("echo \"install %s\" >> %s;\n", file, OR_PATH));
        }
        sb.append(String.format("echo \"wipe cache\" >> %s;\n", OR_PATH));
        sb.append(Utils.setPermissions(OR_PATH, "0644", 0, 2001));

        return sb.toString();
    }

    private static String createCwmScript(final List<String> files) {
        final StringBuilder sb = new StringBuilder();

        for (String file : files) {
            if (file.startsWith("/sdcard")) file = file.replaceFirst("/sdcard", "/sdcard/0");
            sb.append(String.format("echo 'install_zip(\"%s\");' >> %s;\n", file, CWM_PATH));
        }
        sb.append(String.format("echo 'run_program(\"/sbin/busybox\", " +
                "\"rm\", \"-rf\", \"/cache/*\");' >> %s;\n", CWM_PATH));
        sb.append(Utils.setPermissions(CWM_PATH, "0644", 0, 2001));

        return sb.toString();
    }

    public static void triggerFlash(final List<String> files) {
        final StringBuilder sb = new StringBuilder();
        Collections.sort(files);

        sb.append("mkdir -p /cache/recovery/;\n");

        final int flashType = PreferenceHelper.getInt(PREF_RECOVERY_TYPE, RECOVERY_TYPE_BOTH);
        if (RECOVERY_TYPE_CWM == flashType) {
            sb.append(createCwmScript(files));
        } else if (RECOVERY_TYPE_OPEN == flashType) {
            sb.append(createOpenRecoveryScript(files));
        } else {
            sb.append(createCwmScript(files));
            sb.append(createOpenRecoveryScript(files));
        }

        sb.append("reboot recovery;");

        final String cmd = sb.toString();
        logDebug("triggerUpdate():\n" + cmd + "\n--------");
        Utils.runRootCommand(cmd);
    }

}
