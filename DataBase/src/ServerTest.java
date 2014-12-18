
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * JUnitTest is used as it is good to keep record of attempted tests, and valid for testing the actual database once created.
 * Some tests may fail due to non-existence of mails
 */
public class ServerTest {

	final boolean AUTO_FLUSH = true;
	final int COMMAND = 0;
	final int ARGUMENT_ONE = 1;
	final int ARGUMENT_TWO = 2;
	final String OK = "+OK ";
	final String ERR = "-ERR ";

	
	String responseText;
	String requestText;
	
	Socket socket;
	PrintWriter request;
	BufferedReader response;
	
	@Before
	public void initializations() {
		String hostName = "127.0.0.1";
		int portNumber = 6789;
		
		try {
			socket = new Socket(hostName, portNumber);
			request = new PrintWriter(socket.getOutputStream(), AUTO_FLUSH);
			response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			responseText = response.readLine();
		} catch (UnknownHostException unknownHostException) {
			System.err.println("Don't know about host " + hostName);
		} catch (IOException exception) {
			System.err.println("Unable to connect to the host");
		}
	}
	
	@After
	public void closeStreams() {
		try {
			socket.close();
			request.close();
			response.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	@Test
	public void testInvalidCommand1() throws IOException {
		String requestText = "GET value"; 
		request.println(requestText);
		responseText = response.readLine();
		assertTrue(responseText.equals(ERR+"Invalid command"+" "+requestText));
	}

	@Test
	public void testInvalidCommand2() throws IOException {
		String requestText = "";
		request.println(requestText);
		responseText = response.readLine();
		assertTrue(responseText.equals(ERR+"Invalid command"+" "+requestText));
	}

	@Test
	public void testUserOk() throws IOException {
		String requestText = "USER alex";
		request.println(requestText);
		responseText = response.readLine();
		assertTrue(responseText.equals(OK+"valid mailbox"+" "+requestText));
	}

	@Test
	public void testPassOk() throws IOException {
		String requestText = "USER alex";
		request.println(requestText);
		responseText = response.readLine();
		requestText = "PASS hello123";
		request.println(requestText);
		responseText = response.readLine();
		responseText = response.readLine();
		assertTrue(responseText.equals(OK+"welcome to mailbox"+" "+requestText));
	}

	@Test
	public void testPassErr() throws IOException {
		String requestText = "PASS basswood";
		request.println(requestText);
		responseText = response.readLine();
		assertTrue(responseText.equals(ERR+"incorrect password"+" "+requestText));
	}

	@Test
	public void testStatOk() throws IOException {
		String requestText = "USER alex";
		request.println(requestText);
		response.readLine();
		requestText = "PASS hello123";
		request.println(requestText);
		response.readLine();
		requestText = "STAT";
		request.println(requestText);
		responseText = response.readLine();
		assertTrue(responseText.split(" ")[0].equals(OK.trim()));
	}

	// To be added later
	/*
	@Test
	public void testListOk1() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "LIST";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
		//test fails as there is no mails
	}

	@Test
	public void testListOk2() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "LIST 3";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
		//test fails as there is no mails
	}

	@Test
	public void testListErr() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "LIST -3";
		assertTrue(command.handleInput(input).split(" ")[0].equals(ERR.trim()));
	}

	@Test
	public void testRetrOk() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "RETR 1";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
		//test fails as there is no mails
	}

	@Test
	public void testRetrErr() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "RETR -1";
		assertTrue(command.handleInput(input).split(" ")[0].equals(ERR.trim()));
	}

	@Test
	public void testNoopOk() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "NOOP";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}

	@Test
	public void testRsetOk() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "RSET";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}

	@Test
	public void testQuitOk() {
		String requestText = "USER test";
		command.handleInput(input);
		requestText = "PASS password";
		command.handleInput(input);
		requestText = "QUIT";
		command.handleInput(input);
		requestText = "QUIT";
		assertTrue(command.handleInput(input).split(" ")[0].equals(OK.trim()));
	}

	@Test
	public void testInitialState() {
		assertTrue(command.getState() == AUTHORIZATION);
	}

	@Test
	public void testStateChange1() {
		String requestText = "PASS password";
		command.handleInput(input);
		assertTrue(command.getState() == TRANSACTION);
	}

	@Test
	public void testStateChange2() {
		String requestText = "PASS password";
		command.handleInput(input);
		requestText = "QUIT";
		command.handleInput(input);
		assertTrue(command.getState() == UPDATE);
	}

*/
	
}
