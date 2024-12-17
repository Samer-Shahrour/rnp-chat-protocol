package core;

import com.google.gson.Gson;
import communication.*;
import utils.IPString;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class RoutingClient implements Runnable {
    int port;
    List<Link> routing_table;
    private final int own_ip;
    private boolean running;

    public RoutingClient(List<Link> rt, int ip){
        port = 8080;
        routing_table = rt;
        own_ip = ip;
        running = true;
    }

    public boolean initiate_connection(String destination_ip){

        try{
            Socket socket = new Socket(destination_ip, port);
            socket.setSoTimeout(1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();

            Header h = Header.ROUTING;
            h.SIZE = (short) routing_table.size();
            h.set_destination_ip(destination_ip);
            h.SENDER_IP = own_ip;

            Body b = new RoutingBody(routing_table);
            Message m = new Message(h, b);

            out.println(gson.toJson(m));
            return true;

        }  catch (IOException e) {
            return false;
        }
    }
    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
    }

    @Override
    public void run(){

        while(running){
            for (Link link : routing_table) {
                if(link.getHOP_COUNT() != 1) continue;

                try (Socket socket = new Socket()) {
                    String destination_ip = IPString.string_from_int(link.getDESTINATION());
                    socket.connect(new InetSocketAddress(destination_ip, port), 1000);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Gson gson = new Gson();

                    Header h = Header.ROUTING;
                    h.SIZE = (short) routing_table.size();
                    h.set_destination_ip(destination_ip);
                    h.SENDER_IP = own_ip;

                    Body b = new RoutingBody(routing_table.stream().filter(
                                    (entry) -> entry.getGATEWAY() != IPString.int_from_string(destination_ip))
                            .toList()); //split horizon muss TODO: test

                    Message m = new Message(h, b);

                    out.println(gson.toJson(m));


                } catch (IOException e) {
                    routing_table.remove(link);
                    for (Link l : routing_table) {
                        if (l.getGATEWAY() == link.getDESTINATION()) {
                            System.out.println(IPString.string_from_int(l.getDESTINATION()) + " just disconnected");
                            routing_table.remove(l);
                        }
                    }

                }

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Stopping Routing Client...");
            }

        }

    }
}
