package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
public class CustomOriginCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {
    private static Logger log = Logger.getLogger(CustomOriginCloudFrontDistributionConfiguration.class);

    private LoginController prompt;
    private S3Session session;

    public CustomOriginCloudFrontDistributionConfiguration(final S3Session session, final LoginController prompt) {
        super(session);
        this.session = session;
        this.prompt = prompt;
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.CUSTOM);
    }

    @Override
    public String getOrigin(final Path container, final Distribution.Method method) {
        final Host origin = container.getSession().getHost();
        try {
            return new URI(origin.getWebURL()).getHost();
        }
        catch(URISyntaxException e) {
            log.error(String.format("Failure parsing URI %s", origin.getWebURL()), e);
        }
        return origin.getHostname(true);
    }

    private <T> T authenticated(final Callable<T> run) throws BackgroundException {
        try {
            prompt.check(session.getHost(), this.getName(), null, true, false, false);
            final T call = run.call();
            prompt.success(session.getHost());
            return call;
        }
        catch(LoginFailureException failure) {
            prompt.fail(session.getHost().getProtocol(), session.getHost().getCredentials(), failure.getMessage());
            return this.authenticated(run);
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            throw new BackgroundException(e);
        }
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        return this.authenticated(new Callable<Distribution>() {
            @Override
            public Distribution call() throws BackgroundException {
                return CustomOriginCloudFrontDistributionConfiguration.super.read(container, method);
            }
        });
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method, final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        this.authenticated(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                CustomOriginCloudFrontDistributionConfiguration.super.write(container, enabled, method, cnames, logging, loggingBucket, defaultRootObject);
                return null;
            }
        });
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        this.authenticated(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                CustomOriginCloudFrontDistributionConfiguration.super.invalidate(container, method, files, recursive);
                return null;
            }
        });
    }
}