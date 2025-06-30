/*
 * Copyright (c) 2025 iterate GmbH. All rights reserved.
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

import AppKit

class CustomTilePlugIn: NSObject, NSDockTilePlugIn {

    func setDockTile(_ dockTile: NSDockTile?) {
        if let dockTile = dockTile {
            // A DockTile was provided by the system
            guard let image = Bundle(for: Self.self).image(forResource: "cyberduck-application-rect")
            else {
                return
            }
            dockTile.contentView = NSImageView(image: image)
            dockTile.display()
        } else {
            // Application icon was removed from the Dock
        }
    }
}
