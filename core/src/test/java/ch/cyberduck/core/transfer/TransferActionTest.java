package ch.cyberduck.core.transfer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransferActionTest {

    @Test
    public void testForName() throws Exception {
        assertEquals(TransferAction.overwrite.hashCode(),
                TransferAction.forName(TransferAction.overwrite.name()).hashCode());
    }

    @Test
    public void testActionsForCopy() throws Exception {
        assertEquals(2, (TransferAction.forTransfer(Transfer.Type.copy).size()));
        assertTrue(TransferAction.forTransfer(Transfer.Type.copy).contains(TransferAction.comparison));
        assertTrue(TransferAction.forTransfer(Transfer.Type.copy).contains(TransferAction.overwrite));
    }
}
