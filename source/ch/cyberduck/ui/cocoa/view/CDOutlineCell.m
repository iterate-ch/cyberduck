/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

#import "CDOutlineCell.h"

@implementation CDOutlineCell

- (void)setIcon:(NSImage *)aIcon
{
    if(icon == aIcon) {
        return;
    }
	[icon release];
	icon = [aIcon retain];
}

- (NSImage*)icon
{
    return icon;
}

- (id)copyWithZone:(NSZone *)zone
{
    CDOutlineCell *cell = (CDOutlineCell *)[super copyWithZone:zone];
    if (cell != nil) {
        cell->icon = nil;
        [cell setIcon:[self icon]];
    }
    return cell;
}

 - (void)dealloc
{
    [icon release];
    [super dealloc];
}

NSString *CDOutlineCellFilename = @"FILENAME";
NSString *CDOutlineCellIcon = @"ICON";

- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
    if ([controlView isFlipped]) {
        [icon compositeToPoint:NSMakePoint(cellFrame.origin.x + 3, cellFrame.origin.y + (cellFrame.size.height + [icon size].height) / 2)
					 operation:NSCompositeSourceOver];
    }
    else {
        [icon compositeToPoint:NSMakePoint(cellFrame.origin.x + 3, cellFrame.origin.y + (cellFrame.size.height - [icon size].height) / 2)
					 operation:NSCompositeSourceOver];
    }
	[super drawInteriorWithFrame:NSMakeRect(cellFrame.origin.x + 6 + [icon size].width, cellFrame.origin.y, cellFrame.size.width - 6 - [icon size].width, cellFrame.size.height)
						  inView:controlView];
}

- (void)selectWithFrame:(NSRect)rect inView:(NSView *)controlView editor:(NSText *)textObj delegate:(id)anObject start:(NSInteger)selStart length:(NSInteger)selLength
{
	[super selectWithFrame:NSMakeRect(rect.origin.x + 20, rect.origin.y, rect.size.width - 20, rect.size.height) inView:controlView editor:textObj delegate:anObject start:selStart length:selLength];
}

- (void)editWithFrame:(NSRect)rect inView:(NSView *)controlView editor:(NSText *)textObj delegate:(id)anObject event:(NSEvent *)theEvent
{
	[super editWithFrame:NSMakeRect(rect.origin.x + 20, rect.origin.y, rect.size.width - 20, rect.size.height) inView:controlView editor:textObj delegate:anObject event:theEvent];
}

@end
