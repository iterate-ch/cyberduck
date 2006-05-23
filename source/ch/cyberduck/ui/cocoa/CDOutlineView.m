/*
 *  Copyright (c) 2005 Whitney Young. All rights reserved.
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

#import "CDOutlineView.h"

@interface CDOutlineView (Private)
- (NSTableColumn *)_typeAheadSelectionColumn;
- (void)selectRow;
- (void)selectRowWithTimer:(NSTimer *)sender;
- (void)_scheduleAutoExpandTimerForItem:(id)object;
@end

@implementation CDOutlineView

- (void)awakeFromNib
{
	[self setTarget:self];
	// [self setAction:@selector(handleBrowserClick:)];
	[self setDoubleAction:@selector(handleBrowserDoubleClick:)];
	
	// browser typeahead selection
	select_string = [[NSMutableString alloc] init];
	select_timer = nil;
}

- (BOOL)acceptsFirstMouse:(NSEvent *)event
{
	return YES;
}

- (void)dealloc
{
	[select_string release];
	[select_timer release];
	[super dealloc];
}

- (void)_scheduleAutoExpandTimerForItem:(id)object {
	if([[[NSUserDefaults standardUserDefaults] stringForKey:@"browser.view.autoexpand"] isEqualToString:@"false"]) {
		return;
	}
	if([super respondsToSelector:@selector(_scheduleAutoExpandTimerForItem:)]) {
		//bug: recursion; why is this calling self? [super performSelector:@selector(_scheduleAutoExpandTimerForItem:) withObject:object];
		[super _scheduleAutoExpandTimerForItem:object];
	}
}

- (void)handleBrowserClick:(id)sender {
	NSPoint where = [self convertPoint:[[NSApp currentEvent] locationInWindow] fromView:nil];
	int row = [self rowAtPoint:where];
	int col = [self columnAtPoint:where];
	if(row >= 0 && col >= 0) {
		NSTableColumn *column = [[self tableColumns] objectAtIndex:col];
		if([[self delegate] respondsToSelector:@selector(isColumnEditable:)]) {
			if([[self delegate] performSelector:@selector(isColumnEditable:) withObject:column]) {
				mBrowserEditingColumn = col;
				mBrowserEditingRow = row;
				NSValue *wrappedMouseLocation = [NSValue valueWithPoint:[NSEvent mouseLocation]];
				[self performSelector:@selector(handleBrowserClickOffloaded:) withObject:wrappedMouseLocation afterDelay:0.5];
			}
		}
		else {
			NSLog(@"WARN: Tableview delegate does not respond to isColumnEditable:");
		}
	}
}

- (void)handleBrowserClickOffloaded:(NSValue *)inWrappedMouseLocation {
	// UI: mouse must not have ben moved since first click, and must not have been double-clicked
	if((!mBrowserWasDoubleClicked) && (NSEqualPoints([inWrappedMouseLocation pointValue], [NSEvent mouseLocation])) ) {
		if(mBrowserEditingRow == [self selectedRow])
			[self editColumn:mBrowserEditingColumn row:mBrowserEditingRow withEvent:nil select:YES];
	}
	mBrowserWasDoubleClicked = NO;
}

- (void)handleBrowserDoubleClick:(id)sender {
	mBrowserWasDoubleClicked = YES;
    if([self clickedRow] != -1) { // make sure double click was not in table header
		if ([[self delegate] respondsToSelector:@selector(tableRowDoubleClicked:)]) {
			[[self delegate] performSelector:@selector(tableRowDoubleClicked:) withObject:self];
		}
	}
	mBrowserWasDoubleClicked = NO;
}

- (BOOL)shouldCollapseAutoExpandedItemsForDeposited:(BOOL)deposited
{
	return !deposited;
}

- (void)cancelOperation:(id)sender
{
	[self abortEditing];
}

// make return and tab only end editing, and not cause other cells to edit
// Taken from http://borkware.com/quickies/one?topic=NSTableView
- (void) textDidEndEditing: (NSNotification *) notification
{
    int textMovement = [[[notification userInfo] valueForKey:@"NSTextMovement"] intValue];
    if (textMovement == NSReturnTextMovement || textMovement == NSTabTextMovement || textMovement == NSBacktabTextMovement) {
        NSMutableDictionary *newInfo;
        newInfo = [NSMutableDictionary dictionaryWithDictionary: [notification userInfo]];
        [newInfo setObject: [NSNumber numberWithInt: NSIllegalTextMovement] forKey: @"NSTextMovement"];
        notification = [NSNotification notificationWithName: [notification name]
													 object: [notification object]
												   userInfo: newInfo];
		[super textDidEndEditing: notification];
		[[self window] makeFirstResponder:self];
    }
	else {
		[super textDidEndEditing: notification];
	}
}

// Taken from http://www.cocoadev.com/index.pl?RightClickSelectInTableView
- (NSMenu *) menuForEvent:(NSEvent *) event 
{
	NSPoint where = [self convertPoint:[event locationInWindow] fromView:nil];
	int row = [self rowAtPoint:where];
	int col = [self columnAtPoint:where];
	if(row >= 0) {
		NSTableColumn *column = nil;
		if(col >= 0) {
			column = [[self tableColumns] objectAtIndex:col];
		}
		if([[self delegate] respondsToSelector:@selector(tableView:shouldSelectRow:)]) {
			if([[self delegate] tableView:self shouldSelectRow:row])
				[self selectRow:row byExtendingSelection:NO];
		} 
		else {
			[self selectRow:row byExtendingSelection:NO];
		}
		return [self menu];
	}
	[self deselectAll:nil];
	return [self menu];
}

- (void)keyDown:(NSEvent *)event
{
	NSString *str = [event charactersIgnoringModifiers];
	unichar key = [str length] ? [str characterAtIndex:0] : '\0';

	if (key == NSCarriageReturnCharacter || key == NSEnterCharacter) {
        if ([[self delegate] respondsToSelector:@selector(enterKeyPressed:)]) {
            [[self delegate] performSelector:@selector(enterKeyPressed:) withObject:self];
        }
		return;
    } 
	else if (key == NSDeleteFunctionKey || key == NSDeleteCharacter || key == NSBackspaceCharacter) {
        if ([[self delegate] respondsToSelector:@selector(deleteKeyPressed:)]) {
            [[self delegate] performSelector:@selector(deleteKeyPressed:) withObject:self];
        }
		return;
	} 
	else if (key == NSLeftArrowFunctionKey) { //left
		NSEnumerator *enumerator = [self selectedRowEnumerator];
		id row;
		while (row = [enumerator nextObject]) {
			id object = [self itemAtRow:[row intValue]];
			if (object && [self isExpandable:object] && [self isItemExpanded:object]) {
				[self collapseItem:object];
				enumerator = [self selectedRowEnumerator];
			}
		}
		return;
	}
	else if (key == NSRightArrowFunctionKey) { //right
		NSEnumerator *enumerator = [self selectedRowEnumerator];
		id row;
		while (row = [enumerator nextObject]) {
			id object = [self itemAtRow:[row intValue]];
			if (object && [self isExpandable:object] && ![self isItemExpanded:object]) {
				[self expandItem:object];
				enumerator = [self selectedRowEnumerator];
			}
		}
		return;
	}
	if ([[NSCharacterSet alphanumericCharacterSet] characterIsMember:key] && 
		(![[NSCharacterSet controlCharacterSet] characterIsMember:key])) {
		
		[select_string appendString:[event charactersIgnoringModifiers]];
		if([select_string length] == 1) {
			[self selectRow];
		}
		else {
			[select_timer invalidate];
			select_timer = [NSTimer scheduledTimerWithTimeInterval:0.5
															target:self 
														  selector:@selector(selectRowWithTimer:) 
														  userInfo:nil 
														   repeats:NO];
		}
	} 
	else {
		[super keyDown:event];
	}
}

- (void)selectRow
{
	int row = -1;
	int to_index = 0;
	int smallest_difference = -1;
	int counter;

	NSString *compare = [select_string lowercaseString];
	for (counter = 0; counter < [[self dataSource] outlineView:self numberOfChildrenOfItem:nil]; counter++) {
	    NSObject *item = [[self dataSource] outlineView:self child:counter ofItem:nil];
		NSString *object = [[[self dataSource] outlineView:self 
								 objectValueForTableColumn:[self _typeAheadSelectionColumn] 
													byItem:item] lowercaseString];
		if (to_index < [object length] && to_index < [compare length] + 1) {
			if (object && [[object substringToIndex:to_index] isEqualToString:[compare substringToIndex:to_index]])	{
				char one = [compare characterAtIndex:to_index];
				char two = (to_index == [object length])?' ':[object characterAtIndex:to_index];
				int difference = abs(one - two);
				if (difference == 0) {
					while (difference == 0) {
						to_index++;
						if (to_index == [compare length] || to_index == [object length] + 1) { break; } // if we hava an exact match
						one = [compare characterAtIndex:to_index];
						two = (to_index == [object length])?' ':[object characterAtIndex:to_index];
						difference = abs(one - two);
					}
					smallest_difference = -1;
					row = [self rowForItem:item];
					if (to_index == [compare length] || to_index == [object length] + 1) { break; } // if we hava an exact match
				} 
				else if (smallest_difference == -1 || difference < smallest_difference) {
					smallest_difference = difference;
					row = [self rowForItem:item];
				}
			}
		}
	}
	if (row != -1) {
		[self selectRow:row byExtendingSelection:NO];
		[self scrollRowToVisible:row];
	}	
}

- (void)selectRowWithTimer:(NSTimer *)sender
{
	[self selectRow];
	[select_timer invalidate];
	select_timer = nil;
	[select_string setString:@""];
}

- (NSTableColumn *)_typeAheadSelectionColumn
{
	return [[NSTableColumn alloc] initWithIdentifier:@"TYPEAHEAD"];
}

- (NSImage *)dragImageForRows:(NSArray *)dragRows 
						event:(NSEvent *)dragEvent 
			  dragImageOffset:(NSPointPointer)dragImageOffset 
{
	NSImage *img = [[NSImage alloc] initByReferencingFile: @"transparent.tiff"];
	[img setCacheMode:NSImageCacheNever];
	return img;

}

- (NSImage *)dragImageForRowsWithIndexes:(NSIndexSet *)dragRows 
							tableColumns:(NSArray *)tableColumns 
								   event:(NSEvent*)dragEvent 
								  offset:(NSPointPointer)dragImageOffset 
{
	NSImage *img = [[NSImage alloc] initByReferencingFile: @"transparent.tiff"];
	[img setCacheMode:NSImageCacheNever];
	return img;
}

@end
