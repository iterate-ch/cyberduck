package ch.cyberduck.core.exception;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;

public class RedirectException extends AccessDeniedException {

    private final Host target;

    public RedirectException(final Host target) {
        super(String.format("Redirect to %s", new HostUrlProvider().get(target)));
        this.target = target;
    }

    public Host getTarget() {
        return target;
    }
}
