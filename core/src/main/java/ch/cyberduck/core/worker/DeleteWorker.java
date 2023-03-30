package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeleteWorker extends Worker<List<Path>> {

    private static final Logger log = LogManager.getLogger(DeleteWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;
    private final LoginCallback prompt;
    private final ProgressListener listener;
    private final Filter<Path> filter;
    /**
     * Trash instead of delete files if feature is available
     */
    private final boolean trash;
    private final Delete.Callback callback;

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener) {
        this(prompt, files, listener, new NullFilter<>());
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener,
                        final Filter<Path> filter) {
        this(prompt, files, listener, filter, PreferencesFactory.get().getBoolean("browser.delete.trash"));
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener,
                        final boolean trash) {
        this(prompt, files, listener, new NullFilter<>(), trash);
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener,
                        final Filter<Path> filter, final boolean trash) {
        this(prompt, files, listener, filter, trash, new Delete.DisabledCallback());
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener,
                        final Filter<Path> filter, final boolean trash, final Delete.Callback callback) {
        this.files = files;
        this.prompt = prompt;
        this.listener = listener;
        this.filter = filter;
        this.trash = trash;
        this.callback = callback;
    }

    @Override
    public List<Path> run(final Session<?> session) throws BackgroundException {
        final Delete delete;
        if(trash) {
            if(null == session.getFeature(Trash.class)) {
                log.warn(String.format("No trash feature available for %s", session));
                delete = session.getFeature(Delete.class);
            }
            else {
                delete = session.getFeature(Trash.class);
            }
        }
        else {
            delete = session.getFeature(Delete.class);
        }
        final ListService list = session.getFeature(ListService.class);
        final Map<Path, TransferStatus> recursive = new LinkedHashMap<>();
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            recursive.putAll(this.compile(delete, list, new WorkerListProgressListener(this, listener), file));
        }
        // Iterate again to delete any files that can be omitted when recursive operation is supported
        if(delete.isRecursive()) {
            recursive.keySet().removeIf(f -> recursive.keySet().stream().anyMatch(f::isChild));
        }
        if(new UploadFilterOptions(session.getHost()).versioning) {
            final Versioning versioning = session.getFeature(Versioning.class);
            for(Iterator<Path> iter = recursive.keySet().iterator(); iter.hasNext(); ) {
                final Path f = iter.next();
                if(versioning.save(f)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Skip deleting %s", f));
                    }
                    iter.remove();
                }
            }
        }
        if(!recursive.isEmpty()) {
            delete.delete(recursive, prompt, new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"), file.getName()));
                    callback.delete(file);
                    if(file.isDirectory()) {
                        if(delete.isRecursive()) {
                            files.stream().filter(f -> f.isChild(file)).forEach(callback::delete);
                        }
                    }
                }
            });
        }
        return new ArrayList<>(recursive.keySet());
    }

    protected Map<Path, TransferStatus> compile(final Delete delete, final ListService list, final ListProgressListener listener, final Path file) throws BackgroundException {
        // Compile recursive list
        final Map<Path, TransferStatus> recursive = new LinkedHashMap<>();
        if(file.isFile() || file.isSymbolicLink()) {
            if(null != file.attributes().getVersionId()) {
                if(file.attributes().isDuplicate()) {
                    // Delete previous versions or pending upload
                    log.warn(String.format("Delete version %s", file));
                }
                else {
                    if(file.getType().contains(Path.Type.upload)) {
                        log.warn(String.format("Delete pending upload %s", file));
                    }
                    else {
                        // Add delete marker
                        log.warn(String.format("Nullify version to add delete marker for %s", file));
                        file.attributes().setVersionId(null);
                    }
                }
            }
            recursive.put(file, new TransferStatus().withLockId(this.getLockId(file)));
        }
        else if(file.isDirectory()) {
            if(!delete.isRecursive()) {
                for(Path child : list.list(file, listener).filter(filter)) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.putAll(this.compile(delete, list, listener, child));
                }
            }
            // Add parent after children
            recursive.put(file, new TransferStatus().withLockId(this.getLockId(file)));
        }
        return recursive;
    }

    protected String getLockId(final Path file) {
        return null;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                this.toString(files));
    }

    @Override
    public List<Path> initialize() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
