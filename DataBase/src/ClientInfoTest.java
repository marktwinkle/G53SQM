
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientInfoTest {

	ClientInfo testClientInfo;
	String testUsername;
	
	
	@Before
	public void initialization() {
		testUsername = "PairProgrammers";
		testClientInfo = new ClientInfo(testUsername);
	}
	
	@After
	public void garbageCollection() {
		testUsername = null;
		testClientInfo = null;
	}
	
	@Test
	public void testClientInfoNotNull() {
		assertNotNull(new ClientInfo(null));
	}
	
	@Test
	public void testUsernamePASS1() {
		assertEquals(null, (new ClientInfo(null)).getUsername());
	}
	
	@Test
	public void testUsernamePASS2() {
		assertEquals(testUsername, testClientInfo.getUsername());
	}
	
	@Test
	public void testMessagesCounterPASS1() {
		assertEquals(true, testClientInfo.getMessagesCounter() >= 0);
	}
	
	@Test
	public void testMessagesCounterPASS2() {
		testClientInfo.increaseMessagesCounter();
		assertEquals(true, testClientInfo.getMessagesCounter() == 1);
	}
	
	
}
