//
//  DashboardPlugin.h
//  Cyberduck Dashboard Widget
//
//  Created by David V. Kocher on 04.03.06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <WebKit/WebKit.h>

@interface BookmarkPlugin : NSObject {
	NSMutableArray *bookmarks;
}

- (void) loadBookmarks;
- (NSNumber*) numberOfBookmarks;
- (NSString*) nicknameAtIndex:(int)index;
- (NSString*) hostnameAtIndex:(int)index;
- (NSString*) usernameAtIndex:(int)index;
- (NSString*) pathAtIndex:(int)index;
- (NSString*) protocolAtIndex:(int)index;

@end
