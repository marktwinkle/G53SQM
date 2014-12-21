

public interface DatabaseInterface {

	/**
	 * Verify if user exists in database
	 * @param user: user name
	 * @return true: if user exists, false: if no such user
	 */
	public boolean user(String user);

	/**
	 * Allows logged in client to end current session, and removes user from active users list
	 * @return true: if successfully removed user from active list, false: if failed to remove the user
	 */
	public boolean quit();
	
	/**
	 * Prints out the list of logged in users 
	 * @return a string list of logged in users
	 */
	public String stat();
	
	/**
	 Prints: (1) The number of logged in users 
			 (2) The current session status (logged in / not logged in)
			 (3) The number of messages sent. 

	 * @return a string of the described stats
	 */
	public String list();
	
	/**
	 * Allows logged in client to send message to named user
	 * @param destinationUser: the user the message should be sent to
	 * @param messageText: the message text
	 * @return true: if successfully sent message, false: if failed to send message
	 */
	public boolean mesg(String destinationUser, String messageText);
	
	/**
	 * Allows logged in client to send message to all users
	 * @return true: if successfully sent message, false: if failed to send message
	 */
	public boolean hail();
	
	/**
	 * Marks selected mail to be deleted once server quits in update state
	 * @param mailNum: the number of selected mail
	 * @return true: if mail has been successfully marked for delet, false: otherwise
	 */
	public boolean dele(int mailNum);
	
	/**
	 * Lists the messages sent to logged user from other users
	 * @return a string text of messages
	 */
	public String myMsgs();

	/**
	 * Gets the logged user id
	 * @return logged user id
	 */
	public int getLoggedUserId();
	
}
