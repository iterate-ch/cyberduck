package ch.cyberduck.core.transfer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class TransferActionTest {

    @Test
    public void testForName() throws Exception {
        assertEquals(TransferAction.overwrite.hashCode(),
                TransferAction.forName(TransferAction.overwrite.name()).hashCode());
    }
}
