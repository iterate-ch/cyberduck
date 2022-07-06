package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.CustomActions;

import java.util.Collections;
import java.util.Set;

public class CteraCustomActions implements CustomActions {

    private final CteraSession session;

    public CteraCustomActions(final CteraSession session) {
        this.session = session;
    }

    @Override
    public void run(final Action type, final Path file) throws BackgroundException {
        switch(CteraActions.valueOf(type.name())) {
            case versioning:
                new CteraCustomActionVersioning(session, file).run();
                break;
            case share:
        }
    }

    @Override
    public Set<Action> list(final Path file) {
        return Collections.singleton(CteraActions.versioning);
    }

    private enum CteraActions implements Action {
        versioning {
            @Override
            public String getTitle() {
                return LocaleFactory.localizedString("Versions", "Info");
            }
        },
        share {
            @Override
            public String getTitle() {
                return LocaleFactory.localizedString("Share…", "Main");
            }
        }
    }
}
