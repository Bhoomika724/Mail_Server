import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class MailClientGUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField regUserField, regPassField;
    private JTextField loginUserField, loginPassField;
    private JTextField sendToField, ccField, bccField;
    private JTextArea sendMsgArea, inboxArea;
    private String currentUser = "";

    public MailClientGUI() {
        connectToServer();
        setTitle("Mail Client with CC/BCC");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(registerPanel(), "Register");
        mainPanel.add(loginPanel(), "Login");
        mainPanel.add(mailPanel(), "Mail");
        add(mainPanel);
        cardLayout.show(mainPanel, "Register");
        setVisible(true);
    }

    private JPanel registerPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1));
        regUserField = new JTextField();
        regPassField = new JTextField();
        JButton registerBtn = new JButton("Register");
        JButton goToLogin = new JButton("Go to Login");
        panel.add(new JLabel("Register Username:"));
        panel.add(regUserField);
        panel.add(new JLabel("Password:"));
        panel.add(regPassField);
        panel.add(registerBtn);
        panel.add(goToLogin);

        registerBtn.addActionListener(e -> {
            out.println("REGISTER");
            out.println(regUserField.getText());
            out.println(regPassField.getText());
            try {
                String response = in.readLine();
                if (response.equals("REGISTERED")) {
                    JOptionPane.showMessageDialog(this, "Registered Successfully!");
                    cardLayout.show(mainPanel, "Login");
                } else {
                    JOptionPane.showMessageDialog(this, "User already exists.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        goToLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        return panel;
    }

    private JPanel loginPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1));
        loginUserField = new JTextField();
        loginPassField = new JTextField();
        JButton loginBtn = new JButton("Login");
        panel.add(new JLabel("Login Username:"));
        panel.add(loginUserField);
        panel.add(new JLabel("Password:"));
        panel.add(loginPassField);
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> {
            out.println("LOGIN");
            out.println(loginUserField.getText());
            out.println(loginPassField.getText());
            try {
                String response = in.readLine();
                if (response.equals("LOGIN_SUCCESS")) {
                    currentUser = loginUserField.getText();
                    cardLayout.show(mainPanel, "Mail");
                } else {
                    JOptionPane.showMessageDialog(this, "Login Failed.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return panel;
    }

    private JPanel mailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(10, 1));
        sendToField = new JTextField();
        ccField = new JTextField();
        bccField = new JTextField();
        sendMsgArea = new JTextArea(3, 20);
        JButton sendBtn = new JButton("Send Mail");

        top.add(new JLabel("Send To:"));
        top.add(sendToField);
        top.add(new JLabel("CC:"));
        top.add(ccField);
        top.add(new JLabel("BCC:"));
        top.add(bccField);
        top.add(new JLabel("Message:"));
        top.add(new JScrollPane(sendMsgArea));
        top.add(sendBtn);

        inboxArea = new JTextArea();
        inboxArea.setEditable(false);
        JButton refreshBtn = new JButton("Refresh Inbox");

        sendBtn.addActionListener(e -> {
            out.println("SEND");
            out.println(currentUser);
            out.println(sendToField.getText());
            out.println(ccField.getText());
            out.println(bccField.getText());
            out.println(sendMsgArea.getText());
            try {
                String response = in.readLine();
                if (response.equals("SENT")) {
                    JOptionPane.showMessageDialog(this, "Mail sent!");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        refreshBtn.addActionListener(e -> {
            out.println("INBOX");
            out.println(currentUser);
            try {
                inboxArea.setText("");
                String line;
                while (!(line = in.readLine()).equals("END_INBOX")) {
                    if (line.equals("INBOX_EMPTY")) {
                        inboxArea.append("No new messages.
");
                        break;
                    }
                    inboxArea.append(line + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(refreshBtn, BorderLayout.CENTER);
        panel.add(new JScrollPane(inboxArea), BorderLayout.SOUTH);
        return panel;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new MailClientGUI();
    }
}