package com.distrsys;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class SmartEnergyGUI extends JFrame {

    private final JTextArea outputArea;

    public SmartEnergyGUI() {

        // Window settings
        setTitle("Smart Energy Automation System");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main heading
        JLabel headingLabel =
                new JLabel("Smart Energy Automation System", JLabel.CENTER);

        headingLabel.setFont(new Font("Arial", Font.BOLD, 22));

        // Buttons
        JButton solarButton = new JButton("Solar Panel");
        JButton batteryButton = new JButton("Battery");
        JButton applianceButton = new JButton("Smart Appliances");
        JButton clearButton = new JButton("Clear");

        // Panel containing the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(solarButton);
        buttonPanel.add(batteryButton);
        buttonPanel.add(applianceButton);
        buttonPanel.add(clearButton);

        // Area used to display information from the services
        outputArea = new JTextArea();

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Add components to the window
        add(headingLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        /*
         * Button actions
         *
         * These currently display simple messages.
         * Later, each button will call the appropriate gRPC method.
         */

        solarButton.addActionListener(event -> {
            outputArea.append("Checking solar-panel status...\n");
        });

        batteryButton.addActionListener(event -> {
            outputArea.append("Checking battery level...\n");
        });

        applianceButton.addActionListener(event -> {
            outputArea.append("Opening appliance controls...\n");
        });

        clearButton.addActionListener(event -> {
            outputArea.setText("");
        });
    }

    public static void main(String[] args) {

        /*
         * SwingUtilities.invokeLater starts the interface
         * on Swing's user-interface thread.
         */
        SwingUtilities.invokeLater(() -> {

            SmartEnergyGUI gui = new SmartEnergyGUI();

            gui.setVisible(true);
        });
    }
}