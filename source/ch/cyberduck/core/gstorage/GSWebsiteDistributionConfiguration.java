package ch.cyberduck.core.gstorage;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.GSWebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id:$
 */
public class GSWebsiteDistributionConfiguration implements DistributionConfiguration {

    private GSSession session;

    public GSWebsiteDistributionConfiguration(final GSSession session) {
        this.session = session;
    }

    /**
     * Distribution methods supported by this S3 provider.
     *
     * @param container Bucket
     * @return Download and Streaming for AWS.
     */
    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.WEBSITE);
    }

    @Override
    public String getName(Distribution.Method method) {
        return method.toString();
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
            final WebsiteConfig configuration = session.getClient().getWebsiteConfigImpl(container.getName());
            return new Distribution(
                    null,
                    container.getName(),
                    method,
                    configuration.isWebsiteConfigActive(),
                    configuration.isWebsiteConfigActive(),
                    // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                    String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()),
                    Locale.localizedString("Deployed", "S3"),
                    new String[]{},
                    false,
                    configuration.getIndexDocumentSuffix());
        }
        catch(ServiceException e) {
            // Not found. Website configuration not enbabled.
            String status = Locale.localizedString(e.getErrorCode());
            if(status.equals(e.getErrorCode())) {
                // No localization found. Use english text
                status = e.getErrorMessage();
            }
            return new Distribution(
                    null,
                    container.getName(),
                    method,
                    false, //Disabled
                    String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()),
                    status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read website configuration", e, session.getHost());
        }
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        //
    }

    @Override
    public boolean isInvalidationSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public String getName() {
        return Locale.localizedString("Website Configuration", "S3");
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        try {
            session.check();
            // Configure Website Index Document
            StringBuilder name = new StringBuilder(Locale.localizedString("Website", "S3")).append(" ").append(method.toString());
            if(enabled) {
                session.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), name));
            }
            else {
                session.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), name));
            }
            if(enabled) {
                String suffix = "index.html";
                if(StringUtils.isNotBlank(defaultRootObject)) {
                    suffix = FilenameUtils.getName(defaultRootObject);
                }
                // Enable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig(suffix));
            }
            else {
                // Disable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig());
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot write website configuration", e, session.getHost());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write website configuration", e, session.getHost());
        }
    }

    @Override
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public boolean isCnameSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public boolean isAnalyticsSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.GOOGLESTORAGE_SSL;
    }
}
