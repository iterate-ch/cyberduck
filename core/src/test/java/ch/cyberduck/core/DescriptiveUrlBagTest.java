package ch.cyberduck.core;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class DescriptiveUrlBagTest {

    @Test
    public void testFilter() throws Exception {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        final DescriptiveUrl url = new DescriptiveUrl(URI.create("http://example.net"));
        list.add(url);
        assertTrue(list.filter(DescriptiveUrl.Type.provider).isEmpty());
        assertTrue(list.filter(DescriptiveUrl.Type.cdn).isEmpty());
        assertFalse(list.filter(DescriptiveUrl.Type.http).isEmpty());
        assertFalse(list.filter(DescriptiveUrl.Type.provider, DescriptiveUrl.Type.http).isEmpty());
        assertEquals(DescriptiveUrl.EMPTY, list.find(DescriptiveUrl.Type.provider));
        assertEquals(url, list.find(DescriptiveUrl.Type.http));
    }

    @Test
    public void testFind() throws Exception {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        final DescriptiveUrl url = new DescriptiveUrl(URI.create("http://example.net"));
        list.add(url);
        assertEquals(DescriptiveUrl.EMPTY, list.find(DescriptiveUrl.Type.provider));
        assertEquals(url, list.find(DescriptiveUrl.Type.http));
    }
}
