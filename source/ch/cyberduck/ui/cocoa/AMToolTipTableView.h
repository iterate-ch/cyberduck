/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 *  Created by Andreas on Fri Oct 18 2002.
 *  Copyright (c) 2002 Andreas Mayer. All rights reserved.
 *
 *	This subclass of NSTableView allows you to display different tool tips
 *	for each cell in the table.
 *
 *	When the table view needs to display a tool tip, it asks it's data source
 *	for it. So you need to implement tableView:toolTipForTableColumn:row: in
 *	your table's data source. See declaration below.
 */

#import <Cocoa/Cocoa.h>


@interface AMToolTipTableView : NSTableView {
	NSMutableDictionary *regionList;
}

- (void)awakeFromNib;

@end


@protocol AMToolTipDelegate

- (NSString *)tableView:(NSTableView *)aTableView toolTipForTableColumn:(NSTableColumn *)aTableColumn row:(int)rowIndex;

@end