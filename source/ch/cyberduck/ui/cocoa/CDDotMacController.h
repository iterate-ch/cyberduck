/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

#import <Cocoa/Cocoa.h>
#import <DotMacKit/DotMacKit.h>

@interface CDDotMacController : NSObject {
	BOOL syncPreferences;
	BOOL syncBookmarks;
	NSException *e;
}

- (DMMemberAccount*)getUserAccount;

- (IBAction)downloadBookmarksFromDotMacAction:(id)sender;
- (IBAction)uploadBookmarksToDotMacAction:(id)sender;

- (IBAction)downloadPreferencesFromDotMacAction:(id)sender;
- (IBAction)uploadPreferencesToDotMacAction:(id)sender;

- (void)downloadFromDotMac:(NSString *)remoteFile usingAccount:(DMMemberAccount*)account;
- (void)uploadToDotMac:(NSString *)localFile usingAccount:(DMMemberAccount*)account;

@end
