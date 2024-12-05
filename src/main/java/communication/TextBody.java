package communication;

import java.util.List;

public class TextBody extends Body {
    int MSG_ID = 0;
    String TEXT = "";

    public TextBody(String text) {
        TEXT = text;
    }

    @Override
    public int get_msg_id() {
        return MSG_ID;
    }

    @Override
    public String get_msg_text(){
        return TEXT;
    }

}
