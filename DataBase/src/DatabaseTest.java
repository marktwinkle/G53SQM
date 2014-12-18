import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 */

/**
 * @author Aiman
 *
 */
public class DatabaseTest {

	DatabaseInterface database;
	String CRLF = "\r\n";
	
	/**
	 * Test method for {@link Database#Database()}.
	 */
	@Test
	public void testDatabaseNotNull() {
		Assert.assertNotNull(new Database());
	}

	/**
	 * Test method for {@link Database#user(java.lang.String)}.
	 */
	@Test
	public void testNotExistingUser() {
		database = new Database();
		String username = "hello";
		Assert.assertFalse(database.user(username));
		database.quit();
	}
	
	
	/**
	 * Test method for {@link Database#user(java.lang.String)}.
	 */
	@Test
	public void testExistingUserUnlockedMaildrop() {
		database = new Database();
		String username = "alex";
		Assert.assertTrue(database.user(username));
		database.quit();
	}

	/**
	 * Test method for {@link Database#pass(java.lang.String)}.
	 */
	@Test
	public void testPassBeforeCorrectUser() {
		database = new Database();
		String password = "hello123";
		Assert.assertFalse(database.pass(password));
		database.quit();
	}
	
	/**
	 * Test method for {@link Database#pass(java.lang.String)}.
	 */
	@Test
	public void testIncorrectPassAfterCorrectUser() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "password";
		Assert.assertFalse(database.pass(password));
		database.quit();
	}
	
	/**
	 * Test method for {@link Database#pass(java.lang.String)}.
	 */
	@Test
	public void testCorrectPassAfterCorrectUser() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		Assert.assertTrue(database.pass(password)); //will lock maildrop
		database.quit(); // releases maildrop
	}

	/**
	 * Test method for {@link Database#quit()}.
	 */
	@Test
	public void testQuit() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		Assert.assertTrue(database.quit());
	}

	/**
	 * Test method for {@link Database#stat()}.
	 */
	@Test
	public void testStat() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		String totalMails = "3";
		String totalSize = "173293";
		Assert.assertEquals(totalMails+" "+totalSize, database.stat());
		database.quit();
	}

	/**
	 * Test method for {@link Database#list()}.
	 */
	@Test
	public void testList() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		String totalMails = "3";
		String totalSize = "173293";
		String listOutput = totalMails+" messages ("+totalSize+" octets)" +CRLF+
				"1 1301" +CRLF+
				"2 2722" +CRLF+
				"3 169270" +CRLF+
				".";
		
		Assert.assertEquals(listOutput, database.list());
		database.quit();
	}

	/**
	 * Test method for {@link Database#list(int)}.
	 */
	@Test
	public void testListInt() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int mailNum = 2;
		int mailSize = 2722;
		
		String listOutput = mailNum+" "+mailSize;	
		Assert.assertEquals(listOutput, database.list(mailNum-1));
		database.quit();
	}

	/**
	 * Test method for {@link Database#retr(int)}.
	 */
	@Test
	public void testRetr() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#dele(int)}.
	 */
	@Test
	public void testDeleExistingMail() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int mailNum = 2;
		Assert.assertTrue(database.dele(mailNum-1));
		database.quit();
	}
	
	/**
	 * Test method for {@link Database#dele(int)}.
	 */
	@Test
	public void testDeleNonExistingMail() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int mailNum = 10000000;
		Assert.assertFalse(database.dele(mailNum-1));
		database.quit();
	}

	/**
	 * Test method for {@link Database#rset()}.
	 */
	@Test
	public void testRset() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#top(int, int)}.
	 */
	@Test
	public void testTop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#uidl()}.
	 */
	@Test
	public void testUidl() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#uidl(int)}.
	 */
	@Test
	public void testUidlInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#getTotalMailsNumber()}.
	 */
	@Test
	public void testGetTotalMailsNumber() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int totalMail = 2; // after one is deleted
	
		Assert.assertEquals(totalMail, database.getTotalMailsNumber());
		database.quit();
	}

	/**
	 * Test method for {@link Database#getTotalMailsSize()}.
	 */
	@Test
	public void testGetTotalMailsSize() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int totalMailSize = 170571; // after one is deleted
		Assert.assertEquals(totalMailSize, database.getTotalMailsSize());
		database.quit();
	}

	/**
	 * Test method for {@link Database#exists(int)}.
	 */
	@Test
	public void testExists() {
		database = new Database();
		String user = "alex";
		database.user(user);
		String password = "hello123";
		database.pass(password);
		int mailNum = 1; 
		Assert.assertTrue(database.exists(mailNum));
		database.quit();
	}

	/**
	 * Test method for {@link Database#getLock()}.
	 */
	@Test
	public void testGetLock() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link Database#releaseLock()}.
	 */
	@Test
	public void testReleaseLock() {
		fail("Not yet implemented");
	}

}
