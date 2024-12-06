package core;

import com.google.gson.Gson;
import communication.Body;
import communication.Header;
import communication.Message;
import communication.TextBody;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class TextClient {
    int port;

    public TextClient(){
        this(8080);
    }

    public TextClient(int port){
        this.port = port;
    }

    public void send_to(String destination_ip, String txt){
        try {


            Socket socket = new Socket(destination_ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();

            Header h = Header.TEXTHEADER;
            h.SIZE = (short) txt.length();
            h.set_destination_ip(destination_ip);

            Body b = new TextBody(txt);
            Message m = new Message(h, b);

            //System.out.println("CLIENT SENDING: \n" + m);

            out.println(gson.toJson(m));

        } catch (IOException e) {
            System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
            System.exit(1);
        }
    }

}
