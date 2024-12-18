package core;

import communication.RoutingClient;
import communication.Server;
import communication.TextClient;
import messages.Link;
import utils.IPString;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    static Server server;
    static List<Link> routing_table;
    static RoutingClient rclient;
    static TextClient tclient;
    static int own_ip;
    static Data data;

    static Thread serverThread;
    static Thread routingClientThread;

    static appGUI gui;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main <server_ip>: please provide ip address.");
            System.exit(3);
        }

        SwingUtilities.invokeLater(() -> {
            gui = new appGUI(args[0]);
            gui.createAndShowGUI();
            initialize(args);
        });
    }


    private static void initialize(String[] args) {
        own_ip = IPString.int_from_string(args[0]);
        routing_table = new CopyOnWriteArrayList<>();
        data = new Data(own_ip, routing_table, gui);

        // Start Server
        server = new Server(data);
        serverThread = new Thread(server);
        serverThread.start();

        // Start Routing Client
        rclient = new RoutingClient(data);
        routingClientThread = new Thread(rclient);
        routingClientThread.start();

        // Start Text Client
        tclient = new TextClient(data);
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

        tclient.send_to(destinationIP, message);
    }


    public static void connect(String ipAddress) {
        if (!ipAddress.trim().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            gui.logMessage("Invalid IP address. Please try again.");
            return;
        }

        gui.logMessage("Initiating connection with " + ipAddress + "...");
        rclient.initiate_connection(ipAddress);
        Link mylink = new Link(IPString.int_from_string(ipAddress),
                IPString.int_from_string(ipAddress), 1);

        routing_table.remove(mylink);
        routing_table.add(mylink);

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
                              "GATEWAY....: " + IPString.string_from_int(link.getGATEWAY()) + "<br>" +
                              "HOPCOUNT...: " + link.getHOP_COUNT() + "<br>" +
                              "------------------------" + "</html>"
                );
            }
        }
    }

    static void exitApplication() {
        gui.logMessage("Exiting application...");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException _) {

        }
        System.exit(0);
    }
}
