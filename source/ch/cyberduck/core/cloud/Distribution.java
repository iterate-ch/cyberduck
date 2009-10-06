package ch.cyberduck.core.cloud;

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

import ch.cyberduck.core.i18n.Locale;

/**
 * @version $Id$
 */
public class Distribution {

    /**
     * Configuration pending
     */
    private boolean inprogress;
    /**
     * Deployment enabled
     */
    private boolean enabled;
    /**
     * Logging enabled
     */
    private boolean logging;
    private String url;
    private String status;
    private String cnames[];

    /**
     * @param enabled
     * @param url
     * @param status
     */
    public Distribution(boolean enabled, String url, String status) {
        this(enabled, url, status, new String[]{});
    }

    /**
     *
     * @param enabled
     * @param url
     * @param status
     * @param logging
     */
    public Distribution(boolean enabled, String url, String status, boolean logging) {
        this(enabled, false, url, status, new String[]{}, logging);
    }

    /**
     * @param enabled    Deployment Enabled
     * @param url        Where to find this distribution
     * @param status     Status Message about Deployment Status
     * @param cnames     Multiple CNAME aliases of this distribution
     */
    public Distribution(boolean enabled, String url, String status, String[] cnames) {
        this(enabled, false, url, status, cnames);
    }

    /**
     * @param enabled    Deployment Enabled
     * @param inprogress Deployment Status is about to be changed
     * @param url        Where to find this distribution
     * @param status     Status Message about Deployment Status
     * @param cnames     Multiple CNAME aliases of this distribution
     */
    public Distribution(boolean enabled, boolean inprogress, String url, String status, String[] cnames) {
        this(enabled, inprogress, url, status, cnames, false);
    }

    /**
     * @param enabled    Deployment Enabled
     * @param inprogress Deployment Status is about to be changed
     * @param url        Where to find this distribution
     * @param status     Status Message about Deployment Status
     * @param cnames     Multiple CNAME aliases of this distribution
     * @param logging
     */
    public Distribution(boolean enabled, boolean inprogress, String url, String status, String[] cnames, boolean logging) {
        this.enabled = enabled;
        this.inprogress = inprogress;
        this.url = url;
        this.status = status;
        this.cnames = cnames;
        this.logging = logging;
    }

    /**
     * @return True if distribution is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInprogress() {
        return inprogress;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    /**
     * @return Null if not available
     */
    public String getUrl() {
        return url;
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

    /**
     * @return Empty array if no CNAMEs configured for this distribution
     */
    public String[] getCNAMEs() {
        if(null == cnames) {
            return new String[]{};
        }
        return cnames;
    }
}
