import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private int portNumber;
	private List<ServerThread> connectionsList;
	private List<ClientInfo> clientsList;

	public Server(int portNumber) {
		this.portNumber = portNumber;
		connectionsList = new ArrayList<ServerThread>();
		clientsList = new ArrayList<ClientInfo>();

	}

	/**
	 * Opens server socket and waits for client connections
	 */
	public void runServer() {
		
		
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
			System.out.println("Server running on port "+portNumber);
			while(true) {
				try {
					
					ServerThread serverThread = new ServerThread(this, serverSocket.accept());
					connectionsList.add(serverThread);
					serverThread.start();
				} catch (IOException e) {
					System.err.println("UNABLE TO SETUP CONNECTION");
					e.printStackTrace();
				}
			}
		// when port is busy
		} catch (IOException ioException) {
			System.err.println("Could not listen on port " + portNumber);
			ioException.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Closes server socket
	 */
	public void shutdownServer() {
//		if(serverSocket != null && !serverSocket.isClosed()) {
//			try {
//				serverSocket.close();
//			} catch (IOException iOException) {
//				System.err.println("Unable to close socket");
//				iOException.printStackTrace();
//			}	
//		} else {
//			System.err.println("Socket is not open");
//		}
		
	}

	/**
	 * Adds a client to the list of active clients
	 * @param client: the client to be added
	 * @return true: if successfully added, false: otherwise
	 */
	public boolean addClient(ClientInfo client) {
		if (client != null) {
			clientsList.add(client);
			messageToEveryone("<\""+client.getUsername()+"\" has joined the chat>");
			return true;
		}
		return false;
	}

	/**
	 * Removes a client from the list of active client
	 * @param client: the client to be removed
	 * @return true: if successfully removed, false: otherwise
	 */
	public boolean removeClient(ClientInfo client) {
		if (clientsList.size() > 0) {
			clientsList.remove(clientsList.indexOf(client));
			for (ServerThread connection : connectionsList) {
				if (!connection.isRunning()) {
					connectionsList.remove(connectionsList.indexOf(connection));
					messageToEveryone("<\""+client.getUsername()+"\" has left the chat>");
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Lists the online username
	 * @return String representing the list of logged in users
	 */
	public String getUsersList() {
		String usersList = "";
		for( ClientInfo client: clientsList){
			usersList += "\r\n"+client.getUsername();
		}
		return usersList;
	}

	/**
	 * Gets the number of users connected to server
	 * @return the number of logged users
	 */
	public int getClientsCount() {
		return clientsList.size();
	}

	/**
	 * Sends a message to every user the is connected to the server
	 * @param message: the message to be sent
	 */
	public void messageToEveryone(String message){
		for(ServerThread connection : connectionsList){
			if (connection.getClient() != null) {
				connection.messageToClient(message);	
			}	
		}
	}

	/**
	 * Sends a message to a specific user that is connected to the server
	 * @param message: the message to be sent
	 * @param username: the username of recipient 
	 * @return true: if successfully sent, false: otherwise
	 */
	public boolean messageToUser(String message, String username) {
		if (isUserExist(username) && message != null) {
			for(ServerThread connection : connectionsList) {
				if (username.equals(connection.getClient().getUsername())) {
					connection.messageToClient(message);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a user is connected to the server
	 * @param username: the username to be checked
	 * @return true: if user exists, false: otherwise
	 */
	public boolean isUserExist(String username) {
		if (username != null) {
			for (ClientInfo client : clientsList) {
				if (username.toLowerCase().equals(client.getUsername().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
}
