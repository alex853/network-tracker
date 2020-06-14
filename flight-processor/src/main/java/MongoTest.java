import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoTest {
    public static void main(String[] args) {
        MongoClientURI connectionURI = new MongoClientURI("mongodb://admin:1234rtyu)(*%26@cluster0-shard-00-00-tcqlq.mongodb.net:27017,cluster0-shard-00-01-tcqlq.mongodb.net:27017,cluster0-shard-00-02-tcqlq.mongodb.net:27017/test?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(connectionURI);
        MongoDatabase testDb = mongoClient.getDatabase("video");
        MongoCollection<Document> collection = testDb.getCollection("movieDetails");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("year", 1977);
        FindIterable<Document> objects = collection.find(searchQuery);
        MongoCursor<Document> cursor = objects.cursor();
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }
    }
}
