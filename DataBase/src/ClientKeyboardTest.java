
public class ClientKeyboardTest {
	public static void main (String args[] ) {
		
		
		// incorrect number of command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java EchoClient <host name> <port number>");
			System.exit(1);
		}

		try {
			String hostName = args[0];
			int portNumber = Integer.parseInt(args[1]);
			
			Client client = new Client(hostName, portNumber);
			client.startRunning();
		
		// invalid command line arguments
		} catch (NumberFormatException numberFormatException) {
			System.err.println("Usage: java EchoClient <host name> <port number>");
			System.exit(1);
		}
	}
}
