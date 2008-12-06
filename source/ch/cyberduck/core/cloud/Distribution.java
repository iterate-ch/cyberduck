package ch.cyberduck.core.cloud;

import com.apple.cocoa.foundation.NSBundle;
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

/**
 * @version $Id$
 */
public class Distribution {

    private boolean enabled;
    private String url;
    private String status;
    private String cnames[];

    /**
     * @param enabled
     * @param origin
     * @param url
     * @param status
     */
    public Distribution(boolean enabled, String url, String status) {
        this(enabled, url, status, new String[]{});
    }

    /**
     * @param enabled
     * @param origin
     * @param url
     * @param status
     * @param cnames
     */
    public Distribution(boolean enabled, String url, String status, String[] cnames) {
        this.enabled = enabled;
        this.url = url;
        this.status = status;
        this.cnames = cnames;
    }

    /**
     *
     * @return True if distribution is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @return Null if not available
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @return "Unknown" if distribution status is not known
     */
    public String getStatus() {
        if(null == status) {
            return NSBundle.localizedString("Unknown", "");
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
