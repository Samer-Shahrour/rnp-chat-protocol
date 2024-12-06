package core;

import communication.Link;
import utils.IPString;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;


public class Main {

    static Server server;
    static List<Link> routing_table;
    static RoutingClient rclient;
    static TextClient tclient;
    static Scanner sc;

    public static void main(String[] args){
        int own_ip = IPString.string_to_ip(args[0]);
        System.out.println("Starting program, instance ip is: " + IPString.ip_to_string(own_ip));
        routing_table = new CopyOnWriteArrayList<>();
        server = new Server(routing_table, own_ip);
        rclient = new RoutingClient(routing_table);
        tclient = new TextClient(routing_table);
        sc = new Scanner(System.in);
        program_loop();
    }

    private static void program_loop(){
            String input;
            boolean connected = false;

            System.out.println("Please type:");
            System.out.println("LIST                  - Show all connected Devices.");
            System.out.println("IP ADDRESS            - Send a message.");
            System.out.println("CONNECT <ipaddress>   - to connect to a device.");
            System.out.println("EXIT                  - Exit the program.");

            while (true) {
                System.out.print("> ");
                input = sc.nextLine().trim();

                if (input.toLowerCase().startsWith("connect")) {
                    connected = connect(input);
                }
                else if (input.equalsIgnoreCase("EXIT")) {
                    System.out.println("Exiting.");
                    break;
                }

                else if (input.equalsIgnoreCase("LIST")) {

                    list();
                }

                else if (input.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                    if(connected){
                        send_msg(input);
                    } else {
                        System.out.println("Not connected to a device.");
                    }
                }

                else {
                    System.out.println("Unknown command.");
                }
            }

            sc.close();
    }

    private static void send_msg(String destination_ip){
        String input;

        System.out.println("Sending to: \"" + destination_ip + "\". Please type your Message:");
        System.out.println("Or type 'switch' to change IP address.");

        while (true) {
            System.out.print("> ");
            input = sc.nextLine().trim();

            if(input.equalsIgnoreCase("switch")){
                break;
            } else {
                tclient.send_to(destination_ip, input);
            }
        }

    }

    private static boolean connect(String input){

        String[] parts = input.split("\\s+");
        if (parts.length == 2) {
            String ipAddress = parts[1];
            if (ipAddress.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {

                System.out.println("initiating connection with " + input + "...");
                return rclient.initiate_connection(input);

            } else {
                System.out.println("Invalid IP address. Please try again.");
            }
        } else {
            System.out.println("Usage: connect <ipaddress>");
        }
        return false;
    }

    private static void list(){

        if(routing_table.isEmpty()){
            System.out.println("List is empty.");
            return;
        }

        System.out.println("Listing all connected devices...");
        for (Link link : routing_table) {
            System.out.println("Destination: " + IPString.ip_to_string(link.getDESTINATION()) + "\n" +
                    "Hop_count: " + IPString.ip_to_string(link.getHOP_COUNT()));
        }
    }
}