import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class ServerTest {
	private final int PORT_NUMBER = 6789;
	private final String CR = "\r";
	private final String LF = "LF";
	private final String EMPTY_STRING = "";
	Server testServer;
	
	@Before
	public void initialization() {
		testServer = new Server(PORT_NUMBER);
	}
	
	@Test
	public void testServerNotNull() {
		assertNotNull(new Server(PORT_NUMBER));
	}
	
	@Test
	public void testAddClientFAIL() {
		assertEquals(false, testServer.addClient(null));
	}
	
	@Test
	public void testAddClientPASS() {
		assertEquals(true, testServer.addClient(new ClientInfo("name")));
	}
	
	@Test
	public void testRemoveClientFAIL1() {
		assertEquals(false, testServer.removeClient(null));
	}
	
	@Test
	public void testRemoveClientFAIL2() {
		assertEquals(false, testServer.removeClient(new ClientInfo("testUsername")));
	}
	
	@Test
	public void testRemoveClientPASS() {
		ClientInfo testClientInfo = new ClientInfo("testUsername");
		testServer.addClient(testClientInfo);
		assertEquals(true, testServer.removeClient(testClientInfo));
	}
	
	@Test
	public void testGetClientCount1() {
		assertEquals(0, testServer.getClientsCount());
	}
	
	@Test
	public void testGetClientCount2() {
		testServer.removeClient(null);
		assertEquals(true, testServer.getClientsCount() == 0);
	}
	
	@Test
	public void testGetClientCount3() {
		testServer.addClient(new ClientInfo("testUsername"));
		assertEquals(true, testServer.getClientsCount() == 1);
	}
	
	@Test
	public void testGetClientCount4() {
		ClientInfo testClientInfo = new ClientInfo("testUsername");
		testServer.addClient(testClientInfo);
		testServer.removeClient(testClientInfo);
		assertEquals(true, testServer.getClientsCount() == 0);
	}
	
	@Test
	public void testMessageToUserFAIL1() {
		assertEquals(false, testServer.messageToUser(null, null));
	}
	
	@Test
	public void testMessageToUserFAIL2() {
		assertEquals(false, testServer.messageToUser("", null));
	}
	
	@Test
	public void testMessageToUserFAIL3() {
		assertEquals(false, testServer.messageToUser(null, "testUsername"));
	}
	
	@Test
	public void testMessageToUserFAIL4() {
		testServer.addClient(new ClientInfo("testNotUsername"));
		assertEquals(false, testServer.messageToUser(null, "testUsername"));
	}
	
	@Test
	public void testMessageToUserFAIL5() {
		testServer.addClient(new ClientInfo("testUsername"));
		assertEquals(false, testServer.messageToUser(null, "testUsername"));
	}
	
	@Test
	public void testMessageToUserPASS() {
		testServer.addClient(new ClientInfo("testUsername"));
		assertEquals(true, testServer.messageToUser("testMessage", "testUsername"));
		//FIXME: fails because no existing connection [refer to Server.messageToUser]
	}
	
	@Test
	public void testIsUserExistFAIL1() {
		assertEquals(false, testServer.isUserExist(null));
	}
	
	@Test
	public void testIsUserExistFAIL2() {
		assertEquals(false, testServer.isUserExist("testUsername"));
	}
	
	@Test
	public void testIsUserExistPASS() {
		testServer.addClient(new ClientInfo("testUsername"));
		assertEquals(true, testServer.isUserExist("testUsername"));
	}
	
	@Test
	public void testGetUsersList1() {
		assertEquals(0, testServer.getUsersList().length());
	}
	
	@Test
	public void testGetUsersList2() {
		testServer.addClient(null);
		assertEquals(0, testServer.getUsersList().length());
	}
	
	@Test
	public void testGetUsersList3() {
		String testUsername1 = "testUsername1";
		String testUsername2 = "testUsername2";
		
		testServer.addClient(new ClientInfo(testUsername1));
		testServer.addClient(new ClientInfo(testUsername2));
		
		String usersList = testServer.getUsersList();
		usersList = usersList.replace(LF, EMPTY_STRING);
		usersList = usersList.replace(CR, EMPTY_STRING);
		assertEquals(testUsername1.length()+testUsername2.length(), usersList.length());
	}
}
