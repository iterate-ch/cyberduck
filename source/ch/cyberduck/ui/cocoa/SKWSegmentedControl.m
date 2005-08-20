/*
 *  Copyright (c) 2005 Shaun Wexler. All rights reserved.
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

#import "SKWSegmentedControl.h"

@implementation SKWSegmentedControl

- (NSWindow *)window
{
	if (!windowProxy) {
		windowProxy = [[SKWSegmentedControlWindowProxy  
allocWithZone:NULL] initWithWindow:[super window]];
	}
	else if (![windowProxy window]) {
		return [super window];
	}
	return (NSWindow *)windowProxy;
}

- (void)viewWillMoveToWindow:(NSWindow *)window
{
	[super viewWillMoveToWindow:window];
	[windowProxy setWindow:window];
}

- (void)dealloc
{
	[windowProxy setWindow:nil];
	[windowProxy release];
	windowProxy = nil;
	
	[super dealloc];
}

@end