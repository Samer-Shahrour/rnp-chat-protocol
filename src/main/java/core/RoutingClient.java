package core;

import com.google.gson.Gson;
import communication.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class RoutingClient {
    int port;
    List<Link> routing_table;

    public RoutingClient(List<Link> rt){
        port = 8080;
        routing_table = rt;
    }

    public boolean initiate_connection(String destination_ip){
        try{
            Socket socket = new Socket(destination_ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();

            Header h = Header.ROUTING;
            h.SIZE = (short) routing_table.size();
            h.set_destination_ip(destination_ip);

            Body b = new RoutingBody(routing_table);
            Message m = new Message(h, b);

            out.println(gson.toJson(m));
            return true;

        }  catch (IOException e) {
            System.err.println("could not connect to " + destination_ip);
            return false;
        }


    }
}
