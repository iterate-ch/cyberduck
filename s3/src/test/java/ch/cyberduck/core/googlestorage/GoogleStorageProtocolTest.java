package ch.cyberduck.core.googlestorage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GoogleStorageProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.googlestorage.GoogleStorage", new GoogleStorageProtocol().getPrefix());
    }

    @Test
    public void testPassword() {
        assertFalse(new GoogleStorageProtocol().isPasswordConfigurable());
    }
}