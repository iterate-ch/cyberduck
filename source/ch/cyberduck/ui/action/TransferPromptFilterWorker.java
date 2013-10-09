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
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class TransferPromptFilterWorker extends Worker<Map<Path, TransferStatus>> {

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
            status.put(file.getParent(), new TransferStatus().exists(true));
        }
        final TransferPathFilter filter = transfer.filter(session, action);
        for(PathReference key : cache.keySet()) {
            final AttributedList<Path> list = cache.get(key);
            for(Path file : list) {
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
}
