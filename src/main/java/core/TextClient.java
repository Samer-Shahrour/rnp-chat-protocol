package core;

import com.google.gson.Gson;
import communication.*;
import utils.IPString;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;


public class TextClient {
    int port;
    List<Link> routing_table;
    private final int own_ip;

    public TextClient(List<Link> rt, int ip){
        this(rt, 8080, ip);

    }

    public TextClient(List<Link>rt, int port, int ip){
        this.routing_table = rt;
        this.port = port;
        this.own_ip = ip;
    }

    public void send_to(String destination_ip, String txt){
        try {
            for (Link link : routing_table) {
                if(link.getDESTINATION() == IPString.int_from_string(destination_ip)){
                    Socket socket = new Socket(IPString.string_from_int(link.getGATEWAY()), port);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Gson gson = new Gson();

                    Header h = Header.TEXTHEADER;
                    h.SIZE = (short) txt.length();
                    h.set_destination_ip(destination_ip);
                    h.SENDER_IP = own_ip;

                    Body b = new TextBody(txt);
                    Message m = new Message(h, b);

                    //System.out.println("CLIENT SENDING: \n" + m);

                    out.println(gson.toJson(m));
                    return;
                }
            }
            System.out.println("No Connection to send Message");


        } catch (IOException e) {
            System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
            System.exit(1);
        }
    }

}
