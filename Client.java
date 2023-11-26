import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private static final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public Client(Socket socket, String username) {
        this.username = username;
        if (socket != null) {
            try {
                this.socket = socket;
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void register() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatappusermanager", "계정이름", "계정비밀번호")) {
            System.out.println("회원가입을 위해 사용자 이름을 입력해주세요:");
            String newUsername = consoleReader.readLine();
            System.out.println("비밀번호를 입력해주세요:");
            String newPassword = hashPassword(consoleReader.readLine());
            System.out.println("성별을 입력해주세요 (남/여):");
            String gender = consoleReader.readLine();

            String query = "INSERT INTO users (username, password, gender) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, newUsername);
                ps.setString(2, newPassword);
                ps.setString(3, gender);
                ps.executeUpdate();
                System.out.println("회원가입이 완료되었습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean login() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatappusermanager", "계정이름", "계정비밀번호")) {
            System.out.println("사용자 이름을 입력해주세요:");
            String username = consoleReader.readLine();
            System.out.println("비밀번호를 입력해주세요:");
            String password = hashPassword(consoleReader.readLine());

            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.username = username;
                        return true;
                    } else {
                        System.out.println("로그인 실패: 사용자 이름 또는 비밀번호가 잘못되었습니다.");
                        return false;
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while (socket.isConnected()) {
                String messageToSend = consoleReader.readLine();

                if (messageToSend.equals("!history")) {
                    bufferedWriter.write(messageToSend);
                } else if (messageToSend.equals("!Clear")) {
                    bufferedWriter.write(messageToSend);
                } else if (messageToSend.equals("!1")) {
                    bufferedWriter.write(messageToSend);
                } else {
                    bufferedWriter.write(username + ": " + messageToSend);
                }

                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;

            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat != null) {
                        System.out.println(msgFromGroupChat); // 서버로부터 받은 메시지를 콘솔에 출력
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("채팅 애플리케이션에 오신 것을 환영합니다.");
        System.out.println("1: 로그인, 2: 회원가입");
        int choice = Integer.parseInt(consoleReader.readLine());

        Client client;
        if (choice == 2) {
            client = new Client(null, null);
            client.register();
            client = new Client(new Socket("localhost", 1234), null);
        } else if (choice == 1) {
            client = new Client(new Socket("localhost", 1234), null);
            if (!client.login()) {
                System.exit(0);
            }
        } else {
            System.out.println("잘못된 선택입니다.");
            return;
        }

        client.listenForMessage();
        client.sendMessage();
    }
}
