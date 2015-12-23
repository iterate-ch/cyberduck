package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.s3.RequestEntityRestStorageService;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public class SpectraSession extends S3Session {
    private static final Logger log = Logger.getLogger(SpectraSession.class);

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, trust, key, proxy);
    }

    @Override
    public RequestEntityRestStorageService connect(final HostKeyCallback key) throws BackgroundException {
        this.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        return super.connect(key);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            // List all buckets
            return super.list(directory, listener);
        }
        else {
            return new SpectraObjectListService(this).list(directory, listener);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Bulk.class) {
            return (T) new SpectraBulkService(this);
        }
        return super.getFeature(type);
    }
}
