import java.io.*;
import java.net.*;
import com.google.gson.Gson;
import communication.Body;
import communication.Header;
import communication.Message;


public class Client {
    String serverAddress = "localhost";
    int port = 8080;
    Socket socket;


    public void send(){

        try {
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

                    Header h = new Header();
                    h.msg_type = 0;
                    h.ttl = 16;
                    h.size = (short) userInput.length();
                    h.set_sender_ip("24.0.35.0");
                    h.set_destination_ip("24.0.67.0");
                    h.sender_port = 8080;
                    h.destination_port = 8080;
                    h.checksum = 0;

                    Body b = new Body();
                    b.msg = userInput;

                    Message m = new Message();
                    m.header = h;
                    m.body = b;

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

}
