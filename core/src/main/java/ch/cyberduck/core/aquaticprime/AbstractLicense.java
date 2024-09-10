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
import ch.cyberduck.core.LocaleFactory;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Objects;

public abstract class AbstractLicense implements License {

    private final Local file;

    protected AbstractLicense(final Local file) {
        this.file = file;
    }

    @Override
    public String getEntitlement() {
        final String name = this.getValue("Name");
        return MessageFormat.format(LocaleFactory.localizedString("Registered to {0}", "License"),
                StringUtils.isNotBlank(name) ? name : this.getValue("Email"));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof AbstractLicense)) {
            return false;
        }
        AbstractLicense that = (AbstractLicense) o;
        if(!Objects.equals(file, that.file)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }
}
