
/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

#import "CDBottomView.h"

@implementation CDBottomView

-(id)initWithFrame:(NSRect)frame
{
	if ((self = [super initWithFrame:frame]) != nil)
	{
		pattern = [[NSImage imageNamed:@"bottom"] retain];
	}
	return self;
}

- (void)drawRect:(NSRect)rect {
	NSRect sourceRect = NSZeroRect;
	sourceRect.size = [pattern size];
	[pattern drawInRect:[self bounds] fromRect:sourceRect operation:NSCompositeSourceOver fraction:1.0];
}

- (BOOL)isOpaque {
	return YES;
}

-(void)dealloc
{
	[pattern release];
	[super dealloc];
}

@end
