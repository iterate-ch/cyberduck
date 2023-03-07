package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

@Optional
public interface PromptUrlProvider<Download, Upload> {
    boolean isSupported(Path file, Type type);

    DescriptiveUrl toDownloadUrl(Path file, Download options, PasswordCallback callback) throws BackgroundException;

    DescriptiveUrl toUploadUrl(Path file, Upload options, PasswordCallback callback) throws BackgroundException;

    enum Type {
        download,
        upload
    }
}
