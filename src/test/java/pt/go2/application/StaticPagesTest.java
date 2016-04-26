package pt.go2.application;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;

import pt.go2.application.EmbeddedPages.Builder;

public class StaticPagesTest {

    @Test
    public void testEmpty() throws IOException, ServletException, URISyntaxException {

        final Builder builder = new Builder(); 
        final EmbeddedPages ep = builder.create(); 
        
        Assert.assertNull(ep.getFile("wtv"));
    }

    @Test
    public void testSimpleFileBuilder() throws IOException, ServletException, URISyntaxException {

        final Builder builder = new Builder(); 
        
        builder.add("filename", "content".getBytes(), MimeTypeConstants.MIME_TEXT_PLAIN);

        final EmbeddedPages ep = builder.create(); 
        
        Assert.assertNull(ep.getFile("wtv"));
        Assert.assertNotNull(ep.getFile("filename"));
    }

    @Test
    public void testAlias() throws IOException, ServletException, URISyntaxException {

        final Builder builder = new Builder(); 
        
        builder.add("filename", "content".getBytes(), MimeTypeConstants.MIME_TEXT_PLAIN);

        builder.setAlias("alias", "filename");
        
        final EmbeddedPages ep = builder.create(); 
        
        Assert.assertNull(ep.getFile("wtv"));
        Assert.assertNotNull(ep.getFile("filename"));
        Assert.assertNotNull(ep.getFile("alias"));
    }
}