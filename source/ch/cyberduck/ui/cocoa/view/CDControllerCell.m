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

#import "CDControllerCell.h"

@implementation CDControllerCell

- (void)setView:(NSView *)aView
{
    if(view == aView) {
        return;
    }
	[view release];
	view = [aView retain];
}

- (NSView*)view
{
    return view;
}

- (id)copyWithZone:(NSZone *)zone
{
    CDControllerCell *cell = (CDControllerCell *)[super copyWithZone:zone];
    if (cell != nil) {
        cell->view = nil;
        [cell setView:[self view]];
    }
    return cell;
}

 - (void)dealloc
{
    [view release];
    [super dealloc];
}

- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
	NSView *controllerView = [self view];
	[controllerView setFrame:cellFrame];
	if([controllerView superview] != controlView) {
		[controlView addSubview:controllerView];
	}
}

@end
