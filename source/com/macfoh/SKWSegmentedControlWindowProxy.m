/*
 *  Copyright (c) 2005 Shaun Wexler. All rights reserved.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dev@macfoh.com
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

- (NSUInteger)styleMask
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
