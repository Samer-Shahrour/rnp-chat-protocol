package core;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class appGUI {

    public JTextArea logArea;
    public DefaultListModel<String> listModel;
    private final Font font;
    private final String myip;

    public appGUI(String ip){
        setLookAndFeel();
        font = new Font("Source Code Pro", Font.BOLD, 16);
        myip = ip;
    }


    private void setLookAndFeel() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Failed to apply Nimbus Look and Feel");
        }
    }

    public void logMessage(String message) {
        logArea.append("> " + message + "\n");
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("RNP Chat, IP: " + myip);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(870, 600);
        frame.setMinimumSize(new Dimension(870, 650));

        // Set background color
        frame.getContentPane().setBackground(Color.DARK_GRAY);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        //label
        JLabel label = createStyledLabel("--- IP: " + myip + " ---");
        label.setFont(font.deriveFont(Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));


        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(font.deriveFont(Font.PLAIN, 16));
        JScrollPane logScrollPane = new JScrollPane(logArea);

        JScrollBar verticalScrollbar = logScrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollbar = logScrollPane.getHorizontalScrollBar();

        verticalScrollbar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(100, 100, 100); // Darker thumb color
                trackColor = new Color(60, 60, 60); // Darker track color
            }
        });

        horizontalScrollbar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(100, 100, 100);
                trackColor = new Color(60, 60, 60);
            }
        });

        // Styling scrollpane
        logScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Connected Devices List
        listModel = new DefaultListModel<>();
        JList<String> deviceList = new JList<>(listModel);
        deviceList.setFocusable(false);
        deviceList.setBackground(new Color(30, 30, 30));
        deviceList.setForeground(Color.WHITE);
        deviceList.setFont(font);

        JScrollPane listScrollPane = new JScrollPane(deviceList);
        listScrollPane.setPreferredSize(new Dimension(300, 0)); // Fixed width for device list
        listScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        verticalScrollbar = listScrollPane.getVerticalScrollBar();
        horizontalScrollbar = listScrollPane.getHorizontalScrollBar();

        verticalScrollbar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(100, 100, 100); // Darker thumb color
                trackColor = new Color(60, 60, 60); // Darker track color
            }
        });

        horizontalScrollbar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(100, 100, 100);
                trackColor = new Color(60, 60, 60);
            }
        });


        // Controls Panel
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBackground(Color.DARK_GRAY);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Row 1: IP Label and IP Field
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        controlsPanel.add(createStyledLabel("Enter IP Address:"), gbc);

        gbc.gridx = 1; // Column 1
        JTextField ipField = new JTextField(11);
        ipField.setBackground(new Color(230, 230, 230));
        ipField.setFont(font.deriveFont(Font.PLAIN, 16));
        controlsPanel.add(ipField, gbc);

        // Row 2: Message Label and Message Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlsPanel.add(createStyledLabel("Type Message:"), gbc);

        gbc.gridx = 1;
        JTextField messageField = new JTextField(11);
        messageField.setBackground(new Color(230, 230, 230));
        messageField.setFont(font.deriveFont(Font.PLAIN, 16));
        controlsPanel.add(messageField, gbc);

        // Row 3: Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span two columns
        gbc.anchor = GridBagConstraints.CENTER; // Center-align the buttons
        // Add vertical gap between Row 2 and Row 3
        gbc.insets = new Insets(10, 5, 5, 5); // Top inset increased to 20 for extra gap

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton connectButton = createFlatButton("Connect", new Color(34, 139, 34));
        JButton sendMessageButton = createFlatButton("Send Message", new Color(30, 144, 255));
        JButton listButton = createFlatButton("List Devices", new Color(204,170,0));
        JButton disconnectButton = createFlatButton("Disconnect", new Color(190, 15, 30));
        //JButton exitButton = createFlatButton("Exit", new Color(139, 0, 0));
        JButton clearButton = createFlatButton("Clear", new Color(139, 139, 139));


        //select function
        deviceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Ensure the event is not fired multiple times
                String selectedValue = deviceList.getSelectedValue();
                if (selectedValue == null) return;

                // Use a regular expression to extract the IP address
                Pattern pattern = Pattern.compile("DESTINATION: (\\d+\\.\\d+\\.\\d+\\.\\d+)");
                Matcher matcher = pattern.matcher(selectedValue);
                if (matcher.find()) {
                    String ipAddress = matcher.group(1); // Extract the matched IP

                    if (ipAddress.equalsIgnoreCase(myip)){
                        ipField.setText("THIS IS YOU, please select a different host");
                    } else {
                        ipField.setText(ipAddress);
                    }

                } else {
                    ipField.setText("please select a destination");
                }
            }
        });

        buttonPanel.add(connectButton);
        buttonPanel.add(sendMessageButton);
        buttonPanel.add(listButton);
        buttonPanel.add(disconnectButton);
        //buttonPanel.add(exitButton);
        buttonPanel.add(clearButton);

        controlsPanel.add(buttonPanel, gbc);

        // Add Components to Main Panel
        mainPanel.add(label, BorderLayout.NORTH);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(listScrollPane, BorderLayout.EAST);
        mainPanel.add(controlsPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        // Event Listeners
        connectButton.addActionListener     (_ -> Main.connect(ipField.getText().trim()));
        sendMessageButton.addActionListener (_ -> Main.send_message(messageField.getText().trim(), ipField.getText()));
        listButton.addActionListener        (_ -> Main.list_devices());
        disconnectButton.addActionListener  (_ -> Main.disconnect());
        //exitButton.addActionListener        (_ -> Main.exit_application());
        clearButton.addActionListener       (_ -> {
            logArea.setText("");
            ipField.setText("");
            messageField.setText("");
        });

    }

    private JButton createFlatButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(font.deriveFont(Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(text.length() * 10 + 40, 35));
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
        label.setFont(font.deriveFont(Font.PLAIN, 16));
        return label;
    }
}
