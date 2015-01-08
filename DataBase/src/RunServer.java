
import java.io.IOException;

public class RunServer {
	public static void main(String[] args) throws IOException {

		// incorrect number of command line arguments
		if (args.length != 1) {
			System.out.println("Usage: java Server <port number>");
			System.exit(1);
		}

		try {
			int portNumber = Integer.parseInt(args[0]);
			new Server(portNumber);

			// invalid command line arguments 
		} catch (NumberFormatException numberFormatException) {
			System.out.println("Usage: java Server <port number> <Server timeout (in seconds)>");
			numberFormatException.printStackTrace();
			System.exit(1);
		}
	}
}