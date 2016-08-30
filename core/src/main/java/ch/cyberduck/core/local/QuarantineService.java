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

public interface QuarantineService {
    /**
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    void setQuarantine(Local file, String originUrl, String dataUrl) throws LocalAccessDeniedException;

    /**
     * @param dataUrl Href where the file was downloaded from
     */
    void setWhereFrom(Local file, String dataUrl) throws LocalAccessDeniedException;
}
