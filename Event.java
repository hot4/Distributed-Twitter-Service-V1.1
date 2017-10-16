import org.joda.time.DateTime;

public class Event implements Comparable<Event> {
	
	private Integer type;
	private String node;
	private String recipient;
	private Integer cI;
	private DateTime dtUTC;
	private String message;
	
	/**
     * @param type: Categorized to be one the following values {block, unblock, tweet}
	 * @param node: Location of where the event occurred (i.e. which User caused the event)
	 * @param recipient: Location of where the event is received (i.e. which User receives the Tweet)
	 * @param cI: The Lamport TimeStamp of the User after the event executed
	 * @param dtUTC: Represents the time at which the event occurred in UTC time
	 * @param message: Description of what Event occurred
	 * @modifies type, node, and rawTimeStamp private fields
	 * @returns A new Event object
	 * */
	public Event(Integer type, String node, String recipient, Integer cI, DateTime dtUTC, String message) {
		this.type = type;
		this.node = node;
		this.recipient = recipient;
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
	 * @returns A copy of recipient private field
	 * */
	public String getRecipient() {
		return new String(this.recipient);
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
