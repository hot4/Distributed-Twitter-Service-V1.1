import org.joda.time.DateTime;

public class Event implements Comparable<Event> {
	/* Constants to map a type to the appropriate category of Event */
	public static Integer TWEET = 1;
	public static Integer BLOCK = 2;
	public static Integer UNBLOCK = 3;
	
	/* Delimiter for Event encapsulation */
	public static String EVENTDELIIMITER = ",";
	public static String FIELDDELIMITER = "|";
	
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
	
	public String toString() {
		return new String(this.getType() + Event.FIELDDELIMITER  + this.getNode() + Event.FIELDDELIMITER + this.getcI() + Event.FIELDDELIMITER + this.getdtUTC() + Event.FIELDDELIMITER + this.getMessage());
	}
	
	/**
	 * @returns 1 if this Tweet is before other Tweet, 0 otherwise to have newest Tweets appear first
	 * */
	@Override
	public int compareTo(Event obj2) {
		if (this.getdtUTC().isBefore(obj2.getdtUTC())){
			return 1;
		}
		return -1;
	}
}
