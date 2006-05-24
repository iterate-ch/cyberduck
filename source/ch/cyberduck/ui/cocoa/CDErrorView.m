
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

#import "CDErrorView.h"

@implementation CDErrorView

-(id)initWithFrame:(NSRect)frame
{
	if ((self = [super initWithFrame:frame]) != nil)
	{
	}
	return self;
}

- (void)drawRect:(NSRect)rect {
	[[NSColor selectedControlColor] set];
	[NSBezierPath fillRect:[self bounds]];
}

- (BOOL)isOpaque {
	return [[NSColor selectedControlColor] alphaComponent] >= 1.0;
}

@end
