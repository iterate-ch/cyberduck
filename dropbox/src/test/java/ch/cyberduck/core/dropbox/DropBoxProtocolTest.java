package ch.cyberduck.core.dropbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by denis on 3/21/16.
 */
public class DropBoxProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.dropbox.DropBox", new DropBoxProtocol().getPrefix());
    }

    @Test
    public void testPassword() {
        assertFalse(new DropBoxProtocol().isPasswordConfigurable());
    }
}
