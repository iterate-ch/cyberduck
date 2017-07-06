package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class MantaProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return "manta";
    }

    @Override
    public String getDescription() {
        return "Triton Manta Object Storage";
    }

    @Override
    public String getName() {
        return "Manta";
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", MantaProtocol.class.getPackage().getName(), "Manta");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        // TODO: if true, we could allow people to traverse other people's public manta folders?
        return false;
    }

    @Override
    public String getDefaultHostname() {
        return "us-east.manta.joyent.com";
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        // TODO: password can be provided, but we're only using the key right now?
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
