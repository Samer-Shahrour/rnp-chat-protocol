package messages;

public class TextBody extends Body {

    int MSG_ID;
    String TEXT;


    public TextBody(String text, int id) {
        TEXT = text;
        MSG_ID = id;
    }

    @Override
    public String toString() {
        return "Body{" + "\n" +
                "  " + "id: " + MSG_ID + "\n" +
                "  " + "text: '" + TEXT + "'\n" +
                "  " + "}";

    }
}
