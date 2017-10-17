import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class User {
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
	private Integer userIndex;
	/* Container for all Tweets sent in the simulation */
	private PriorityQueue<Tweet> tweets;
	/* Container for all Events that occurred that some User does not know about */
	private PriorityQueue<Event> PL;
	
	/**
	 * @param userName: Identifier for this User
	 * @param portNumber: Socket port number for this User to listen on
	 * @effects Assigns parameters to private field
	 * @modifies userName, portNumber, cI, matrixTi, portsToSendMsg, and tweets private fields
	 * @returns A new User object
	 * */
	public User(String userName, Integer portNumber) {
		this.userName = userName;
		this.portNumber = portNumber;
		this.cI = 0;
		this.matrixTi = new HashMap<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>();
		this.portsToSendMsg = new HashMap<String, Integer>();
		this.userIndex = 0;
		this.tweets = new PriorityQueue<Tweet>();
		this.PL = new PriorityQueue<Event>();
		
		/* Have this User be first row in matrix */
		this.matrixTi.put(makePair(userName), new ArrayList<Pair<String, Integer>>());
	}
	
	/**
	 * @param userName: Identifier for some User
	 * @effects Creates a new Pair object and increments userIndex
	 * @modifies userIndex private field 
	 * */
	private Pair<String, Integer> makePair(String userName) {
		Pair<String, Integer> pair = Pair.createPair(userName, this.userIndex);
		this.userIndex++;
		return pair;
	}
	
	/**
	 * @returns A copy of userName private field
	 * */
	public String getUserName() { 
		return new String(userName); 
	}
	
	/**
	 * @returns A copy of portNumber private field
	 * */
	public Integer getPortNumber() {
		return new Integer(portNumber);
	}
	
	/**
	 * @returns A copy of portsToSendMsg private field
	 * */
	public Map<String, Integer> getPortsToSendMsg() {
		Map<String, Integer> copy = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : this.portsToSendMsg.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	/**
	 * @returns A copy of matrixT private field
	 * */
	public Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> getMatrixTi() { 
		Map<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> copy = new HashMap<Pair<String, Integer>, ArrayList<Pair<String, Integer>>>();
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	/**
	 * @param userName: Identifier for some User for this User to follow
	 * @param portNumber: Socket port number that some User uses to listen on
	 * @param size: Total amount of Users
	 * @effects Adds User to matrixTi with size and portNumber to portsToSendMsg
	 * @modifies matrixTi and portsToSendMsg private fields
	 * */
	public void follow(String userName, Integer portNumber, Integer size) {
		 /* Create row with a size that represents each User */
		 ArrayList<Pair<String, Integer>> value = new ArrayList<Pair<String, Integer>>();
		 for (int i = 0; i < size; i++) {
		 	 /* Initialize all column entries to zero */
	 		 value.add(Pair.createPair(userName, 0));
		 }
		 
		 this.matrixTi.put(makePair(userName), value);
		 this.portsToSendMsg.put(userName, portNumber);
	}
	
	/**
	 * @effects Prints matrixTi as an NxN matrix where N is the amount of Users
	 * */
	public void printMatrixTi() {
		/* Iterate through all keys in map */
		for (Map.Entry<Pair<String, Integer>, ArrayList<Pair<String, Integer>>> entry : this.matrixTi.entrySet()) {
			/* Iterate through the size of the current value in map */
			for (int i = 0; i < entry.getValue().size(); i++) {
				System.out.println(entry.getValue().get(i).getValue());
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
			System.out.println("\tTime: " + tweet.getdtUTC());
			System.out.println("\tMessage: " + tweet.getMessage());
			System.out.println("");
		}
	}
	
	/**
	 * @param eR: Event that has occurred
	 * @param recipient: User to check if User received eR
	 * @effects Checks if indirect knowledge of recipient knows about eR
	 * @returns true if Lamport timestamp of indirect knowledge for recipient is greater than or equal to Lamport timestamp
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
	 * @effects Creates a unique container of Event objects that each User needs to know about
	 * @returns A partial log of events that each User needs to be sent
	 * */
	public Map<String, PriorityQueue<Event>> onEvent(Integer type, String message) {
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
}
