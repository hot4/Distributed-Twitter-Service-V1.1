import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class User {
	public static String MATRIXROWDELIMITER = ",";
	public static String MATRIXFIELDDELIMITER = "|";
	
	/* Identifier for this User */
	private String userName;
	/* Port number this User is listening on */
	private Integer portNumber;
	/* Local event counter */
	private Integer cI;
	/* Port numbers of other Users that they are listening on */
	private Map<String, Integer> portsToSendMsg;
	/* Matrix to represent direct and indirect knowledge */
	private Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> matrixTi;
	/* Index associated with this and other Users for matrix */
	private Integer amtOfUsers;
	/* Container for all Tweets sent in the simulation */
	private PriorityQueue<Tweet> tweets;
	/* Container for all Events that occurred that some User does not know about */
	private PriorityQueue<Event> PL;
	
	/**
	 * @param userName: Identifier for this User
	 * @param portNumber: Socket port number for this User to listen on
	 * @effects Assigns parameters to private field
	 * @modifies userName, portNumber, cI, matrixTi, portsToSendMsg, and tweets private fields
	 * @return A new User object
	 * */
	public User(String userName, Integer portNumber) {
		this.userName = userName;
		this.portNumber = portNumber;
		this.cI = 0;
		this.matrixTi = new HashMap<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>();
		this.portsToSendMsg = new HashMap<String, Integer>();
		this.amtOfUsers = 0;
		this.tweets = new PriorityQueue<Tweet>();
		this.PL = new PriorityQueue<Event>();
	}
	
	/**
	 * @param userName: Identifier for some User
	 * @effects Creates a new Pair object and increments userIndex
	 * @modifies userIndex private field 
	 * */
	private Pair<String, Integer> makePair(String userName) {
		Pair<String, Integer> pair = Pair.createPair(userName, new Integer(this.amtOfUsers));
		this.amtOfUsers++;
		return pair;
	}
	
	/**
	 * @param matrixTkArr: A matrix represented in an array of strings
	 * @effects Converts matrixTkArr to a matrix
	 * @return A matrix type from matrixTkArr
	 * */
	private Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> stringToMatrixTk(String[] matrixTkArr) {
		Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> matrixTk = new HashMap<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>();
		
		/* Key and Value temporary holders to generate matrixTk */
		Pair<String, Integer> key = null;
		ArrayList<Pair<String, Integer>> values = new ArrayList<Pair<String, Integer>>();
		
		/* Iterate through each row of the string array and convert to (key, value) */
		String[] item = null;
		for (int i = 0; i < matrixTkArr.length; i++) {
			/* Split each row of matrixTkArr by User.MATRIXFIELDDELIMITER */
			item = matrixTkArr[i].split(User.MATRIXFIELDDELIMITER);
			/* Generate (key, value) pair for row */
			key = Pair.createPair(item[0], Integer.parseInt(item[1]));
			
			/* Create value items for remaining indexes in array */
			for (int j = 2; j < item.length-1; j++) {
				values.add(Pair.createPair(item[j], Integer.parseInt(item[j+1])));
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
	private PriorityQueue<Event> stringToNP(String[] NPArr) {
		PriorityQueue<Event> NP = new PriorityQueue<Event>();
		
		String[] item = null;
		/* Iterate through each index of the string array and convert to Event object */
		for (int i = 0; i < NPArr.length; i++) {
			/* Split each item of NPArr by Event.FIELDDELIMITER */
			item = NPArr[i].split(Event.FIELDDELIMITER);
			/* Create Event object for each item and add to NP */
			NP.add(new Event(Integer.parseInt(item[0]), item[1], Integer.parseInt(item[2]), new DateTime(item[3]), item[4]));
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
	public Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> getMatrixTi() { 
		Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> copy = new HashMap<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>();
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
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
			this.matrixTi.put(makePair(allUsers.get(i)[0]), columns);
			
			if (!allUsers.get(i)[0].equals(this.getUserName())) {
				this.portsToSendMsg.put(allUsers.get(i)[0], Integer.parseInt(allUsers.get(i)[1]));
			}
		}
	}
	
	/**
	 * @effects Converts matrixTi to a string with User.MATRIXFIELDDELIMITER for each associated value in the entry and User.MATRIXROWDELIMITER for each entry in the map
	 * @return A string representation of matrixTi 
	 * */
	public String matrixTiToString() {
		/* String to represent matrixTi */
		String matrixTiStr = "";
		
		/* Iterate through all entries in matrixTi */
		Iterator<Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>> itrMatrixTi = this.matrixTi.entrySet().iterator();
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Advance matrixTi iterator */
			itrMatrixTi.next();
			/* Concatenate key for given entry */
			matrixTiStr += entry.getKey().getKey() + User.MATRIXFIELDDELIMITER + entry.getKey().getValue() + User.MATRIXFIELDDELIMITER;
			
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
	public String NPtoString(PriorityQueue<Event> NP) {
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
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Iterate through the size of the current value in map */
			for (int i = 0; i < entry.getValue().size(); i++) {
				System.out.print(entry.getValue().get(i).getKey() + " ");
				System.out.print(entry.getValue().get(i).getValue() + " ");
			}
			System.out.println("");
		}
	}
	
	/**
	 * @effects Prints all tweets this User has either created or received
	 * */
	public void printTweets() {
		for (Tweet tweet : this.tweets) {
			System.out.println("\tUsername: " + tweet.getUserName());
			System.out.println("\tTime: " + tweet.getdtUTC().withZone(DateTimeZone.getDefault()).toString("EEEE, MMM d 'at' hh:mma"));
			System.out.println("\tMessage: " + tweet.getMessage());
			System.out.println("");
		}
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
		ArrayList<Pair<String, Integer>> indirectKnowledge = null;
		
		/* Iterate through matrixTi until recipient is found */
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			if (entry.getKey().getKey().equals(recipient)) {
				indirectKnowledge = entry.getValue();
				break;
			}
		}
		
		/* Indirect knowledge of recipient knowing about some event occurring at node of Event */
		Integer cK = -1;
		
		/* Iterate through indirect knowledge of recipient until node is found */
		for (int i = 0; i < indirectKnowledge.size(); i++) {
			if (indirectKnowledge.get(i).getKey().equals(eR.getNode())) {
				cK = indirectKnowledge.get(i).getValue();
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
	 * */
	public void onEvent(Integer type, String message) {
		/* Capture current time the Event was triggered in UTC */
		DateTime dtUTC = new DateTime(DateTimeZone.UTC);
		
		/* Increment this User's local event counter */
		this.cI += 1;
		
		/* Increment this User's Ti(i,i) by iterating through matrixTi */
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Check if current key is this User */
			if (entry.getKey().getKey().equals(this.getUserName())) {
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
		this.PL.add(event);
		
		/* Check if current Event is a Tweet */
		if (type.equals(Event.TWEET)) {
			/* Create new Tweet and add to tweets */
			Tweet tweet = new Tweet(this.userName, message, dtUTC);
			this.tweets.add(tweet);
		}
	}
	
	/**
	 * @effects Creates a unique container of Event objects that each User needs to know about
	 * @return A partial log of events that each User needs to be sent
	 * */
	public Map<String, PriorityQueue<Event>> onSend() {
		/* Container to store all Events that some User needs to be sent */
		Map<String, PriorityQueue<Event>> unblockedUsersNP = new HashMap<String, PriorityQueue<Event>>();
		
		/* Iterate through all other Users */
		for (String currUserName : this.portsToSendMsg.keySet()){
			PriorityQueue<Event> missingKnowledge = new PriorityQueue<Event>();
			
			/* Iterate through all Events in Li and determine if current User needs to know about it */
			Iterator<Event> itrPL = this.PL.iterator();
			while (itrPL.hasNext()) {
				/* Get current Event in Li */
				Event currEvent = itrPL.next();
				/* Check if current User does not know about the current Event */
				if (!hasRecv(currEvent, currUserName)) {
					/* Add the Event to container for current User to know about */
					missingKnowledge.add(currEvent);
				}
			}
			/* Update NP for current User */
			unblockedUsersNP.put(currUserName, missingKnowledge);
		}
		
		return unblockedUsersNP;
	}
	
	public void onRecv(ByteBuffer data) {
		/* Convert ByteBuffer to String */
		String dataStr = new String(data.array());
		
		/* Split on UserServer.DELIMITER to get matrixTk and NP */
		String[] dataArr = dataStr.trim().split(UserServer.DELIMITER);
		
		/* Split each index based on unique delimiter */
		String[] matrixTkArr = (dataArr[0]).split(User.MATRIXROWDELIMITER);
		String[] NPArr = (dataArr[1]).split(Event.EVENTDELIIMITER);
		
		/* matrixTk to compare to matrixTi */
		Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> matrixTk = this.stringToMatrixTk(matrixTkArr);
		
		/* NP to add to PL */
		PriorityQueue<Event> NP = this.stringToNP(NPArr);
	}
}
