import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class UserServer {
	
	public static void main(String[] args) {
		new UserServer();
	}
	
	public UserServer() {
		try {
			/* Create a selector to check activity on port */
			Selector selector = Selector.open();
			
			/* Create a listen socket on the given port */
	        ServerSocketChannel serverSocket = ServerSocketChannel.open();
	        serverSocket.bind(new InetSocketAddress("localhost", 12345));
	        serverSocket.configureBlocking(false);
	        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	        
	        /* Stores incoming information sent from socket(s) */
	        ByteBuffer buffer = ByteBuffer.allocate(256);
	        
	        /* Return code maps to a closed connection or data received from socket connection */
	        Integer rc = -1;
	 
	        while (true) {
	        	/* Initial output to indicate all activity on the socket has been handled */
	        	System.out.println("Waiting for incoming messages");
	            
	        	/* Block on socket until activity occurs */
	        	selector.select();
	        	
	        	/* Container for activity on listener socket */
	            Set<SelectionKey> selectedKeys = selector.selectedKeys();
	            Iterator<SelectionKey> itr = selectedKeys.iterator();
	            
	            /* Iterate through all keys and handle activity */
	            while (itr.hasNext()) {
	            	/* Get and remove current key from container since it only needs to be handled once */
	            	SelectionKey selKey = itr.next();
	                itr.remove();
	                
	                /* Check if this key is ready to accept a new incoming connection */
	                if (selKey.isAcceptable()) {
	                	/* Get socket channel information and accept */
	                	ServerSocketChannel ssChannel = (ServerSocketChannel) selKey.channel();
	                	SocketChannel sc = ssChannel.accept();
	                	
	                	/* Read data being sent through socket connection */
	                	rc = sc.read(buffer);
	              
	                	/* Check return code from read() */
	                	if (rc == -1) {
	                		/* No data was sent, close socket connection */
	                		sc.close();
	                	} else {
	                		/* Format data and echo back to socket connection */
	                		buffer.flip();
	                		sc.write(buffer);
	                		
	                		System.out.println("Received: " + new String(buffer.array()).trim());
	                		
	                		buffer.clear();
	                	}
	                }
	            }
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
