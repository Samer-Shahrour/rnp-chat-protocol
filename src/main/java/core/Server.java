package core;

import java.io.*;
import java.net.*;
import java.util.List;

import communication.Link;
import org.json.*;
import utils.IPString;


public class Server implements Runnable {

    private int port;
    private ServerSocket socket;
    private List<Link> routingTable;

    public Server(List<Link> rt) {
        routingTable = rt;
        port = 8080;  //default
        try {

            socket = new ServerSocket(port);
            System.out.println("core.Server started on port " + port);

        } catch (IOException e) {
            System.err.println("Could not create socket");
        }
    }

    public Server(List<Link> rt, String own_ip) {
        routingTable = rt;
        port = 8080;  //default
        try {
            socket = new ServerSocket(port, 1, InetAddress.getByName(own_ip));
            System.out.println("core.Server started on port " + port);

        } catch (IOException e) {
            System.err.println("Could not create socket");
        }
    }


    @Override
    public void run() {
        while (true) {
            try (Socket clientSocket = socket.accept()) {
                System.out.println("Connected to client");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    JSONObject m = new JSONObject(inputLine);
                    handle_message(m);
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
        String text = message.getJSONObject("BODY").getString("TEXT");
        System.out.println("SERVER received: \n"
                + "  " + text);
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
            for (int j = 0; j < routingTable.size(); j++) {
                if (routingTable.get(j).getDESTINATION() == l.getDESTINATION()) {
                    found = true;
                    if (routingTable.get(j).getHOP_COUNT() > l.getHOP_COUNT() + 1) {
                        routingTable.get(j).setGATEWAY(l.getGATEWAY());
                        routingTable.get(j).setNETMASK(l.getNETMASK());
                        routingTable.get(j).setHOP_COUNT(l.getHOP_COUNT() + 1);
                    }
                    if (routingTable.get(j).getHOP_COUNT() == l.getHOP_COUNT() + 1 &&
                            routingTable.get(j).getGATEWAY() != l.getGATEWAY()) {

                        l.incrementHopCount();
                        routingTable.add(l);
                    }
                    break;
                }
            }


            if (!found) {
                l.incrementHopCount();
                routingTable.add(l);
                System.out.println("SERVER received connection: " + IPString.ip_to_string(l.getDESTINATION()));
            }


        }
        System.out.println(routingTable);
    }
}

