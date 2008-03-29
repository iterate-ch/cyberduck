/*
 *  Copyright (c) 2005 Shaun Wexler. All rights reserved.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dev@macfoh.com
 */

#import <Cocoa/Cocoa.h>

@interface SKWSegmentedControlWindowProxy : NSProxy
{
    id realWindow;
}

- (id)initWithWindow:(id)window;
- (id)window;
- (void)setWindow:(NSWindow *)window;
- (unsigned int)styleMask;

@end
