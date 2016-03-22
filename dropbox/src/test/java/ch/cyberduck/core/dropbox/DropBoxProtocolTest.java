package ch.cyberduck.core.dropbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by denis on 3/21/16.
 */
public class DropboxProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.dropbox.Dropbox", new DropboxProtocol().getPrefix());
    }

    @Test
    public void testPassword() {
        assertFalse(new DropboxProtocol().isPasswordConfigurable());
    }
}
