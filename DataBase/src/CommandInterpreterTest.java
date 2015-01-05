
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
	
	CommandInterpreterOLD command = new CommandInterpreterOLD();

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
		String input = "IDEN " +
				"0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789" +
				"0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789" +
				"0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789" +
				"0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789" +
				"0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789" +
				"012345";
		assertTrue(!command.isValidArgs(input));
	}

	@Test
	public void testUserOk() {
		String input = "IDEN Aiman";
		assertTrue(command.handleInput(input).equals(OK+"valid mailbox"+" "+input+CRLF));
	}

	@Test
	public void testUserErr() {
		String input = "IDEN hello";
		String inputline [] = input.split(" ");
		assertTrue(command.handleInput(input).equals(ERR+inputline[ARGUMENT_ONE]+" mailbox does not exist"+" "+input+CRLF));
	}


	
	@Test
	public void testStatOk() {
		String input = "IDEN alex";
		command.handleInput(input);
		input = "PASS hello123";
		command.handleInput(input);
		input = "STAT";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}

	@Test
	public void testListOk1() {
		String input = "IDEN alex";
		command.handleInput(input);
		input = "PASS hello123";
		command.handleInput(input);
		input = "LIST";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}



	@Test
	public void testQuitOk() {
		String input = "IDEN alex";
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
		String input = "IDEN alex";
		command.handleInput(input);
		assertTrue(command.getState() == TRANSACTION);
	}

	@Test
	public void testStateChange2() {
		String input = "IDEN alex";
		command.handleInput(input);
		input = "QUIT";
		command.handleInput(input);
		assertTrue(command.getState() == UPDATE);
	}

}
