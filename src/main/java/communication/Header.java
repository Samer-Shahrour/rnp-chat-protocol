package communication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {
    public byte MSG_TYPE;
    public byte TTL;
    public short SIZE;
    public int SENDER_IP;
    public int DESTINATION_IP;
    public short SENDER_PORT;
    public short DESTINATION_PORT;
    public int CHECKSUM;



    @Override
    public String toString(){
        return "HEADER{\n"
                + "        " + "msg_type: " + MSG_TYPE + "\n"
                + "        " + "ttl: " + TTL + "\n"
                + "        " + "size: " + SIZE + "\n"
                + "        " + "sender_ip: " + SENDER_IP + "\n"
                + "        " + "destination_ip: " + DESTINATION_IP + "\n"
                + "        " + "sender_port: " + SENDER_PORT + "\n"
                + "        " + "destination_port: " + DESTINATION_PORT + "\n"
                + "        " + "checksum: " + CHECKSUM + "\n"
                + "        " + "}";
    }

    public void set_destination_ip(String ip){
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);

            byte[] ipBytes = inetAddress.getAddress();

            int ipNumber = 0;
            for (int i = 0; i < ipBytes.length; i++) {
                ipNumber |= ((ipBytes[i] & 0xFF) << (8 * (3 - i)));
            }


            DESTINATION_IP = ipNumber;

        } catch (UnknownHostException e) {
            System.out.println("Invalid IP address: " + ip);
        }
    }

    public void set_sender_ip(String ip){
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);

            byte[] ipBytes = inetAddress.getAddress();

            int ipNumber = 0;
            for (int i = 0; i < ipBytes.length; i++) {
                ipNumber |= ((ipBytes[i] & 0xFF) << (8 * (3 - i)));
            }


            SENDER_IP = ipNumber;

        } catch (UnknownHostException e) {
            System.out.println("Invalid IP address: " + ip);
        }
    }
}
