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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class WriteMetadataWorker extends Worker<Void> {
    private static Logger log = Logger.getLogger(WriteMetadataWorker.class);

    private Session<?> session;

    private Headers feature;

    /**
     * Selected files.
     */
    private List<Path> files;

    /**
     * The updated metadata to apply
     */
    private Map<String, String> metadata;

    protected WriteMetadataWorker(final Session session, final Headers feature,
                                  final List<Path> files, final Map<String, String> metadata) {
        this.session = session;
        this.feature = feature;
        this.files = files;
        this.metadata = metadata;
    }

    @Override
    public Void run() throws BackgroundException {
        for(Path file : files) {
            if(!metadata.equals(file.attributes().getMetadata())) {
                session.message(MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                        file.getName()));
                for(Map.Entry<String, String> entry : metadata.entrySet()) {
                    // Prune metadata from entries which are unique to a single file. For example md5-hash.
                    if(StringUtils.isBlank(entry.getValue())) {
                        // Reset with previous value
                        metadata.put(entry.getKey(), file.attributes().getMetadata().get(entry.getKey()));
                    }
                }
                feature.setMetadata(file, metadata);
                file.attributes().setMetadata(metadata);
            }
        }
        return null;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                this.toString(files));
    }
}