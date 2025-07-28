import java.io.*;
import java.net.*;

public class MailServer {
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Mail Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private static final String USERS_FILE = "users.txt";
    private static final String MAILS_FILE = "mails.txt";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String command;
            while ((command = in.readLine()) != null) {
                switch (command) {
                    case "REGISTER" -> handleRegister();
                    case "LOGIN" -> handleLogin();
                    case "SEND" -> handleSend();
                    case "INBOX" -> handleInbox();
                    case "EXIT" -> {
                        socket.close();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegister() throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(username + ":")) {
                out.println("USER_EXISTS");
                reader.close();
                return;
            }
        }
        reader.close();
        PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true));
        writer.println(username + ":" + password);
        writer.close();
        out.println("REGISTERED");
    }

    private void handleLogin() throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts[0].equals(username) && parts[1].equals(password)) {
                out.println("LOGIN_SUCCESS");
                reader.close();
                return;
            }
        }
        reader.close();
        out.println("LOGIN_FAILED");
    }

    private void handleSend() throws IOException {
        String sender = in.readLine();
        String to = in.readLine();
        String cc = in.readLine();
        String bcc = in.readLine();
        String message = in.readLine();
        PrintWriter writer = new PrintWriter(new FileWriter(MAILS_FILE, true));
        writer.println(to + ":" + sender + ":[TO] " + message);
        if (!cc.isEmpty()) {
            String[] ccList = cc.split(",");
            for (String user : ccList) {
                writer.println(user.trim() + ":" + sender + ":[CC] " + message);
            }
        }
        if (!bcc.isEmpty()) {
            String[] bccList = bcc.split(",");
            for (String user : bccList) {
                writer.println(user.trim() + ":" + sender + ":[BCC] " + message);
            }
        }
        writer.close();
        out.println("SENT");
    }

    private void handleInbox() throws IOException {
        String username = in.readLine();
        BufferedReader reader = new BufferedReader(new FileReader(MAILS_FILE));
        String line;
        boolean found = false;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":", 3);
            if (parts[0].equals(username)) {
                out.println("FROM: " + parts[1] + " - " + parts[2]);
                found = true;
            }
        }
        reader.close();
        if (!found) {
            out.println("INBOX_EMPTY");
        }
        out.println("END_INBOX");
    }
}