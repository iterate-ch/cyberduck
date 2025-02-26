package ch.cyberduck.core.local;

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

import static ch.cyberduck.binding.foundation.NSURL.NSURLBookmarkCreationOptions.NSURLBookmarkCreationSuitableForBookmarkFile;
import static ch.cyberduck.binding.foundation.NSURL.NSURLBookmarkCreationOptions.NSURLBookmarkCreationWithoutImplicitSecurityScope;
import static ch.cyberduck.binding.foundation.NSURL.NSURLBookmarkResolutionOptions.NSURLBookmarkResolutionWithoutMounting;
import static ch.cyberduck.binding.foundation.NSURL.NSURLBookmarkResolutionOptions.NSURLBookmarkResolutionWithoutUI;

public class AliasFilesystemBookmarkResolver extends NSURLPromptBookmarkResolver {

    public AliasFilesystemBookmarkResolver() {
        super(NSURLBookmarkCreationSuitableForBookmarkFile | NSURLBookmarkCreationWithoutImplicitSecurityScope, NSURLBookmarkResolutionWithoutUI | NSURLBookmarkResolutionWithoutMounting);
    }
}
