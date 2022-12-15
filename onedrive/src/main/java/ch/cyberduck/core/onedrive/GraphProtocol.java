package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;

public abstract class GraphProtocol extends AbstractProtocol {
    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Authorization code", "Credentials");
    }

    @Override
    public boolean isPasswordConfigurable() {
        // Only provide account email
        return false;
    }

    @Override
    public Type getType() {
        return Type.onedrive;
    }

    @Override
    public String disk() {
        return "onedrive.tiff";
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(DefaultComparisonService.DEFAULT_FILE_COMPARISON_CHAIN, ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
