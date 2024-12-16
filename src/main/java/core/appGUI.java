package core;

import javax.swing.*;
import java.awt.*;

public class appGUI {

    public JTextArea logArea;
    public DefaultListModel<String> listModel;
    private Font font;

    public appGUI(){
        setLookAndFeel();
        font = new Font("Source Code Pro", Font.BOLD, 14);

    }


    private void setLookAndFeel() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Failed to apply Nimbus Look and Feel");
        }
    }

    public void logMessage(String message) {
        logArea.append(message + "\n");
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("RNP Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Set background color
        frame.getContentPane().setBackground(Color.DARK_GRAY);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(font);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Styling scrollpane
        logScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Connected Devices List
        listModel = new DefaultListModel<>();
        JList<String> deviceList = new JList<>(listModel);
        deviceList.setBackground(new Color(30, 30, 30));
        deviceList.setForeground(Color.WHITE);
        deviceList.setFont(font);
        JScrollPane listScrollPane = new JScrollPane(deviceList);
        listScrollPane.setPreferredSize(new Dimension(400, 0)); // Fixed width for device list
        listScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // Controls Panel
        JPanel controlsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        controlsPanel.setBackground(Color.DARK_GRAY);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField ipField = new JTextField();
        JTextField messageField = new JTextField();
        JButton connectButton = createFlatButton("Connect", new Color(34, 139, 34));
        JButton sendMessageButton = createFlatButton("Send Message", new Color(30, 144, 255));
        JButton listButton = createFlatButton("List Devices", new Color(255, 165, 0));
        JButton disconnectButton = createFlatButton("Disconnect", new Color(220, 20, 60));
        JButton exitButton = createFlatButton("Exit", new Color(139, 0, 0));

        // Add components to Controls Panel
        controlsPanel.add(createStyledLabel("Enter IP Address:"));
        controlsPanel.add(ipField);
        controlsPanel.add(createStyledLabel("Type Message:"));
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
        connectButton.addActionListener(e -> Main.connect(ipField.getText()));
        sendMessageButton.addActionListener(e -> Main.sendMessage(messageField.getText(), ipField.getText()));
        listButton.addActionListener(e -> Main.listDevices());
        disconnectButton.addActionListener(e -> Main.disconnect());
        exitButton.addActionListener(e -> Main.exitApplication());
    }

    private JButton createFlatButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(font);
        return label;
    }
}
