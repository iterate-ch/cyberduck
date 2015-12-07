/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

#import "CDListView.h"

@interface CDListView (Private)
+ (NSTableColumn *)_localSelectionColumn;
@end
 
@implementation CDListView

static NSTableColumn *localSelectionColumn;

- (void)awakeFromNib
{
	[self setTarget:self];
	// [self setAction:@selector(handleBrowserClick:)];
	[self setDoubleAction:@selector(handleBrowserDoubleClick:)];
}

- (BOOL)acceptsFirstMouse:(NSEvent *)event
{
	return YES;
}

- (void)dealloc
{
	[super dealloc];
}

- (void)handleBrowserClick:(id)sender {
	mBrowserWasDoubleClicked = NO;
	NSPoint where = [self convertPoint:[[NSApp currentEvent] locationInWindow] fromView:nil];
	NSInteger row = [self rowAtPoint:where];
	NSInteger col = [self columnAtPoint:where];
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
}

- (void)cancelOperation:(id)sender
{
	[self abortEditing];
    [[self window] makeFirstResponder:self];
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
    NSInteger row = [self rowAtPoint:where];
	if(row >= 0) {
		if([[self delegate] respondsToSelector:@selector(tableView:shouldSelectRow:)]) {
			if([[self delegate] tableView:self shouldSelectRow:row])
				[self selectRowIndexes:[NSIndexSet indexSetWithIndex:row] byExtendingSelection:[self isRowSelected:row]];
		} 
		else {
			[self selectRowIndexes:[NSIndexSet indexSetWithIndex:row] byExtendingSelection:[self isRowSelected:row]];
		}
		return [self menu];
	}
	[self deselectAll:nil];
	return [self menu];
}

// This is the Quick Look delegate method. It should return the frame for the item represented by the URL. If an
// empty frame is returned then the panel will fade in/out instead
- (NSRect)previewPanel:(NSPanel*)panel frameForURL:(NSURL*)url
{
	NSRect frame = NSMakeRect(0, 0, 0, 0);
	NSRange visibleRows = [self rowsInRect:[self bounds]];
	int row, endRow;
	for(row = visibleRows.location, endRow = row + visibleRows.length; row <= endRow; ++row) {
		id path = [[self dataSource] tableView:self
                     objectValueForTableColumn:[CDListView _localSelectionColumn]
                                           row:row];
		if(nil == path) {
			continue;
		}
		if([[path string] isEqualToString:[url lastPathComponent]]) {
			frame           = [self rectOfRow:row];
			frame.origin    = [self convertPoint:frame.origin toView:nil];
			frame.origin    = [[self window] convertBaseToScreen:frame.origin];
			frame.origin.y -= frame.size.height;
			break;
		}
	}
	return frame;
}

- (NSRect)previewPanel:(id)panel sourceFrameOnScreenForPreviewItem:(id)item
{
	if ([item respondsToSelector:@selector(previewItemURL)]) {
		return [self previewPanel:panel frameForURL:[item performSelector:@selector(previewItemURL)]];
	}
	return NSZeroRect;
}

- (void)keyDown:(NSEvent *)event
{
	NSString *str = [event characters];
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
	else if(key == ' ') {
        if ([[self delegate] respondsToSelector:@selector(spaceKeyPressed:)]) {
            [[[QLPreviewPanel sharedPreviewPanel] windowController] setDelegate:self];
            // Space bar invokes Quick Look
            [[self delegate] performSelector:@selector(spaceKeyPressed:) withObject:event];
        }
        return;
	}
	[super keyDown:event];
}

- (void)swipeWithEvent:(NSEvent *)event {
    if ([[self delegate] respondsToSelector:@selector(swipeWithEvent:)]) {
        [[self delegate] performSelector:@selector(swipeWithEvent:) withObject:event];
    }
    else {
        [super swipeWithEvent:event];
    }
}

+ (NSTableColumn *)_localSelectionColumn
{
	if(nil == localSelectionColumn) {
		localSelectionColumn = [[NSTableColumn alloc] initWithIdentifier:@"filename"];
	}
	return localSelectionColumn;
}

- (NSImage *)dragImageForRows:(NSArray *)dragRows
						event:(NSEvent *)dragEvent
			  dragImageOffset:(NSPointPointer)dragImageOffset
{
	NSImage *img = [NSImage imageNamed: @"transparent.tiff"];
	[img setCacheMode:NSImageCacheNever];
	return img;
}

- (NSImage *)dragImageForRowsWithIndexes:(NSIndexSet *)dragRows
							tableColumns:(NSArray *)tableColumns
								   event:(NSEvent*)dragEvent
								  offset:(NSPointPointer)dragImageOffset
{
	NSImage *img = [NSImage imageNamed: @"transparent.tiff"];
	[img setCacheMode:NSImageCacheNever];
	return img;
}

@end
