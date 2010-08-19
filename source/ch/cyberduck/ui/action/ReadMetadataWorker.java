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

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id:$
 */
public abstract class ReadMetadataWorker extends Worker<Map<String, String>> {
    private static Logger log = Logger.getLogger(ReadMetadataWorker.class);

    private Map<String, Integer> count = new HashMap<String, Integer>();
    private Map<String, String> updated = new HashMap<String, String>() {
        @Override
        public String put(String key, String value) {
            int n = 0;
            if(count.containsKey(key)) {
                n = count.get(key);
            }
            count.put(key, ++n);
            return super.put(key, value);
        }
    };

    /**
     * Selected files.
     */
    private List<Path> files;

    public ReadMetadataWorker(List<Path> files) {
        this.files = files;
    }

    /**
     * @return Metadata
     */
    @Override
    public Map<String, String> run() {
        for(Path next : files) {
            // Reading HTTP headers custom metadata
            if(next.attributes().getMetadata().isEmpty()) {
                ((CloudPath) next).readMetadata();
            }
            final Map<String, String> metadata = next.attributes().getMetadata();
            for(String key : metadata.keySet()) {
                // Prune metadata from entries which are unique to a single file.
                // For example md5-hash
                if(updated.containsKey(key)) {
                    if(!metadata.get(key).equals(updated.get(key))) {
                        log.info("Nullify " + key + " from metadata because value is not equal for selected files.");
                        updated.put(key, null);
                        continue;
                    }
                }
                updated.put(key, metadata.get(key));
            }
        }
        for(String key : count.keySet()) {
            if(files.size() == count.get(key)) {
                // Every file has this metadata set.
                continue;
            }
            // Not all files selected have this metadata. Remove for editing.
            log.info("Remove " + key + " from metadata not available for all selected files.");
            updated.remove(key);
        }
        return updated;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                this.toString(files));
    }
}
