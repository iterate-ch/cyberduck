package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathReference;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class TransferPromptFilterWorker extends Worker<Map<Path, TransferStatus>> {
    private static final Logger log = Logger.getLogger(TransferPromptFilterWorker.class);

    private Transfer transfer;

    private Session session;

    private TransferAction action;

    private Cache cache;

    public TransferPromptFilterWorker(final Session session, final Transfer transfer, final TransferAction action,
                                      final Cache cache) {
        this.session = session;
        this.cache = cache;
        this.action = action;
        this.transfer = transfer;
    }

    @Override
    public Map<Path, TransferStatus> run() throws BackgroundException {
        final Map<Path, TransferStatus> status = new HashMap<Path, TransferStatus>();
        for(Path file : transfer.getRoots()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            status.put(file.getParent(), new TransferStatus().exists(true));
        }
        final TransferPathFilter filter = transfer.filter(session, action);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter cache %s with filter %s", cache, filter));
        }
        for(PathReference key : cache.keySet()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            final AttributedList<Path> list = cache.get(key);
            for(Path file : list) {
                if(this.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                if(filter.accept(file, status.get(file.getParent()))) {
                    status.put(file, filter.prepare(file, status.get(file.getParent())));
                }
            }
        }
        return status;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Apply {0} filter", "Status"), StringUtils.uncapitalize(action.getTitle()));
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransferPromptFilterWorker that = (TransferPromptFilterWorker) o;
        if(cache != null ? !cache.equals(that.cache) : that.cache != null) {
            return false;
        }
        if(transfer != null ? !transfer.equals(that.transfer) : that.transfer != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = transfer != null ? transfer.hashCode() : 0;
        result = 31 * result + (cache != null ? cache.hashCode() : 0);
        return result;
    }
}
