import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Event implements Comparable<Event> {
	/* Constants to map a type to the appropriate category of Event */
	public static Integer TWEET = 1;
	public static Integer BLOCK = 2;
	public static Integer UNBLOCK = 3;
	
	/* Delimiter for Event encapsulation */
	public static String EVENTDELIIMITER = ",";
	public static String FIELDDELIMITER = "|";
	public static String FIELDREGEX = "\\|";
	
	/* Maps to category of Event */
	private Integer type;
	/* Username of where the Event occurred */
	private String node;
	/* Local event counter of User who created the Event */
	private Integer cI;
	/* Time at which the Event occurred in UTC time */
	private DateTime dtUTC;
	/* Description of what Event occurred */
	private String message;
	
	/**
     * @param type: Categorized to be one the following values {block, unblock, tweet}
	 * @param node: Location of where the event occurred (i.e. which User caused the event)
	 * @param cI: The Lamport TimeStamp of the User after the event executed
	 * @param dtUTC: Represents the time at which the event occurred in UTC time
	 * @param message: Description of what Event occurred
	 * @modifies type, node, and rawTimeStamp private fields
	 * @returns A new Event object
	 * */
	public Event(Integer type, String node, Integer cI, DateTime dtUTC, String message) {
		this.type = type;
		this.node = node;
		this.cI = cI;
		this.dtUTC = dtUTC;
		this.message = message;
	}
	
	/**
	 * @param event: Event object to write to file
	 * @effects Appends event information to file
	 * */
	public static void writeEventToFile(Event event) {
		File temp = new File("");
		String path = temp.getAbsolutePath() + UserServer.DIRREGEX + UserServer.SOURCE + UserServer.DIRREGEX + UserServer.DIRECTORY + UserServer.DIRREGEX + event.getNode() + UserServer.DIRREGEX;;
		File file = new File(path + UserServer.DIRREGEX + User.LOGFILE);
		
		try {
			Writer writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(event.toReadableString());
			writer.close();
		} catch (IOException e) {
			System.err.println("ERROR: Could not write to file");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * @returns A copy of type private field
	 * */
	public Integer getType() {
		return new Integer(this.type);
	}
	
	/**
	 * @returns A copy of node private field
	 * */
	public String getNode() {
		return new String(this.node);
	}
	
	/**
	 * @returns A copy of cI private field
	 * */
	public Integer getcI() {
		return new Integer(this.cI);
	}
	
	/**
	 * @returns A copy of dtUTC private field
	 * */
	public DateTime getdtUTC() {
		return new DateTime(this.dtUTC);
	}
	
	/**
	 * @returns A copy of message private field
	 * */
	public String getMessage() {
		return new String(this.message);
	}
	
	/**
	 * @return Event object's information encapsulated
	 * */
	public String toString() {
		return new String(this.getType() + Event.FIELDDELIMITER  + this.getNode() + Event.FIELDDELIMITER + this.getcI() + Event.FIELDDELIMITER + this.getdtUTC() + Event.FIELDDELIMITER + this.getMessage());
	}
	
	/**
	 * @return Event object's information in a human readable format
	 * */
	public String toReadableString() {
		return new String("Type: " + this.translateType() + 
						  "\n\tNode: " + this.getNode() + 
						  "\n\tcI: " + this.getcI() + 
						  "\n\tTime: " + this.getdtUTC().withZone(DateTimeZone.getDefault()).toString("EEEE, MMM d 'at' hh:mma") + 
						  "\n\tMessage: " + this.getMessage() + "\n\n");
	}
	
	/**
	 * @returns The mapped value of private field type
	 * */
	public String translateType() {
		String type = null;
		
		if (this.getType().equals(Event.TWEET)) {
			type = "Tweet";
		} else if (this.getType().equals(Event.BLOCK)) {
			type = "Block";
		} else if (this.getType().equals(Event.UNBLOCK)) {
			type = "Unblock";
		}
		
		return type;
	}
	
	/**
	 * @effects Prints Event object's information to standard output
	 **/
	public void printEvent() {
		String typeStr = this.translateType();		
		System.out.println("\tType: " + typeStr);
		System.out.println("\tCreator: " + this.node);
		System.out.println("\tci: " + Integer.toString(this.cI));
		System.out.println("\tMessage: " + this.message);
		System.out.println("\tTime: " + this.getdtUTC().withZone(DateTimeZone.getDefault()).toString("EEEE, MMM d 'at' hh:mma"));
		System.out.println("");
	}
	
	/**
	 * @returns 1 if this Event is before other Event, 0 otherwise to have newest Events appear first
	 * */
	@Override
	public int compareTo(Event obj2) {
		if (this.getdtUTC().isBefore(obj2.getdtUTC())){
			return 1;
		}
		return -1;
	}
}
