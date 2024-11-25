package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DescriptiveUrlTest {

    @Test
    public void testEquals() {
        assertEquals(new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.provider, "a"), new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.provider, "b"));
        assertNotEquals(new DescriptiveUrl("http://host.domainb", DescriptiveUrl.Type.provider, "a"), new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.provider, "b"));
        assertEquals(new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.http), new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.http));
        assertEquals("http://host.domain", new DescriptiveUrl("http://host.domain", DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testDefault() {
        assertEquals("HTTP URL", new DescriptiveUrl("http://me").getHelp());
    }
}
