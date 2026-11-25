package com.example.firewall.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Simulator extends JFrame {

    private JTextField urlField;
    private JTextField threadField;
    private JTextField repeatField;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton stopButton;

    private JCheckBox sqlInjectionQuery;
    private JCheckBox sqlInjectionBody;
    private JCheckBox xssAttack;
    private JCheckBox pathTraversal;
    private JCheckBox normalRequest;

    private ExecutorService executor;
    private volatile boolean isRunning = false;

    private final AtomicInteger blockedCount = new AtomicInteger(0);
    private final AtomicInteger allowedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger totalExecuted = new AtomicInteger(0);

    public static String url = ConstantsInUse.URL_USE;

    public Simulator() {
        setTitle("Almighty Firewall - Attack & Traffic Simulator");
        setSize(780, 620);
        setMinimumSize(new Dimension(650, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ---------------- TOP: Configuration Panel ----------------
        JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 6));
        configPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10),
                BorderFactory.createTitledBorder("Simulation Configuration")
        ));

        configPanel.add(new JLabel("Target URL:"));
        urlField = new JTextField(url != null && !url.isEmpty() ? url : "http://localhost:7080/auth");
        configPanel.add(urlField);

        configPanel.add(new JLabel("Worker Threads:"));
        threadField = new JTextField("5");
        configPanel.add(threadField);

        configPanel.add(new JLabel("Repeat Count (per attack):"));
        repeatField = new JTextField("5"); // Default 5 instead of 0
        configPanel.add(repeatField);

        add(configPanel, BorderLayout.NORTH);

        // ---------------- CENTER: Attacks on Left, Output Console on Right ----------------
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Left: Attack Vector Checkboxes
        JPanel attackPanel = new JPanel();
        attackPanel.setLayout(new BoxLayout(attackPanel, BoxLayout.Y_AXIS));
        attackPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Attack Vectors"),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        attackPanel.setPreferredSize(new Dimension(240, 300));

        sqlInjectionQuery = new JCheckBox("SQL Injection (Query)", true);
        sqlInjectionBody = new JCheckBox("SQL Injection (Body)", false);
        xssAttack = new JCheckBox("XSS Attack", true);
        pathTraversal = new JCheckBox("Path Traversal", true);
        normalRequest = new JCheckBox("Normal Request", true);

        attackPanel.add(sqlInjectionQuery);
        attackPanel.add(Box.createVerticalStrut(6));
        attackPanel.add(sqlInjectionBody);
        attackPanel.add(Box.createVerticalStrut(6));
        attackPanel.add(xssAttack);
        attackPanel.add(Box.createVerticalStrut(6));
        attackPanel.add(pathTraversal);
        attackPanel.add(Box.createVerticalStrut(6));
        attackPanel.add(normalRequest);
        attackPanel.add(Box.createVerticalStrut(15));

        JPanel selectButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        selectAllBtn.addActionListener(e -> setAllCheckboxes(true));

        JButton deselectAllBtn = new JButton("Clear");
        deselectAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        deselectAllBtn.addActionListener(e -> setAllCheckboxes(false));

        selectButtons.add(selectAllBtn);
        selectButtons.add(deselectAllBtn);
        attackPanel.add(selectButtons);

        attackPanel.add(Box.createVerticalGlue());

        centerPanel.add(attackPanel, BorderLayout.WEST);

        // Right: Log Console
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder("Simulation Console Logs"));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(new Color(24, 24, 27));
        outputArea.setForeground(new Color(228, 228, 231));
        outputArea.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel logActions = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Ready. Configure parameters and click 'Start Simulation'.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logActions.add(statusLabel, BorderLayout.WEST);

        JButton clearLogBtn = new JButton("Clear Logs");
        clearLogBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        clearLogBtn.addActionListener(e -> outputArea.setText(""));
        logActions.add(clearLogBtn, BorderLayout.EAST);

        logPanel.add(logActions, BorderLayout.SOUTH);

        centerPanel.add(logPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // ---------------- SOUTH: Action Buttons ----------------
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        startButton = new JButton("Start Simulation");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        startButton.setPreferredSize(new Dimension(160, 40));
        startButton.addActionListener(this::startSimulation);

        stopButton = new JButton("Stop Simulation");
        stopButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        stopButton.setPreferredSize(new Dimension(160, 40));
        stopButton.setEnabled(false);
        stopButton.addActionListener(this::stopSimulation);

        bottomPanel.add(stopButton);
        bottomPanel.add(startButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void setAllCheckboxes(boolean selected) {
        sqlInjectionQuery.setSelected(selected);
        sqlInjectionBody.setSelected(selected);
        xssAttack.setSelected(selected);
        pathTraversal.setSelected(selected);
        normalRequest.setSelected(selected);
    }

    public void startSimulation(ActionEvent e) {
        String targetUrl = urlField.getText().trim();
        if (targetUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Target URL cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int threads;
        try {
            threads = Integer.parseInt(threadField.getText().trim());
            if (threads <= 0) threads = 1;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Threads must be a positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int repeat;
        try {
            repeat = Integer.parseInt(repeatField.getText().trim());
            if (repeat <= 0) {
                JOptionPane.showMessageDialog(this, "Repeat Count must be greater than 0! (e.g. 5 or 10)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Repeat Count must be a positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Attack> attacks = getSelectedAttacks(targetUrl);
        if (attacks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one attack vector from the left panel!", "No Attack Selected", JOptionPane.WARNING_MESSAGE);
            updateUI("⚠ [WARNING] No attack vectors selected. Please check at least one box.");
            return;
        }

        final int finalRepeat = repeat;
        final int totalRequests = attacks.size() * finalRepeat;

        blockedCount.set(0);
        allowedCount.set(0);
        failedCount.set(0);
        totalExecuted.set(0);
        isRunning = true;

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("Status: Running simulation... (0 / " + totalRequests + " requests)");

        updateUI("================================================================================");
        updateUI("▶ SIMULATION STARTED: " + totalRequests + " total requests (" + attacks.size() + " types x " + repeat + " repeat, " + threads + " threads)");
        updateUI("Target: " + targetUrl);
        updateUI("================================================================================");

        executor = Executors.newFixedThreadPool(threads);

        // Run dispatcher on background thread so Swing UI doesn't freeze
        new Thread(() -> {
            for (int i = 0; i < finalRepeat && isRunning; i++) {
                for (Attack attack : attacks) {
                    if (!isRunning) break;
                    executor.submit(() -> {
                        if (!isRunning) return;
                        sendAttack(attack, totalRequests);
                    });
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                isRunning = false;
                statusLabel.setText(String.format("Status: Complete. Total: %d | Blocked: %d | Allowed: %d | Failed: %d",
                        totalExecuted.get(), blockedCount.get(), allowedCount.get(), failedCount.get()));

                updateUI("================================================================================");
                updateUI(String.format("🏁 SIMULATION COMPLETED: Total: %d | 🛡 Blocked: %d | ✔ Allowed: %d | ✖ Failed: %d",
                        totalExecuted.get(), blockedCount.get(), allowedCount.get(), failedCount.get()));
                updateUI("================================================================================");
            });
        }).start();
    }

    public void stopSimulation(ActionEvent e) {
        if (isRunning) {
            isRunning = false;
            if (executor != null) {
                executor.shutdownNow();
            }
            updateUI("⏹ [STOPPED] Simulation interrupted by user.");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusLabel.setText("Status: Stopped by user.");
        }
    }

    private List<Attack> getSelectedAttacks(String baseUrl) {
        List<Attack> list = new ArrayList<>();

        if (sqlInjectionQuery.isSelected()) {
            list.add(new Attack(
                    "SQLi (Query)",
                    baseUrl + "?query=" + ConstantsInUse.ENCODEDQUERY,
                    "POST",
                    "{}"
            ));
        }

        if (sqlInjectionBody.isSelected()) {
            list.add(new Attack(
                    "SQLi (Body)",
                    baseUrl,
                    "POST",
                    "{\"query\":\"" + ConstantsInUse.ENCODEDQUERY + "\",\"username\":\"admin' OR '1'='1\"}"
            ));
        }

        if (xssAttack.isSelected()) {
            list.add(new Attack(
                    "XSS Attack",
                    baseUrl + "?query=" + ConstantsInUse.ENCODEDXSS,
                    "POST",
                    "{}"
            ));
        }

        if (pathTraversal.isSelected()) {
            list.add(new Attack(
                    "Path Traversal",
                    baseUrl + "?query=" + ConstantsInUse.ENCODEDPATH,
                    "POST",
                    "{}"
            ));
        }

        if (normalRequest.isSelected()) {
            list.add(new Attack(
                    "Normal Request",
                    baseUrl,
                    "POST",
                    "{\"username\":\"john_doe\",\"action\":\"health_check\"}"
            ));
        }

        return list;
    }

    public static SimulationResult executeAttack(Attack attack) {
        HttpURLConnection conn = null;
        try {
            URL target = new URL(attack.url);
            conn = (HttpURLConnection) target.openConnection();
            conn.setRequestMethod(attack.method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if ("POST".equalsIgnoreCase(attack.method) || "PUT".equalsIgnoreCase(attack.method)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                byte[] bytes = attack.payload != null ? attack.payload.getBytes(StandardCharsets.UTF_8) : new byte[0];
                conn.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                    os.flush();
                }
            }

            int responseCode = conn.getResponseCode();
            boolean isBlocked = (responseCode == 403);
            return new SimulationResult(attack.name, attack.url, responseCode, isBlocked, null);
        } catch (Exception ex) {
            return new SimulationResult(attack.name, attack.url, -1, false, ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void sendAttack(Attack attack, int totalRequests) {
        SimulationResult result = executeAttack(attack);
        int current = totalExecuted.incrementAndGet();

        if (result.responseCode == 403) {
            blockedCount.incrementAndGet();
            updateUI(String.format("[%s] 🛡 [403 BLOCKED BY FIREWALL] %s -> %s", timestamp(), attack.name, attack.url));
        } else if (result.responseCode == 200) {
            allowedCount.incrementAndGet();
            updateUI(String.format("[%s] ✔ [200 ALLOWED] %s -> %s", timestamp(), attack.name, attack.url));
        } else if (result.responseCode > 0) {
            updateUI(String.format("[%s] ℹ [%d RESPONSE] %s -> %s", timestamp(), result.responseCode, attack.name, attack.url));
        } else {
            failedCount.incrementAndGet();
            updateUI(String.format("[%s] ✖ [FAILED] %s -> Error: %s", timestamp(), attack.name, result.errorMessage));
        }

        SwingUtilities.invokeLater(() -> statusLabel.setText(
                String.format("Status: Simulating... (%d / %d) | Blocked: %d | Allowed: %d | Failed: %d",
                        current, totalRequests, blockedCount.get(), allowedCount.get(), failedCount.get())
        ));
    }

    private static String timestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    private void updateUI(String message) {
        SwingUtilities.invokeLater(() -> {
            if (outputArea != null) {
                outputArea.append(message + "\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            }
        });
    }

    public static void runCliSimulation(String[] args) {
        String targetUrl = (args.length > 1 && !args[1].isEmpty()) ? args[1] : ConstantsInUse.URL_USE;
        int repeat = 2;
        if (args.length > 2) {
            try {
                repeat = Integer.parseInt(args[2]);
            } catch (Exception ignored) {
            }
        }

        System.out.println("================================================================================");
        System.out.println("  ALMIGHTY FIREWALL - ATTACK & TRAFFIC SIMULATION (CLI MODE)");
        System.out.println("  Target URL : " + targetUrl);
        System.out.println("  Repeat     : " + repeat);
        System.out.println("================================================================================");

        List<Attack> attacks = List.of(
                new Attack("SQLi (Query)", targetUrl + "?query=" + ConstantsInUse.ENCODEDQUERY, "POST", "{}"),
                new Attack("SQLi (Body)", targetUrl, "POST", "{\"query\":\"" + ConstantsInUse.ENCODEDQUERY + "\"}"),
                new Attack("XSS Attack", targetUrl + "?query=" + ConstantsInUse.ENCODEDXSS, "POST", "{}"),
                new Attack("Path Traversal", targetUrl + "?query=" + ConstantsInUse.ENCODEDPATH, "POST", "{}"),
                new Attack("Normal Request", targetUrl, "POST", "{\"username\":\"john_doe\"}")
        );

        int blocked = 0;
        int allowed = 0;
        int failed = 0;

        for (int i = 1; i <= repeat; i++) {
            System.out.println("\n--- Round " + i + " of " + repeat + " ---");
            for (Attack attack : attacks) {
                SimulationResult res = executeAttack(attack);
                if (res.responseCode == 403) {
                    blocked++;
                    System.out.printf("  [%s] 🛡 [403 BLOCKED BY FIREWALL] %s -> %s\n", timestamp(), res.name, res.url);
                } else if (res.responseCode == 200) {
                    allowed++;
                    System.out.printf("  [%s] ✔ [200 ALLOWED] %s -> %s\n", timestamp(), res.name, res.url);
                } else if (res.responseCode > 0) {
                    System.out.printf("  [%s] ℹ [%d RESPONSE] %s -> %s\n", timestamp(), res.responseCode, res.name, res.url);
                } else {
                    failed++;
                    System.out.printf("  [%s] ✖ [FAILED] %s -> %s (Error: %s)\n", timestamp(), res.name, res.url, res.errorMessage);
                }
            }
        }

        System.out.println("\n================================================================================");
        System.out.printf("  SIMULATION COMPLETE: Total: %d | 🛡 Blocked: %d | ✔ Allowed: %d | ✖ Failed: %d\n",
                (blocked + allowed + failed), blocked, allowed, failed);
        System.out.println("================================================================================\n");
    }

    public static class Attack {
        public String name;
        public String url;
        public String method;
        public String payload;

        public Attack(String name, String url, String method, String payload) {
            this.name = name;
            this.url = url;
            this.method = method;
            this.payload = payload;
        }
    }

    public static class SimulationResult {
        public String name;
        public String url;
        public int responseCode;
        public boolean isBlocked;
        public String errorMessage;

        public SimulationResult(String name, String url, int responseCode, boolean isBlocked, String errorMessage) {
            this.name = name;
            this.url = url;
            this.responseCode = responseCode;
            this.isBlocked = isBlocked;
            this.errorMessage = errorMessage;
        }
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || (args.length > 0 && "--cli".equalsIgnoreCase(args[0]))) {
            runCliSimulation(args);
        } else {
            SwingUtilities.invokeLater(Simulator::new);
        }
    }
}
