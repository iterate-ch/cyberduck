package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class WriteMetadataWorker extends Worker<Map<String, String>> {

    /**
     * Selected files.
     */
    private List<Path> files;

    /**
     * The updated metadata to apply
     */
    private Map<String, String> metadata;

    protected WriteMetadataWorker(List<Path> files, Map<String, String> metadata) {
        this.files = files;
        this.metadata = metadata;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public Map<String, String> run() {
        for(Path next : files) {
            Map<String, String> updated = new HashMap<String, String>(metadata);
            for(String key : updated.keySet()) {
                // Prune metadata from entries which are unique to a single file.
                // For example md5-hash
                if(StringUtils.isBlank(updated.get(key))) {
                    // Reset with previous value
                    updated.put(key, next.attributes().getMetadata().get(key));
                }
            }
            ((CloudPath) next).writeMetadata(updated);
        }
        return metadata;
    }
}
