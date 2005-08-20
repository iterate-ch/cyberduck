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

#import "SKWSegmentedControlWindowProxy.h"

@implementation SKWSegmentedControlWindowProxy

- (id)initWithWindow:(id)window
{
    [self setWindow:window];
    return self;
}

- (void)dealloc
{
    if (realWindow) {
        [self setWindow:nil];
    }
    [super dealloc];
}

- (void)windowWillClose:(NSNotification *)notification
{
    if (realWindow) {
        [self setWindow:nil];
    }
}

- (id)window
{
    return realWindow;
}

- (void)setWindow:(NSWindow *)window
{
    if (realWindow != window) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
    if ((realWindow = window)) {
        [[NSNotificationCenter defaultCenter] addObserver:self  
selector:@selector(windowWillClose:)  
name:NSWindowWillCloseNotification object:window];
    }
}

- (unsigned int)styleMask
{
    return ([realWindow styleMask] &~ NSUnifiedTitleAndToolbarWindowMask);
}

- (NSMethodSignature *)methodSignatureForSelector:(SEL)selector
{
    if (realWindow) {
        return [realWindow methodSignatureForSelector:selector];
    } else {
        return [NSWindow instanceMethodSignatureForSelector:selector];
    }
}

- (void)forwardInvocation:(NSInvocation *)invocation
{
    if (realWindow) {
        [invocation invokeWithTarget:realWindow];
    }
}

@end
