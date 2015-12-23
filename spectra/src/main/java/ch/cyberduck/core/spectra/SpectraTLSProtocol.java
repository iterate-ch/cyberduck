/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

public class SpectraTLSProtocol extends SpectraProtocol {

    @Override
    public String getName() {
        return "Spectra S3 (TLS)";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Spectra S3 (TLS)", "S3");
    }

    @Override
    public Type getType() {
        return Type.spectra;
    }

    @Override
    public String getIdentifier() {
        return "spectra-tls";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }
}
