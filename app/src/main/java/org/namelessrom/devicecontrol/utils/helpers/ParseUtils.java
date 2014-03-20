package org.namelessrom.devicecontrol.utils.helpers;

import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class ParseUtils implements DeviceConstants {

    public static int parseFstrim(final String position) {
        try {
            return parseFstrim(Integer.parseInt(position));
        } catch (Exception exc) {
            return 480;
        }
    }

    public static int parseFstrim(final int position) {
        int value;
        switch (position) {
            case 0:
                value = 5;
                break;
            case 1:
                value = 10;
                break;
            case 2:
                value = 20;
                break;
            case 3:
                value = 30;
                break;
            case 4:
                value = 60;
                break;
            case 5:
                value = 120;
                break;
            case 6:
                value = 240;
                break;
            default:
            case 7:
                value = 480;
                break;
        }
        return value;
    }

    public static int getFstrim() {
        int position;

        final int value = PreferenceHelper.getInt(FSTRIM_INTERVAL, 480);
        switch (value) {
            case 5:
                position = 0;
                break;
            case 10:
                position = 1;
                break;
            case 20:
                position = 2;
                break;
            case 30:
                position = 3;
                break;
            case 60:
                position = 4;
                break;
            case 120:
                position = 5;
                break;
            case 240:
                position = 6;
                break;
            default:
            case 480:
                position = 7;
                break;
        }

        return position;
    }

}
