import java.io.IOException;


public class IdeRunClient {
	public static void main(String[] args) throws IOException {
		String [] argument = {"127.0.0.1", "6789"};
		RunClient.main(argument);
	}
}
