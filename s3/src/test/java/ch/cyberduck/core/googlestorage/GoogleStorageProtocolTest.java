package ch.cyberduck.core.googlestorage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class GoogleStorageProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.gstorage.GoogleStorage", new GoogleStorageProtocol().getPrefix());
    }
}