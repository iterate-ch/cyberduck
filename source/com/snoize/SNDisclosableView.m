/*
 Copyright (c) 2002, Kurt Revis.  All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Snoize nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#import "SNDisclosableView.h"

#import <Cocoa/Cocoa.h>


@interface SNDisclosableView (Private)

- (void)removeSubviews;
- (void)restoreSubviews;

- (void)changeWindowHeightBy:(CGFloat)amount;
- (void)restoreAutoresizeMasks:(NSArray *)masks toViews:(NSArray *)views;

@end


@implementation SNDisclosableView

const CGFloat kDefaultHiddenHeight = 0.0;

static BOOL showSubviewsWhileResizing = NO;
    // Under Mac OS X 10.2, we can show our subviews while we animate the resizing of the window, so this can be set to YES.
    // Earlier versions of the AppKit would pin a view's y origin to 0, which caused that to fail.

- (id)initWithFrame:(NSRect)frameRect;
{
    if (!(self = [super initWithFrame:frameRect]))
        return nil;

    isShown = YES;
    originalHeight = [self frame].size.height;
    hiddenHeight = kDefaultHiddenHeight;

    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder;
{
    if (!(self = [super initWithCoder:aDecoder]))
        return nil;

    isShown = YES;
    originalHeight = [self frame].size.height;
    hiddenHeight = kDefaultHiddenHeight;
    [aDecoder decodeValueOfObjCType:@encode(typeof(hiddenHeight)) at:&hiddenHeight];
    
    return self;
}

- (void)dealloc;
{
    [hiddenSubviews release];
    
    [super dealloc];
}

- (void)awakeFromNib;
{
    if ([[self superclass] instancesRespondToSelector:@selector(awakeFromNib)])
        [super awakeFromNib];

    if ([self autoresizingMask] & NSViewHeightSizable)
        NSLog(@"Warning: SNDisclosableView: You probably don't want this view to be resizeable vertically. I suggest turning that off in the inspector in IB.");

	[self setIsShown:NO];
}

- (void)encodeWithCoder:(NSCoder *)aCoder;
{
    [super encodeWithCoder:aCoder];
    [aCoder encodeValueOfObjCType:@encode(typeof(hiddenHeight)) at:&hiddenHeight];
}

- (BOOL)acceptsFirstResponder
{
    return NO;
}

//
// New methods
//

- (BOOL)isShown;
{
    return isShown;
}

- (void)setIsShown:(BOOL)value;
{
    if (value)
        [self show:nil];
    else
        [self hide:nil];
}

- (CGFloat)hiddenHeight;
{
    return hiddenHeight;
}

- (void)setHiddenHeight:(CGFloat)value;
{
    hiddenHeight = value;
}

//
// Actions
//

- (IBAction)toggleDisclosure:(id)sender;
{
    [self setIsShown:!isShown];
}

- (IBAction)hide:(id)sender;
{
    NSView *keyLoopView;

    if (!isShown)
        return;

    keyLoopView = [self nextKeyView];
    if ([keyLoopView isDescendantOf:self]) {
        // We need to remove our subviews (which will be hidden) from the key loop.
    
        // Remember our nextKeyView so we can restore it later.
        nonretainedOriginalNextKeyView = keyLoopView;

        // Find the last view in the key loop which is one of our descendants.
        nonretainedLastChildKeyView = keyLoopView;
        while ((keyLoopView = [nonretainedLastChildKeyView nextKeyView])) {
            if ([keyLoopView isDescendantOf:self])
                nonretainedLastChildKeyView = keyLoopView;
            else
                break;
        }
            
        // Set our nextKeyView to its nextKeyView, and set its nextKeyView to nil.
        // (If we don't do the last step, when we restore the key loop later, it will be missing views in the backwards direction.)
        [self setNextKeyView:keyLoopView];
        [nonretainedLastChildKeyView setNextKeyView:nil];
    } else {
        nonretainedOriginalNextKeyView = nil;
    }

    // Remember our current size.
    // When showing, we will use this to resize the subviews properly.
    // (The window width may change while the subviews are hidden.)
    sizeBeforeHidden = [self frame].size;

    // Now shrink the window, causing this view to shrink and our subviews to be obscured.
    // Also remove the subviews from the view hierarchy.
    if (showSubviewsWhileResizing) {
        [self changeWindowHeightBy:-(originalHeight - hiddenHeight)];
        [self removeSubviews];
    } else {
        [self removeSubviews];
        [self changeWindowHeightBy:-(originalHeight - hiddenHeight)];
    }
    
    [self setNeedsDisplay:YES];

    isShown = NO;
}

- (IBAction)show:(id)sender;
{
    if (isShown)
        return;

    // Expand the window, causing this view to expand, and put our hidden subviews back into the view hierarchy.

    if (showSubviewsWhileResizing) {
        NSSize hiddenSize;
        
        [self restoreSubviews];

        // Temporarily set our frame back to its original height.
        // Then tell our subviews to resize themselves, according to their normal autoresize masks.
        // (This may cause their widths to change, if the window was resized horizontally while the subviews were out of the view hierarchy.)
        // Then set our frame size back so we are hidden again.
        hiddenSize = [self frame].size;
        [self setFrameSize:NSMakeSize(hiddenSize.width, originalHeight)];
        [self resizeSubviewsWithOldSize:sizeBeforeHidden];
        [self setFrameSize:hiddenSize];
        
        [self changeWindowHeightBy:(originalHeight - hiddenHeight)];
    } else {
        [self changeWindowHeightBy:(originalHeight - hiddenHeight)];
        [self restoreSubviews];
        [self resizeSubviewsWithOldSize:sizeBeforeHidden];
    }

    if (nonretainedOriginalNextKeyView) {
        // Restore the key loop to its old configuration.
        [nonretainedLastChildKeyView setNextKeyView:[self nextKeyView]];
        [self setNextKeyView:nonretainedOriginalNextKeyView];
    }

    isShown = YES;

    [self setNeedsDisplay:YES];
}

@end


@implementation SNDisclosableView (Private)

- (void)removeSubviews;
{
    unsigned int subviewIndex;

    NSAssert(hiddenSubviews == nil, @"-[SNDisclosableView removeSubviews]: should have no hidden subviews yet");

    hiddenSubviews = [[NSArray alloc] initWithArray:[self subviews]];
    subviewIndex = [hiddenSubviews count];
    while (subviewIndex--)
        [[hiddenSubviews objectAtIndex:subviewIndex] removeFromSuperview];
}

- (void)restoreSubviews;
{
    unsigned int subviewIndex;

    NSAssert(hiddenSubviews != nil, @"-[SNDisclosableView restoreSubviews]: hiddenSubviews array is nil");

    subviewIndex = [hiddenSubviews count];
    while (subviewIndex--)
        [self addSubview:[hiddenSubviews objectAtIndex:subviewIndex]];

    [hiddenSubviews release];
    hiddenSubviews = nil;
}

- (void)changeWindowHeightBy:(CGFloat)amount;
{
    // This turns out to be more complicated than one might expect, because the way that the other views in the window should move is different than the normal case that the AppKit handles.
    //
    // We want the other views in the window to stay the same size. If a view is above us, we want it to stay in the same position relative to the top of the window; likewise, if a view is below us, we want it to stay in the same position relative to the bottom of the window.
    // Also, we want this view to resize vertically, with its top and bottom attached to the top and bottom of its parent.
    // And: this view's subviews should not resize vertically, and should stay attached to the top of this view.  (This only matters if showSubviewsWhileResizing is YES; otherwise, we have no subviews at this point in time.)
    //
    // However, all of these views may have their autoresize masks configured differently than we want. So:
    //
    // * For each of the window's content view's immediate subviews, including this view,
    //   - Save the current autoresize mask
    //   - And set the autoresize mask how we want
    // * Do the same for the view's subviews.
    // * Then resize the window, and fix up the window's min/max sizes.
    // * For each view that we touched earlier, restore the old autoresize mask.

    NSRect ourFrame;
    NSWindow *window;
    NSView *contentView;
    NSArray *windowSubviews;
    unsigned int windowSubviewCount, windowSubviewIndex;
    NSMutableArray *windowSubviewMasks;
    NSArray *ourSubviews;
    unsigned int ourSubviewCount, ourSubviewIndex;
    NSMutableArray *ourSubviewMasks;
    NSRect newWindowFrame;
    NSSize newWindowMinOrMaxSize;

    ourFrame = [self frame];
    window = [self window];
    contentView = [window contentView];

    // Adjust the autoresize masks of the window's subviews, remembering the original masks.
    windowSubviews = [contentView subviews];
    windowSubviewCount = [windowSubviews count];
    windowSubviewMasks = [NSMutableArray arrayWithCapacity:windowSubviewCount];
    for (windowSubviewIndex = 0; windowSubviewIndex < windowSubviewCount; windowSubviewIndex++) {
        NSView *windowSubview;
        unsigned int mask;
        
        windowSubview = [windowSubviews objectAtIndex:windowSubviewIndex];
        mask = [windowSubview autoresizingMask];
        [windowSubviewMasks addObject:[NSNumber numberWithUnsignedInt:mask]];

        if (windowSubview == self) {
            // This is us.  Make us stick to the top and bottom of the window, and resize vertically.
            mask |= NSViewHeightSizable;
            mask &= ~NSViewMaxYMargin;
            mask &= ~NSViewMinYMargin;
        } else if (NSMaxY([windowSubview frame]) < NSMaxY(ourFrame)) {
            // This subview is below us. Make it stick to the bottom of the window.
            // It should not change height.
            mask &= ~NSViewHeightSizable;
            mask |= NSViewMaxYMargin;
            mask &= ~NSViewMinYMargin;
        } else {
            // This subview is above us. Make it stick to the top of the window.
            // It should not change height.
            mask &= ~NSViewHeightSizable;
            mask &= ~NSViewMaxYMargin;
            mask |= NSViewMinYMargin;
        }

        [windowSubview setAutoresizingMask:mask];
    }

    // Adjust the autoresize masks of our subviews, remembering the original masks.
    // (Note that if showSubviewsWhileResizing is NO, [self subviews] will be empty.)
    ourSubviews = [self subviews];
    ourSubviewCount = [ourSubviews count];
    ourSubviewMasks = [NSMutableArray arrayWithCapacity:ourSubviewCount];
    for (ourSubviewIndex = 0; ourSubviewIndex < ourSubviewCount; ourSubviewIndex++) {
        NSView *ourSubview;
        unsigned int mask;

        ourSubview = [ourSubviews objectAtIndex:ourSubviewIndex];
        mask = [ourSubview autoresizingMask];
        [ourSubviewMasks addObject:[NSNumber numberWithUnsignedInt:mask]];

        // Don't change height, and stick to the top of the view.
        mask &= ~NSViewHeightSizable;
        mask &= ~NSViewMaxYMargin;
        mask |= NSViewMinYMargin;

        [ourSubview setAutoresizingMask:mask];
    }

    // Compute the window's new frame, and resize it.
    newWindowFrame = [window frame];
    newWindowFrame.origin.y -= amount;
    newWindowFrame.size.height += amount;
    if ([window isVisible])
        [window setFrame:newWindowFrame display:YES animate:YES];
    else
        [window setFrame:newWindowFrame display:NO];

    // Adjust the window's min and max sizes to make sense.
    newWindowMinOrMaxSize = [window minSize];
    newWindowMinOrMaxSize.height += amount;
    [window setMinSize:newWindowMinOrMaxSize];

    newWindowMinOrMaxSize = [window maxSize];
    // If there is no max size set (height of 0), don't change it.
    if (newWindowMinOrMaxSize.height > 0) {
        newWindowMinOrMaxSize.height += amount;
        [window setMaxSize:newWindowMinOrMaxSize];
    }

    // Restore the saved autoresize masks.
    [self restoreAutoresizeMasks:windowSubviewMasks toViews:windowSubviews];
    [self restoreAutoresizeMasks:ourSubviewMasks toViews:ourSubviews];
}

- (void)restoreAutoresizeMasks:(NSArray *)masks toViews:(NSArray *)views;
{
    unsigned int count, index;

    count = [masks count];
    for (index = 0; index < count; index++)
        [[views objectAtIndex:index] setAutoresizingMask:[[masks objectAtIndex:index] unsignedIntValue]];    
}

@end
