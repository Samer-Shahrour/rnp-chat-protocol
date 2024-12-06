package communication;

public class Message {
    public Header HEADER;
    public Body BODY;

    public Message(Header header, Body body) {
        HEADER = header;
        BODY = body;
    }

    @Override
    public String toString() {
        return "MESSAGE{\n" +
                "  "+ HEADER.toString() + "\n" +
                "  "+ BODY.toString() + "\n" +
                '}';
    }
}
