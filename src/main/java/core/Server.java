package core;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import communication.Link;
import org.json.*;
import utils.IPString;


public class Server implements Runnable {

    private int port;
    private ServerSocket socket;
    private List<Link> routing_table;
    private ExecutorService executor;
    private final int own_ip;

    public Server(List<Link> rt, int ip) {
        routing_table = rt;
        port = 8080;  //default
        own_ip = ip;
        executor = Executors.newFixedThreadPool(5);
        try {

            socket = new ServerSocket(port);
            System.out.println("SERVER started on port " + port);

        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }

    public Server(List<Link> rt, String own_ip) {
        routing_table = rt;
        port = 8080;  //default
        this.own_ip = IPString.string_to_ip(own_ip);
        executor = Executors.newFixedThreadPool(5);
        try {
            socket = new ServerSocket(port, 1, InetAddress.getByName(own_ip));
            System.out.println("SERVER started on port " + port);

        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }


    @Override
    public void run() {
        while (true) {
            try (Socket clientSocket = socket.accept()) {
                System.out.println("SERVER Connected to client");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    JSONObject m = new JSONObject(inputLine);
                    executor.submit(()-> {handle_message(m);});
                }

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }

    private void handle_message(JSONObject m) {
        switch (m.getJSONObject("HEADER").getInt("MSG_TYPE")) {
            case 0:
                handle_text_message(m);
                break;
            case 1:
                handle_routing_information(m);
                break;
        }

    }

    private void handle_text_message(JSONObject message) {
        int destination = message.getJSONObject("HEADER").getInt("DESTINATION_IP");

        //message for me
        if(destination == own_ip) {
            String text = message.getJSONObject("BODY").getString("TEXT");
            System.out.println("SERVER " + IPString.ip_to_string(own_ip) + " received Message: \n"
                    + "  " + text);
            return;
        }

        //forward msg
        for(Link link : routing_table){
            if(link.getDESTINATION() == destination){
                try{
                    System.out.println("SERVER " + IPString.ip_to_string(own_ip) + " forwarding Message to " + IPString.ip_to_string(link.getGATEWAY()));
                    Socket s = new Socket(IPString.ip_to_string(link.getGATEWAY()), 8080);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    Gson gson = new Gson();
                    out.println(message.toString());
                    return;
                } catch (IOException e) {
                    System.err.println("SERVER Could not create socket to send message");
                }

            }
        }

        System.out.println("SERVER " + IPString.ip_to_string(own_ip) + " received Lost Message: \n");
        //TODO
    }

    private void handle_routing_information(JSONObject message) {
        JSONArray rt = message.getJSONObject("BODY").getJSONArray("ROUTING_TABLE");

        for (int i = 0; i < rt.length(); i++) {

            Link l = new Link(rt.getJSONObject(i).getInt("DESTINATION"),
                    rt.getJSONObject(i).getInt("NETMASK"),
                    rt.getJSONObject(i).getInt("GATEWAY"),
                    rt.getJSONObject(i).getInt("HOP_COUNT")
            );

            boolean found = false;
            for (int j = 0; j < routing_table.size(); j++) {
                if (routing_table.get(j).getDESTINATION() == l.getDESTINATION()) {
                    found = true;
                    if (routing_table.get(j).getHOP_COUNT() > l.getHOP_COUNT() + 1) {
                        routing_table.get(j).setGATEWAY(l.getGATEWAY());
                        routing_table.get(j).setNETMASK(l.getNETMASK());
                        routing_table.get(j).setHOP_COUNT(l.getHOP_COUNT() + 1);
                    }
                    if (routing_table.get(j).getHOP_COUNT() == l.getHOP_COUNT() + 1 &&
                            routing_table.get(j).getGATEWAY() != l.getGATEWAY()) {

                        l.incrementHopCount();
                        routing_table.add(l);
                    }
                    break;
                }
            }


            if (!found) {
                l.incrementHopCount();
                routing_table.add(l);
                System.out.println("SERVER received connection: " + IPString.ip_to_string(l.getDESTINATION()));
            }


        }
        System.out.println(routing_table);
    }
}

