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

#import "CDBookmarkCell.h"

@implementation CDBookmarkCell

- (id)objectValue 
{
	return bookmark;
}

- (void)setObjectValue:(NSDictionary*)aDict
{
	[bookmark autorelease];
	bookmark = [aDict retain];
}

- (void) dealloc
{
    [bookmark release];
    [super dealloc];
}

- (NSUInteger)hitTestForEvent:(NSEvent *)event inRect:(NSRect)cellFrame ofView:(NSView *)controlView
{
	return NSCellHitContentArea;
}

- (void)drawInteriorWithFrame:(NSRect)cellFrame inView:(NSView *)controlView
{
	static NSMutableParagraphStyle *PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL = nil;
	if(nil == PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL) {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL = [[NSMutableParagraphStyle alloc] init];
        [PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL setParagraphStyle:[NSParagraphStyle defaultParagraphStyle]];
        [PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL setAlignment:NSLeftTextAlignment];
        [PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL setLineBreakMode:NSLineBreakByTruncatingTail];
    }
	
	static NSDictionary *SMALL_BOLD_FONT_ATTRIBUTES = nil;
	if(nil == SMALL_BOLD_FONT_ATTRIBUTES) {
		SMALL_BOLD_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
									  [NSArray arrayWithObjects:[NSFont boldSystemFontOfSize:[NSFont smallSystemFontSize]], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
																 forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSParagraphStyleAttributeName, nil] //keys
									  ];
	}
	
	static NSDictionary *HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES = nil;
	if(nil == HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES) {
		HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
												  [NSArray arrayWithObjects:[NSFont boldSystemFontOfSize:[NSFont smallSystemFontSize]], [NSColor whiteColor], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
																			 forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSForegroundColorAttributeName, NSParagraphStyleAttributeName, nil] //keys
												  ];
	}
	
	static NSDictionary *LARGE_BOLD_FONT_ATTRIBUTES = nil;
	if(nil == LARGE_BOLD_FONT_ATTRIBUTES) {
		LARGE_BOLD_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
									  [NSArray arrayWithObjects:[NSFont boldSystemFontOfSize:[NSFont systemFontSize]], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
																 forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSParagraphStyleAttributeName, nil] //keys
									  ];
	}
	
	static NSDictionary *HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES = nil;
	if(nil == HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES) {
		HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
												  [NSArray arrayWithObjects:[NSFont boldSystemFontOfSize:[NSFont systemFontSize]], [NSColor whiteColor], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
																			 forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSForegroundColorAttributeName, NSParagraphStyleAttributeName, nil] //keys
												  ];
	}
	
	static NSDictionary *SMALL_FONT_ATTRIBUTES = nil;
	if(nil == SMALL_FONT_ATTRIBUTES) {
		SMALL_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
								 [NSArray arrayWithObjects:[NSFont systemFontOfSize:[NSFont labelFontSize]], [NSColor darkGrayColor], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
															forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSForegroundColorAttributeName, NSParagraphStyleAttributeName, nil] //keys
								 ];
	}
	
	static NSDictionary *HIGHLIGHTED_SMALL_FONT_ATTRIBUTES = nil;
	if(nil == HIGHLIGHTED_SMALL_FONT_ATTRIBUTES) {
		HIGHLIGHTED_SMALL_FONT_ATTRIBUTES = [[NSDictionary alloc] initWithObjects:
											 [NSArray arrayWithObjects:[NSFont systemFontOfSize:[NSFont labelFontSize]], [NSColor whiteColor], PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL, nil]
																		forKeys: [NSArray arrayWithObjects:NSFontAttributeName, NSForegroundColorAttributeName, NSParagraphStyleAttributeName, nil] //keys
											 ];
	}
	
    BOOL highlighted = [self isHighlighted] && ![[self highlightColorWithFrame:cellFrame inView:controlView] isEqualTo:[NSColor secondarySelectedControlColor]];
    int size = [[NSUserDefaults standardUserDefaults] integerForKey:@"bookmark.icon.size"];
	
    NSDictionary *nicknameFont;
    if(LARGE_BOOKMARK_SIZE == size) {
        nicknameFont = highlighted ? HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES : LARGE_BOLD_FONT_ATTRIBUTES;
    }
    else {
        nicknameFont = highlighted ? HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES : SMALL_BOLD_FONT_ATTRIBUTES;
    }
    NSDictionary *detailsFont = highlighted ? HIGHLIGHTED_SMALL_FONT_ATTRIBUTES : SMALL_FONT_ATTRIBUTES;
	
    NSLayoutManager *l = [[NSLayoutManager alloc] init];
    float nicknameFontHeight = [l defaultLineHeightForFont:[nicknameFont objectForKey:NSFontAttributeName]] + 2;
    float detailsFontHeight = [l defaultLineHeightForFont:[detailsFont objectForKey:NSFontAttributeName]] + 2;
	[l release];
	
	NSString *nickname = [bookmark objectForKey:@"Nickname"];
	if(nickname) {
		[nickname drawInRect:NSMakeRect(cellFrame.origin.x, cellFrame.origin.y + 1, cellFrame.size.width - 5, cellFrame.size.height) withAttributes:nicknameFont];
    }
    if(SMALL_BOOKMARK_SIZE == size) {
        return;
    }
	NSString *username = [bookmark objectForKey:@"Username"];
	if(username) {
		[username drawInRect:NSMakeRect(cellFrame.origin.x, cellFrame.origin.y + nicknameFontHeight, cellFrame.size.width - 5, cellFrame.size.height) withAttributes:detailsFont];
    }
	NSString *protocol = [bookmark objectForKey:@"Protocol"];
	NSString *hostname = [bookmark objectForKey:@"Hostname"];
	NSString *path = [bookmark objectForKey:@"Path"];
	if(!path) {
		path = @"";
	}
	NSString *url = [NSString stringWithFormat:@"%@://%@%@", protocol, hostname, path];
	[url drawInRect:NSMakeRect(cellFrame.origin.x, cellFrame.origin.y + nicknameFontHeight + detailsFontHeight, 
							   cellFrame.size.width - 5, cellFrame.size.height) withAttributes:detailsFont];
}

@end
