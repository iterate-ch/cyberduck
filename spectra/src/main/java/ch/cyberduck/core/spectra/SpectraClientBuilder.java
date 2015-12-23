package ch.cyberduck.core.spectra;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Session;

import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;

import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.Ds3ClientImpl;
import com.spectralogic.ds3client.NetworkClientImpl;
import com.spectralogic.ds3client.models.Credentials;
import com.spectralogic.ds3client.networking.ConnectionDetails;

public class SpectraClientBuilder {
    public Ds3Client wrap(final S3Session session) {
        final Host host = session.getHost();
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
        }, (CloseableHttpClient) session.getClient().getHttpClient()));
    }
}
