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
package org.namelessrom.devicecontrol.net;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.namelessrom.devicecontrol.App;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Helper class for network informations like getting the ip address of the device
 */
public class NetworkInfo {
    private static final String TAG = NetworkInfo.class.getSimpleName();

    public static String getAnyIpAddress() {
        String ip = getWifiIp();
        if ("0.0.0.0".equals(ip)) {
            ip = getIpAddress(true);
        }
        return ip;
    }

    public static String getWifiIp() {
        final WifiManager wifiManager = (WifiManager) App.get().getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        final String formattedIp = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        Timber.v("formattedIp (wifi) -> %s", formattedIp);
        return formattedIp;
    }

    public static String getIpAddress(final boolean useIPv4) {
        List<NetworkInterface> interfaces;
        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (Exception e) {
            interfaces = null;
        }
        if (interfaces == null) {
            return "0.0.0.0";
        }
        for (NetworkInterface intf : interfaces) {
            final List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (final InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    final String sAddr = addr.getHostAddress().toUpperCase();
                    boolean isIPv4 = isIPv4Address(sAddr);
                    if (useIPv4) {
                        if (isIPv4) {
                            return sAddr;
                        }
                    } else {
                        if (!isIPv4) {
                            final int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                            return ((delim < 0) ? sAddr : sAddr.substring(0, delim));
                        }
                    }
                }
            }
        }
        return "0.0.0.0";
    }

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

}
