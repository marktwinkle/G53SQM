
import static org.junit.Assert.*;

import org.junit.Test;


/**
 * 
 * JUnitTest is used as it is good to keep record of attempted tests, and valid for testing the actual database once created.
 */
public class CommandInterpreterTest {


	final int COMMAND = 0;
	final int ARGUMENT_ONE = 1;
	final int ARGUMENT_TWO = 2;
	private final int AUTHORIZATION = 0;
	private final int TRANSACTION = 1;
	private final int UPDATE = 2;
	final String OK = "+OK ";
	final String ERR = "-ERR ";
	private final String CRLF = "\r\n";
	
	CommandInterpreter command = new CommandInterpreter();

	@Test
	public void testInvalidCommand1() {
		String input = "GET value";
		assertTrue(command.handleInput(input).equals(ERR+"Invalid command"+" "+input+CRLF));
	}

	@Test
	public void testInvalidCommand2() {
		String input = "";
		assertTrue(command.handleInput(input).equals(ERR+"Invalid command"+" "+input+CRLF));
	}


	@Test
	public void testCommandInvalidArguemtns2() {
		String input = "STAT 2";
		assertTrue(!command.isValidArgs(input));
	}
	
	@Test
	public void testCommandInvalidArguemtns3() {
		String input = "USER " +
				"0123456789" +
				"0123456789" +
				"0123456789" +
				"0123456789" +
				"0";
		assertTrue(!command.isValidArgs(input));
	}

	@Test
	public void testUserOk() {
		String input = "USER alex";
		assertTrue(command.handleInput(input).equals(OK+"valid mailbox"+" "+input+CRLF));
	}

	@Test
	public void testUserErr() {
		String input = "USER hello";
		String inputline [] = input.split(" ");
		assertTrue(command.handleInput(input).equals(ERR+inputline[ARGUMENT_ONE]+" mailbox does not exist"+" "+input+CRLF));
	}


	
	@Test
	public void testStatOk() {
		String input = "USER alex";
		command.handleInput(input);
		input = "PASS hello123";
		command.handleInput(input);
		input = "STAT";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
		int userId = command.getDatabase().getLoggedUserId();
		command.getDatabase().releaseLock(userId);
	}

	@Test
	public void testListOk1() {
		String input = "USER alex";
		command.handleInput(input);
		input = "PASS hello123";
		command.handleInput(input);
		input = "LIST";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
		int userId = command.getDatabase().getLoggedUserId();
		command.getDatabase().releaseLock(userId);
	}



	@Test
	public void testQuitOk() {
		String input = "USER alex";
		command.handleInput(input);
		input = "PASS hello123";
		command.handleInput(input);
		input = "QUIT";
		command.handleInput(input);
		input = "QUIT";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}

	@Test
	public void testInitialState() {
		assertTrue(command.getState() == AUTHORIZATION);
	}

	@Test
	public void testStateChange1() {
		String input = "USER alex";
		command.handleInput(input);
		assertTrue(command.getState() == TRANSACTION);
		int userId = command.getDatabase().getLoggedUserId();
		command.getDatabase().releaseLock(userId);
	}

	@Test
	public void testStateChange2() {
		String input = "USER alex";
		command.handleInput(input);
		input = "QUIT";
		command.handleInput(input);
		assertTrue(command.getState() == UPDATE);
		int userId = command.getDatabase().getLoggedUserId();
		command.getDatabase().releaseLock(userId);
	}

}
