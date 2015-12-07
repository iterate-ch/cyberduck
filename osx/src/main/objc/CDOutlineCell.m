/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

#import "CDOutlineCell.h"

@implementation CDOutlineCell

 - (id)init {
    if ((self = [super init])) {
        [self setLineBreakMode:NSLineBreakByTruncatingTail];
        [self setSelectable:YES];
    }
    return self;
}

- (void)setIcon:(NSImage *)aIcon
{
    if(icon == aIcon) {
       return;
    }
    [icon release];
    icon = [aIcon retain];
}

- (NSImage*)icon
{
    return icon;
}

- (id)copyWithZone:(NSZone *)zone
{
    CDOutlineCell *cell = (CDOutlineCell *)[super copyWithZone:zone];
    if (cell != nil) {
       cell->icon = nil;
       [cell setIcon:[self icon]];
    }
    return cell;
}

 - (void)dealloc
{
    [icon release];
    [super dealloc];
}

- (NSRect)imageRectForBounds:(NSRect)cellFrame {
    NSRect result;
    if (icon != nil) {
       result.size = NSMakeSize(16, 16);
       result.origin = cellFrame.origin;
       result.origin.x += 3;
       result.origin.y += ceil((cellFrame.size.height - result.size.height) / 2);
    } else {
       result = NSZeroRect;
    }
    return result;
}

- (NSRect)titleRectForBounds:(NSRect)cellFrame {
    NSRect result;
    if (icon != nil) {
       CGFloat imageWidth = NSMakeSize(16, 16).width;
       result = cellFrame;
       result.origin.x += (3 + imageWidth);
       result.size.width -= (3 + imageWidth);
    } else {
       result = [super titleRectForBounds:cellFrame];
    }
    return result;
}

- (void)editWithFrame:(NSRect)aRect inView:(NSView *)controlView editor:(NSText *)textObj delegate:(id)anObject event:(NSEvent *)theEvent {
    [super editWithFrame:[self titleRectForBounds:aRect] inView:controlView editor:textObj delegate:anObject event:theEvent];
}

- (void)selectWithFrame:(NSRect)aRect inView:(NSView *)controlView editor:(NSText *)textObj delegate:(id)anObject start:(NSInteger)selStart length:(NSInteger)selLength {
    [super selectWithFrame:[self titleRectForBounds:aRect] inView:controlView editor:textObj delegate:anObject start:selStart length:selLength];
}

- (void)drawWithFrame:(NSRect)cellFrame inView:(NSView *)controlView {
    if (icon != nil) {
       NSRect imageFrame = [self imageRectForBounds:cellFrame];
       [icon drawInRect:imageFrame fromRect:NSZeroRect operation:NSCompositeSourceOver fraction:1.0 respectFlipped:YES hints:nil];
       NSInteger newX = NSMaxX(imageFrame) + 3;
       cellFrame.size.width = NSMaxX(cellFrame) - newX;
       cellFrame.origin.x = newX;
    }
    [super drawWithFrame:cellFrame inView:controlView];
}

- (NSSize)cellSize {
    NSSize cellSize = [super cellSize];
    if (icon != nil) {
       cellSize.width += NSMakeSize(16, 16).width;
    }
    cellSize.width += 3;
    return cellSize;
}
 
- (NSCellHitResult)hitTestForEvent:(NSEvent *)event inRect:(NSRect)cellFrame ofView:(NSView *)controlView {
    NSPoint point = [controlView convertPoint:[event locationInWindow] fromView:nil];
    // If we have an image, we need to see if the user clicked on the image portion.
    if (icon != nil) {
       // This code closely mimics drawWithFrame:inView:
       NSSize imageSize = NSMakeSize(16, 16);
       NSRect imageFrame;
       NSDivideRect(cellFrame, &imageFrame, &cellFrame, 3 + imageSize.width, NSMinXEdge);

       imageFrame.origin.x += 3;
       imageFrame.size = imageSize;
       // If the point is in the image rect, then it is a content hit
       if (NSMouseInRect(point, imageFrame, [controlView isFlipped])) {
          // We consider this just a content area. It is not trackable, nor it it editable text. If it was, we would or in the additional items.
          // By returning the correct parts, we allow NSTableView to correctly begin an edit when the text portion is clicked on.
          return NSCellHitContentArea;
       }
    }
    // At this point, the cellFrame has been modified to exclude the portion for the image. Let the superclass handle the hit testing at this point.
    return [super hitTestForEvent:event inRect:cellFrame ofView:controlView];
}

@end
