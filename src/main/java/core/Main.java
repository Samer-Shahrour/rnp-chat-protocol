package core;

import communication.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {

    static Server server;

    public static void main(String[] args){
        List<Link> routing_table = new ArrayList<>();
        Server s = new Server(routing_table);
        program_loop();
    }

    private static void program_loop(){
            Scanner scanner = new Scanner(System.in);
            String input;
            boolean connected = false;

            System.out.println("Please type:");
            System.out.println("LIST       - Show all connected Devices.");
            System.out.println("IP ADDRESS - Send a message.");
            System.out.println("CONNECT    - to connect to a device.");
            System.out.println("EXIT       - Exit the program.");

            while (true) {
                System.out.print("> ");
                input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("CONNECT")) {
                    connect(scanner);
                    connected = true;
                }

                if (input.equalsIgnoreCase("EXIT")) {
                    System.out.println("Exiting.");
                    break;
                }

                else if (input.equalsIgnoreCase("LIST")) {
                    System.out.println("Listing all connected devices...");
                    //TODO
                }

                else if (input.matches("\\d.\\d.\\d.\\d")) {
                    send_msg(scanner, input);
                }

                else {
                    System.out.println("Unknown command.");
                }
            }

            scanner.close();
    }

    private static void send_msg(Scanner s, String destination_ip){
        String input;

        System.out.println("Sending to: \"" + destination_ip + "\". Please type your Message:");
        System.out.println("Or type 'switch' to change IP address.");

        while (true) {
            System.out.print("> ");
            input = s.nextLine().trim();

            if(input.equalsIgnoreCase("switch")){
                break;
            } else {
                System.out.println("Unknown command. Please try again.");
            }
        }

    }

    private static void connect(Scanner scanner){
        String input;

        System.out.println("Type IP Address to connect to:");

        while (true) {
            System.out.print("> ");
            input = scanner.nextLine().trim();

            if(input.matches("\\d.\\d.\\d.\\d")){
                break;
            } else {
                System.out.println("Unknown command. Please try again.");
            }
        }

    }
}