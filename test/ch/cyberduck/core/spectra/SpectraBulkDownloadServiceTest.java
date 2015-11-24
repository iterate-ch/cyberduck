package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @version $Id:$
 */
public class SpectraBulkDownloadServiceTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testPre() throws Exception {
        final Host host = new Host(new SpectraProtocol(), "192.168.56.101", 8080, new Credentials(
                "aXRlcmF0ZQ==", "sVYKkwL9"
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        new SpectraBulkDownloadService(session).pre(Collections.singletonMap(
                new Path(String.format("/cyberduck/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().length(1L)
        ));
        session.close();
    }
}