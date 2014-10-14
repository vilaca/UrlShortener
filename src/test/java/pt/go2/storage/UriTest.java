
package pt.go2.storage;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test Uri class, don't mind testing URL validator
 * 
 * @author vilaca
 *
 */
public class UriTest {

	@Test
	public void testEqual() {
		
		final Uri u1 = Uri.create("http://abcdefghij.ws/123", false);
		final Uri u2 = Uri.create("http://ABCDEFGHIJ.ws/123", false);
		final Uri u3 = Uri.create("http://ABCDEFGHIJ.WS/123", false);

		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		Assert.assertNotNull(u3);		
		
		Assert.assertThat(u1, is(u2));
		Assert.assertThat(u2, is(u3));
		Assert.assertThat(u3, is(u1));
	}


	@Test
	public void testEqualHttp() {
		
		final Uri u1 = Uri.create("http://abcdefghij.ws/123", false);
		final Uri u2 = Uri.create("ABCDEFGHIJ.ws/123", false);

		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		
		Assert.assertThat(u1, is(u2));
	}

	@Test
	public void testTrim() {
		
		final Uri u1 = Uri.create(" http://abcdefghij.ws/123", false);
		final Uri u2 = Uri.create("http://abcdefghij.ws/123 ", false);
		final Uri u3 = Uri.create(" http://abcdefghij.ws/123 ", false);
		
		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		Assert.assertNotNull(u3);
		
		Assert.assertThat(u1, is(u2));
		Assert.assertThat(u2, is(u3));
		Assert.assertThat(u3, is(u1));
	}

	@Test
	public void testDifferentScheme() {
		
		final Uri u1 = Uri.create("http://abcdefghij.ws/123", false);
		final Uri u2 = Uri.create("https://ABCDEFGHIJ.ws/123", false);
		
		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		
		Assert.assertThat(u1, is(not(u2)));
	}

	@Test
	public void testDifferentCaseInPath() {
		
		final Uri u1 = Uri.create("http://abcdefghij.ws/123A", false);
		final Uri u2 = Uri.create("http://abcdefghij.ws/123a", false);
		
		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		
		Assert.assertThat(u1, is(not(u2)));
	}

	@Test
	public void testDifferentPaths() {
		
		final Uri u1 = Uri.create("http://abcdefghij.ws/123A", false);
		final Uri u2 = Uri.create("http://abcdefghij.ws/____", false);
		
		Assert.assertNotNull(u1);
		Assert.assertNotNull(u2);
		
		Assert.assertThat(u1, is(not(u2)));
	}

}
