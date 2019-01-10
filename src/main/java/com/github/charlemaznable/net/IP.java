package com.github.charlemaznable.net;

import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class IP {

    @SneakyThrows
    public static String localIP() {
        Enumeration<NetworkInterface> netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress ip = address.nextElement();
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
        Enumeration<NetworkInterface> netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress ip = address.nextElement();
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
