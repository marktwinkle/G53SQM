import static org.junit.Assert.*;

import org.junit.Test;


public class CommandTest {

	Commands cm = new Commands();
	int state=0;
	@Test
	public void testInvalidCommands() {
		//System.out.println(cm.authorization("IDEN aiman"));
		assertEquals(false, cm.validCommand(""));
		assertEquals(false, cm.validCommand(" "));
		assertEquals(false, cm.validCommand(null));
		assertEquals(false, cm.validCommand("^&%"));
		assertEquals(false, cm.validCommand("wrong"));
	}
	@Test
	public void testIDEN() {
		assertEquals(true, cm.validCommand("IDEN user"));
		assertEquals(false, cm.validateUserName("user%"));
		assertEquals(true, cm.validateUserName("user"));
	}
	
	@Test
	public void testMESG() {
		assertEquals(true, cm.validCommand("MESG user hi "));
		assertEquals(false, cm.validCommand("MESG user"));
		assertEquals(false, cm.validCommand("MESG "));
	}
	@Test
	public void testSTAT() {
		assertEquals(true, cm.validCommand("STAT"));
		assertEquals(false, cm.validCommand("STAT onlineNumber"));
		assertEquals(false, cm.validCommand(" STAT"));
	}
	@Test
	public void testLIST() {
		assertEquals(true, cm.validCommand("LIST"));
		assertEquals(false, cm.validCommand("LIST onlinePerson"));
	}
	@Test
	public void testHAIL() {
		assertEquals(true, cm.validCommand("LIST"));
		assertEquals(false, cm.validCommand("LIST onlinePerson"));
	}
	@Test
	public void testQUIT() {
		assertEquals(false, cm.validCommand("QUIT ME"));
		assertEquals(true, cm.validCommand("QUIT"));
	}
}
