import java.io.*;
import java.net.*;
import java.util.List;

import com.google.gson.Gson;
import communication.*;


public class Client {
    String serverAddress = "10.8.0.6";
    int port;
    Socket socket;
    boolean routing;
    List<Link> routing_table;

    public Client(boolean routing, List<Link> rt){
        serverAddress = "10.8.0.6";
        port = 8080;
        routing_table = rt;
        this.routing = routing;
    }

    public Client(){
        serverAddress = "10.8.0.6";
        port = 8080;
        routing = false;
    }


    public void send(){


        try {
            if(routing){
                send_routing_information();
            }

            socket = new Socket(serverAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;

            Gson gson = new Gson();




            while (true) {


                System.out.println("Please enter message: ");
                userInput = stdIn.readLine();
                if(userInput != null) {

                    Header h = null;
                    h.MSG_TYPE = 0;
                    h.TTL = 16;
                    h.SIZE = (short) userInput.length();
                    h.set_sender_ip("24.0.35.0");
                    h.set_destination_ip("24.0.67.0");
                    h.SENDER_PORT = 8080;
                    h.DESTINATION_PORT = 8080;
                    h.CHECKSUM = 0;

                    Body b = new Body();
                    Message m = new Message(h, b);

                    System.out.println("CLIENT SENDING: \n" + m);
                    out.println(gson.toJson(m));


                } else{
                    System.out.println("Bye");
                    break;
                }
            }


        } catch (UnknownHostException e) {
            System.err.println("Host unknown: " + serverAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void send_routing_information() throws IOException {
        socket = new Socket(serverAddress, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Gson gson = new Gson();
        while(true){
            Header h = new Header();
            h.MSG_TYPE = 1;

            Body B = new RoutingBody(routing_table);
            Message m = new Message(h, B);
            System.out.println("CLIENT SENDING: \n" + m);
            out.println(gson.toJson(m));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void send_message(){

    }

}
