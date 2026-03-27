package com.example.firewall.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Simulator extends JFrame{
 
    private JTextField urlField;
    private JTextField threadField;
    private JTextField repeatField;
    private JTextArea outputArea;

    private JCheckBox sqlInjectionQuery;
    private JCheckBox sqlInjectionBody;
    private JCheckBox xssAttack;
    private JCheckBox pathTraversal;

    public Simulator() {

        setTitle("Attack Simulator");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);    
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 2));

        topPanel.add(new JLabel("Target URL:"));
        urlField = new JTextField("http://localhost:7080/auth");
        topPanel.add(urlField);

        topPanel.add(new JLabel("Threads:"));
        threadField = new JTextField("5");
        topPanel.add(threadField);

        topPanel.add(new JLabel("Repeat Count:"));
        repeatField = new JTextField("0");
        topPanel.add(repeatField);

        add(topPanel, BorderLayout.NORTH);

        JPanel attackPanel = new JPanel(new GridLayout(1, 1));
        attackPanel.setBorder(BorderFactory.createTitledBorder("Select Attacks"));
        attackPanel.setPreferredSize(new Dimension(20,40));

        Box box = Box.createVerticalBox();

        sqlInjectionQuery = new JCheckBox("SQL Injection query");
        xssAttack = new JCheckBox("XSS Attack");
        pathTraversal = new JCheckBox("Path Traversal");

        box.add(sqlInjectionQuery);
        box.add(xssAttack);
        box.add(pathTraversal);

        attackPanel.add(box);

        outputArea = new JTextArea(20,40);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        attackPanel.add(scrollPane);

        add(attackPanel);
       
        JButton startButton = new JButton("Start Simulation");
        startButton.setPreferredSize(new Dimension(200, 50));
        add(startButton, BorderLayout.SOUTH);

        startButton.addActionListener(this::startSimulation);

        setVisible(true);
    }

    public void startSimulation(ActionEvent e) {
        String url = urlField.getText();
        int threads = Integer.parseInt(threadField.getText());
        int repeat = Integer.parseInt(repeatField.getText());

        List<Attack> attacks = getSelectedAttacks(url);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (Attack attack : attacks) {
            for (int i = 0; i < repeat; i++) {
                executor.submit(() -> sendAttack(attack));
            }
        }

        executor.shutdown();
    }

    private List<Attack> getSelectedAttacks(String url) {
        List<Attack> list = new ArrayList<>();

        if (sqlInjectionQuery.isSelected()) {
            list.add(new Attack("SQL Injection","http://localhost:8080/auth?query=id=1 OR 1=1","GET",null
            ));
        }   

        // if (xssAttack.isSelected()) {
        //     list.add(new Attack("XSS Attack",url,"POST","<script>alert('XSS')</script>"
        //     ));
        // }

        // if(pathTraversal.isSelected()){
        //     list.add(new Attack("XSS Attack",url,"POST", "/../../etc/passwd"
        //     ));
        // }

        return list;
    }

    private void sendAttack(Attack attack) {
        try {
            URL target = new URL(attack.url);
            HttpURLConnection conn = (HttpURLConnection) target.openConnection(); 
            conn.setRequestMethod(attack.method); 
            conn.setDoOutput(true); 
            conn.setConnectTimeout(5000); 
            if (attack.payload != null && !attack.payload.isEmpty()) { 
                OutputStream os = conn.getOutputStream(); 
                os.write(attack.payload.getBytes()); 
                os.flush(); os.close(); 
            } 
            int responseCode = conn.getResponseCode(); 
            updateUI("✔ " + attack.name + " -> Response: " + responseCode);

        } catch (Exception ex) {
            updateUI("✖ " + attack.name + " -> Failed");
            ex.printStackTrace();
        }
    }

    private void updateUI(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(message + "\n");
        });
    }

    static class Attack {
        String name;
        String url;
        String method;
        String payload;

        public Attack(String name, String url, String method, String payload) {
            this.name = name;
            this.url = url;
            this.method = method;
            this.payload = payload;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Simulator::new);
    }
}