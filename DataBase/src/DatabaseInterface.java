

public interface DatabaseInterface {

	/**
	 * Verify if user exists in database
	 * @param user: user name
	 * @return true: if user exists, false: if no such user
	 */
	public boolean user(String user);

	/**
	 * Verify if password for user is correct
	 * @param pswd: password
	 * @return true: if correct password, false: if incorrect password
	 */
	public boolean pass(String pswd); 
	
	/**
	 * Deletes mails if any are to be deleted
	 * @return true: if successfully deleted mails, false: if failed to delete mails
	 */
	public boolean quit();
	
	/**
	 * Prints out the stats of maildrop (total number of mails, and total size of mails)
	 * @return maildrop stats as string
	 */
	public String stat();
	
	/**
	 * Lists out the maildrop in a multi-lines response showing: total mails and size
	 * @return multi-lines list of maildrop content (mail number and size)
	 */
	public String list();
	
	/**
	 * Lists only one mail's information (number and size)
	 * @param mailNum: the number of selected mail
	 * @return mail number and size as string
	 */
	public String list(int mailNum);
	
	/**
	 * prints out the selected mail
	 * @param mailNum: the number of selected mail
	 * @return multi-lines response of entire mail
	 */
	public String retr(int mailNum);
	
	/**
	 * Marks selected mail to be deleted once server quits in update state
	 * @param mailNum: the number of selected mail
	 * @return true: if mail has been successfully marked for delet, false: otherwise
	 */
	public boolean dele(int mailNum);
	
	/**
	 * Maintains connection with server, with no effects
	 */
	public void noop();
	
	/**
	 * Unmark all marked deleted messaged
	 */
	public void rset();
	
	/**
	 * Prints selected mail's header and a number of lines from the mail's body
	 * @param mailNum: the number of selected mail
	 * @param lines: the quantity of lines to be displayed
	 * @return multi-lines response of header and lines of body specified by lines argument
	 */
	public String top(int mailNum, int lines);
	
	/**
	 * Lists mails in maildrop with their unique id's
	 * @return multi-lines response of mails numbers and their id's
	 */
	public String uidl();
	
	/**
	 * Prints the id of specified mail
	 * @param mailNum: the number of selected mail
	 * @return one line containing selected mail number and it's id as String
	 */
	public String uidl(int mailNum);
	
	/**
	 * Verified if the selected mail exists on database (those marked for delete are considered to be no existent)
	 * @param mailNum: the number of selected mail
	 * @return true: if selected mail exists, false: if a non-existent mail selected
	 */
	public boolean exists(int mailNum);
	
	/**
	 * Releases the lock for user in maildrop
	 * @param userId: the id for user logged in
	 * @return true: if lock is successfully released, false: otherwise
	 */
	public boolean releaseLock(int userId);
	
	/**
	 * gets the total number of mails in maildrop that are not marked for delete
	 * @return number of mails not marked with delete in maildrop
	 */
	public int getTotalMailsNumber();
	
	/**
	 * gets the total size of mails in maildrop that are not marked for delete
	 * @return size of mails not marked with delete in maildrop
	 */
	public int getTotalMailsSize() ;

	/**
	 * Gets the logged user id
	 * @return logged user id
	 */
	public int getLoggedUserId();
	
}
