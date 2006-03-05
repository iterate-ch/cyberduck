/**
 *	Cyberduck Dashboard Widget
 *	Copyright (c) 2006 David Kocher. All rights reserved.
 *	http://cyberduck.ch/
 *
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	Bug fixes, suggestions and comments should be sent to:
 *	dkocher@cyberduck.ch
 */

#import "BookmarkPlugin.h"

@implementation BookmarkPlugin

#pragma mark Methods required by the WidgetPlugin protocol

-(id)initWithWebView:(WebView*)w {
	if(self = [super init]) {
		[self loadBookmarks];
	}
	return self;
}

-(void)dealloc {
	if(bookmarks) {
		[bookmarks release];
	}
	[super dealloc];
}

#pragma mark Methods required by the WebScripting protocol

// This method gives you the object that you use to bridge between the
// Obj-C world and the JavaScript world.  Use setValue:forKey: to give
// the object the name it's refered to in the JavaScript side.
-(void)windowScriptObjectAvailable:(WebScriptObject*)wso {
	[wso setValue:self forKey:@"Plugin"];
}

// This method lets you offer friendly names for methods that normally 
// get mangled when bridged into JavaScript.
+(NSString*)webScriptNameForSelector:(SEL)selector {
	if (selector == @selector(loadBookmarks)) {
		return @"loadBookmarks";
	} 
	if (selector == @selector(numberOfBookmarks)) {
		return @"numberOfBookmarks";
	} 
	if (selector == @selector(nicknameAtIndex:)) {
		return @"nicknameAtIndex";
	} 
	if (selector == @selector(hostnameAtIndex:)) {
		return @"hostnameAtIndex";
	} 
	if (selector == @selector(usernameAtIndex:)) {
		return @"usernameAtIndex";
	} 
	if (selector == @selector(pathAtIndex:)) {
		return @"pathAtIndex";
	} 
	if (selector == @selector(protocolAtIndex:)) {
		return @"protocolAtIndex";
	} 
	return nil;
}

// This method lets you filter which methods in your plugin are accessible 
// to the JavaScript side.
+(BOOL)isSelectorExcludedFromWebScript:(SEL)selector {
	if (selector == @selector(loadBookmarks)) {
		return NO;
	}
	if (selector == @selector(numberOfBookmarks)) {
		return NO;
	} 
	if (selector == @selector(nicknameAtIndex:)) {
		return NO;
	} 
	if (selector == @selector(hostnameAtIndex:)) {
		return NO;
	} 
	if (selector == @selector(usernameAtIndex:)) {
		return NO;
	} 
	if (selector == @selector(pathAtIndex:)) {
		return NO;
	} 
	if (selector == @selector(protocolAtIndex:)) {
		return NO;
	} 
	return YES;
}

// Prevents direct key access from JavaScript.
+(BOOL)isKeyExcludedFromWebScript:(const char*)k {
	return YES;
}

#pragma mark The actual methods used in this plugin, to be called by JavaScript and identified in isSelectorExcludedFromWebScript:.

- (void) loadBookmarks {
	bookmarks = [[NSMutableArray alloc] init];
	NSData *propertyListXMLData;
	NSString *favorites = [[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] 
								stringByAppendingPathComponent:@"Application Support"] 
								stringByAppendingPathComponent:@"Cyberduck"] 
								stringByAppendingPathComponent:@"Favorites.plist"];
	NSFileManager *manager = [NSFileManager defaultManager];
	if ([manager fileExistsAtPath:favorites]) {
		id plistData = [[NSData alloc] initWithContentsOfFile: favorites];
		if(plistData) {
			NSString *error;
			propertyListXMLData = [NSPropertyListSerialization propertyListFromData:plistData 
																   mutabilityOption:NSPropertyListImmutable
																			 format:nil 
																   errorDescription:&error];
			if (error) {
				NSLog(@"Problem reading bookmark file: %@", error);
				[error release];
			}
			NSEnumerator *enumerator = [(NSArray *)propertyListXMLData objectEnumerator];
			id bookmark;
			while (bookmark = [enumerator nextObject]) {
				[bookmarks addObject:bookmark];
			}
		}
	}
}

- (NSNumber*) numberOfBookmarks {
	return [NSNumber numberWithInt:[bookmarks count]];
}

- (NSString*) nicknameAtIndex:(int)index {
	return [[bookmarks objectAtIndex:index] objectForKey:@"Nickname"];
}

- (NSString*) hostnameAtIndex:(int)index {
	return [[bookmarks objectAtIndex:index] objectForKey:@"Hostname"];
}

- (NSString*) usernameAtIndex:(int)index {
	return [[bookmarks objectAtIndex:index] objectForKey:@"Username"];
}

- (NSString*) pathAtIndex:(int)index {
	return [[bookmarks objectAtIndex:index] objectForKey:@"Path"];
}

- (NSString*) protocolAtIndex:(int)index {
	return [[bookmarks objectAtIndex:index] objectForKey:@"Protocol"];
}

@end
