package communication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.IPString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {

    //TODO: PORTS, CHECKSUM

    public static int own_ip;

    public byte MSG_TYPE;
    public byte TTL;
    public short SIZE;
    public int SENDER_IP;
    public int DESTINATION_IP;
    public short SENDER_PORT;
    public short DESTINATION_PORT;
    public int CHECKSUM;

    public static final Header TEXTHEADER = create_text_header();
    public static final Header ROUTING = create_routing_header();



    public Header(byte msgType, byte ttl, short size, int senderIp, int destinationIp, short senderPort, short destinationPort) {
        this.MSG_TYPE = msgType;
        this.TTL = ttl;
        this.SIZE = size;
        this.SENDER_IP = senderIp;
        this.DESTINATION_IP = destinationIp;
        this.SENDER_PORT = senderPort;
        this.DESTINATION_PORT = destinationPort;
        this.CHECKSUM = 0;
    }

    private static Header create_text_header() {

        return new Header(
                (byte) 0,
                (byte) 16,
                (short) 0,
                own_ip,
                0,
                (short) 8080,
                (short) 8080
        );
    }

    private static Header create_routing_header() {

        return new Header(
                (byte) 1,
                (byte) 16,
                (short) 0,
                own_ip,
                0,
                (short) 8080,
                (short) 8080
        );
    }



    @Override
    public String toString(){
        return "HEADER{\n"
                + "  " + "msg_type: " + MSG_TYPE + "\n"
                + "  " + "ttl: " + TTL + "\n"
                + "  " + "size: " + SIZE + "\n"
                + "  " + "sender_ip: " + SENDER_IP + "\n"
                + "  " + "destination_ip: " + DESTINATION_IP + "\n"
                + "  " + "sender_port: " + SENDER_PORT + "\n"
                + "  " + "destination_port: " + DESTINATION_PORT + "\n"
                + "  " + "checksum: " + CHECKSUM + "\n"
                + "  " + "}";
    }

    public void set_destination_ip(String ip){
        DESTINATION_IP = IPString.int_from_string(ip);
    }

    public void set_sender_ip(String ip){
        SENDER_IP = IPString.int_from_string(ip);
    }


}
