/*
 *  Copyright (c) 2004 Whitney Young. All rights reserved.
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

#import "CDTableView.h"

@interface CDTableView (Private)
- (NSTableColumn *)_typeAheadSelectionColumn;
- (void)passSelectString:(NSTimer *)sender;
@end

@implementation CDTableView

- (void)awakeFromNib
{
	select_string = [[NSMutableString alloc] init];
	select_timer = nil;
}

- (void)dealloc
{
	[select_string release];
	[select_timer release];
	[super dealloc];
}

- (void)keyDown:(NSEvent *)event
{
	NSString *str = [event characters];
	char key = [str length]?[str characterAtIndex:0]:'\0';
	
	if (key == NSCarriageReturnCharacter || key == NSEnterCharacter) {
        if ([[self target] respondsToSelector:[self doubleAction]]) {
            [[self target] performSelector:[self doubleAction] withObject:self];
            return;
        }
    } 
	else if (key == NSDeleteCharacter || key == NSBackspaceCharacter) {
        if ([[self target] respondsToSelector:@selector(deleteKeyPerformed:)]) {
            [[self target] performSelector:@selector(deleteKeyPerformed:) withObject:self];
            return;
        }
    }
	
	if ([[NSCharacterSet alphanumericCharacterSet] characterIsMember:key] || (![[NSCharacterSet controlCharacterSet] characterIsMember:key])){
		[select_string appendString:[event charactersIgnoringModifiers]];
		[select_timer invalidate];
		select_timer = [NSTimer scheduledTimerWithTimeInterval:0.4
														target:self 
													  selector:@selector(passSelectString:) 
													  userInfo:nil 
													   repeats:NO];
	} 
	else {
		[super keyDown:event];
	}
}

- (void)passSelectString:(NSTimer *)sender
{
	[select_timer invalidate];
	select_timer = nil;
	
	int row = -1;
	int to_index = 0;
	int smallest_difference = -1;
	int counter;

	NSString *compare = [select_string lowercaseString];
	for (counter = 0; counter < [[self dataSource] numberOfRowsInTableView: self]; counter++) {
		NSString *object = [[[self dataSource] tableView:self 
								objectValueForTableColumn:[self _typeAheadSelectionColumn] 
													  row:counter] lowercaseString];
		if (to_index < [object length] && to_index < [compare length] + 1) {
			if (object && [[object substringToIndex:to_index] isEqualToString:[compare substringToIndex:to_index]])	{
				char one = [compare characterAtIndex:to_index];
				char two = (to_index == [object length])?' ':[object characterAtIndex:to_index];
				int difference = abs(one - two);
				if (difference == 0) {
					while (difference == 0) {
						to_index++;
						if (to_index == [compare length] || to_index == [object length] + 1) { break; } // if we hava an exact match
						one = [compare characterAtIndex:to_index];
						two = (to_index == [object length])?' ':[object characterAtIndex:to_index];
						difference = abs(one - two);
					}
					smallest_difference = -1;
					row = counter;
					if (to_index == [compare length] || to_index == [object length] + 1) { break; } // if we hava an exact match
				} 
				else if (smallest_difference == -1 || difference < smallest_difference) {
					smallest_difference = difference;
					row = counter;
				}
			}
		}
	}
	if (row != -1) {
		[self selectRow:row byExtendingSelection:NO];
		[self scrollRowToVisible:row];
	}
	[select_string setString:@""];
}

- (NSTableColumn *)_typeAheadSelectionColumn;
{
	return [[NSTableColumn alloc] initWithIdentifier:@"TYPEAHEAD"];
}

@end
