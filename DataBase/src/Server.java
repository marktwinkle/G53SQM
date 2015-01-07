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

	public void runServer() {

		// open server socket and wait for client connections
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
			System.exit(-1);
		}
	}

	public boolean addClient(ClientInfo client) {
		if (client != null) {
			clientsList.add(client);
			messageToEveryone("<\""+client.getUsername()+"\" has joined the chat>");
			return true;
		}
		return false;
	}

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

	public String getUsersList() {
		String usersList = "";
		for( ClientInfo client: clientsList){
			usersList += "\r\n"+client.getUsername();
		}
		return usersList;
	}

	public int getClientsCount() {
		return clientsList.size();
	}

	public void messageToEveryone(String message){
		for(ServerThread connection : connectionsList){
			if (connection.getClient() != null) {
				connection.messageToClient(message);	
			}	
		}
	}

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
