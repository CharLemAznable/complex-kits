package com.github.charlemaznable.net;

import lombok.SneakyThrows;
import lombok.val;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static java.net.NetworkInterface.getNetworkInterfaces;

public enum IP {

    V4{
        @Override
        protected boolean checkInetAddress(InetAddress inetAddress) {
            return inetAddress instanceof Inet4Address;
        }
    },
    V6{
        @Override
        protected boolean checkInetAddress(InetAddress inetAddress) {
            return inetAddress instanceof Inet6Address;
        }
    };

    protected abstract boolean checkInetAddress(InetAddress inetAddress);

    @SneakyThrows
    public String localIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (ip.isSiteLocalAddress() &&
                        !isReservedAddress(ip) &&
                        checkInetAddress(ip)) {
                    return ip.getHostAddress();
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public String netIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (!ip.isSiteLocalAddress() &&
                        !isReservedAddress(ip) &&
                        checkInetAddress(ip)) {
                    return ip.getHostAddress();
                }
            }
        }
        return localIP();
    }

    private boolean isReservedAddress(InetAddress inetAddress) {
        return (inetAddress.isLinkLocalAddress() ||
                inetAddress.isLoopbackAddress() ||
                inetAddress.isAnyLocalAddress());
    }
}
