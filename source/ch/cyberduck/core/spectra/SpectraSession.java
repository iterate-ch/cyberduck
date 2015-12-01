package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.s3.S3HomeFinderService;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.security.SignatureException;
import java.util.EnumSet;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientImpl;
import com.spectralogic.ds3client.NetworkClientImpl;
import com.spectralogic.ds3client.commands.GetServiceRequest;
import com.spectralogic.ds3client.commands.GetServiceResponse;
import com.spectralogic.ds3client.models.Bucket;
import com.spectralogic.ds3client.models.Credentials;
import com.spectralogic.ds3client.networking.ConnectionDetails;
import com.spectralogic.ds3client.networking.FailedRequestException;

/**
 * @version $Id:$
 */
public class SpectraSession extends HttpSession<Ds3Client> {
    private static final Logger log = Logger.getLogger(SpectraSession.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public SpectraSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, trust, key, proxy);
    }

    @Override
    protected Ds3Client connect(final HostKeyCallback key) throws BackgroundException {
        return new Ds3ClientImpl(new NetworkClientImpl(new ConnectionDetails() {
            @Override
            public String getEndpoint() {
                return String.format("%s:%d", host.getHostname(), host.getPort());
            }

            @Override
            public Credentials getCredentials() {
                return new Credentials(host.getCredentials().getUsername(), host.getCredentials().getPassword());
            }

            @Override
            public boolean isHttps() {
                return host.getProtocol().getScheme() == Scheme.https;
            }

            @Override
            public URI getProxy() {
                return null;
            }

            @Override
            public int getRetries() {
                return 0;
            }

            @Override
            public int getBufferSize() {
                return PreferencesFactory.get().getInteger("connection.chunksize");
            }

            @Override
            public boolean isCertificateVerification() {
                return true;
            }
        }, builder.build(this).build()));
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        final Path home = new S3HomeFinderService(this).find();
        cache.put(home, this.list(home, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, final AttributedList<Path> list) throws ListCanceledException {
                try {
                    cancel.verify();
                }
                catch(ConnectionCanceledException e) {
                    throw new ListCanceledException(list, e);
                }
            }
        }));
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            if(directory.isRoot()) {
                // List all buckets
                final GetServiceResponse response = client.getService(new GetServiceRequest());
                final AttributedList<Path> buckets = new AttributedList<Path>();
                if(null == response.getResult().getBuckets()) {
                    log.warn(String.format("No buckets in response %s", response));
                }
                else {
                    for(final Bucket bucket : response.getResult().getBuckets()) {
                        buckets.add(new Path(bucket.getName(), EnumSet.of(Path.Type.volume, Path.Type.directory)));
                        listener.chunk(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), buckets);
                    }
                }
                return buckets;
            }
            else {
                return new SpectraObjectListService(this).list(directory, listener);
            }
        }
        catch(FailedRequestException e) {
            throw new SpectraExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(SignatureException e) {
            throw new BackgroundException(e);
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
