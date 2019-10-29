package ch.cyberduck.core;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class DescriptiveUrlTest {

    @Test
    public void testEquals() {
        assertEquals(new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.provider, "a"), new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.provider, "b"));
        assertNotEquals(new DescriptiveUrl(URI.create("http://host.domainb"), DescriptiveUrl.Type.provider, "a"), new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.provider, "b"));
        assertEquals(new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.http), new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.http));
        assertEquals("http://host.domain", new DescriptiveUrl(URI.create("http://host.domain"), DescriptiveUrl.Type.http).toString());
    }

    @Test
    public void testDefault() {
        assertEquals("Open in Web Browser", new DescriptiveUrl(URI.create("http://me")).getHelp());
    }
}
