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
}
