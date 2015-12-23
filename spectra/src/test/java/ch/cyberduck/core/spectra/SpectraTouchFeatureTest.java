package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MetadataFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SpectraTouchFeatureTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final Host host = new Host(new SpectraProtocol(), properties.getProperty("spectra.hostname"), 8080, new Credentials(
                properties.getProperty("spectra.user"), properties.getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        assertFalse(new SpectraTouchFeature(session).isSupported(new Path("/", EnumSet.of(Path.Type.volume))));
        assertTrue(new SpectraTouchFeature(session).isSupported(new Path(new Path("/", EnumSet.of(Path.Type.volume)), "/container", EnumSet.of(Path.Type.volume))));
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new SpectraProtocol(), properties.getProperty("spectra.hostname"), 8080, new Credentials(
                properties.getProperty("spectra.user"), properties.getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new SpectraTouchFeature(session).touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }
}
