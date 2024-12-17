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
        if (!ipAddress.trim().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
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
        rclient.pause();

        routing_table.clear();
        Link mylink = new Link(own_ip, own_ip, 0);
        routing_table.add(mylink);

        gui.logMessage("Disconnected from the network.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Handle Exception
        }

        serverThread.interrupt();
        routingClientThread.interrupt();

        rclient.resume();
        routingClientThread = new Thread(rclient);
        routingClientThread.start();

        server.resume();
        serverThread = new Thread(server);
        serverThread.start();

    }

    static void listDevices() {
        gui.logMessage("Listing connected devices...");
        gui.listModel.clear();

        gui.listModel.addElement( "<html><div style='white-space: pre;'>" +
                "========================" + "<br>" +
                "CONNECTED DEVICES:      " + "<br>" +
                "========================" + "</div><html>"
        );


        for (Link link : routing_table) {
            if (link.getDESTINATION() == own_ip) {
                gui.listModel.addElement(
                        "<html>DESTINATION: " + IPString.string_from_int(link.getDESTINATION()) + "<br>" +
                                "THIS IS YOU" + "<br>" +
                                "------------------------" + "</html>"
                );

            } else {
                gui.listModel.addElement(
                        "<html>DESTINATION: " + IPString.string_from_int(link.getDESTINATION()) + "<br>" +
                                "GATEWAY    : " + IPString.string_from_int(link.getGATEWAY()) + "<br>" +
                                "HOPCOUNT   : " + link.getHOP_COUNT() + "<br>" +
                                "------------------------" + "</html>"
                );
            }
        }
    }

    static void exitApplication() {
        gui.logMessage("Exiting application...");
        disconnect();
        System.exit(0);
    }
}
