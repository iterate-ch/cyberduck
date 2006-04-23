/*-------------------------------------------------------------------------------------------------------*\
| Adium, Copyright (C) 2001-2005, Adam Iser  (adamiser@mac.com | http://www.adiumx.com)                   |
\---------------------------------------------------------------------------------------------------------/
 | This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 | General Public License as published by the Free Software Foundation; either version 2 of the License,
 | or (at your option) any later version.
 |
 | This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 | the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 | Public License for more details.
 |
 | You should have received a copy of the GNU General Public License along with this program; if not,
 | write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 \------------------------------------------------------------------------------------------------------ */

#import "AIAutoScrollView.h"

#define AUTOSCROLL_CATCH_SIZE 	20	//The distance (in pixels) that the scrollview must be within (from the bottom) for auto-scroll to kick in.

@interface AIAutoScrollView (PRIVATE)
- (void)_initAutoScrollView;
@end

@implementation AIAutoScrollView

/*
 A subclass of NSScrollView that:

    - Automatically scrolls to bottom on new content
	- Shows a focus ring even if the contained view would not normally show one (an NSTextView, for example)
 */


- (id)initWithCoder:(NSCoder *)aDecoder
{
	if ((self = [super initWithCoder:aDecoder])) {
		[self _initAutoScrollView];
	}
	return self;
}

- (id)initWithFrame:(NSRect)frameRect
{
	if ((self = [super initWithFrame:frameRect])) {
		[self _initAutoScrollView];
	}
	return self;
}

- (void)_initAutoScrollView
{
    autoScrollToBottom = NO;
	inAutoScrollToBottom = NO;
	passKeysToDocumentView = NO;
	
	//Focus ring
	alwaysDrawFocusRingIfFocused = NO;
	lastResp = nil;
	shouldDrawFocusRing = NO;
	
    [self setAutohidesScrollers:YES];
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self
													name:NSViewFrameDidChangeNotification 
												  object:nil];

    [super dealloc];
}

//Auto Scrolling ---------------------------------------------------------------
#pragma mark Auto scrolling

- (void)setAutoScrollToBottom:(BOOL)inValue
{
    autoScrollToBottom = inValue;

    [self setDocumentView:[self documentView]];
    [self setFrame:[self frame]];
}

- (void)setDocumentView:(NSView *)aView
{
    [super setDocumentView:aView];

    //Observe the document view's frame changes
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NSViewFrameDidChangeNotification object:nil];

    if (autoScrollToBottom) {
        [[NSNotificationCenter defaultCenter] addObserver:self 
												 selector:@selector(documentFrameDidChange:) 
													 name:NSViewFrameDidChangeNotification
												   object:aView];

        [self scrollToBottom];
    }

    oldDocumentFrame = [aView frame];
}

//Our frame changes
- (void)setFrame:(NSRect)frameRect
{
    BOOL 	autoScroll = NO;

    if (autoScrollToBottom) {
        NSRect	documentVisibleRect = [self documentVisibleRect];
        NSRect	documentFrame = [[self documentView] frame];

        //Autoscroll if we're scrolled close to the bottom
        autoScroll = ((documentVisibleRect.origin.y + documentVisibleRect.size.height) > (documentFrame.size.height - AUTOSCROLL_CATCH_SIZE));
    }

    //Set our frame first
    [super setFrame:frameRect];

    //Then auto-scroll
    if (autoScroll) [self scrollToBottom];
}

//When our document resizes
- (void)documentFrameDidChange:(NSNotification *)notification
{
	//We guard against a recursive call to this method, which may occur if the user is resizing the view at the same time
	//content is being modified
    if (autoScrollToBottom && !inAutoScrollToBottom) {
        NSRect	documentVisibleRect =  [self documentVisibleRect];
        NSRect	   newDocumentFrame = [[self documentView] frame];
        
        //We autoscroll if the height of the document frame changed AND (Using the old frame to calculate) we're scrolled close to the bottom.
        if ((newDocumentFrame.size.height != oldDocumentFrame.size.height) && 
		   ((documentVisibleRect.origin.y + documentVisibleRect.size.height) > (oldDocumentFrame.size.height - AUTOSCROLL_CATCH_SIZE))) {
			inAutoScrollToBottom = YES;
            [self scrollToBottom];
			inAutoScrollToBottom = NO;
        }
    
        //Remember the new frame
        oldDocumentFrame = newDocumentFrame;
    }
}

