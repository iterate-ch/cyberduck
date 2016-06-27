package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.library.Native;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public final class LaunchServicesQuarantineService implements QuarantineService {
    private static final Logger log = Logger.getLogger(LaunchServicesQuarantineService.class);

    static {
        Native.load("core");
    }

    private static final Object lock = new Object();

    /**
     * @param originUrl The URL of the resource originally hosting the quarantined item, from the user's point of
     *                  view. The origin URL should not be set to the data URL, or the quarantining application may start
     *                  downloading the file again if the user chooses to view the origin URL while resolving a quarantine
     *                  warning.
     * @param dataUrl   The URL from which the data for the quarantined item data was
     *                  actaully streamed or downloaded, if available
     */
    @Override
    public void setQuarantine(final Local file, final String originUrl, final String dataUrl) throws LocalAccessDeniedException {
        if(StringUtils.isEmpty(originUrl)) {
            log.warn("No origin url given for quarantine");
            return;
        }
        if(StringUtils.isEmpty(dataUrl)) {
            log.warn("No data url given for quarantine");
            return;
        }
        synchronized(lock) {
            if(!this.setQuarantine(file.getAbsolute(), originUrl, dataUrl)) {
                throw new LocalAccessDeniedException(file.getAbsolute());
            }
        }
    }

    /**
     * UKXattrMetadataStore
     *
     * @param path      Absolute path reference
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    private native boolean setQuarantine(String path, String originUrl, String dataUrl);

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param dataUrl Href where the file was downloaded from
     */
    @Override
    public void setWhereFrom(final Local file, final String dataUrl) throws LocalAccessDeniedException {
        synchronized(lock) {
            if(StringUtils.isBlank(dataUrl)) {
                log.warn("No data url given");
                return;
            }
            if(!this.setWhereFrom(file.getAbsolute(), dataUrl)) {
                throw new LocalAccessDeniedException(file.getAbsolute());
            }
        }
    }

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param path    Absolute path reference
     * @param dataUrl Href where the file was downloaded from
     */
    private native boolean setWhereFrom(String path, String dataUrl);
}
