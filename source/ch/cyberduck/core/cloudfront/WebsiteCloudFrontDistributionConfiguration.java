package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3WebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.model.cloudfront.CustomOrigin;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class WebsiteCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {

    private S3Session session;

    public WebsiteCloudFrontDistributionConfiguration(final S3Session session) {
        super(session);
        this.session = session;
    }

    /**
     * Distribution methods supported by this S3 provider.
     *
     * @return Download and Streaming for AWS.
     */
    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        if(!ServiceUtils.isBucketNameValidDNSName(container.getName())) {
            // Disable website configuration if bucket name is not DNS compatible
            return super.getMethods(container);
        }
        final List<Distribution.Method> methods = new ArrayList<Distribution.Method>(super.getMethods(container));
        methods.addAll(Arrays.asList(Distribution.WEBSITE, Distribution.WEBSITE_CDN));
        return methods;
    }

    @Override
    public String getName(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE)) {
            return method.toString();
        }
        return super.getName(method);
    }

    @Override
    public String getOrigin(final Path container, final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE_CDN)) {
            return this.getWebsiteHostname(container);
        }
        return super.getOrigin(container, method);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        if(method.equals(Distribution.WEBSITE)) {
            try {
                final WebsiteConfig configuration = session.getClient().getWebsiteConfig(container.getName());
                final Distribution distribution = new Distribution(
                        null,
                        this.getOrigin(container, method),
                        method,
                        configuration.isWebsiteConfigActive(),
                        configuration.isWebsiteConfigActive(),
                        // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                        String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container)),
                        Locale.localizedString("Deployed", "S3"),
                        new String[]{},
                        false,
                        configuration.getIndexDocumentSuffix());
                distribution.setContainers(session.getContainers(false));
                return distribution;
            }
            catch(ServiceException e) {
                // Not found. Website configuration not enbabled.
                String status = Locale.localizedString(e.getErrorCode());
                if(status.equals(e.getErrorCode())) {
                    // No localization found. Use english text
                    status = e.getErrorMessage();
                }
                return new Distribution(null, this.getOrigin(container, method), method, false,
                        String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container)), status);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot read website configuration", e, session.getHost());
            }
        }
        else {
            return super.read(container, method);
        }
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        if(method.equals(Distribution.WEBSITE)) {
            try {
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
                    session.getClient().setWebsiteConfig(container.getName(), new S3WebsiteConfig(suffix));
                }
                else {
                    // Disable website endpoint
                    session.getClient().deleteWebsiteConfig(container.getName());
                }
            }
            catch(S3ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot write website configuration", e, session.getHost());
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot write website configuration", e, session.getHost());
            }
        }
        else {
            super.write(container, enabled, method, cnames, logging, loggingBucket, defaultRootObject);
        }
    }

    @Override
    protected CustomOrigin.OriginProtocolPolicy getPolicy(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE_CDN)) {
            return CustomOrigin.OriginProtocolPolicy.HTTP_ONLY;
        }
        return super.getPolicy(method);
    }

    @Override
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE)) {
            return true;
        }
        return super.isDefaultRootSupported(method);
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE)) {
            return false;
        }
        return super.isLoggingSupported(method);
    }

    /**
     * The website endpoint given the location of the bucket. When you configure a bucket as
     * a website, the website is available via the region-specific website endpoint.
     * The website endpoint you use must be in the same region that your bucket resides.
     * These website endpoints are different than the REST API endpoints (see Request
     * Endpoints). Amazon S3 supports the following website endpoint.
     *
     * @param bucket Bucket name
     * @return Website distribution hostname
     */
    protected String getWebsiteHostname(final Path bucket) {
        // Geographical location
        final String location = bucket.attributes().getRegion();
        // US Standard
        final String endpoint;
        if(null == location || "US".equals(location)) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_EUROPE.equals(location)) {
            endpoint = "s3-website-eu-west-1.amazonaws.com";
        }
        else {
            endpoint = String.format("s3-website-%s.amazonaws.com", location);
        }
        return String.format("%s.%s", bucket.getName(), endpoint);
    }
}
