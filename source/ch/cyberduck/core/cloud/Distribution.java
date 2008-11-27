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
 * @version $Id:$
 */
public class Distribution {

    private String origin;
    private boolean enabled;
    private String url;
    private String status;
    private String cnames[];

    /**
     * @param enabled
     * @param origin
     * @param url
     * @param status
     * @param cnames
     */
    public Distribution(boolean enabled, String origin, String url, String status, String[] cnames) {
        this.enabled = enabled;
        this.origin = origin;
        this.url = url;
        this.status = status;
        this.cnames = cnames;
    }

    /**
     * @param enabled
     * @param origin
     * @param url
     * @param status
     */
    public Distribution(boolean enabled, String origin, String url, String status) {
        this.enabled = enabled;
        this.origin = origin;
        this.url = url;
        this.status = status;
    }

    public Distribution(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getOrigin() {
        if(null == origin) {
            return NSBundle.localizedString("Unknown", "");
        }
        return origin;
    }

    public String getUrl() {
        if(null == url) {
            return NSBundle.localizedString("Unknown", "");
        }
        return url;
    }

    public String getStatus() {
        if(null == status) {
            return NSBundle.localizedString("Unknown", "");
        }
        return status;
    }

    /**
     * @return May return null
     */
    public String[] getCNAMEs() {
        if(null == cnames) {
            return new String[]{};
        }
        return cnames;
    }
}
