package ch.cyberduck.core.aquaticprime;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * A Donation Key.
 *
 * @version $Id$
 */
public abstract class AbstractLicense implements License {
    private static Logger log = Logger.getLogger(AbstractLicense.class);

    private Local file;

    protected AbstractLicense(Local file) {
        this.file = file;
    }

    protected Local getFile() {
        return file;
    }

    public String getName() {
        String to = this.getValue("Name");
        if(StringUtils.isBlank(to)) {
            to = this.getValue("Email"); // primary key
        }
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AbstractLicense) {
            return this.getFile().equals(((AbstractLicense) obj).getFile());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MessageFormat.format(Locale.localizedString("Registered to {0}", "License"), this.getName());
    }
}
