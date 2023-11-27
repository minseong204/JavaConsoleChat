import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBUtil {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> chatCollection;

    public MongoDBUtil() {
        // MongoDB 서버에 연결
        mongoClient = MongoClients.create("mongodb+srv://minseong2083:ms010204324@cluster0.xhgjfwi.mongodb.net/?retryWrites=true&w=majority");
        // 데이터베이스 선택 (없으면 자동 생성)
        database = mongoClient.getDatabase("chatmanager");
        // 채팅 내역을 저장할 컬렉션 선택 (없으면 자동 생성)
        chatCollection = database.getCollection("chatmanager");
    }

    public void addChatMessage(String username, String message) {
        try {
            Document chatMessage = new Document("username", username)
                    .append("message", message)
                    .append("timestamp", System.currentTimeMillis());
            chatCollection.insertOne(chatMessage);
        } catch (Exception e) {
            System.out.println("메시지 저장 중 오류 발생: " + e.getMessage());
        }
    }

    public List<Document> getRecentChatMessages(int limit) {
        List<Document> messages = new ArrayList<>();
        try {
            // MongoDB 컬렉션에서 메시지를 조회하고, timestamp 필드를 기준으로 내림차순 정렬
            FindIterable<Document> iterable = chatCollection.find()
                    .sort(new Document("timestamp", 1)) // timestamp 대해 오름차순 정렬
                    .limit(limit); // 결과를 limit 제한

            for (Document doc : iterable) {
                messages.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void deleteChatMessagesByUser() {
        try {
            chatCollection.deleteMany(Filters.exists("_id"));
        } catch (Exception e) {
            System.out.println("메시지 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}
