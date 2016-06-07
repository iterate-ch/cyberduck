package ch.cyberduck.core;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

public final class DistributionCredentials extends Credentials {

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Access Key ID", "S3");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Secret Access Key", "S3");
    }
}
