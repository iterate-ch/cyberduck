package ch.cyberduck.binding.quicklook;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.foundation.NSURL;

public abstract class QLPreviewItem extends ProxyController {

    public abstract NSURL previewItemURL();

    /*!
    * @abstract The item's title this will be used as apparent item title.
    * @discussion The title replaces the default item display name. This property is optional.
    */
    public abstract String previewItemTitle();
}
