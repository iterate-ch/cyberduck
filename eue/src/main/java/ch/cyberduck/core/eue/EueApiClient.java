package ch.cyberduck.core.eue;

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.eue.io.swagger.client.ApiClient;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.JSON;
import ch.cyberduck.core.eue.io.swagger.client.Pair;
import ch.cyberduck.core.jersey.HttpComponentsProvider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EueApiClient extends ApiClient {

    static {
        Logger.getLogger("org.glassfish.jersey.client.ClientExecutorProvidersConfigurator").setLevel(java.util.logging.Level.SEVERE);
    }

    public EueApiClient(final EueSession session) {
        this.setHttpClient(ClientBuilder.newClient(new ClientConfig().register(new InputStreamProvider())
                .register(MultiPartFeature.class).register(new JSON()).register(JacksonFeature.class)
                .connectorProvider(new HttpComponentsProvider(session.getClient()))));
        final int timeout = ConnectionTimeoutFactory.get(session.getHost()).getTimeout() * 1000;
        this.setConnectTimeout(timeout);
        this.setReadTimeout(timeout);
        this.setUserAgent(new PreferencesUseragentProvider().get());
        this.setBasePath(session.getBasePath());
    }

    @Override
    protected Client buildHttpClient(final boolean debugging) {
        // No need to build default client
        return null;
    }

    @Override
    public <T> T invokeAPI(final String path, final String method, final List<Pair> queryParams, final Object body,
                           final Map<String, String> headerParams, final Map<String, Object> formParams, final String accept,
                           final String contentType, final String[] authNames, final GenericType<T> returnType) throws ApiException {
        try {
            return super.invokeAPI(path, method, queryParams, body, headerParams, formParams, accept, contentType,
                    authNames, returnType);
        }
        catch(ProcessingException e) {
            throw new ApiException(e);
        }
    }

}
