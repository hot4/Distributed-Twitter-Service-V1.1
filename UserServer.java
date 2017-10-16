import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserServer {
	
	public static void main(String[] args) {
		if(args.length != 2) {
	        System.out.println("FAILURE: Improper amount of arguments used");
	        System.exit(1);
	        return;
	    }
		
		new UserServer(args[0], args[1]);
	}
	
	public UserServer(String fileName, String userName) {		
		/* Determine current director to read file from */
		File testFile = new File("");
	    String currentPath = testFile.getAbsolutePath();
		
		/* Variables to read through *.csv file */
		BufferedReader in = null;
		String line = null;
		List<String[]> allUsers = new ArrayList<String[]>(); 
		String[] userInfo = null;
		
		User user = null;
		
		try {
			/* Read file from src folder */
			in = new BufferedReader(new FileReader(currentPath + "\\src\\" + fileName));
			/* Parse file to get User information */
			while ((line = in.readLine()) != null) {
				/* Remove white spaces and split based on comma separator */
				userInfo = line.replace(" ", "").substring(1, line.length()-2).split(",");
				
				/* Gather all information from file */
				if (userInfo[0].equals(userName)) {
					/* Create User object based on command argument */
					user = new User(userInfo[0], Integer.parseInt(userInfo[1]));
				} else {
					/* Store all user informations */
					allUsers.add(userInfo);
				}
			}
		} catch (FileNotFoundException e) {
			/* ERROR: Could not open file */
			System.err.println("ERROR: No file to open " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			/* ERROR: Could not parse file */
			System.err.println("ERROR: Improper file format " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (in != null) {
				try {
					/* Close BufferedReader */
					in.close();
				} catch (IOException e) {
					/* ERROR: BufferedReader could not be closed */
					System.err.println("ERROR: Could not close BufferedReader " + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}	
			}
		}
		
		if (user == null) { 
			System.out.println("Username passed in through Run Configuration does not match username in CSV file");
			System.exit(1);
		}
		
		/* Follow all users */
		for (int i = 0; i < allUsers.size(); i++) {
			/* Pass userName and portNumber information of all Users from file */
			user.follow(allUsers.get(i)[0], Integer.parseInt(allUsers.get(i)[1]), allUsers.size() + 1);
		}
		
		
		try {	
			/* To get input from console */
			in = new BufferedReader(new InputStreamReader(System.in));
			String command = null;
			
			/* To determine timeout for input from console */
			long startTime = -1;
			
			/* Timeout to stop listening for console input or socket activity */
			Integer timeOut = 5000;
			
			/* Create a selector to check activity on port */
			Selector selector = Selector.open();
			
			/* Create a listen socket on the given port */
	        ServerSocketChannel serverSocket = ServerSocketChannel.open();
	        serverSocket.bind(new InetSocketAddress("localhost", user.getPortNumber()));
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
			/* ERROR: System error */
			System.err.println("ERROR: System error " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
