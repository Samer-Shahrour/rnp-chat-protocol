package core;

import communication.Header;
import communication.Link;
import utils.IPString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLOutput;
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

    private static JTextArea logArea;
    private static DefaultListModel<String> listModel;

    public static void main(String[] args) {
        initialize(args);
        createAndShowGUI();
    }

    private static void initialize(String[] args) {
        own_ip = IPString.int_from_string(args[0]);
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

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Networking Application, IP: " + IPString.string_from_int(own_ip));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Connected Devices List
        listModel = new DefaultListModel<>();
        JList<String> deviceList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(deviceList);

        // Controls Panel
        JPanel controlsPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // 6 rows, 2 columns with spacing
        JTextField ipField = new JTextField();
        JTextField messageField = new JTextField();
        JButton connectButton = new JButton("Connect");
        JButton sendMessageButton = new JButton("Send Message");
        JButton listButton = new JButton("List Devices");
        JButton disconnectButton = new JButton("Disconnect");
        JButton exitButton = new JButton("Exit");

        // Add components to Controls Panel
        controlsPanel.add(new JLabel("Enter IP Address:"));
        controlsPanel.add(ipField);
        controlsPanel.add(new JLabel("Type Message:"));
        controlsPanel.add(messageField);
        controlsPanel.add(connectButton);
        controlsPanel.add(sendMessageButton);
        controlsPanel.add(listButton);
        controlsPanel.add(disconnectButton);
        controlsPanel.add(exitButton);

        // Add Components to Main Panel
        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(listScrollPane, BorderLayout.EAST);
        mainPanel.add(controlsPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        // Event Listeners
        connectButton.addActionListener(e -> connect(ipField.getText()));
        sendMessageButton.addActionListener(e -> sendMessage(messageField.getText(), ipField.getText()));
        listButton.addActionListener(e -> listDevices());
        disconnectButton.addActionListener(e -> disconnect());
        exitButton.addActionListener(e -> exitApplication());
    }

    private static void sendMessage(String message, String destinationIP) {
        if (!destinationIP.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            logMessage("Invalid IP address. Please enter a valid IP address in the 'Enter IP Address' field.");
            return;
        }

        if (message.isEmpty()) {
            logMessage("Message cannot be empty.");
            return;
        }

        logMessage("Sending to " + destinationIP + ": " + message);
        tclient.send_to(destinationIP, message);
    }

    private static void logMessage(String message) {
        logArea.append(message + "\n");
    }

    private static void connect(String ipAddress) {
        if (!ipAddress.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            logMessage("Invalid IP address. Please try again.");
            return;
        }

        logMessage("Initiating connection with " + ipAddress + "...");
        if (rclient.initiate_connection(ipAddress)) {
            Link mylink = new Link(IPString.int_from_string(ipAddress),
                    IPString.int_from_string(ipAddress), 1);

            if (!routing_table.contains(mylink)) {
                routing_table.add(mylink);
            }

            logMessage("Successfully connected to " + ipAddress);
        } else {
            logMessage("Failed to connect to " + ipAddress);
        }
    }

    private static void disconnect() {
        logMessage("Disconnecting...");
        server.pause();
        routing_table = routing_table.stream().filter(
                (entry) -> entry.getDESTINATION() == own_ip
        ).toList();
        System.out.println(routing_table);
        logMessage("Disconnected from the network.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        server.resume();
    }

    private static void listDevices() {
        logMessage("Listing connected devices...");
        listModel.clear(); // Clear the list model to avoid duplicates
        listModel.addElement( "<html><div style='white-space: pre;'>" +
                "==============================" + "<br>" +
                "      CONNECTED DEVICES:      " + "<br>" +
                "==============================" + "</div><html>"
        );

        for (Link link : routing_table) {

            if (link.getDESTINATION() == own_ip) {
                listModel.addElement(
                        "<html>DESTINATION: " + IPString.string_from_int(link.getDESTINATION()) + "<br>" +
                              "THIS IS YOU" + "<br>" +
                              "------------------------------" + "</html>"
                );

            } else {

                listModel.addElement(
                        "<html>DESTINATION: " + IPString.string_from_int(link.getDESTINATION()) + "<br>" +
                              "GATEWAY    : " + IPString.string_from_int(link.getGATEWAY()) + "<br>" +
                              "HOPCOUNT   : " + link.getHOP_COUNT() + "<br>" +
                              "------------------------------" + "</html>"
                );
            }
        }
    }

    private static void exitApplication() {
        logMessage("Exiting application...");
        disconnect();
        System.exit(0);
    }
}
