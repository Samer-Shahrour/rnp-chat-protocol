import java.io.*;
import java.net.*;

import com.google.gson.Gson;
import communication.Header;
import communication.Message;


public class Server implements Runnable{

    private int port;
    private ServerSocket socket;

    public Server() {
        port = 8080;  //default
        try {

            socket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

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

                Gson gson = new Gson();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    Message m = gson.fromJson(inputLine, Message.class);
                    m.body.msg = m.body.msg + " from server";
                    System.out.println("SERVER REPLYING: \n" + m);
                    out.println(gson.toJson(m));
                }
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }
}
