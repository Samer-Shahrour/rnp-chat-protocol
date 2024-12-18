package messages;

public class AckBody extends Body{
    int MSG_ID;

    public AckBody(int id) {
        this.MSG_ID = id;
    }
}
