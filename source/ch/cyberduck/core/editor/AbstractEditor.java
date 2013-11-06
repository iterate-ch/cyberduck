package ch.cyberduck.core.editor;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.ui.action.Worker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public abstract class AbstractEditor implements Editor {
    private static final Logger log = Logger.getLogger(AbstractEditor.class);

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    private boolean closed;

    /**
     * File has changed but not uploaded yet
     */
    private boolean modified;

    /**
     * The edited path
     */
    protected Path edited;

    protected Local local;

    /**
     * The editor application
     */
    private Application application;

    /**
     * Store checksum of downloaded file to detect modifications
     */
    protected String checksum
            = StringUtils.EMPTY;

    /**
     * Session for transfers
     */
    protected Session<?> session;

    public AbstractEditor(final Application application, final Session session, final Path file) {
        this.application = application;
        this.edited = file;
        this.local = TemporaryFileServiceFactory.get().create(session.getHost().getUuid(), edited);
        this.edited.setLocal(local);
        this.session = session;
    }

    /**
     * @param background Download transfer
     */
    protected abstract void open(Worker background);

    /**
     * @param background Upload transfer
     */
    protected abstract void save(Worker background);

    public Path getEdited() {
        return edited;
    }

    public Application getApplication() {
        return application;
    }

    protected void setClosed(boolean closed) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set deferred delete flag for %s", local.getAbsolute()));
        }
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    protected void delete() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Delete edited file %s", local.getAbsolute()));
        }
        local.trash();
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public void open() {
        final Worker worker = new EditBackgroundAction(this);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Download file for edit %s", local.getAbsolute()));
        }
        this.open(worker);
    }

    /**
     * Watch for changes in external editor
     */
    protected abstract void edit() throws IOException;

    /**
     * Upload changes to server if checksum of local file has changed since last edit.
     */
    @Override
    public void save() {
        // If checksum still the same no need for save
        final String current = local.attributes().getChecksum();
        if(null == current) {
            log.warn(String.format("Ignore save with unknown checksum for %s", local));
            return;
        }
        if(checksum.equals(current)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("File %s not modified with checkum %s", local, current));
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Save new checksum %s for file %s", current, local));
            }
            // Store current checksum
            checksum = current;
            final Worker worker = new SaveBackgroundAction(this);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Upload changes for %s", local.getAbsolute()));
            }
            this.save(worker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractEditor that = (AbstractEditor) o;
        if(application != null ? !application.equals(that.application) : that.application != null) {
            return false;
        }
        if(local != null ? !local.equals(that.local) : that.local != null) {
            return false;
        }
        if(session != null ? !session.equals(that.session) : that.session != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = local != null ? local.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractEditor{");
        sb.append("application=").append(application);
        sb.append(", local=").append(local);
        sb.append('}');
        return sb.toString();
    }
}