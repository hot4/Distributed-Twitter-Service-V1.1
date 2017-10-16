import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			/* To get input from console */
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String command = null;
			
			/* To determine timeout for input from console */
			long startTime = -1;
			
			/* Timeout to stop listening for console input or socket activity */
			Integer timeOut = 5000;
			
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
	        
	        /* Start server */
	        while (true) {
	        	/* Prompt to indicate all incoming messages have been handled and waiting for input from console */
	        	System.out.println("Waiting for incoming command from console");
	        	
	        	/* Wait for User to input command into console. Timeout if no response was provided in time */
	        	startTime = System.currentTimeMillis();
	        	while ((System.currentTimeMillis() - startTime) < timeOut && !in.ready()) {}
	        	if (in.ready()) {
	        		command = in.readLine();
	        		System.out.println("Entered: " + command);
	        		switch(command) {
	        			case "Tweet": 
	        				System.out.println("Tweet was selected");
	        				break;
	        			case "Block":
	        				System.out.println("Block was selected");
	        				break;
	        			case "Unblock":
	        				System.out.println("Unblock was seclted");
	        				break;
	        			case "View":
	        				System.out.println("View was selected");
	        				break;
	        			case "Help":
	        				System.out.println("Tweet: Input a message for User's to see whom you did not block.");
	        				System.out.println("Block: By inputting a username, the subsequent User will be blocked from viewing your tweets");
	        				System.out.println("Unblock: By inputting a username, the subsequent User will be unblocked from viewing your tweets.");
	        				System.out.println("View: View Tweets posted by yourself or by User's you are following and are not blocked from.");
	        				break;
	        			default:
	        				System.out.println("Only valid commands include: {Tweet, Block, Unblock, View, Help}");
	        				break;
	        		}
	        	}
	        	
	        	/* Prompt to indicate all commands from console has been handled and waiting for activity on socket */
	        	System.out.println("Waiting for incoming messages");
	            
	        	/* Block on socket for five seconds to check for activity */
	        	selector.select(timeOut);
	        	
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
