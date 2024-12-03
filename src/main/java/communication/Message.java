package communication;

public class Message {
    public Header header;
    public Body body;

    @Override
    public String toString() {
        return "MESSAGE{\n" +
                "    "+header.toString() + "\n" +
                "    "+body.toString() + "\n" +
                '}';
    }
}
