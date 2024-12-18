package messages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.IPString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Header {

    //TODO: CHECKSUM

    public static int own_ip;

    public byte MSG_TYPE;
    public byte TTL;
    public short SIZE;
    public int SENDER_IP;
    public int DESTINATION_IP;
    public int CHECKSUM;

    public static final Header TEXTHEADER = create_text_header();
    public static final Header ROUTING = create_routing_header();
    public static final Header ACKHEADER = create_ack_header();



    public Header(byte msgType, byte ttl, short size, int senderIp, int destinationIp) {
        this.MSG_TYPE = msgType;
        this.TTL = ttl;
        this.SIZE = size;
        this.SENDER_IP = senderIp;
        this.DESTINATION_IP = destinationIp;
        this.CHECKSUM = 0;
    }

    private static Header create_text_header() {

        return new Header(
                (byte) MessageTypes.TEXT_MESSAGE,
                (byte) 16,
                (short) 0,
                own_ip,
                0
        );
    }

    private static Header create_ack_header() {

        return new Header(
                (byte) MessageTypes.ACK,
                (byte) 16,
                (short) 0,
                own_ip,
                0
        );
    }

    private static Header create_routing_header() {

        return new Header(
                (byte) MessageTypes.ROUTING_INFORMATION,
                (byte) 16,
                (short) 0,
                own_ip,
                0
        );
    }



    @Override
    public String toString(){
        return "HEADER{\n"
                + "  " + "msg_type: " + MSG_TYPE + "\n"
                + "  " + "ttl: " + TTL + "\n"
                + "  " + "size: " + SIZE + "\n"
                + "  " + "sender_ip: " + IPString.string_from_int(SENDER_IP) + "\n"
                + "  " + "destination_ip: " + IPString.string_from_int(DESTINATION_IP) + "\n"
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
