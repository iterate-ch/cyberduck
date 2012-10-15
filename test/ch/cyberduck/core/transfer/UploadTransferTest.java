package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class UploadTransferTest extends AbstractTestCase {

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new UploadTransfer(new NullPath("t", Path.FILE_TYPE));
        assertEquals(t, new UploadTransfer(t.getAsDictionary(), new NullSession(new Host("t"))));
    }

    @Test
    public void testChildren() throws Exception {

    }
}
