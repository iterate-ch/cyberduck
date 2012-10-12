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

#import "NSAppleEventDescriptor-Extensions.h"
#import "ODBEditorSuite.h"
#import <Carbon/Carbon.h>
#import <Cocoa/Cocoa.h>

extern NSString * const ODBEditorCustomPathKey;

@interface ODBEditor : NSObject
{
	UInt32					_signature;
	NSString				*_editorBundleIdentifier;
	NSMutableDictionary		*_filesBeingEdited;
}

+ (id)sharedODBEditor:(NSString *)editorBundleIdentifier;

- (void)setEditorBundleIdentifier:(NSString *)bundleIdentifier;
- (NSString *)editorBundleIdentifier;

//- (void)abortEditingFile:(NSString *)path;
//- (void)abortAllEditingSessionsForClient:(id)client;

	// NOTE that client is never retained - it is your reponsibility to
	// make sure the client sticks around and abort editing for that client
	// before it is dealloc'd
	//
	// Also note that while it is possible to start several editString
	// sessions for a single client it is the client's responsibility to
	// distinguish between the sessions (possibly using the original
	// context that you supplied.) It is also the clients responsibility to
	// do the same for file editing sessions, but this should be easier
	// since the file path will remain static (except in the save as case)
	// whereas the string returned is obviously going to change as the user
	// edits it.

- (BOOL)editFile:(NSString *)path options:(NSDictionary *)options forClient:(id)client context:(void *)context;

@end

@interface NSObject(ODBEditorClient)

// see the ODB Editor documentation for when newFileLocation is sent
// if the file wasn't subject to a save as newpath will be nil

-(void)odbEditor:(ODBEditor *)editor didModifyFile:(NSString *)path newFileLocation:(NSString *)newPath  context:(NSDictionary *)context;
-(void)odbEditor:(ODBEditor *)editor didClosefile:(NSString *)path context:(NSDictionary *)context;

@end