//Scroll to the top of our view
- (void)scrollToTop
{    
    [[self documentView] scrollPoint:NSZeroPoint];
}

//Scroll to the bottom of our view
- (void)scrollToBottom
{
    [[self documentView] scrollPoint:NSMakePoint(0, 1000000)];
}

//Key forwarding ----------------------
#pragma mark Key Forwarding
- (void)setPassKeysToDocumentView:(BOOL)inValue
{
	passKeysToDocumentView = inValue;
}

- (void)keyDown:(NSEvent*)theEvent
{
	NSString *charactersIgnoringModifiers = [theEvent charactersIgnoringModifiers];
	
	if ([charactersIgnoringModifiers length]) {
		unichar inChar = [charactersIgnoringModifiers characterAtIndex:0];
		
		switch (inChar)
		{
			case NSUpArrowFunctionKey:
			{
				NSRect visibleRect = [self documentVisibleRect];
				visibleRect.origin.y -= [self verticalLineScroll]*2;
				[[self documentView] scrollRectToVisible:visibleRect]; 
				break;
			}
				
			case NSDownArrowFunctionKey:
			{
				NSRect visibleRect = [self documentVisibleRect];
				visibleRect.origin.y += [self verticalLineScroll]*2;
				[[self documentView] scrollRectToVisible:visibleRect]; 
				break;
			}
				
			case NSPageUpFunctionKey:
			{
				[self pageUp:nil];
				break;
			}
				
			case NSPageDownFunctionKey:
			{
				[self pageDown:nil];
				break;
			}
				
			case NSHomeFunctionKey:
			{
				NSRect visibleRect = [self documentVisibleRect];
				visibleRect.origin.y = 0;
				[[self documentView] scrollRectToVisible:visibleRect]; 
				break;
			}
				
			case NSEndFunctionKey:
			{
				NSRect frame = [[self documentView] frame];
				frame.origin.y = frame.size.height;
				frame.size.height = 0;
				[[self documentView] scrollRectToVisible:frame];
				break;
			}
				
			default:
			{
				if (passKeysToDocumentView) {
					[[self documentView] keyDown:theEvent];
				} else {
					[super keyDown:theEvent];
				}
				break;
			}
		}
	} else {
		if (passKeysToDocumentView) {
			[[self documentView] keyDown:theEvent];
		} else {
			[super keyDown:theEvent];
		}	
	}
}

// Drawing ------------------------------------------------------------------------
#pragma mark Drawing
- (void)setAlwaysDrawFocusRingIfFocused:(BOOL)inFlag
{
	alwaysDrawFocusRingIfFocused = inFlag;
	shouldDrawFocusRing = NO;
	lastResp = nil;
}

//Focus ring drawing code by Nicholas Riley, posted on cocoadev and available at:
//http://cocoa.mamasam.com/COCOADEV/2002/03/2/29535.php
- (BOOL)needsDisplay
{
	if (alwaysDrawFocusRingIfFocused) {
		NSResponder *resp = nil;
		NSWindow	*window = [self window];
		
		if ([window isKeyWindow]) {
			resp = [window firstResponder];
			if (resp == lastResp) {
				return [super needsDisplay];
			}
			
		} else if (lastResp == nil) {
			return [super needsDisplay];
			
		}
		
		shouldDrawFocusRing = (resp != nil &&
							   [resp isKindOfClass:[NSView class]] &&
							   [(NSView *)resp isDescendantOf:self]); // [sic]
		lastResp = resp;
		
		[self setKeyboardFocusRingNeedsDisplayInRect:[self bounds]];
		return YES;
	} else {
		return [super needsDisplay];
	}
}

//Draw a focus ring around our view
- (void)drawRect:(NSRect)rect
{
	[super drawRect:rect];
	
	if (shouldDrawFocusRing) {
		NSSetFocusRingStyle(NSFocusRingOnly);
		NSRectFill(rect);
	}
} 

@end


