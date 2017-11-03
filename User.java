

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class User {
	/* Delimiter for User encapsulation */
	public static String MATRIXROWDELIMITER = ",";
	public static String MATRIXFIELDDELIMITER = "|";
	public static String MATRIXFIELDREGEX = "\\|";
	
	/* Name of file to write information to */
	public static String LOGFILE = "Log.txt";
	
	/* Identifier for this User */
	private String userName;
	/* Port number this User is listening on */
	private Integer portNumber;
	/* Local event counter */
	private Integer cI;
	/* Port numbers of other Users that they are listening on */
	private Map<String, Integer> portsToSendMsg;
	/* Matrix to represent direct and indirect knowledge */
	/* Key: User where direct knowledge is only for the key who matches this User's username */
	/* Value: Knowledge about other User's */
	private Map<String, ArrayList<Pair<String, Integer>>> matrixTi;
	/* Container for all Tweets sent in the simulation */
	private TreeSet<Tweet> tweets;
	/* Container for all Events that occurred that some User does not know about */
	private TreeSet<Event> PL;
	/* Container for all Users who blocked some other User */
	/* Key: User who is being blocked */
	/* Value: User who is blocking key */
	private Map<String, ArrayList<String>> dictionary;
	
	/**
	 * @param userName: Identifier for this User
	 * @param portNumber: Socket port number for this User to listen on
	 * @effects Assigns parameters to private field
	 * @modifies userName, portNumber, cI, matrixTi, portsToSendMsg, tweets, PL, Dictionary private fields
	 * @return A new User object
	 * */
	public User(String userName, Integer portNumber) {
		this.userName = userName;
		this.portNumber = portNumber;
		this.cI = 0;
		this.matrixTi = new HashMap<String, ArrayList<Pair<String, Integer>>>();
		this.portsToSendMsg = new HashMap<String, Integer>();
		this.tweets = new TreeSet<Tweet>();
		this.PL = new TreeSet<Event>();
		this.dictionary = new HashMap<String, ArrayList<String>>();
	}
	
	/**
	 * @param matrixTkArr: A matrix represented in an array of strings
	 * @effects Converts matrixTkArr to a matrix
	 * @return A matrix type from matrixTkArr
	 * */
	private Map<String, ArrayList<Pair<String, Integer>>> stringToMatrixTk(String[] matrixTkArr) {
		Map<String, ArrayList<Pair<String, Integer>>> matrixTk = new HashMap<String, ArrayList<Pair<String, Integer>>>();
		
		/* Iterate through each row of the string array and convert to (key, value) */
		String key = null;
		ArrayList<Pair<String, Integer>> values = null;
		
		String[] item = null;
		for (int i = 0; i < matrixTkArr.length; i++) {
			/* Key and Value temporary holders to generate matrixTk */
			 values = new ArrayList<Pair<String, Integer>>();
			
			/* Split each row of matrixTkArr by User.MATRIXFIELDDELIMITER */
			item = matrixTkArr[i].split(User.MATRIXFIELDREGEX);
			/* Generate (key, value) pair for row */
			key = item[0];
			
			/* Create value items for remaining indexes in array */
			for (int j = 1; j < item.length-1; j++) {
				values.add(Pair.createPair(item[j], Integer.parseInt(item[j+1])));
				j++;
			}
			
			/* Add (key, value) pair to matrixTk */
			matrixTk.put(key, values);
		}
		
		return matrixTk;
	}
	
	/**
	 * @param NPArr: A container of Event objects in a string format
	 * @effects Converts each item in NPArr into an Event
	 * @return A new container of Event objects from NPArr 
	 * */
	private ArrayList<Event> stringToNP(String[] NPArr) {
		ArrayList<Event> NP = new ArrayList<Event>();
		
		String[] item = null;
		/* Iterate through each index of the string array and convert to Event object */
		for (int i = 0; i < NPArr.length; i++) {
			/* Split each item of NPArr by Event.FIELDDELIMITER */
			item = NPArr[i].split(Event.FIELDREGEX);
			/* Create Event object for each item and add to NP */
			NP.add(new Event(Integer.parseInt(item[0]), item[1], Integer.parseInt(item[2]), new DateTime(item[3]).withZone(DateTimeZone.UTC), item[4]));
		}
		
		return NP;
	}
	
	/**
	 * @return A copy of userName private field
	 * */
	public String getUserName() { 
		return new String(userName); 
	}
	
	/**
	 * @return A copy of portNumber private field
	 * */
	public Integer getPortNumber() {
		return new Integer(portNumber);
	}
	
	/**
	 * @return A copy of portsToSendMsg private field
	 * */
	public Map<String, Integer> getPortsToSendMsg() {
		Map<String, Integer> copy = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : this.portsToSendMsg.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	/**
	 * @return A copy of matrixT private field
	 * */
	public Map<String, ArrayList<Pair<String, Integer>>> getMatrixTi() { 
		Map<String, ArrayList<Pair<String, Integer>>> copy = new HashMap<String, ArrayList<Pair<String, Integer>>>();
		for (Map.Entry<String, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	/**
	 * @param allUsers: A list of (username, port number)
	 * @effects Generates NxN matrix for this Users where N represents the amount of users
	 * @effects Adds username and port number for all users in the list that are not this User's username
	 * @modifies matrixTi and portsToSendMsg private fields
	 * */
	public void follow(List<String[]> allUsers) {
		for (int i = 0; i < allUsers.size(); i++) {
			ArrayList<Pair<String, Integer>> columns = new ArrayList<Pair<String, Integer>>();
			for (int j = 0; j < allUsers.size(); j++) {
				columns.add(Pair.createPair(allUsers.get(j)[0], 0));
			}
			this.matrixTi.put(allUsers.get(i)[0], columns);
			
			if (!allUsers.get(i)[0].equals(this.getUserName())) {
				this.portsToSendMsg.put(allUsers.get(i)[0], Integer.parseInt(allUsers.get(i)[1]));
			}
		}
	}
	
	/**
	 * @param amount: The amount of events this User has caused during a previous state
	 * @effects Updates cI and Ti(i, i) based on amount
	 * @modifies cI and matrixTi private fields
	 * */
	public void initAmountOfEvents(Integer amount) {
		/* Update cI */
		this.cI = amount;
		
		/* Get direct knowledge */
		ArrayList<Pair<String, Integer>> directKnowledge = this.matrixTi.get(this.getUserName());
		for (int i = 0; i < directKnowledge.size(); i++) {
			/* Get pair that matches this username */
			if (directKnowledge.get(i).getKey().equals(this.getUserName())) {
				/* Update Ti(i, i) */
				directKnowledge.get(i).setValue(amount);
			}
		}
		
		this.matrixTi.put(this.getUserName(), directKnowledge);
	}
	
	/**
	 * @effects Converts matrixTi to a string with User.MATRIXFIELDDELIMITER for each associated value in the entry and User.MATRIXROWDELIMITER for each entry in the map
	 * @return A string representation of matrixTi 
	 * */
	public String matrixTiToString() {
		/* String to represent matrixTi */
		String matrixTiStr = "";
		
		/* Iterate through all entries in matrixTi */
		Iterator<Map.Entry<String, ArrayList<Pair<String, Integer>>>> itrMatrixTi = this.matrixTi.entrySet().iterator();
		for (Map.Entry<String, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Advance matrixTi iterator */
			itrMatrixTi.next();
			/* Concatenate key for given entry */
			matrixTiStr += entry.getKey() + User.MATRIXFIELDDELIMITER;
			
			/* Iterate through all objects in the value container */
			Iterator<Pair<String, Integer>> itrValue =  entry.getValue().iterator();
			for (Pair<String, Integer> value : entry.getValue()) {
				/* Advance container iterator for given value in matrixTi */
				itrValue.next();
				/* Concatenate each object in container */
				matrixTiStr += value.getKey() + User.MATRIXFIELDDELIMITER + value.getValue();
				/* Include delimiter if there are more objects in container */
				if (itrValue.hasNext()) matrixTiStr += User.MATRIXFIELDDELIMITER;
			}
			/* Include delimiter if there are more entries in matrixTi */
			if(itrMatrixTi.hasNext()) matrixTiStr += User.MATRIXROWDELIMITER;
		}
		
		return matrixTiStr;
	}
	
	/**
	 * @param NP: Container of Events that some User needs to be sent
	 * @effects Converts NP to a string with Event.FIELDDELIMITER for Event private fields and Event.EVENTDELIMIETER for each unique Event
	 * @return A string representation of all Events in NP 
	 * */
	public String NPtoString(ArrayList<Event> NP) {
		/* String to represent NP */
		String NPStr = "";
		
		/* Iterate through NP */
		Iterator<Event> itrNP =  NP.iterator();
		while (itrNP.hasNext()) {
			/* Concatenate Event after it has been translated to a string */
			NPStr += itrNP.next().toString();
			/* Include delimiter if there are more Events */
			if (itrNP.hasNext()) NPStr += Event.EVENTDELIIMITER;
		}
		
		return NPStr;
	}
	
	/**
	 * @effects Prints matrixTi as an NxN matrix where N is the amount of Users
	 * */
	public void printMatrixTi() {
		/* Iterate through all keys in map */
		for (Map.Entry<String, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			System.out.print("Row: " + entry.getKey() + " ==> ");
			
			/* Iterate through the size of the current value in map */
			for (int i = 0; i < entry.getValue().size(); i++) {
				System.out.print("Column: " + entry.getValue().get(i).getKey() + " ");
				System.out.print("cI: " + entry.getValue().get(i).getValue());
				if (i != entry.getValue().size()) {
					System.out.print(" | ");
				}
			}
			System.out.println("");
		}
	}
	
	/**
	 * @effects Prints all tweets this User has either created or received
	 * */
	public void printTweets() {	 	
		ArrayList<String> blockedView =	null;
		
		for (Tweet tweet : this.tweets) {
			/* Get a list of usernames who blocked this User */
		 	blockedView = this.dictionary.get(this.getUserName());
		 	/* Prevent this user from viewing tweet because this user is being blocked */
			if ((blockedView != null) && (blockedView.contains(tweet.getUserName()))) continue;
			
			System.out.println("Tweet:");
			tweet.printTweet();
		}
	}
	
	/**
	 * @effects Prints all events this User has created or received
	 * */
	public void printPL() {
		for (Event event : this.PL) {
			System.out.println("Event:");
			event.printEvent();
		}
	}
	
	/**
	 * @param event: Event that occurred
	 * @effects Adds event to PL
	 * @effects Adds event to tweets if the event is a tweet
	 * @effects Adds event to dictionary if the event is blocking some User
	 * @effects Removes event from dictionary if the envet is unblocking some User
	 * @modifies PL, tweets, and dictionary private fields
	 * */
	public void addEventBasedOnType(Event event) {
		this.PL.add(event);
		
		if (event.getType().equals(Event.TWEETINT)) {
			/* Create new Tweet and add to tweets */
			this.tweets.add(new Tweet(event));
		} else if (event.getType().equals(Event.BLOCKINT)) {
			/* Add blocked information to dictionary */
			String[] blocked = event.getMessage().split(Event.BLOCKEDSTR);
			
			/* Format who blocked who */
			String blockedInitiator = blocked[0].trim();
			String blockedRecipient = blocked[1].trim();
			
			/* Add blockedInitiator to blockedRecipient's list of Users who blocked blockedRecipient */
			ArrayList<String> blockedFromViewing = this.dictionary.get(blockedRecipient);
			
			/* Create a container for blockedRecipient since this is the first User blocking */
			if (blockedFromViewing == null) {
				blockedFromViewing = new ArrayList<String>();
			} 
			
			/* Add blockedInitiator to container and add to dictionary */
			blockedFromViewing.add(blockedInitiator);
			this.dictionary.put(blockedRecipient, blockedFromViewing);
		} else if (event.getType().equals(Event.UNBLOCKINT)) {
			/* Add unblocked information to dictionary */
			String[] unblocked = event.getMessage().split(Event.UNBLOCKEDSTR);
			
			/* Format who unblocked who */
			String unblockedInitiator = unblocked[0].trim();
			String unblockedRecipient = unblocked[1].trim();
			
			/* Remove unblockedInitiator from blockedRecipient's list of Users who blocked blockedRecipient */
			ArrayList<String> blockedFromViewing = this.dictionary.get(unblockedRecipient);
			
			if (blockedFromViewing != null) { 
				/* Remove unblockedInitiator from unblockedRecipient's container */
				blockedFromViewing.remove(unblockedInitiator);
				
				if (!blockedFromViewing.isEmpty()) {
					/* Maintain container of Users who blocked unblockedRecipient */
					this.dictionary.put(unblockedRecipient, blockedFromViewing);
				} else {
					/* Remove mapping from dictionary */
					this.dictionary.remove(unblockedRecipient);
				}
			}
		}
	}
	
	/**
	 * @param usernName: Name of user
	 * @return True if userName is a valid User, false otherwise
	 * */
	public Boolean userNameExists(String userName) {
		return this.getPortsToSendMsg().containsKey(userName);
	}
	
	/**
	 * @param userName: Name of User to block
	 * @return True if userName is being blocked by this User, false otherwise
	 * */
	public Boolean blockExists(String userName) {		
		/* Get container of Users who blocked userName from viewing their tweets */
		ArrayList<String> blockedFromViewing = this.dictionary.get(userName);
		if (blockedFromViewing != null) {
			return blockedFromViewing.contains(this.getUserName());
		}
		
		return false;
	}
	
	/**
	 * @param userName: Name of some User
	 * @return True if userName is blocked from viewing this User's tweets, false otherwise
	 * */
	public Boolean blockedFromView(String userName) {
		ArrayList<String> blocked = this.dictionary.get(userName);
		if (blocked != null) {
			for (String value : blocked) {
				if (this.getUserName().equals(value)) return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param event: Event to compare
	 * @effects Checks if event is contained in PL private field
	 * @returns True if event is already found in PL, false otherwise
	 * */
	public Boolean alreadyRecv(Event event) {
		for (Event currEvent : this.PL) {
			if (currEvent.equals(event)) return true;
		}
		return false;
	}
	
	/**
	 * @param eR: Event that has occurred
	 * @param recipient: User to check if User received eR
	 * @effects Checks if indirect knowledge of recipient knows about eR
	 * @return true if Lamport timestamp of indirect knowledge for recipient is greater than or equal to Lamport timestamp
	 *          of eR; false otherwise 
	 * */
	public Boolean hasRecv(Event eR, String recipient) {
		/* Indirect knowledge of recipient (row of recipient in matrixTi) */
		ArrayList<Pair<String, Integer>> indirectKnowledge = this.matrixTi.get(recipient);
		
		/* Indirect knowledge of recipient knowing about some event occurring at node of Event */
		Integer cK = -1;
		
		/* Iterate through indirect knowledge of recipient until node is found */
		for (int i = 0; i < indirectKnowledge.size(); i++) {
			if (indirectKnowledge.get(i).getKey().equals(eR.getNode())) {
				cK = indirectKnowledge.get(i).getValue();
				break;
			}
		}
		
		return cK >= eR.getcI();
	}
	
	/**
	 * @param type: Categorized to be one the following values {block, unblock, tweet}
	 * @param message: Description of Event
	 * @effects Increments local event counter 
	 * @effects Increments Ti(i,i)
	 * @effects Adds eR to PL
	 * @effects Wrives event to storage
	 * @modifies PL, tweets, and dictionary private fields
	 * */
	public void onEvent(Integer type, String message) {
		/* Capture current time the Event was triggered in UTC */
		DateTime dtUTC = new DateTime(DateTimeZone.UTC);
		
		/* Increment this User's local event counter */
		this.cI += 1;
		
		/* Increment this User's Ti(i,i) by iterating through matrixTi */
		for (Map.Entry<String, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Check if current key is this User */
			if (entry.getKey().equals(this.getUserName())) {
				/* Iterate through direct knowledge of this User */
				for (Pair<String, Integer> value : entry.getValue()) {
					/* Check if current direct knowledge index is this User */
					if (value.getKey().equals(this.getUserName())){
						/* Increment Ti(i,i) */
						value.setValue(value.getValue()+1);
						break;	
					}
				}
				break;
			}
		}
		
		/* Create new Event and add to PL */
		Event event = new Event(type, this.userName, this.cI, dtUTC, message);
		
		/* Write event to storage */
		Event.writeEventToFile(this.getUserName(), event);
		
		/* Add event based on specified type to private fields */
		this.addEventBasedOnType(event);
	}
	
	/**
	 * @effects Creates a unique container of Event objects that each User needs to know about
	 * @return A partial log of events that each User needs to be sent
	 * */
	public Map<String, ArrayList<Event>> onSend() {
		/* Container to store all Events that some User needs to be sent */
		Map<String, ArrayList<Event>> unblockedUsersNP = new HashMap<String, ArrayList<Event>>();
		
		/* Iterate through all other Users */
		for (String recipient : this.portsToSendMsg.keySet()){
			/* Skip over self */
			if (recipient.equals(this.getUserName())) continue;
			
			/* Container of events a User may need to know about */
			ArrayList<Event> missingKnowledge = new ArrayList<Event>();
			
			/* Iterate through all Events in PL and determine if current User needs to know about it */
			Iterator<Event> itrPL = this.PL.iterator();
			while (itrPL.hasNext()) {
				/* Get current Event in PL */
				Event currEvent = itrPL.next();
				/* Check if current User does not know about the current Event */
				if (!hasRecv(currEvent, recipient)) {
					/* Add the Event to container for current User to know about */
					missingKnowledge.add(0, currEvent);
				}
			}
			
			/* Update NP for current User */
			unblockedUsersNP.put(recipient, missingKnowledge);
			
		}
		
		return unblockedUsersNP;
	}
	
	/**
	 * @param data: Information that was sent from other User containing username, matrixTk, and NP (a contain of events)
	 * @effects Updates this User's matrixTi to have max values for both direct and indirect knowledge using data
	 * @effects Updates this User's PL using data
	 * @effects Updates this User's tweets using data
	 * @effects Writes event to storage
	 * @modifies matrixTi, PL, tweets, and dictionary private fields
	 * */
	public void onRecv(ByteBuffer data) {		
		/* Convert ByteBuffer to String */
		String dataStr = new String(data.array());
		
		/* Split on UserServer.DELIMITER to get matrixTk and NP */
		String[] dataArr = dataStr.trim().split(UserServer.DELIMITER);
		
		/* Split each index based on unique delimiter */
		String siteK = dataArr[0];
		String[] matrixTkArr = (dataArr[1]).split(User.MATRIXROWDELIMITER);
		String[] NPArr = (dataArr[2]).split(Event.EVENTDELIIMITER);
		
		/* matrixTk to compare to matrixTi */
		Map<String, ArrayList<Pair<String, Integer>>> matrixTk = this.stringToMatrixTk(matrixTkArr);
		
		/* Get value mapped by this User's username in matrixTi */
		ArrayList<Pair<String, Integer>> matrixTiDirect = this.getMatrixTi().get(this.getUserName());
		
		/* Get value mapped by the site's username in matrixTk */
		ArrayList<Pair<String, Integer>> matrixTkDirect = matrixTk.get(siteK);
		
		/* Update Direct Knowledge of matrixTi: Ti(i,j) = max(Ti(i,j), Tk(k,j)) */
		for (int j = 0; j < matrixTiDirect.size(); j++) {
			matrixTiDirect.set(j, Pair.createPair(matrixTiDirect.get(j).getKey(), Math.max(matrixTiDirect.get(j).getValue(), matrixTkDirect.get(j).getValue())));
		}
		
		/* Update Indirect Knowledge of matrixTi: Ti(j, k) = max(Ti(j, l), Tk(j, l)) */
		ArrayList<Pair<String, Integer>> matrixTkIndirect = null;
		for (Map.Entry<String, ArrayList<Pair<String, Integer>>> matrixTiEntry : this.matrixTi.entrySet()) {
			/* Skip over Direct Knowledge */
			if (matrixTiEntry.getKey().equals(this.getUserName())) continue;
			
			/* Get value mapped by current key for matrixTk */
			matrixTkIndirect = matrixTk.get(matrixTiEntry.getKey());
			
			/* Update Ti(j,k) */
			for (int l = 0; l < matrixTiEntry.getValue().size(); l++) {
				/* Get current value mapping in matrixTi, use index of use index of mapping to compare values of each pair */
				matrixTiEntry.getValue().set(l, Pair.createPair(matrixTiEntry.getValue().get(l).getKey(), Math.max(matrixTiEntry.getValue().get(l).getValue(), matrixTkIndirect.get(l).getValue())));
			}
		}
		
		/* NP to add to PL */
		ArrayList<Event> NP = this.stringToNP(NPArr);
		
		/* Add event based on specified type to private fields and write to storage */
		for (Event event : NP) {
			/* This User already knows about the event */
			if (!this.alreadyRecv(event)) {
				Event.writeEventToFile(this.getUserName(), event);
				this.addEventBasedOnType(event);
			}
		}
	}
}
