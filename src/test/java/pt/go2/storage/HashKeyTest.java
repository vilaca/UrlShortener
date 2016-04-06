package pt.go2.storage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class HashKeyTest {

    @Test
    public void testEqual() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC".getBytes(StandardCharsets.US_ASCII));
        final HashKey hk6 = new HashKey("AABBCC".getBytes(StandardCharsets.US_ASCII));

        Assert.assertTrue(hk5.equals(hk6));
    }

    @Test
    public void testNotEqual() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC".getBytes(StandardCharsets.US_ASCII));
        final HashKey hk6 = new HashKey("AABBCD".getBytes(StandardCharsets.US_ASCII));

        Assert.assertTrue(!hk5.equals(hk6));
    }

    @Test
    public void testDecoding() throws UnsupportedEncodingException {

        final HashKey hk5 = new HashKey("AABBCC".getBytes(StandardCharsets.US_ASCII));
        final HashKey hk6 = new HashKey(hk5.getHash());

        Assert.assertTrue(hk5.equals(hk6));
    }
}
