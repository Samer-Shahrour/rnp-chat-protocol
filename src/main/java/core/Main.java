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



    public static void send_message(String message, String destination_IP) {
        if (!destination_IP.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            gui.logMessage("Invalid IP address");
            return;
        }

        if (message.isEmpty()) {
            gui.logMessage("Message cannot be empty.");
            return;
        }

        tclient.send_to(destination_IP, message);
    }


    public static void connect(String ipAddress) {
        if (!ipAddress.trim().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            gui.logMessage("Invalid IP address. Please try again.");
            return;
        }

        if (ipAddress.equalsIgnoreCase(data.get_own_IP_String())) {
            gui.logMessage("You just tried to connect to yourself?");
            return;
        }

        rclient.initiate_connection(ipAddress);
    }

    static void disconnect() {
        gui.logMessage("Disconnecting...");
        server.pause();
        rclient.pause();

        routing_table.clear();
        Link mylink = new Link(data.get_own_IP_int(), data.get_own_IP_int(), 0);
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

    static void list_devices() {
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
                              "HOP_COUNT..: " + link.getHOP_COUNT() + "<br>" +
                              "------------------------" + "</html>"
                );
            }
        }
    }

    static void exit_application() {
        gui.logMessage("Exiting application...");

        try {
            Thread.sleep(300);
        } catch (InterruptedException _) {

        }

        System.exit(0);
    }
}
