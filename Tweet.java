import org.joda.time.DateTime;

public class Tweet implements Comparable<Tweet> {
	/* Creator of Tweet */
	private String userName;
	/* Message of Tweet */
	private String message;
	/* Time at which the Tweet was created, represented in UTC time */
	private DateTime dtUTC;
	
	/**
	 * @param userName: Creator of Tweet
	 * @param message: Message of Tweet
	 * @param dtUTC: Time at which the Tweet was created, represented in UTC time
	 * @effects Assigns parameters to private fields
	 * @modifies userName, message, and dtUTC private fields 
	 * @return A new Tweet object
	 * */
	public Tweet(String userName, String message, DateTime dtUTC) {
		this.userName = userName;
		this.message = message;
		this.dtUTC = dtUTC;
	}
	
	/**
	 * @param event: Event object that is of type Tweet
	 * @effects Maps relevant private fields from Event object to Tweet private fields
	 * @return A new Tweet object
	 * */
	public Tweet(Event event) { 
		this.userName = event.getNode();
		this.message = event.getMessage();
		this.dtUTC = event.getdtUTC();
	}
	
	/**
	 * @return A copy of userName private field
	 * */
	public String getUserName() {
		return new String(this.userName);
	}
	
	/**
	 * @return A copy of message private field
	 * */
	public String getMessage() {
		return new String(this.message);
	}
	
	/**
	 * @return A copy of dtUTC private field
	 * */
	public DateTime getdtUTC(){
		return new DateTime(dtUTC);
	}

	/**
	 * @return 1 if this Tweet is before other Tweet, 0 otherwise to have newest Tweets appear first
	 * */
	@Override
	public int compareTo(Tweet obj2) {
		if (this.getdtUTC().isBefore(obj2.getdtUTC())){
			return 1;
		}
		return -1;
	}
}
