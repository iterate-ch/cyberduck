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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class Distribution {

    private String id;
    /**
     * Configuration sucessfully applied and distributed
     */
    private boolean deployed;
    /**
     * S3 bucket name or DNS
     */
    private String origin;
    /**
     * Deployment enabled
     */
    private boolean enabled;
    /**
     * Logging enabled
     */
    private boolean logging;
    /**
     * CDN URL
     */
    private String url;
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
     * Key of the default root object.
     */
    private String defaultRootObject;

    private String invalidationStatus;

    /**
     * Protocol and context of distribution.
     */
    public static interface Method {
        public abstract String toString();

        public abstract String getProtocol();

        public abstract String getContext();
    }

    public static final Method DOWNLOAD = new Method() {
        public String toString() {
            return Locale.localizedString("Download (HTTP)", "S3");
        }

        public String getProtocol() {
            return "http://";
        }

        public String getContext() {
            return "";
        }
    };

    public static final Method CUSTOM = new Method() {
        public String toString() {
            return Locale.localizedString("Custom Origin Server (HTTP/HTTPS)", "S3");
        }

        public String getProtocol() {
            return "http://";
        }

        public String getContext() {
            return "";
        }
    };

    public static final Method STREAMING = new Method() {
        public String toString() {
            return Locale.localizedString("Streaming (RTMP)", "S3");
        }

        public String getProtocol() {
            return "rtmp://";
        }

        public String getContext() {
            return "/cfx/st";
        }
    };

    /**
     * @param origin Server to fetch original content
     * @param method Protocol
     */
    public Distribution(String origin, Method method) {
        this(null, origin, method, false, false, null, null, new String[]{}, false);
    }

    /**
     * @param id      Identifier of this distribution
     * @param origin
     * @param method  Kind of distribution
     * @param enabled Deployment Enabled
     * @param url     Where to find this distribution
     * @param status  Status Message about Deployment Status
     */
    public Distribution(String id, String origin, Method method, boolean enabled, String url, String status) {
        this(id, origin, method, enabled, url, status, new String[]{});
    }

    /**
     * @param id      Identifier of this distribution
     * @param origin  Server to fetch original content
     * @param method  Kind of distribution
     * @param enabled Deployment Enabled
     * @param url     Where to find this distribution
     * @param status  Status Message about Deployment Status
     * @param logging
     */
    public Distribution(String id, String origin, Method method, boolean enabled, String url, String status, boolean logging) {
        this(id, origin, method, enabled, enabled, url, status, new String[]{}, logging);
    }

    /**
     * @param id      Identifier of this distribution
     * @param origin  Server to fetch original content
     * @param method  Kind of distribution
     * @param enabled Deployment Enabled
     * @param url     Where to find this distribution
     * @param status  Status Message about Deployment Status
     * @param cnames  Multiple CNAME aliases of this distribution
     */
    public Distribution(String id, String origin, Method method, boolean enabled, String url, String status, String[] cnames) {
        this(id, origin, method, enabled, enabled, url, status, cnames);
    }

    /**
     * @param id       Identifier of this distribution
     * @param origin   Server to fetch original content
     * @param method   Kind of distribution
     * @param enabled  Deployment Enabled
     * @param deployed Deployment Status is about to be changed
     * @param url      Where to find this distribution
     * @param status   Status Message about Deployment Status
     * @param cnames   Multiple CNAME aliases of this distribution
     */
    public Distribution(String id, String origin, Method method, boolean enabled, boolean deployed, String url, String status, String[] cnames) {
        this(id, origin, method, enabled, deployed, url, status, cnames, false);
    }

    /**
     * @param id       Identifier of this distribution
     * @param origin   Server to fetch original content
     * @param method   Kind of distribution
     * @param enabled  Deployment Enabled
     * @param deployed Deployment Status is about to be changed
     * @param url      Where to find this distribution
     * @param status   Status Message about Deployment Status
     * @param cnames   Multiple CNAME aliases of this distribution
     * @param logging  Logging status
     */
    public Distribution(String id, String origin, Method method, boolean enabled, boolean deployed, String url, String status, String[] cnames, boolean logging) {
        this(id, origin, DOWNLOAD, enabled, deployed, url, status, cnames, logging, null);
    }

    /**
     * @param id                Identifier of this distribution
     * @param origin            Server to fetch original content
     * @param method            Kind of distribution
     * @param enabled           Deployment Enabled
     * @param deployed          Deployment Status is about to be changed
     * @param url               Where to find this distribution
     * @param status            Status Message about Deployment Status
     * @param cnames            Multiple CNAME aliases of this distribution
     * @param logging           Logging status
     * @param defaultRootObject Index file
     */
    public Distribution(String id, String origin, Method method, boolean enabled, boolean deployed, String url, String status, String[] cnames, boolean logging, String defaultRootObject) {
        this.id = id;
        this.origin = origin;
        this.enabled = enabled;
        this.deployed = deployed;
        this.url = url;
        this.status = status;
        this.cnames = cnames;
        this.logging = logging;
        this.method = method;
        this.defaultRootObject = defaultRootObject;
    }

    public String getId() {
        return id;
    }

    /**
     * Origin server to fetch original content. S3 bucket or custom host.
     *
     * @return DNS hostname of origin server
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Origin server to fetch original content. S3 bucket or custom host.
     *
     * @param file
     * @return Origin URL of specific file.
     */
    public String getOrigin(Path file) {
        StringBuilder url = new StringBuilder().append("http://").append(this.getOrigin());
        if(!file.isContainer()) {
            url.append(Path.DELIMITER).append(Path.encode(file.getKey()));
        }
        return URI.create(url.toString()).normalize().toString();
    }

    /**
     * @return True if distribution is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Deployment status
     *
     * @return True if available
     */
    public boolean isDeployed() {
        return deployed;
    }

    /**
     * @return
     */
    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    /**
     * Distribution URL from CDN provider.
     *
     * @return Null if not available
     */
    public String getURL() {
        return url;
    }

    /**
     * @param file
     * @return
     */
    public String getURL(Path file) {
        if(StringUtils.isEmpty(this.getURL())) {
            return null;
        }
        StringBuilder b = new StringBuilder(this.getURL());
        if(StringUtils.isNotEmpty(file.getKey())) {
            b.append(Path.encode(file.getKey()));
        }
        return URI.create(b.toString()).normalize().toString();
    }

    /**
     * Both CNAME and original URL
     *
     * @param file
     * @return
     */
    public List<AbstractPath.DescriptiveUrl> getURLs(Path file) {
        List<AbstractPath.DescriptiveUrl> urls = this.getCnameURL(file);
        urls.add(new AbstractPath.DescriptiveUrl(this.getURL(file),
                MessageFormat.format(Locale.localizedString("{0} CDN URL"), Locale.localizedString(method.toString(), "S3"))));
        return urls;
    }

    /**
     * @param file
     * @return
     */
    public List<AbstractPath.DescriptiveUrl> getCnameURL(Path file) {
        List<AbstractPath.DescriptiveUrl> urls = new ArrayList<AbstractPath.DescriptiveUrl>();
        for(String cname : cnames) {
            urls.add(new AbstractPath.DescriptiveUrl(this.getCnameURL(cname, file),
                    MessageFormat.format(Locale.localizedString("{0} CDN URL"), Locale.localizedString(method.toString(), "S3"))));
        }
        return urls;
    }

    private String getCnameURL(String cname, Path file) {
        StringBuilder b = new StringBuilder();
        b.append(this.getMethod().getProtocol()).append(cname).append(this.getMethod().getContext());
        if(StringUtils.isNotEmpty(file.getKey())) {
            b.append(Path.encode(file.getKey()));
        }
        return URI.create(b.toString()).normalize().toString();
    }

    /**
     * @return "Unknown" if distribution status is not known
     */
    public String getStatus() {
        if(null == status) {
            return Locale.localizedString("Unknown");
        }
        return status;
    }

    public String getInvalidationStatus() {
        if(null == invalidationStatus) {
            return Locale.localizedString("None");
        }
        return invalidationStatus;
    }

    public void setInvalidationStatus(String invalidationStatus) {
        this.invalidationStatus = invalidationStatus;
    }

    /**
     * @return Empty array if no CNAMEs configured for this distribution
     */
    public String[] getCNAMEs() {
        if(null == cnames) {
            return new String[]{};
        }
        return cnames;
    }

    /**
     * Index file
     *
     * @return Null if not supported or not set
     */
    public String getDefaultRootObject() {
        return defaultRootObject;
    }

    public void setDefaultRootObject(String defaultRootObject) {
        this.defaultRootObject = defaultRootObject;
    }

    /**
     * Distribution method.
     *
     * @return
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

    @Override
    public String toString() {
        return this.getId();
    }
}
