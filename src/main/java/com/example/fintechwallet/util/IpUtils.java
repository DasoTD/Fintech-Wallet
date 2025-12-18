package com.example.fintechwallet.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IpUtils {
    public static boolean isIpAllowed(String clientIp, List<String> allowedList) {
        if (allowedList == null || allowedList.isEmpty()) return false;

        try {
            InetAddress clientAddr = InetAddress.getByName(clientIp);
            for (String entry : allowedList) {
                if (entry.contains("/")) {
                    if (isInCidrRange(clientIp, entry)) return true;
                } else {
                    InetAddress allowedAddr = InetAddress.getByName(entry);
                    if (clientAddr.equals(allowedAddr)) return true;
                }
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return false;
    }

    private static boolean isInCidrRange(String ip, String cidr) throws UnknownHostException {
        String[] parts = cidr.split("/");
        if (parts.length != 2) return false;

        InetAddress networkAddr = InetAddress.getByName(parts[0]);
        int prefix = Integer.parseInt(parts[1]);

        byte[] ipBytes = InetAddress.getByName(ip).getAddress();
        byte[] networkBytes = networkAddr.getAddress();

        int len = ipBytes.length * 8;
        if (prefix > len) return false;

        long ipLong = bytesToLong(ipBytes);
        long networkLong = bytesToLong(networkBytes);
        long mask = prefix == 0 ? 0 : -1L << (len - prefix);

        return (ipLong & mask) == (networkLong & mask);
    }

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) + (b & 0xFF);
        }
        return result;
    }
}