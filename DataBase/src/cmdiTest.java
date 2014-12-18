import java.io.IOException;


public class cmdiTest {
	public static void main(String[] args) throws IOException {
		CommandInterpreter cmd = new CommandInterpreter();
		cmd.handleInput("USER");
	}
}
