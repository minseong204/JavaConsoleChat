import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private MongoDBUtil mongoDBUtil;

    public ClientHandler(Socket socket, MongoDBUtil mongoDBUtil) {
        this.mongoDBUtil = mongoDBUtil;
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + "이(가) 채팅방에 들어왔습니다.");
        } catch (IOException e) {
            logger.error("An error occurred while initializing client handler", e);
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void askForLoadingChatHistory() throws IOException {
        bufferedWriter.write("이전 대화 내용을 불러오시겠습니까? 예[!1], 아니오[!2]");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        String response = bufferedReader.readLine();
        if (response.equals("!1")) {
            loadChatHistory();
        }
    }

    private void loadChatHistory() {
        List<Document> recentMessages = mongoDBUtil.getRecentChatMessages(100);

        if (recentMessages.isEmpty()) {
            try {
                bufferedWriter.write("SERVER: 채팅 내역이 없습니다.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                logger.error("No chat history message sending failed", e);
            }
        } else {
            try {
                // 메시지 내용을 클라이언트에게 전송
                for (Document message : recentMessages) {
                    String sender = message.getString("username");
                    String messageText = message.getString("message");
                    String formattedMessage = String.format("%s: %s", sender, messageText);

                    // 클라이언트에게 메시지 전송
                    bufferedWriter.write(formattedMessage);
                    bufferedWriter.newLine();
                }

                // 모든 메시지를 클라이언트로 전송한 후 버퍼 비우기
                bufferedWriter.flush();
            } catch (IOException e) {
                logger.error("Error sending chat history", e);
            }
        }
    }

    @Override
    public void run() {
        try {
            String messageFromClient;

            while (socket.isConnected()) {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    logger.info("클라이언트 접속 종료");
                    break;
                } else if (messageFromClient.equals("!Clear")) {
                    confirmAndDeleteChatHistory();
                } else if (messageFromClient.startsWith("!history")) {
                    askForLoadingChatHistory();
                } else {
                    broadcastMessage(messageFromClient);
                }
            }
        } catch (IOException e) {
            logger.error("Error in client communication", e);
        } finally {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void confirmAndDeleteChatHistory() throws IOException {
        bufferedWriter.write("저장된 대화 내용을 삭제하시겠습니까? 예[!1] 아니오[!2]");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        String response = bufferedReader.readLine();
        if (response.equals("!1")) {
            mongoDBUtil.deleteChatMessagesByUser(clientUsername);
            bufferedWriter.write("대화 내용이 삭제되었습니다.");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                logger.error("Error broadcasting message", e);
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        mongoDBUtil.addChatMessage(clientUsername, messageToSend);
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + "이(가) 채팅방에서 나갔습니다.");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
            logger.error("Error closing client connection", e);
        }
    }
}
