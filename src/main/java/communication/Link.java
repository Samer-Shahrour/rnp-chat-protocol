package communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import utils.IPString;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Data
@AllArgsConstructor
public class Link {
    int DESTINATION;
    int NETMASK;
    int GATEWAY;
    int HOP_COUNT;

    public Link(String ip) {
        DESTINATION = ip_string_to_int(ip);
        NETMASK = 0;
        GATEWAY = 0;
        HOP_COUNT = 0;
    }

    public int ip_string_to_int(String ip){
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Link link = (Link) obj;

        return DESTINATION == link.DESTINATION;  // Compare DESTINATION values
    }

    public void incrementHopCount() {
        HOP_COUNT++;
    }

    public String toString(){
        return "Link{" + "\n" +
                "  " + "destination: " + IPString.ip_to_string(DESTINATION) + "\n" +
                "  " + "netmask: " + IPString.ip_to_string(NETMASK) + "\n" +
                "  " + "gateway: " + IPString.ip_to_string(GATEWAY) + "\n" +
                "  " + "hop_count: " + HOP_COUNT + "\n" +
                "  " + "}\n";
    }
}
