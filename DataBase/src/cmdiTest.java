import java.io.IOException;


public class cmdiTest {
	public static void main(String[] args) throws IOException {
		CommandInterpreterOLD cmd = new CommandInterpreterOLD();
		cmd.handleInput("USER");
	}
}
