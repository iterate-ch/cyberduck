package ch.cyberduck.core.transfer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class TransferActionTest {

    @Test
    public void testForName() throws Exception {
        assertEquals(TransferAction.ACTION_OVERWRITE.hashCode(),
                TransferAction.forName(TransferAction.ACTION_OVERWRITE.name()).hashCode());
    }
}
