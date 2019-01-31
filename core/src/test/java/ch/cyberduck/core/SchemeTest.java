package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class SchemeTest {

    @Test
    public void testUrl() {
        assertTrue(Scheme.isURL("ftp://h.name"));
        assertTrue(Scheme.isURL("ftps://h.name"));
        assertTrue(Scheme.isURL("sftp://h.name"));
        assertTrue(Scheme.isURL("http://h.name"));
        assertTrue(Scheme.isURL("https://h.name"));
        assertTrue(Scheme.isURL("irods://h.name"));
        assertFalse(Scheme.isURL("h.name"));
    }
}