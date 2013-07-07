package ch.cyberduck.ui.action;

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

import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class DeleteWorker extends Worker<Boolean> {

    private Session<?> session;

    /**
     * Selected files.
     */
    private List<Path> files;

    private LoginController prompt;

    public DeleteWorker(final Session session, final LoginController prompt, final List<Path> files) {
        this.session = session;
        this.prompt = prompt;
        this.files = files;
    }

    @Override
    public Boolean run() throws BackgroundException {
        for(Path file : files) {
            session.delete(file, prompt);
        }
        return true;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"), StringUtils.EMPTY);
    }
}
