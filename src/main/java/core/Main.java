package core;

import communication.Header;
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
    static int own_ip;

    private static void initialize(String[] args) {
        own_ip = IPString.int_from_string(args[0]);
        Header.own_ip = own_ip;
        System.out.println("Starting program, instance ip is: " + IPString.string_from_int(own_ip));
        routing_table = new CopyOnWriteArrayList<>();
        Link mylink = new Link(own_ip,
                own_ip,
                0);
        routing_table.add(mylink);
        server = new Server(routing_table, own_ip);
        Thread t = new Thread(server);
        t.start();
        rclient = new RoutingClient(routing_table, own_ip);
        Thread t2 = new Thread(rclient);
        t2.start();
        tclient = new TextClient(routing_table, own_ip);
        sc = new Scanner(System.in);
    }

    public static void main(String[] args){
        initialize(args);
        program_loop();
    }

    private static void program_loop(){
        String input;

        System.out.println("============================================================");
        System.out.println("         Please type one of the following commands:         ");
        System.out.println("============================================================");
        System.out.println("1. LIST                : Display all connected devices.");
        System.out.println("2. IP ADDRESS          : View the device's IP address.");
        System.out.println("3. CONNECT <ipaddress> : Establish a connection to a device.");
        System.out.println("4. EXIT                : Terminate the program.");
        System.out.println("============================================================");

            while (true) {
                System.out.print("> ");
                input = sc.nextLine().trim();

                if (input.toLowerCase().startsWith("connect")) {
                    connect(input);
                } else if (input.equalsIgnoreCase("EXIT")) {
                    System.out.println("Exiting.");
                    break;
                } else if (input.equalsIgnoreCase("LIST")) {
                    list();
                } else if (input.equalsIgnoreCase("DISCONNECT")) {
                    disconnect();
                } else if (input.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                    send_msg(input);
                } else {
                    System.out.println("Unknown command.");
                }
            }
            sc.close();
    }

    private static void disconnect() {
        //TODO
    }

    private static void send_msg(String destination_ip){
        String input;

        System.out.println("Sending to: \"" + destination_ip + "\". Please type your Message:");
        System.out.println("Or type 'switch' to change IP address.");

        while (true) {
            System.out.print("> ");
            input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("switch")) {
                break;
            }
            tclient.send_to(destination_ip, input);
        }

    }

    private static void connect(String input){

        String[] parts = input.split("\\s+");
        if(parts.length != 2){
            System.out.println("Usage: connect <ipaddress>");
            return;
        }

        String ipAddress = parts[1];
        if (!ipAddress.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            System.out.println("Invalid IP address. Please try again.");
            return;
        }

        System.out.println("initiating connection with " + ipAddress + "...");

        if(rclient.initiate_connection(ipAddress)){
            Link mylink = new Link(IPString.int_from_string(ipAddress),
                    IPString.int_from_string(ipAddress),
                    1);

            if (!routing_table.contains(mylink)){
                routing_table.add(mylink);
            }

            System.out.println("Successfully connected to " + ipAddress);
            return;
        }

        System.out.println("Failed to connect to " + ipAddress);
    }

    private static void list() {
        if (routing_table.isEmpty()) {
            System.out.println("No devices are connected.");
            return;
        }
        System.out.println("======================================");
        System.out.println("          Connected devices:          ");
        System.out.println("======================================");
        for (Link link : routing_table) {
            if(link.getDESTINATION() != own_ip){
                System.out.println("Destination : " + IPString.string_from_int(link.getDESTINATION()));
                System.out.println("Gateway     : " + IPString.string_from_int(link.getGATEWAY()));
                System.out.println("Hop Count   : " + link.getHOP_COUNT());
                System.out.println("--------------------------------------");
            } else {
                System.out.println("Destination : " + IPString.string_from_int(link.getDESTINATION()));
                System.out.println("This is You");
                System.out.println(" ");
                System.out.println("--------------------------------------");
            }

        }
    }
}