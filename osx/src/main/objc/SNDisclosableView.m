/*
 Copyright (c) 2002-2014, Kurt Revis.  All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Snoize nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#import "SNDisclosableView.h"

@interface SNDisclosableView ()

@property (nonatomic, assign) CGFloat originalHeight;
@property (nonatomic, copy) NSArray *hiddenSubviews;
@property (nonatomic, assign) NSView *nonretainedOriginalNextKeyView;
@property (nonatomic, assign) NSView *nonretainedLastChildKeyView;
@property (nonatomic, assign) NSSize sizeBeforeHidden;

@end


@implementation SNDisclosableView

const float kDefaultHiddenHeight = 0.0;

- (instancetype)initWithFrame:(NSRect)frameRect
{
    if ((self = [super initWithFrame:frameRect])) {
        _shown = YES;
        _originalHeight = [self frame].size.height;
        _hiddenHeight = kDefaultHiddenHeight;
    }

    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super initWithCoder:aDecoder])) {
        _shown = YES;
        _originalHeight = [self frame].size.height;
        _hiddenHeight = kDefaultHiddenHeight;
    }

    return self;
}

- (void)dealloc
{
    [_hiddenSubviews release];
    
    [super dealloc];
}

- (void)awakeFromNib
{
    [super awakeFromNib];

    if ([self autoresizingMask] & NSViewHeightSizable) {
        NSLog(@"Warning: SNDisclosableView: You probably don't want this view to be resizeable vertically. I suggest turning that off in the inspector in IB.");
    }
}

- (BOOL)acceptsFirstResponder
{
    return NO;
}


//
// New methods
//

- (void)setShown:(BOOL)value
{
    if (value != _shown) {
        if (value) {
            [self show];
        } else {
            [self hide];
        }
        _shown = value;
    }
}

//
// Actions
//

- (IBAction)toggleDisclosure:(id)sender
{
    self.shown = !self.isShown;
}

#pragma mark Private

- (void)hide
{
    NSView *keyLoopView = self.nextKeyView;
    if ([keyLoopView isDescendantOf:self]) {
        // We need to remove our subviews (which will be hidden) from the key loop.

        // Remember our nextKeyView so we can restore it later.
        self.nonretainedOriginalNextKeyView = keyLoopView;

        // Find the last view in the key loop which is one of our descendants.
        self.nonretainedLastChildKeyView = keyLoopView;
        while ((keyLoopView = [self.nonretainedLastChildKeyView nextKeyView])) {
            if ([keyLoopView isDescendantOf:self]) {
                self.nonretainedLastChildKeyView = keyLoopView;
            } else {
                break;
            }
        }

        // Set our nextKeyView to its nextKeyView, and set its nextKeyView to nil.
        // (If we don't do the last step, when we restore the key loop later, it will be missing views in the backwards direction.)
        self.nextKeyView = keyLoopView;
        self.nonretainedLastChildKeyView.nextKeyView = nil;
    } else {
        self.nonretainedOriginalNextKeyView = nil;
    }

    // Remember our current size.
    // When showing, we will use this to resize the subviews properly.
    // (The window width may change while the subviews are hidden.)
    self.sizeBeforeHidden = self.frame.size;

    // Now shrink the window, causing this view to shrink and our subviews to be obscured.
    // Also remove the subviews from the view hierarchy.
    [self changeWindowHeightBy:-(self.originalHeight - self.hiddenHeight)];
    [self removeSubviews];

    [self setNeedsDisplay:YES];
}

- (void)show
{
    // Expand the window, causing this view to expand, and put our hidden subviews back into the view hierarchy.
    [self restoreSubviews];

    // Temporarily set our frame back to its original height.
    // Then tell our subviews to resize themselves, according to their normal autoresize masks.
    // (This may cause their widths to change, if the window was resized horizontally while the subviews were out of the view hierarchy.)
    // Then set our frame size back so we are hidden again.
    NSSize hiddenSize = [self frame].size;
    [self setFrameSize:NSMakeSize(hiddenSize.width, self.originalHeight)];
    [self resizeSubviewsWithOldSize:self.sizeBeforeHidden];
    [self setFrameSize:hiddenSize];

    [self changeWindowHeightBy:(self.originalHeight - self.hiddenHeight)];

    if (self.nonretainedOriginalNextKeyView) {
        // Restore the key loop to its old configuration.
        self.nonretainedLastChildKeyView.nextKeyView = self.nextKeyView;
        self.nextKeyView = self.nonretainedOriginalNextKeyView;
    }

    [self setNeedsDisplay:YES];
}

- (void)removeSubviews
{
    NSAssert(self.hiddenSubviews == nil, @"-[SNDisclosableView removeSubviews]: should have no hidden subviews yet");

    self.hiddenSubviews = self.subviews;
    NSUInteger subviewIndex = self.hiddenSubviews.count;
    while (subviewIndex--) {
        [self.hiddenSubviews[subviewIndex] removeFromSuperview];
    }
}

- (void)restoreSubviews
{
    NSAssert(self.hiddenSubviews != nil, @"-[SNDisclosableView restoreSubviews]: hiddenSubviews array is nil");

    NSUInteger subviewIndex = self.hiddenSubviews.count;
    while (subviewIndex--) {
        [self addSubview:self.hiddenSubviews[subviewIndex]];
    }

    self.hiddenSubviews = nil;
}

- (void)changeWindowHeightBy:(float)amount
{
    // This turns out to be more complicated than one might expect, because the way that the other views in the window should move is different than the normal case that the AppKit handles.
    //
    // We want the other views in the window to stay the same size. If a view is above us, we want it to stay in the same position relative to the top of the window; likewise, if a view is below us, we want it to stay in the same position relative to the bottom of the window.
    // Also, we want this view to resize vertically, with its top and bottom attached to the top and bottom of its parent.
    // And: this view's subviews should not resize vertically, and should stay attached to the top of this view.
    //
    // However, all of these views may have their autoresize masks configured differently than we want. So:
    //
    // * For each of the window's content view's immediate subviews, including this view,
    //   - Save the current autoresize mask
    //   - And set the autoresize mask how we want
    // * Do the same for the view's subviews.
    // * Then resize the window, and fix up the window's min/max sizes.
    // * For each view that we touched earlier, restore the old autoresize mask.

    NSWindow *window = self.window;

    // Compute the window's new frame.
    NSRect newWindowFrame = window.frame;
    newWindowFrame.origin.y -= amount;
    newWindowFrame.size.height += amount;

    // If we're growing a visible window, will AppKit constrain it?  It might not fit on the screen.
    if (window.isVisible && amount > 0) {
        NSRect constrainedNewWindowFrame = [window constrainFrameRect:newWindowFrame toScreen:window.screen];
        if (constrainedNewWindowFrame.size.height < newWindowFrame.size.height) {
            // We can't actually make the window that size. Something will have to give.
            // Shrink to a height such that, when we grow later on, the window will fit.
            float shrunkenHeight = constrainedNewWindowFrame.size.height - amount;
            NSRect immediateNewFrame = window.frame;
            immediateNewFrame.origin.y += (immediateNewFrame.size.height - shrunkenHeight);
            immediateNewFrame.size.height = shrunkenHeight;
            [window setFrame:immediateNewFrame display:YES animate:YES];

            // Have to recompute based on the new frame...
            newWindowFrame = window.frame;
            newWindowFrame.origin.y -= amount;
            newWindowFrame.size.height += amount;
        }
    }        

    // Now that we're in a configuration where we can change the window's size how we want, start with our current frame.
    NSRect ourFrame = self.frame;
        
    // Adjust the autoresize masks of the window's subviews, remembering the original masks.
    NSArray *windowSubviews = ((NSView *)window.contentView).subviews;
    NSMutableArray *windowSubviewMasks = [NSMutableArray array];
    for (NSView* windowSubview in windowSubviews) {
        NSUInteger mask = windowSubview.autoresizingMask;
        [windowSubviewMasks addObject:@(mask)];

        if (windowSubview == self) {
            // This is us.  Make us stick to the top and bottom of the window, and resize vertically.
            mask |= NSViewHeightSizable;
            mask &= ~NSViewMaxYMargin;
            mask &= ~NSViewMinYMargin;
        } else if (NSMaxY(windowSubview.frame) < NSMaxY(ourFrame)) {
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

        windowSubview.autoresizingMask = mask;
    }

    // Adjust the autoresize masks of our subviews, remembering the original masks.
    NSArray *ourSubviews = self.subviews;
    NSMutableArray *ourSubviewMasks = [NSMutableArray array];
    for (NSView* ourSubview in ourSubviews) {
        NSUInteger mask = ourSubview.autoresizingMask;
        [ourSubviewMasks addObject:@(mask)];

        // Don't change height, and stick to the top of the view.
        mask &= ~NSViewHeightSizable;
        mask &= ~NSViewMaxYMargin;
        mask |= NSViewMinYMargin;

        ourSubview.autoresizingMask = mask;
    }

    // Finally we can resize the window.
    if (window.isVisible) {
        BOOL didPreserve = window.preservesContentDuringLiveResize;
        window.preservesContentDuringLiveResize = NO;

        [window setFrame:newWindowFrame display:YES animate:YES];
        
        window.preservesContentDuringLiveResize = didPreserve;
    } else {
        [window setFrame:newWindowFrame display:NO animate:NO];
    }

    // Adjust the window's min and max sizes to make sense.
    NSSize newWindowMinSize = window.minSize;
    newWindowMinSize.height += amount;
    window.minSize = newWindowMinSize;

    NSSize newWindowMaxSize = window.maxSize;
    // If there is no max size set (height of 0), don't change it.
    if (newWindowMaxSize.height > 0) {
        newWindowMaxSize.height += amount;
        window.maxSize = newWindowMaxSize;
    }

    // Restore the saved autoresize masks.
    [self restoreAutoresizeMasks:windowSubviewMasks toViews:windowSubviews];
    [self restoreAutoresizeMasks:ourSubviewMasks toViews:ourSubviews];
}

- (void)restoreAutoresizeMasks:(NSArray *)masks toViews:(NSArray *)views
{
    for (NSUInteger index = 0, count = masks.count; index < count; index++) {
        ((NSView *)views[index]).autoresizingMask = [masks[index] unsignedIntValue];
    }
}

@end
