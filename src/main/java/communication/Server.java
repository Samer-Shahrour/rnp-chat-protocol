package communication;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

import com.google.gson.Gson;
import core.Data;
import messages.*;
import org.json.*;
import utils.ErrorTypes;
import utils.IPString;
import utils.MessageTypes;


public class Server implements Runnable {


    private ServerSocket socket;
    private boolean running;
    private Data data;


    public Server(Data data) {
        this.running = true;
        this.data = data;
        try{
            this.socket = new ServerSocket(data.port, 16, InetAddress.getByName(data.get_own_IP_String()));
            this.socket.setReuseAddress(true);
            System.out.println("SERVER started on IP: " + data.get_own_IP_String() + " and port: " + data.port);
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

        //System.out.println("SERVER SHUTTING DOWN");
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
            socket = new ServerSocket(data.port, 16, InetAddress.getByName(data.get_own_IP_String()));
            socket.setReuseAddress(true);
            //System.out.println("SERVER started on IP: " + data.get_own_IP_String() + " and port: " + data.port);
        } catch (IOException e) {
            System.err.println("SERVER Could not create socket");
        }
    }

    private void handle_client(Socket clientSocket) {
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
                handle_message(m);
            }
        }

    }


    private void handle_message(JSONObject message) {
        int msg_type = message.getJSONObject("HEADER").getInt("MSG_TYPE");
        int destination = message.getJSONObject("HEADER").getInt("DESTINATION_IP");

        //message for me
        if(destination == data.get_own_IP_int()){
            switch (msg_type) {
                case MessageTypes.TEXT_MESSAGE:
                    handle_text_message(message);
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
            System.out.println("SERVER " + data.get_own_IP_String() + " received Dead Message: \n");
            int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");
            int id = message.getJSONObject("BODY").getInt("MSG_ID");
            send_nack(sender, id, ErrorTypes.TTL_ERROR);
            return;
        }

        //forward msg
        forward(message);
    }

    private void forward(JSONObject message) {
        int destination = message.getJSONObject("HEADER").getInt("DESTINATION_IP");
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");
        int type = message.getJSONObject("HEADER").getInt("MSG_TYPE");
        int id = message.getJSONObject("BODY").getInt("MSG_ID");


        Optional<Link> opt = data.routing_table.stream().filter(l -> l.getDESTINATION() == destination).findFirst();
        opt.ifPresentOrElse(
            existing_link -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(IPString.string_from_int(destination), data.port), 1000);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    message.getJSONObject("HEADER").put("TTL", message.getJSONObject("HEADER").getInt("TTL")-1);
                    out.println(message);
                    System.out.println(build_forward_string(existing_link, type));

                } catch (IOException e) {
                    System.err.println("In Forward: SERVER Could not create socket to send message");
                    send_nack(sender, id, ErrorTypes.DESTINATION_NOT_REACHABLE);
                }
            },

            () -> {
                Optional<Link> opt2 = data.routing_table.stream().filter(l -> l.getDESTINATION() == sender).findFirst();
                opt2.ifPresentOrElse(
                    existing_link -> {
                        //send nack
                        System.err.println("Error. Received message to offline user");
                        send_nack(sender, id, ErrorTypes.DESTINATION_NOT_REACHABLE);
                    },
                        () -> System.out.println("hallo")
                );
            }

        );
    }



    private void handle_text_message(JSONObject message) {
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");
        int checksum = message.getJSONObject("HEADER").getInt("CHECKSUM");
        int id = message.getJSONObject("BODY").getInt("MSG_ID");
        String text = message.getJSONObject("BODY").getString("TEXT");
        String bodystring = message.getJSONObject("BODY").toString();

        CRC32 crc = new CRC32();
        crc.update(bodystring.getBytes(StandardCharsets.UTF_8));

        //sending response
        Optional<Link> opt = data.routing_table.stream().filter(l -> l.getDESTINATION() == sender).findFirst();
        opt.ifPresentOrElse(
            existing_link -> {
                try (Socket s = new Socket(IPString.string_from_int(existing_link.getGATEWAY()), data.port)) {

                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    Gson gson = new Gson();

                    if(checksum != ((int) crc.getValue())){

                        send_nack(sender, id, ErrorTypes.CHECKSUM_MISMATCH);
                        data.gui.logMessage("RECEIVED MESSAGE FROM: " + IPString.string_from_int(sender) + "\n" +
                                "  -> " + "\"" + text + "\"" + "\n" +
                                "  -> " + "NACK sent because of checksum mismatch");

                    } else {
                        Body b = new AckBody(id);
                        String bs = gson.toJson(b);
                        crc.reset();
                        crc.update(bs.getBytes());

                        Header h = Header.ACKHEADER;
                        h.DESTINATION_IP = sender;
                        h.SENDER_IP = data.get_own_IP_int();
                        h.CHECKSUM = (int) crc.getValue();

                        Message m = new Message(h, b);

                        out.println(gson.toJson(m));

                        data.gui.logMessage("RECEIVED MESSAGE FROM: " + IPString.string_from_int(sender) + "\n" +
                                "  -> " + "\"" + text + "\"" + "\n" +
                                "  -> " + "Ack sent");
                    }

                } catch (IOException e) {
                    System.err.println("SERVER sending ack Could not create socket to send message:" + e.getMessage());
                }
            },
            () -> data.gui.logMessage("No Connection to send ACK?")
        );

    }

    private void handle_acknowledge(JSONObject message) {
        int id = message.getJSONObject("BODY").getInt("MSG_ID");
        data.gui.logMessage("ACK: id: " + id);
        data.free_id(id);
    }

    private void handle_not_acknowledge(JSONObject message) {
        int id = message.getJSONObject("BODY").getInt("MSG_ID");
        int errorCode = message.getJSONObject("BODY").getInt("ERROR_CODE");
        StringBuilder sb = new StringBuilder("NACK: ");
        switch(errorCode){
            case ErrorTypes.TTL_ERROR:
                sb.append("TTL_ERROR, ");
                break;
            case ErrorTypes.DESTINATION_NOT_REACHABLE:
                sb.append("DESTINATION_NOT_REACHABLE, ");
                break;
            case ErrorTypes.CHECKSUM_MISMATCH:
                sb.append("CHECKSUM_MISMATCH, ");
                break;
            case ErrorTypes.INVALID_MESSAGE_FORMAT:
                sb.append("INVALID_MESSAGE_FORMAT, ");
                break;
            case ErrorTypes.BODY_SIZE_EXCEEDED:
                sb.append("BODY_SIZE_EXCEEDED, ");
                break;
            case ErrorTypes.OTHER:
                sb.append("OTHER, ");
                break;
        }
        sb.append("id: ").append(id);

        data.gui.logMessage(sb.toString());
        data.free_id(id);
    }

    private void handle_routing_information(JSONObject message) {
        JSONArray rt = message.getJSONObject("BODY").getJSONArray("ROUTING_TABLE");
        int sender = message.getJSONObject("HEADER").getInt("SENDER_IP");

        for (int i = 0; i < rt.length(); i++) {

            Link new_link = new Link(rt.getJSONObject(i).getInt("DESTINATION"),
                    sender,
                    rt.getJSONObject(i).getInt("HOP_COUNT")
            );

            Optional<Link> opt = data.routing_table.stream().filter(e -> e.getDESTINATION() == new_link.getDESTINATION()).findFirst();
            opt.ifPresentOrElse(
                existing_link -> {
                    if (existing_link.getHOP_COUNT() > new_link.getHOP_COUNT() + 1) {
                        existing_link.setGATEWAY(new_link.getGATEWAY());
                        existing_link.setHOP_COUNT(new_link.getHOP_COUNT() + 1);
                    }
                    /*
                    if (existing_link.getHOP_COUNT() == new_link.getHOP_COUNT() + 1 &&
                            existing_link.getGATEWAY() != new_link.getGATEWAY()) {

                        new_link.increment_hop_count();
                        data.routing_table.add(new_link);
                    }
                    */
                },
                () -> {
                    new_link.increment_hop_count();
                    data.routing_table.add(new_link);
                    data.gui.logMessage("Just CONNECTED: " + IPString.string_from_int(new_link.getDESTINATION()));
                }
            );

        }


        //check if indirect connections lost
        for(Link link : data.routing_table.stream().filter(e -> e.getGATEWAY() == sender).toList()){
            boolean found = false;
            for (int i = 0; i < rt.length(); i++) {
                if (rt.getJSONObject(i).getInt("DESTINATION") == link.getDESTINATION() &&
                        rt.getJSONObject(i).getInt("HOP_COUNT") <= 16) {
                    found = true;
                }
            }

            if (!found) {
                data.routing_table.remove(link);
                data.gui.logMessage("Just DISCONNECTED: " + IPString.string_from_int(link.getDESTINATION()));
            }
        }

    }

    private void send_nack(int sender, int id, int type) {
        Optional<Link> link = data.routing_table.stream().filter(e -> e.getDESTINATION() == sender).findFirst();
        link.ifPresentOrElse(
            existing_link -> {

                try (Socket s = new Socket(IPString.string_from_int(existing_link.getGATEWAY()), data.port)) {

                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);

                    Header h = new Header();
                    h.MSG_TYPE = MessageTypes.NACK;
                    h.DESTINATION_IP = sender;
                    h.SENDER_IP = data.get_own_IP_int();
                    NackBody b = new NackBody(id, type);
                    Message m = new Message(h, b);
                    Gson gson = new Gson();
                    out.println(gson.toJson(m));

                } catch (IOException e) {
                    System.err.println("In send nack: SERVER Could not create socket to send message");
                }
            },
                () -> System.out.println("No Connection to send nack?")
        );
    }

    private String build_forward_string(Link link, int type){
        StringBuilder sb = new StringBuilder("Forwarding");
        switch(type){
            case MessageTypes.TEXT_MESSAGE:
                sb.append(" TEXT MESSAGE");
                break;
            case MessageTypes.ROUTING_INFORMATION:
                sb.append(" ROUTING INFORMATION");
                break;
            case MessageTypes.ACK:
                sb.append(" ACK");
                break;
            case MessageTypes.NACK:
                sb.append(" NACK");
                break;
        }
        sb.append(" to: ").append(IPString.string_from_int(link.getGATEWAY()));
        return sb.toString();
    }

}

