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
    private final int own_ip;
    private boolean running;


    public Server(List<Link> rt, String ip) {
        routing_table = rt;
        port = 8080;  //default
        own_ip = IPString.int_from_string(ip);
        running = true;
        try{
            socket = new ServerSocket(port, 16, InetAddress.getByName(ip));
            socket.setReuseAddress(true);
            System.out.println("SERVER started on IP: " + ip + " and port: " + port);
        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }


    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        while (running) {
            try {
                Socket clientSocket = socket.accept();
                executor.submit(() -> handle_client(clientSocket));
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
        System.out.println("SERVER SHUTTING DOWN");

    }

    public void pause() {
        running = false;
        try {
            socket.close();
        } catch (IOException _) {

        }

    }

    public void resume() {
        running = true;
        try{
            socket = new ServerSocket(port, 16, InetAddress.getByName(IPString.string_from_int(own_ip)));
            socket.setReuseAddress(true);
            System.out.println("SERVER started on IP: " + IPString.string_from_int(own_ip) + " and port: " + port);
        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }

    private void handle_client(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())))
        {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JSONObject m = new JSONObject(inputLine);
                handle_message(m);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
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
            case 2:
                handle_acknowledge(m);
                break;
            case 3:
                handle_not_acknowledge(m);
                break;
        }

    }

    private void handle_text_message(JSONObject message) {
        int destination = message.getJSONObject("HEADER").getInt("DESTINATION_IP");

        //message for me
        if(destination == own_ip) {
            String text = message.getJSONObject("BODY").getString("TEXT");
            System.out.println("SERVER " + IPString.string_from_int(own_ip) + " received Message: \n"
                    + "  " + text);
            return;

            //TODO: send ACK
        }

        if(message.getJSONObject("HEADER").getInt("TTL") <= 0){
            System.out.println("SERVER " + IPString.string_from_int(own_ip) + " received Dead Message: \n");
            //TODO
            return;
        }

        //forward msg
        for(Link link : routing_table){
            if(link.getDESTINATION() == destination){
                try{
                    System.out.println("SERVER " + IPString.string_from_int(own_ip) + " forwarding Message to " + IPString.string_from_int(link.getGATEWAY()));
                    Socket s = new Socket(IPString.string_from_int(link.getGATEWAY()), 8080);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    message.getJSONObject("HEADER").put("TTL", message.getJSONObject("HEADER").getInt("TTL")-1);
                    out.println(message.toString());
                    return;
                } catch (IOException e) {
                    System.err.println("SERVER Could not create socket to send message");
                }

            }
        }

        System.out.println("SERVER " + IPString.string_from_int(own_ip) + " received Lost Message: \n");
        //TODO
    }

    private void handle_acknowledge(JSONObject message) {
        //TODO
    }

    private void handle_not_acknowledge(JSONObject message) {
        //TODO
    }

    private void handle_routing_information(JSONObject message) {
        JSONArray rt = message.getJSONObject("BODY").getJSONArray("ROUTING_TABLE");
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");

        for (int i = 0; i < rt.length(); i++) {

            Link l = new Link(rt.getJSONObject(i).getInt("DESTINATION"),
                    sender,
                    rt.getJSONObject(i).getInt("HOP_COUNT")
            );

            boolean found = false;
            for (int j = 0; j < routing_table.size(); j++) {
                if (routing_table.get(j).getDESTINATION() == l.getDESTINATION()) {
                    found = true;
                    if (routing_table.get(j).getHOP_COUNT() > l.getHOP_COUNT() + 1) {
                        routing_table.get(j).setGATEWAY(l.getGATEWAY());
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
                System.out.println("SERVER received connection: " + IPString.string_from_int(l.getDESTINATION()));
            }
        }



        for(Link link : routing_table.stream().filter(e -> e.getGATEWAY() == sender).toList()){
            boolean found = false;
                for (int i = 0; i < rt.length(); i++) {
                    if (rt.getJSONObject(i).getInt("DESTINATION") == link.getDESTINATION()) {
                        found = true;
                    }
                }

                if (!found) {
                    routing_table.remove(link);
                }
        }//TODO: TEST

    }
}

