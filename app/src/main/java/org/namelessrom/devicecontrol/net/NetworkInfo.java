package org.namelessrom.devicecontrol.net;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.apache.http.conn.util.InetAddressUtils;
import org.namelessrom.devicecontrol.Application;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for network informations like getting the ip address of the device
 */
public class NetworkInfo {

    public static String getAnyIpAddress() {
        String ip = getWifiIp();
        if (ip.equals("0.0.0.0")) ip = getIpAddress(true);
        return ip;
    }

    public static String getWifiIp() {
        final WifiManager wifiManager = (WifiManager) Application.applicationContext
                .getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        final byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = "0.0.0.0";
        }

        return ipAddressString;
    }

    public static String getIpAddress(final boolean useIPv4) {
        List<NetworkInterface> interfaces;
        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (Exception e) {
            interfaces = null;
        }
        if (interfaces == null) return "0.0.0.0";
        for (NetworkInterface intf : interfaces) {
            final List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (final InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    final String sAddr = addr.getHostAddress().toUpperCase();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    if (useIPv4) {
                        if (isIPv4) { return sAddr; }
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

}
