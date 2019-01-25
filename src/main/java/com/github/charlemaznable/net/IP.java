package com.github.charlemaznable.net;

import lombok.SneakyThrows;
import lombok.val;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class IP {

    @SneakyThrows
    public static String localIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()
                        && !ip.getHostAddress().contains(":")) {
                    return ip.getHostAddress();
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public static String netIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (!ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()
                        && !ip.getHostAddress().contains(":")) {
                    return ip.getHostAddress();
                }
            }
        }
        return localIP();
    }
}
