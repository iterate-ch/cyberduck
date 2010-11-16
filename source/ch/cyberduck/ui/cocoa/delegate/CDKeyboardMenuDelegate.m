/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
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

#import "CDKeyboardMenuDelegate.h"

@implementation CDKeyboardMenuDelegate

- (BOOL)menuHasKeyEquivalent:(NSMenu *)menu forEvent:(NSEvent *)event target:(id *)target action:(SEL *)action {
	NSLog(@"menuHasKeyEquivalent");
    if([self respondsToSelector:@selector(hasKeyEquivalent:)]) {
		NSLog(@"menuHasKeyEquivalent responds to hasKeyEquivalent");
        if([self performSelector:@selector(hasKeyEquivalent:) withObject:event]) {
            //action = NSSelectorFromString([self performSelector:@selector(getActionForKeyEquivalent:) withObject:event]);
            //target = nil;
            //return YES;
        }
    }
	return NO;
}

@end

