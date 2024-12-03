package communication;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Header {
    public byte msg_type;
    public byte ttl;
    public short size;
    public int sender_ip;
    public int destination_ip;
    public short sender_port;
    public short destination_port;
    public int checksum;

    @Override
    public String toString(){
        return "HEADER{\n"
                + "        " + "msg_type: " + msg_type + "\n"
                + "        " + "ttl: " + ttl + "\n"
                + "        " + "size: " + size + "\n"
                + "        " + "sender_ip: " + sender_ip + "\n"
                + "        " + "destination_ip: " + destination_ip + "\n"
                + "        " + "sender_port: " + sender_port + "\n"
                + "        " + "destination_port: " + destination_port + "\n"
                + "        " + "checksum: " + checksum + "\n"
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


            destination_ip = ipNumber;

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


            sender_ip = ipNumber;

        } catch (UnknownHostException e) {
            System.out.println("Invalid IP address: " + ip);
        }
    }
}
