package ch.cyberduck.core.cdn;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Distribution {

    /**
     * Unique identifier
     */
    private String id;

    /**
     * For If-Match when updating configuration
     */
    private String etag;

    /**
     * Caller reference
     */
    private String reference;

    /**
     * Configuration successfully applied and distributed
     */
    private boolean deployed;

    /**
     * S3 bucket name or DNS
     */
    private URI origin;

    /**
     * Deployment enabled
     */
    private boolean enabled;

    /**
     * Logging enabled
     */
    private boolean logging;

    /**
     * Logging target container
     */
    private String loggingContainer;

    /**
     * Logging target containers
     */
    private List<Path> containers = Collections.emptyList();

    /**
     * CDN URL
     */
    private URI url;

    /**
     * CDN SSL URL
     */
    private URI sslUrl;

    /**
     * X-CDN-Streaming-URI
     */
    private URI streamingUrl;

    /**
     * X-Cdn-Ios-Uri
     */
    private URI iOSstreamingUrl;

    /**
     * Deployment status description
     */
    private String status;

    /**
     * CNAME DNS entires to the CDN hostname
     */
    private String cnames[];

    /**
     * Kind of distribution
     */
    private Method method;

    /**
     * Key of the default root object or index document
     */
    private String indexDocument;

    /**
     * Custom Error Document Support. Amazon S3 returns your custom error document
     * for only the HTTP 4XX class of error codes.
     */
    private String errorDocument;

    /**
     * Edge purge status message
     */
    private String invalidationStatus;

    /**
     * Protocol and context of distribution.
     */
    public static abstract class Method {
        public abstract String toString();

        public abstract Scheme getScheme();

        public int getDefaultPort() {
            return this.getScheme().getPort();
        }

        public abstract String getContext();

        public static Method forName(final String name) {
            if(DOWNLOAD.toString().equals(name)) {
                return DOWNLOAD;
            }
            if(STREAMING.toString().equals(name)) {
                return STREAMING;
            }
            if(CUSTOM.toString().equals(name)) {
                return CUSTOM;
            }
            if(WEBSITE.toString().equals(name)) {
                return WEBSITE;
            }
            if(WEBSITE_CDN.toString().equals(name)) {
                return WEBSITE_CDN;
            }
            throw new FactoryException(name);
        }
    }

    /**
     * Website endpoint for S3
     */
    public static final Method WEBSITE = new Method() {
        public String toString() {
            return LocaleFactory.localizedString("Website Configuration (HTTP)", "S3");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.http;
        }

        @Override
        public String getContext() {
            return StringUtils.EMPTY;
        }
    };

    /**
     * Website configuration endpoint with custom origin CDN
     */
    public static final Method WEBSITE_CDN = new Method() {
        public String toString() {
            return LocaleFactory.localizedString("Website Configuration (HTTP) CDN", "S3");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.http;
        }

        @Override
        public String getContext() {
            return StringUtils.EMPTY;
        }
    };

    public static final Method DOWNLOAD = new Method() {
        public String toString() {
            return LocaleFactory.localizedString("Download (HTTP) CDN", "S3");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.http;
        }

        @Override
        public String getContext() {
            return StringUtils.EMPTY;
        }
    };

    public static final Method CUSTOM = new Method() {
        public String toString() {
            return LocaleFactory.localizedString("Custom Origin Server (HTTP/HTTPS) CDN", "S3");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.http;
        }

        @Override
        public String getContext() {
            return StringUtils.EMPTY;
        }
    };

    public static final Method STREAMING = new Method() {
        public String toString() {
            return LocaleFactory.localizedString("Streaming (RTMP) CDN", "S3");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.rtmp;
        }

        @Override
        public String getContext() {
            return "/cfx/st";
        }
    };

    /**
     * @param origin  Server
     * @param method  Kind of distribution
     * @param enabled Deployment Enabled
     */
    public Distribution(final URI origin, final Method method, final boolean enabled) {
        this.origin = origin;
        this.enabled = enabled;
        this.deployed = enabled;
        this.method = method;
    }

    public Distribution(final Method method, final boolean enabled) {
        this.enabled = enabled;
        this.deployed = enabled;
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getEtag() {
        return etag;
    }

    public String getReference() {
        return reference;
    }

    /**
     * Origin server to fetch original content. S3 bucket or custom host.
     *
     * @return DNS hostname of origin server
     */
    public URI getOrigin() {
        return origin;
    }

    /**
     * @return True if distribution is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Deployment status
     *
     * @return True if available
     */
    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(final boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * @return True if logging for distribution is enabled
     */
    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public void setLoggingContainer(final String container) {
        this.loggingContainer = container;
    }

    /**
     * @return The container where log files are stored
     */
    public String getLoggingContainer() {
        return loggingContainer;
    }

    public void setUrl(final URI url) {
        this.url = url;
    }

    /**
     * Distribution URL from CDN provider.
     *
     * @return Null if not available
     */
    public URI getUrl() {
        return url;
    }

    public void setSslUrl(final URI sslUrl) {
        this.sslUrl = sslUrl;
    }

    /**
     * Distribution HTTPS URL from CDN provider.
     *
     * @return Null if not available
     */
    public URI getSslUrl() {
        return sslUrl;
    }

    public void setStreamingUrl(final URI streamingUrl) {
        this.streamingUrl = streamingUrl;
    }

    public URI getStreamingUrl() {
        return streamingUrl;
    }

    public URI getiOSstreamingUrl() {
        return iOSstreamingUrl;
    }

    public void setiOSstreamingUrl(URI iOSstreamingUrl) {
        this.iOSstreamingUrl = iOSstreamingUrl;
    }

    /**
     * @return "Unknown" if distribution status is not known
     */
    public String getStatus() {
        if(null == status) {
            return LocaleFactory.localizedString("Unknown");
        }
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getInvalidationStatus() {
        if(null == invalidationStatus) {
            return LocaleFactory.localizedString("None");
        }
        return invalidationStatus;
    }

    public void setInvalidationStatus(final String invalidationStatus) {
        this.invalidationStatus = invalidationStatus;
    }

    public List<Path> getContainers() {
        return containers;
    }

    public void setContainers(final List<Path> containers) {
        this.containers = containers;
    }

    /**
     * Index file
     *
     * @return Null if not supported or not set
     */
    public String getIndexDocument() {
        return indexDocument;
    }

    public void setIndexDocument(final String indexDocument) {
        this.indexDocument = indexDocument;
    }

    public String getErrorDocument() {
        return errorDocument;
    }

    public void setErrorDocument(String errorDocument) {
        this.errorDocument = errorDocument;
    }

    /**
     * @return Distribution method.
     * @see ch.cyberduck.core.cdn.Distribution#CUSTOM
     * @see ch.cyberduck.core.cdn.Distribution#DOWNLOAD
     * @see ch.cyberduck.core.cdn.Distribution#STREAMING
     */
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String[] getCNAMEs() {
        if(null == cnames) {
            return new String[]{};
        }
        return cnames;
    }

    public void setCNAMEs(final String[] cnames) {
        this.cnames = cnames;
    }

    public void setEtag(final String etag) {
        this.etag = etag;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Distribution{");
        sb.append("id='").append(id).append('\'');
        sb.append(", reference='").append(reference).append('\'');
        sb.append(", origin=").append(origin);
        sb.append(", enabled=").append(enabled);
        sb.append(", logging=").append(logging);
        sb.append(", url=").append(url);
        sb.append(", status='").append(status).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final Distribution that = (Distribution) o;
        if(deployed != that.deployed) {
            return false;
        }
        if(enabled != that.enabled) {
            return false;
        }
        if(logging != that.logging) {
            return false;
        }
        if(!Arrays.equals(cnames, that.cnames)) {
            return false;
        }
        if(errorDocument != null ? !errorDocument.equals(that.errorDocument) : that.errorDocument != null) {
            return false;
        }
        if(indexDocument != null ? !indexDocument.equals(that.indexDocument) : that.indexDocument != null) {
            return false;
        }
        if(loggingContainer != null ? !loggingContainer.equals(that.loggingContainer) : that.loggingContainer != null) {
            return false;
        }
        if(method != null ? !method.equals(that.method) : that.method != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (deployed ? 1 : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (logging ? 1 : 0);
        result = 31 * result + (loggingContainer != null ? loggingContainer.hashCode() : 0);
        result = 31 * result + (cnames != null ? Arrays.hashCode(cnames) : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (indexDocument != null ? indexDocument.hashCode() : 0);
        result = 31 * result + (errorDocument != null ? errorDocument.hashCode() : 0);
        return result;
    }
}