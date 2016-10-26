package ch.cyberduck.core.worker;

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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.text.MessageFormat;
import java.util.List;

public abstract class CalculateSizeWorker extends Worker<Long> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final ProgressListener listener;

    protected CalculateSizeWorker(final List<Path> files, final ProgressListener listener) {
        this.files = files;
        this.listener = listener;
    }

    private Long total = 0L;

    @Override
    public Long run(final Session<?> session) throws BackgroundException {
        for(Path next : files) {
            next.attributes().setSize(this.calculateSize(session, next));
        }
        return total;
    }

    /**
     * Calculates recursively the size of this path if a directory
     * Potentially lengthy operation
     *
     * @param p Directory or file
     * @return The size of the file or the sum of all containing files if a directory
     */
    private long calculateSize(final Session<?> session, final Path p) throws BackgroundException {
        long size = 0;
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Getting size of {0}", "Status"),
                p.getName()));
        if(p.isDirectory()) {
            for(Path next : session.list(p, new ActionListProgressListener(this, listener))) {
                size += this.calculateSize(session, next);
            }
        }
        else if(p.isFile()) {
            size += p.attributes().getSize();
            total += size;
            this.update(total);
        }
        return size;
    }

    /**
     * Incremental update with latest size value.
     *
     * @param size Current known size
     */
    protected abstract void update(long size);

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Getting size of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public Long initialize() {
        return total;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final CalculateSizeWorker that = (CalculateSizeWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CalculateSizeWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
