


public class ClientInfo {
	private String username;
	private int messagesCounter;
		
	public ClientInfo (String username) {
		this.username = username;
		messagesCounter = 0;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getMessagesCounter() {
		return messagesCounter;
	}
	
	public void increaseMessagesCounter() {
		messagesCounter++;
	}
}
