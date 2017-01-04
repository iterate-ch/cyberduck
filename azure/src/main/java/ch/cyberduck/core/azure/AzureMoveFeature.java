package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.util.Collections;

import com.microsoft.azure.storage.OperationContext;

public class AzureMoveFeature implements Move {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureMoveFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public boolean isSupported(final Path file) {
        return !containerService.isContainer(file);
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        if(file.isFile() || file.isPlaceholder()) {
            new AzureCopyFeature(session, context).copy(file, renamed);
            final Delete delete = session.getFeature(Delete.class);
            delete.delete(Collections.singletonList(file),
                    new DisabledLoginCallback(), callback);
        }
        else if(file.isDirectory()) {
            for(Path i : session.getFeature(ListService.class).list(file, new DisabledListProgressListener())) {
                this.move(i, new Path(renamed, i.getName(), i.getType()), false, callback);
            }
        }
    }
}
