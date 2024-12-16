package core;

import communication.Header;
import communication.Link;
import utils.IPString;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    static Server server;
    static List<Link> routing_table;
    static RoutingClient rclient;
    static TextClient tclient;
    static int own_ip;

    static Thread serverThread;
    static Thread routingClientThread;

    static appGUI gui;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gui = new appGUI();
            gui.createAndShowGUI();
            initialize(args);
        });
    }


    private static void initialize(String[] args) {
        own_ip = IPString.int_from_string(args[0]);
        gui.logMessage("Program started on IP: " + args[0]);
        Header.own_ip = own_ip;
        routing_table = new CopyOnWriteArrayList<>();
        Link mylink = new Link(own_ip, own_ip, 0);
        routing_table.add(mylink);

        // Start Server
        server = new Server(routing_table, IPString.string_from_int(own_ip));
        serverThread = new Thread(server);
        serverThread.start();

        // Start Routing Client
        rclient = new RoutingClient(routing_table, own_ip);
        routingClientThread = new Thread(rclient);
        routingClientThread.start();

        // Start Text Client
        tclient = new TextClient(routing_table, own_ip);
    }



    public static void sendMessage(String message, String destinationIP) {
        if (!destinationIP.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            gui.logMessage("Invalid IP address. Please enter a valid IP address in the 'Enter IP Address' field.");
            return;
        }

        if (message.isEmpty()) {
            gui.logMessage("Message cannot be empty.");
            return;
        }

        gui.logMessage("Sending to " + destinationIP + ": " + message);
        tclient.send_to(destinationIP, message);
    }


    public static void connect(String ipAddress) {
        if (!ipAddress.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            gui.logMessage("Invalid IP address. Please try again.");
            return;
        }

        gui.logMessage("Initiating connection with " + ipAddress + "...");
        if (rclient.initiate_connection(ipAddress)) {
            Link mylink = new Link(IPString.int_from_string(ipAddress),
                    IPString.int_from_string(ipAddress), 1);

            if (!routing_table.contains(mylink)) {
                routing_table.add(mylink);
            }

            gui.logMessage("Successfully connected to " + ipAddress);
        } else {
            gui.logMessage("Failed to connect to " + ipAddress);
        }
    }

    static void disconnect() {
        gui.logMessage("Disconnecting...");
        server.pause();
        routing_table = routing_table.stream().filter(
                (entry) -> entry.getDESTINATION() == own_ip
        ).toList();
        gui.logMessage("Disconnected from the network.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Handle Exception
        }
        server.resume();
    }

    static void listDevices() {
        gui.logMessage("Listing connected devices...");
        gui.listModel.clear(); // Clear the list model to avoid duplicates

        for (Link link : routing_table) {
            if (link.getDESTINATION() == own_ip) {
                gui.listModel.addElement("You are connected as: " + IPString.string_from_int(link.getDESTINATION()));
            } else {
                gui.listModel.addElement("Destination: " + IPString.string_from_int(link.getDESTINATION()) + " (Gateway: " + IPString.string_from_int(link.getGATEWAY()) + ")");
            }
        }
    }

    static void exitApplication() {
        gui.logMessage("Exiting application...");
        disconnect();
        System.exit(0);
    }
}
