/**
 * 
 */
package pt.go2.keystore;

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
		HashKey hk6 = new HashKey(hk5.toString());

		Assert.assertTrue(hk5.equals(hk6));

		HashKey hk9 = new HashKey();
		HashKey hk10 = new HashKey(hk9.toString());

		Assert.assertTrue(hk9.equals(hk10));

		HashKey hk11 = new HashKey();
		HashKey hk12 = new HashKey(hk11.toString());

		Assert.assertTrue(hk11.equals(hk12));

		HashKey hk1 = new HashKey("aaaaa0");
		HashKey hk2 = new HashKey(hk1.toString());

		Assert.assertTrue(hk1.equals(hk2));

		HashKey hk3 = new HashKey("aAbCeF");
		HashKey hk4 = new HashKey(hk3.toString());

		Assert.assertTrue(hk3.equals(hk4));

		HashKey hk7 = new HashKey("------");
		HashKey hk8 = new HashKey(hk7.toString());

		Assert.assertTrue(hk7.equals(hk8));

	}
}
