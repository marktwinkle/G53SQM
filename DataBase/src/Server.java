import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Server extends Thread {
	private Socket connectionSocket;
	private PrintWriter output;
	private BufferedReader input; 
	private CommandInterpreter cmd;
	
	public Server(Socket socket) {
		connectionSocket = socket;
		cmd = new CommandInterpreter(this);
	}
}
