package messages;

public class NackBody extends Body{
    int MSG_ID;
    int ERROR_CODE;

    public NackBody(int id, int error_code) {
        this.MSG_ID = id; this.ERROR_CODE = error_code;
    }
    
}
