package pt.go2.storage;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class HashKeyTest {

    @Test
    public void testEqual() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC");
        final HashKey hk6 = new HashKey("AABBCC");

        Assert.assertTrue(hk5.equals(hk6));
    }

    @Test
    public void testNotEqual() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC");
        final HashKey hk6 = new HashKey("AABBCD");

        Assert.assertTrue(!hk5.equals(hk6));
    }

    @Test
    public void testDecoding() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC");
        final HashKey hk6 = new HashKey(hk5.toString());

        Assert.assertTrue(hk5.equals(hk6));

    }
}
