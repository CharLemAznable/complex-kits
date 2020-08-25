package com.github.charlemaznable.core.net;

import lombok.SneakyThrows;
import lombok.val;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static java.net.NetworkInterface.getNetworkInterfaces;

public enum IP {

    V4 {
        @Override
        protected final boolean checkInetAddress(InetAddress inetAddress) {
            return inetAddress instanceof Inet4Address;
        }
    },
    V6 {
        @Override
        protected final boolean checkInetAddress(InetAddress inetAddress) {
            return inetAddress instanceof Inet6Address;
        }
    },;

    protected abstract boolean checkInetAddress(InetAddress inetAddress);

    @SneakyThrows
    public final String localIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (ip.isSiteLocalAddress() &&
                        isNotReservedAddress(ip) &&
                        checkInetAddress(ip)) {
                    return filterAdapterName(ip.getHostAddress());
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public final String netIP() {
        val netInterfaces = getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            val ni = netInterfaces.nextElement();
            val address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                val ip = address.nextElement();
                if (!ip.isSiteLocalAddress() &&
                        isNotReservedAddress(ip) &&
                        checkInetAddress(ip)) {
                    return filterAdapterName(ip.getHostAddress());
                }
            }
        }
        return localIP();
    }

    private boolean isNotReservedAddress(InetAddress inetAddress) {
        return (!inetAddress.isLinkLocalAddress() &&
                !inetAddress.isLoopbackAddress() &&
                !inetAddress.isAnyLocalAddress());
    }

    private String filterAdapterName(String hostAddress) {
        val index = hostAddress.indexOf('%');
        if (index > 0) hostAddress = hostAddress.substring(0, index);
        return hostAddress;
    }
}
