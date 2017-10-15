import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UserClient {
	
	private static SocketChannel client;
	private static ByteBuffer buffer;
	private static UserClient instance;
	
	public static void main(String[] args) {
		UserClient currClient = UserClient.start();
		currClient.sendMessage("Yes");
	}
	 
    public static UserClient start() {
        if (instance == null)
            instance = new UserClient();
 
        return instance;
    }
 
    public static void stop() throws IOException {
        client.close();
        buffer = null;
    }
 
    private UserClient() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 12345));
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public String sendMessage(String msg) {
        buffer = ByteBuffer.wrap(msg.getBytes());
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
 
    }
}