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
 */

#import "CDTableView.h"

@interface CDTableView (Private)
- (NSTableColumn *)_typeAheadSelectionColumn;
@end

@implementation CDTableView

- (id)init
{
	[super init];
	return self;
}

- (void)awakeFromNib
{
	
}

- (void)dealloc
{
	[super dealloc];
}

- (void)keyDown:(NSEvent *)theEvent;
{
    NSString *characters;
    unichar key;
	
    characters = [theEvent characters];
    key = [characters characterAtIndex:0];
	
    if (key == NSCarriageReturnCharacter || key == NSEnterCharacter) {
        if ([[self target] respondsToSelector:[self doubleAction]]) {
            [[self target] performSelector:[self doubleAction] withObject:self];
            return;
        }
    } 
	else if (key == NSDeleteCharacter || key == NSBackspaceCharacter) {
        if ([[self target] respondsToSelector:@selector(deleteKeyPerformed:)]) {
            [[self target] performSelector:@selector(deleteKeyPerformed:) withObject:self];
            return;
        }
    }
	
	NSTableColumn *typeAheadColumn = [self _typeAheadSelectionColumn];
	if (typeAheadColumn != nil && ([[NSCharacterSet alphanumericCharacterSet] characterIsMember:key] || (![[NSCharacterSet controlCharacterSet] characterIsMember:key]))) {
		int count = [[self dataSource] numberOfRowsInTableView:self];
		int startIndex = [self selectedRow];
		int rowIndex = startIndex < count - 1 ? startIndex : -1;
		rowIndex++;
		for (; rowIndex < count; rowIndex++) {
			NSAttributedString *name = [[self dataSource] tableView:self 
							   objectValueForTableColumn:typeAheadColumn
													 row:rowIndex];
			if ([[[name string] lowercaseString] hasPrefix: characters]) {
				[self selectRow:rowIndex byExtendingSelection:NO];
				[self scrollRowToVisible:rowIndex];
				return;
			}
			if(rowIndex == count - 1 && (startIndex == -1 || startIndex > 0)) {
				count = startIndex;
				rowIndex = -1;
				startIndex = 0;
			}
		}
	}
	[super keyDown:theEvent];
}

- (NSString *)view:(NSView *)view stringForToolTip:(NSToolTipTag)tag point:(NSPoint)point userData:(void *)data
{
	// ask our data source for the tool tip
	if ([[self dataSource] respondsToSelector:@selector(tableView:objectValueForTableColumn:row:)]) {
		if ([self rowAtPoint:point] >= 0) {
			return [[self dataSource] tableView:self 
					  objectValueForTableColumn:[[NSTableColumn alloc] initWithIdentifier:@"TOOLTIP"] 
											row:[self rowAtPoint:point]];
		}
	}
	return nil;
}

- (NSTableColumn *)_typeAheadSelectionColumn;
{
	return [self tableColumnWithIdentifier:@"FILENAME"];
}

@end
