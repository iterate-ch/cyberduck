package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.RegistryBackgroundAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchiveController extends ProxyController {

    private BrowserController parent;

    public ArchiveController(final BrowserController parent) {
        this.parent = parent;
    }

    public void archive(final Archive format, final List<Path> selected) {
        new OverwriteController(parent).overwrite(Collections.singletonList(format.getArchive(selected)), new DefaultMainAction() {
            @Override
            public void run() {
                parent.background(new RegistryBackgroundAction<Boolean>(parent, parent.getSession(), parent.getCache()) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        final Compress feature = parent.getSession().getFeature(Compress.class);
                        feature.archive(format, parent.workdir(), selected, this, parent);
                        return true;
                    }

                    @Override
                    public void cleanup() {
                        super.cleanup();
                        // Update Selection
                        parent.reload(parent.workdir(), selected, Collections.singletonList(format.getArchive(selected)));
                    }

                    @Override
                    public String getActivity() {
                        return format.getCompressCommand(parent.workdir(), selected);
                    }
                });
            }
        });

    }

    public void unarchive(final List<Path> selected) {
        final List<Path> expanded = new ArrayList<Path>();
        for(final Path s : selected) {
            final Archive archive = Archive.forName(s.getName());
            if(null == archive) {
                continue;
            }
            new OverwriteController(parent).overwrite(archive.getExpanded(Collections.singletonList(s)), new DefaultMainAction() {
                @Override
                public void run() {
                    parent.background(new RegistryBackgroundAction<Boolean>(parent, parent.getSession(), parent.getCache()) {
                        @Override
                        public Boolean run() throws BackgroundException {
                            final Compress feature = parent.getSession().getFeature(Compress.class);
                            feature.unarchive(archive, s, this, parent);
                            return true;
                        }

                        @Override
                        public void cleanup() {
                            super.cleanup();
                            expanded.addAll(archive.getExpanded(Collections.singletonList(s)));
                            // Update Selection
                            parent.reload(parent.workdir(), selected, expanded);
                        }

                        @Override
                        public String getActivity() {
                            return archive.getDecompressCommand(s);
                        }
                    });
                }
            });
        }

    }
}
