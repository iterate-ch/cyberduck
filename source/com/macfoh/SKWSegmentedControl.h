/*
 *  Copyright (c) 2005 Shaun Wexler. All rights reserved.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dev@macfoh.com
 */

#import <Cocoa/Cocoa.h>
#import "SKWSegmentedControlWindowProxy.h"

@interface SKWSegmentedControl : NSSegmentedControl
{
    SKWSegmentedControlWindowProxy *windowProxy;
}

@end
