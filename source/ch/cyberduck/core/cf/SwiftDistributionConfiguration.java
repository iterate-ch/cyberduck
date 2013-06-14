package ch.cyberduck.core.cf;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.HttpExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesContainerMetaData;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;

/**
 * @version $Id:$
 */
public class SwiftDistributionConfiguration implements DistributionConfiguration {
    private static final Logger log = Logger.getLogger(SwiftDistributionConfiguration.class);

    private CFSession session;

    public SwiftDistributionConfiguration(final CFSession session) {
        this.session = session;
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        try {
            if(enabled) {
                session.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), "CDN"));
            }
            else {
                session.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), "CDN"));
            }
            if(StringUtils.isNotBlank(defaultRootObject)) {
                session.getClient().updateContainerMetadata(session.getRegion(container),
                        container.getName(), Collections.singletonMap("X-Container-Meta-Web-Index", defaultRootObject));
            }
            try {
                session.getClient().getCDNContainerInfo(session.getRegion(container),
                        container.getName());
            }
            catch(FilesNotFoundException e) {
                // Not found.
                session.getClient().cdnEnableContainer(session.getRegion(container), container.getName());
            }
            // Toggle content distribution for the container without changing the TTL expiration
            session.getClient().cdnUpdateContainer(session.getRegion(container),
                    container.getName(), -1, enabled, logging);
        }
        catch(HttpException e) {
            throw new HttpExceptionMappingService().map("Cannot write CDN configuration", e, session.getHost());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e, session.getHost());
        }
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                    container.getName()));
            try {
                final FilesCDNContainer info = session.getClient().getCDNContainerInfo(session.getRegion(container),
                        container.getName());
                final Distribution distribution = new Distribution(info.getName(),
                        session.getRegion(container).getStorageUrl().getHost(),
                        method, info.isEnabled(), info.getCdnURL(), info.getSslURL(), info.getStreamingURL(),
                        info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"),
                        info.getRetainLogs()) {
                    @Override
                    public String getLoggingTarget() {
                        return ".CDN_ACCESS_LOGS";
                    }
                };
                final FilesContainerMetaData metadata = session.getClient().getContainerMetaData(session.getRegion(container),
                        container.getName());
                if(metadata.getMetaData().containsKey("X-Container-Meta-Web-Index")) {
                    distribution.setDefaultRootObject(metadata.getMetaData().get("X-Container-Meta-Web-Index"));
                }
                distribution.setContainers(Collections.singletonList(PathFactory.createPath(session, String.valueOf(Path.DELIMITER), ".CDN_ACCESS_LOGS",
                        Path.VOLUME_TYPE | Path.DIRECTORY_TYPE))
                );
                return distribution;
            }
            catch(FilesNotFoundException e) {
                // Not found.
                return new Distribution(null, session.getRegion(container).getStorageUrl().getHost(),
                        method, false, null, Locale.localizedString("CDN Disabled", "Mosso"));
            }
        }
        catch(HttpException e) {
            throw new HttpExceptionMappingService().map("Cannot read CDN configuration", e, session.getHost());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read CDN configuration", e, session.getHost());
        }
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    container.getName()));
            for(Path file : files) {
                if(file.isContainer()) {
                    session.getClient().purgeCDNContainer(session.getRegion(file.getContainer()),
                            container.getName(), null);
                }
                else {
                    session.getClient().purgeCDNObject(session.getRegion(file.getContainer()),
                            container.getName(), file.getKey(), null);
                }
            }
        }
        catch(HttpException e) {
            throw new HttpExceptionMappingService().map("Cannot write CDN configuration", e, session.getHost());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e, session.getHost());
        }
    }

    @Override
    public boolean isInvalidationSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD);
    }

    @Override
    public boolean isAnalyticsSupported(final Distribution.Method method) {
        return this.isLoggingSupported(method);
    }

    @Override
    public boolean isCnameSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.CLOUDFILES;
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        if(session.isCDNSupported()) {
            return Arrays.asList(Distribution.DOWNLOAD);
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return Locale.localizedString("Akamai", "Mosso");
    }

    @Override
    public String getName(final Distribution.Method method) {
        return this.getName();
    }
}
