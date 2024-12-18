package communication;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import core.Data;
import messages.*;
import org.json.*;
import utils.IPString;


public class Server implements Runnable {


    private ServerSocket socket;
    private boolean running;
    private Data data;


    public Server(Data data) {
        this.running = true;
        this.data = data;
        try{
            this.socket = new ServerSocket(data.port, 16, InetAddress.getByName(data.getIPString()));
            this.socket.setReuseAddress(true);
            System.out.println("SERVER started on IP: " + data.getIPString() + " and port: " + data.port);
        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }


    @Override
    public void run() {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {

            while (running) {
                try {
                    Socket clientSocket = socket.accept();
                    executor.submit(() -> handle_client(clientSocket));
                } catch (IOException e) {
                    System.out.println("Error accepting client connection: " + e.getMessage());
                }
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
            socket = new ServerSocket(data.port, 16, InetAddress.getByName(data.getIPString()));
            socket.setReuseAddress(true);
            System.out.println("SERVER started on IP: " + data.getIPString() + " and port: " + data.port);
        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }

    private void handle_client(Socket clientSocket) {
        String sourceIP = clientSocket.getInetAddress().toString();
        JSONObject m = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())))
        {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                m = new JSONObject(inputLine);

            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
            if (m != null) {
                handle_message(m, sourceIP);
            }
        }

    }


    private void handle_message(JSONObject message, String source) {
        int msg_type = message.getJSONObject("HEADER").getInt("MSG_TYPE");
        int destination = message.getJSONObject("HEADER").getInt("DESTINATION_IP");
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");

        //message for me
        if(destination == data.getIPint()){
            switch (msg_type) {
                case MessageTypes.TEXT_MESSAGE:
                    handle_text_message(message, source);
                    break;

                case MessageTypes.ROUTING_INFORMATION:
                    handle_routing_information(message);
                    break;

                case MessageTypes.ACK:
                    handle_acknowledge(message);
                    break;

                case MessageTypes.NACK:
                    handle_not_acknowledge(message);
                    break;

                default:
                    System.err.println("Unknown message type: " + msg_type);
                    break;
            }
            return;
        }

        //dead message ttl
        if(message.getJSONObject("HEADER").getInt("TTL") <= 0){
            System.out.println("SERVER " + data.getIPString() + " received Dead Message: \n");

            //TODO: what to do?
            return;
        }

        //forward msg
        for(Link link : data.routing_table){
            if(link.getDESTINATION() == destination){
                data.gui.logMessage("Forwarding Message from: " + IPString.string_from_int(sender) + ", to: " + IPString.string_from_int(link.getGATEWAY()));

                try (Socket s = new Socket(IPString.string_from_int(link.getGATEWAY()), data.port)) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    message.getJSONObject("HEADER").put("TTL", message.getJSONObject("HEADER").getInt("TTL")-1);
                    out.println(message.toString());
                    data.gui.logMessage(message.toString());
                    return;
                } catch (IOException e) {
                    System.err.println("SERVER Could not create socket to send message");
                }

            }
        }


    }

    private void handle_text_message(JSONObject message, String source) {
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");
        int id = message.getJSONObject("BODY").getInt("MSG_ID");


        String text = message.getJSONObject("BODY").getString("TEXT");


        //sending ACK
        try (Socket s = new Socket(source, data.port)) {
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            Gson gson = new Gson();

            Header h = Header.ACKHEADER;
            h.MSG_TYPE = 2;
            h.SIZE = 0;
            h.DESTINATION_IP = sender;
            h.SENDER_IP = data.getIPint();

            Body b = new AckBody(id);
            Message m = new Message(h, b);

            out.println(gson.toJson(m));

            data.gui.logMessage("RECEIVED MESSAGE FROM: " + IPString.string_from_int(sender) + "\n" +
                    "  -> " + "\"" +  text + "\"" + "\n" +
                    "  -> " + "Ack sent" + m);


        } catch (IOException e) {
            System.err.println("SERVER sending ack Could not create socket to send message:" + e.getMessage());
        }


    }

    private void handle_acknowledge(JSONObject message) {
        data.gui.logMessage("message acknowledged");
    }

    private void handle_not_acknowledge(JSONObject message) {
        data.gui.logMessage("message lost");
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
            for (int j = 0; j < data.routing_table.size(); j++) {
                if (data.routing_table.get(j).getDESTINATION() == l.getDESTINATION()) {
                    found = true;
                    if (data.routing_table.get(j).getHOP_COUNT() > l.getHOP_COUNT() + 1) {
                        data.routing_table.get(j).setGATEWAY(l.getGATEWAY());
                        data.routing_table.get(j).setHOP_COUNT(l.getHOP_COUNT() + 1);
                    }
                    if (data.routing_table.get(j).getHOP_COUNT() == l.getHOP_COUNT() + 1 &&
                            data.routing_table.get(j).getGATEWAY() != l.getGATEWAY()) {

                        l.incrementHopCount();
                        data.routing_table.add(l);
                    }
                    break;
                }
            }

            if (!found) {
                l.incrementHopCount();
                data.routing_table.add(l);
                System.out.println("SERVER received connection: " + IPString.string_from_int(l.getDESTINATION()));
            }
        }



        for(Link link : data.routing_table.stream().filter(e -> e.getGATEWAY() == sender).toList()){
            boolean found = false;
                for (int i = 0; i < rt.length(); i++) {
                    if (rt.getJSONObject(i).getInt("DESTINATION") == link.getDESTINATION()) {
                        found = true;
                    }
                }

                if (!found) {
                    data.routing_table.remove(link);
                    data.gui.logMessage("SANITY CHECK: disconnected: " + IPString.string_from_int(link.getDESTINATION()));
                }
        }//TODO: Sanity check

    }

}

