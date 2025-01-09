package communication;

import com.google.gson.Gson;
import core.Data;
import messages.*;
import utils.IPString;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class RoutingClient implements Runnable {

    private boolean running;
    private Data data;
    private Map<Link, Integer> number_tries;

    public RoutingClient(Data data) {
        this.running = true;
        this.data = data;
        this.number_tries = new HashMap<>();
    }

    public void initiate_connection(String destination_ip){
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(destination_ip, data.port), 800);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();

            Header h = Header.ROUTING;
            h.SIZE = (short) data.routing_table.size();
            h.set_destination_ip(destination_ip);
            h.SENDER_IP = data.get_own_IP_int();

            Body b = new RoutingBody(data.routing_table);
            Message m = new Message(h, b);

            out.println(gson.toJson(m));

            data.gui.logMessage("SUCCESSFULLY connected to " + destination_ip);

            Link mylink = new Link(IPString.int_from_string(destination_ip),
                    IPString.int_from_string(destination_ip), 1);

            data.routing_table.remove(mylink);
            data.routing_table.add(mylink);

        }  catch (IOException e) {
            data.gui.logMessage("FAILED to connect to " + destination_ip);
        }
    }
    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
    }

    private void update_number_tries() {
        for (Link link : data.routing_table.stream().filter(l -> l.getHOP_COUNT() == 1).toList()) {
            number_tries.putIfAbsent(link, 0);
        }
    }

    @Override
    public void run(){

        while(running){
            update_number_tries();
            data.routing_table.stream().filter(l -> l.getHOP_COUNT() == 1)
                .forEach(l -> handle_direct_connection(l));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Stopping Routing Client...");
            }

        }

    }

    private void handle_direct_connection(Link link) {
        try (Socket socket = new Socket()) {
            String destination_ip = IPString.string_from_int(link.getDESTINATION());
            socket.connect(new InetSocketAddress(destination_ip, data.port), 1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();

            Body b = new RoutingBody(data.routing_table.stream().filter(
                            (entry) -> entry.getGATEWAY() != IPString.int_from_string(destination_ip))
                    .toList()); //split horizon muss TODO: test

            String body = gson.toJson(b);
            CRC32 crc = new CRC32();
            crc.update(body.getBytes());

            Header h = Header.ROUTING;
            h.SIZE = (short) data.routing_table.size();
            h.set_destination_ip(destination_ip);
            h.SENDER_IP = data.get_own_IP_int();
            h.CHECKSUM = (int) crc.getValue();

            Message m = new Message(h, b);
            out.println(gson.toJson(m));

            number_tries.put(link, 0);

        } catch (IOException e) {

            number_tries.put(link, number_tries.get(link) + 1);
            if (number_tries.get(link) >= 4) {
                data.routing_table.remove(link);
                data.gui.logMessage("IP: "+ IPString.string_from_int(link.getDESTINATION()) +" Just disconnected");

                for (Link l : data.routing_table) {
                    if (l.getGATEWAY() == link.getDESTINATION()) {
                        data.routing_table.remove(l);
                        data.gui.logMessage("IP: "+ IPString.string_from_int(l.getDESTINATION()) +" Just disconnected");
                    }
                }
            }

        }
    }
}
