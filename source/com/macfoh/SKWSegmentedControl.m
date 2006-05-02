/*
 *  Copyright (c) 2005 Shaun Wexler. All rights reserved.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dev@macfoh.com
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