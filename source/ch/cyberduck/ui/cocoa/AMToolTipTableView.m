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
 */

#import "AMToolTipTableView.h"

@interface AMToolTipTableView (Private)
- (NSString *)_amKeyForColumn:(int)columnIndex row:(int)rowIndex;
- (NSTableColumn *)_typeAheadSelectionColumn;
@end

@implementation AMToolTipTableView

- (id)init
{
	[super init];
	regionList = [[NSMutableDictionary alloc] initWithCapacity:20];
	return self;
}

- (void)awakeFromNib
{
	
}

- (void)dealloc
{
	[regionList release];
	[super dealloc];
}

- (void)reloadData
{
	// we have to invalidate the region list here
	[regionList removeAllObjects];
	[self removeAllToolTips];
	[super reloadData];
}

- (void)keyDown:(NSEvent *)theEvent;
{
    NSString *characters;
    unichar firstCharacter;
	
    characters = [theEvent characters];
    firstCharacter = [characters characterAtIndex:0];
	
	NSTableColumn *typeAheadColumn = [self _typeAheadSelectionColumn];
	if (typeAheadColumn != nil && ([[NSCharacterSet alphanumericCharacterSet] characterIsMember:firstCharacter] || (![[NSCharacterSet controlCharacterSet] characterIsMember:firstCharacter]))) {

//		int rowIndex;
		int count = [[self dataSource] numberOfRowsInTableView:self];
		int startIndex = [self selectedRow];
		int rowIndex = startIndex < count - 1 ? startIndex : -1;
		rowIndex++;
		
//		for (rowIndex = 0; rowIndex < [[self dataSource] numberOfRowsInTableView: self]; rowIndex++) {
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

/*
- (NSRect)frameOfCellAtColumn:(int)columnIndex row:(int)rowIndex
{
	// this cell is apparently displayed, so we need to add a region for it
	NSNumber *toolTipTag;

	NSRect result = [super frameOfCellAtColumn:columnIndex row:rowIndex];

	// check if cell is already in the list
	NSString *cellKey = [self _amKeyForColumn:columnIndex row:rowIndex];
	if (toolTipTag = [regionList objectForKey:cellKey]) {
		// remove old region
		[self removeToolTip:[toolTipTag intValue]];
	}
	// add new region
	[regionList setObject:[NSNumber numberWithInt:[self addToolTipRect:result owner:self userData:cellKey]] forKey:cellKey];
	return [super frameOfCellAtColumn:columnIndex row:rowIndex];
}
*/

- (NSString *)_amKeyForColumn:(int)columnIndex row:(int)rowIndex
{
	return [NSString stringWithFormat:@"%d,%d", rowIndex, columnIndex];
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
