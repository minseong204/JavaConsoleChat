import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerSocket serverSocket;
    private MongoDBUtil mongoDBUtil;

    public Server(ServerSocket serversocket) {
        this.serverSocket = serversocket;
        this.mongoDBUtil = new MongoDBUtil();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                logger.info("새로운 클라이언트가 연결되었습니다.");
                ClientHandler clientHandler = new ClientHandler(socket, mongoDBUtil);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            logger.error("서버 소켓을 닫는 중 오류 발생", e);
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("서버 소켓 닫기 실패", e);
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
