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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class UserServer {
	
	public static String DELIMITER = "&";
	
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
				}
				/* Store all user information */
				allUsers.add(userInfo);
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
		user.follow(allUsers);		
		
		try {	
			/* To get input from console */
			in = new BufferedReader(new InputStreamReader(System.in));
			String command = null;
			String message = null;
			String matrixTiStr = null;
			String NPStr = null;
			SocketChannel sendSC = null;
			ByteBuffer buffer = null;
			Map<String, PriorityQueue<Event>> NP = new HashMap<String, PriorityQueue<Event>>();
			
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
	        
	        /* Return code maps to a closed connection or data received from socket connection */
	        Integer rc = -1;
	        
	        System.out.println("Hello " + user.getUserName() + ". Welcome to Twitter!");
	        
	        /* Start server */
	        while (true) {
	        	/* All incoming messages have been handled and waiting for input from console */
	        	
	        	/* Wait for User to input command into console. Timeout if no response was provided in time */
	        	startTime = System.currentTimeMillis();
	        	while ((System.currentTimeMillis() - startTime) < timeOut && !in.ready()) {}
	        	if (in.ready()) {
	        		command = in.readLine();
	        		switch(command) {
	        			case "Tweet": 
	        				System.out.print("Input Message: ");
	        				message = in.readLine();
	        				
	        				/* Send all Events that some other User needs to know about given unblocked */
	        				user.onEvent(Event.TWEET, message);
	        				NP = user.onSend();
	        				
	        				/* Iterate through NP to see what messages need to be sent to other User(s) */
	        				for (Map.Entry<String, PriorityQueue<Event>> NPEntry : NP.entrySet()) {
	        					/* Iterate through known ports until current User is found */
	        					for (Map.Entry<String, Integer> portEntry : user.getPortsToSendMsg().entrySet()) {
	        						/* Check if given portEntry has the same username as the given NPEntry username */
	        						if (NPEntry.getKey().equals(portEntry.getKey())) {
	        							/* Open socket to current User */
	        							sendSC = SocketChannel.open(new InetSocketAddress("localhost", portEntry.getValue()));
	        							
	        							/* Convert this User's matrixTi to a string */
	        							matrixTiStr = user.matrixTiToString();
	        							
	        							/* Convert all Events current User needs to know about to a string */
	        							NPStr = user.NPtoString(NPEntry.getValue());
	        							
	        							/* Write this User's matrix to socket and the NP Events the current User needs to know about */
	        							message = user.getUserName() + UserServer.DELIMITER + matrixTiStr + UserServer.DELIMITER + NPStr;
	        							buffer = ByteBuffer.allocate(message.getBytes().length);
	        							buffer = ByteBuffer.wrap(message.getBytes());
	        							sendSC.write(buffer);
	        							buffer.clear();
	        							
	        							/* Close socket since all Event(s) have been sent to given port */
	        							sendSC.close();
	        							/* Since current NPEntry has been satisfied and next entry requires a different port */
	        							break;
	        						}
	        					}
	        				}
	        				break;
	        			case "Block":
	        				System.out.println("Block was selected");
	        				break;
	        			case "Unblock":
	        				System.out.println("Unblock was seclted");
	        				break;
	        			case "View":
	        				user.printTweets();
	        				break;
	        			case "Log":
	        				user.printPL();
	        				break;
	        			case "Matrix":
	        				user.printMatrixTi();
	        				break;
	        			case "Help":
	        				System.out.println("Tweet: Input a message for User's to see whom you did not block.");
	        				System.out.println("Block: By inputting a username, the subsequent User will be blocked from viewing your tweets");
	        				System.out.println("Unblock: By inputting a username, the subsequent User will be unblocked from viewing your tweets.");
	        				System.out.println("View: View Tweets posted by yourself or by User's you are following and are not blocked from.");
	        				System.out.println("Log: View Events that have occurred either by yourself or by User's in the system.");
	        				System.out.println("Matrix: View Matrix of local even counter. Represented as NxN.");
	        				break;
	        			default:
	        				System.out.println("Only valid commands include: {Tweet, Block, Unblock, View, Log, Matrix, Help}");
	        				break;
	        		}
	        	}
	        	
	        	/* All commands from console has been handled and waiting for activity on socket */
	            
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
	                	buffer = ByteBuffer.allocate(1024);
	                	rc = sc.read(buffer);
	              
	                	/* Check return code from read() */
	                	if (rc == -1) {
	                		/* No data was sent, close socket connection */
	                		sc.close();
	                	} else {
	                		/* Format data and echo back to socket connection */
	                		buffer.flip();
	                		sc.write(buffer);
	                		
	                		user.onRecv(buffer);
	                		
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
