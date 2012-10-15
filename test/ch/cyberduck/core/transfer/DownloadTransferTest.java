package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullProtocol;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class DownloadTransferTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testSerialize() throws Exception {
        Transfer t = new DownloadTransfer(new NullPath("t", Path.FILE_TYPE));
        assertEquals(t, new DownloadTransfer(t.getAsDictionary(), new NullSession(new Host("t") {
            @Override
            public Protocol getProtocol() {
                return new NullProtocol();
            }
        })));
    }

    @Test
    public void testChildren() throws Exception {

    }
}
