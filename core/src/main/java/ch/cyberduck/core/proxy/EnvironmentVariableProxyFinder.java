package ch.cyberduck.core.proxy;

import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class EnvironmentVariableProxyFinder implements ProxyFinder {
    private static final Logger log = LogManager.getLogger(EnvironmentVariableProxyFinder.class);

    @Override
    public Proxy find(final String target) {
        switch(Scheme.valueOf(URI.create(target).getScheme())) {
            case ftp:
            case ftps:
            case sftp:
                final String ftp_proxy = System.getenv("ftp_proxy");
                if(StringUtils.isNotBlank(ftp_proxy)) {
                    try {
                        final URI uri = new URI(ftp_proxy);
                        return new Proxy(Proxy.Type.SOCKS, uri.getHost(), uri.getPort());
                    }
                    catch(URISyntaxException e) {
                        log.warn(String.format("Invalid URL in ftp_proxy environment variable. %s", ftp_proxy));
                    }
                }
                break;
            case http:
                final String http_proxy = System.getenv("http_proxy");
                if(StringUtils.isNotBlank(http_proxy)) {
                    try {
                        final URI uri = new URI(http_proxy);
                        return new Proxy(Proxy.Type.HTTP, uri.getHost(), uri.getPort());
                    }
                    catch(URISyntaxException e) {
                        log.warn(String.format("Invalid URL in ftp_proxy environment variable. %s", http_proxy));
                    }
                }
                break;
            case https:
                final String https_proxy = System.getenv("https_proxy");
                if(StringUtils.isNotBlank(https_proxy)) {
                    try {
                        final URI uri = new URI(https_proxy);
                        return new Proxy(Proxy.Type.HTTP, uri.getHost(), uri.getPort());
                    }
                    catch(URISyntaxException e) {
                        log.warn(String.format("Invalid URL in ftp_proxy environment variable. %s", https_proxy));
                    }
                }
                break;
        }
        return new DefaultProxyFinder().find(target);
    }
}
