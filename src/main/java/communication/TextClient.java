package communication;

import com.google.gson.Gson;
import core.Data;
import messages.*;
import utils.IPString;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class TextClient {
    private Data data;
    private Socket socket;

    public TextClient(Data data){
        this.data = data;
    }

    public void send_to(String destination_ip, String txt){
        boolean sent = false;
        try {
            for (Link link : data.routing_table) {
                if(link.getDESTINATION() == IPString.int_from_string(destination_ip)){
                    socket = new Socket(IPString.string_from_int(link.getGATEWAY()), data.port);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Gson gson = new Gson();

                    Header h = Header.TEXTHEADER;
                    h.SIZE = (short) txt.length();
                    h.set_destination_ip(destination_ip);
                    h.SENDER_IP = data.getIPint();

                    Body b = new TextBody(txt);
                    Message m = new Message(h, b);

                    out.println(gson.toJson(m));
                    sent = true;
                }
            }

            if(!sent){
                data.gui.logMessage("No Connection to send Message\n");
            }

        } catch (IOException e) {
            System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("CLINET SOCKET CLOSE: " + e.getMessage());
            }
        }
    }

}
