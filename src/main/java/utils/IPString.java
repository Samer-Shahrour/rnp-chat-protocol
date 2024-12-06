package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPString {
    public static int string_to_ip(String ip){
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);

            byte[] ipBytes = inetAddress.getAddress();

            int ipNumber = 0;
            for (int i = 0; i < ipBytes.length; i++) {
                ipNumber |= ((ipBytes[i] & 0xFF) << (8 * (3 - i)));
            }


            return ipNumber;

        } catch (UnknownHostException e) {

            System.out.println("Invalid IP address: " + ip);
            return -1;
        }
    }

    public static String ip_to_string(int ip){
        int byte1 = (ip >> 24) & 0xFF;
        int byte2 = (ip >> 16) & 0xFF;
        int byte3 = (ip >> 8) & 0xFF;
        int byte4 = ip & 0xFF;

        return byte1 + "." + byte2 + "." + byte3 + "." + byte4;

    }
}
