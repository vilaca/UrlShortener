/**
 * 
 */
package eu.vilaca.keystore;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author vilaca
 * 
 */
public class HashKeyTest {

	@Test
	public void testCloning() {

		HashKey hk5 = new HashKey();
		HashKey hk6 = new HashKey(hk5.getBytes());

		Assert.assertTrue(hk5.equals(hk6));

		HashKey hk9 = new HashKey();
		HashKey hk10 = new HashKey(hk9.getBytes());

		Assert.assertTrue(hk9.equals(hk10));

		HashKey hk11 = new HashKey();
		HashKey hk12 = new HashKey(hk11.getBytes());

		Assert.assertTrue(hk11.equals(hk12));

		HashKey hk1 = new HashKey("aaaaa0".getBytes());
		HashKey hk2 = new HashKey(hk1.getBytes());

		Assert.assertTrue(hk1.equals(hk2));

		HashKey hk3 = new HashKey("aAbCeF".getBytes());
		HashKey hk4 = new HashKey(hk3.getBytes());

		Assert.assertTrue(hk3.equals(hk4));

		HashKey hk7 = new HashKey("------".getBytes());
		HashKey hk8 = new HashKey(hk7.getBytes());

		Assert.assertTrue(hk7.equals(hk8));

	}
}
