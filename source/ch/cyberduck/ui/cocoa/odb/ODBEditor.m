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

#import "ODBEditor.h"

NSString * const ODBEditorCustomPathKey		= @"ODBEditorCustomPath";
NSString * const ODBEditorNonRetainedClient = @"ODBEditorNonRetainedClient";
NSString * const ODBEditorClientContext		= @"ODBEditorClientContext";
NSString * const ODBEditorFileName			= @"ODBEditorFileName";
NSString * const ODBEditorIsEditingString	= @"ODBEditorIsEditingString";

@interface ODBEditor(Private)

- (BOOL)_launchExternalEditor;

- (BOOL)_editFile:(NSString *)path isEditingString:(BOOL)editingStringFlag options:(NSDictionary *)options forClient:(id)client context:(NSDictionary *)context;

- (void)handleModifiedFileEvent:(NSAppleEventDescriptor *)event withReplyEvent:(NSAppleEventDescriptor *)replyEvent;

- (void)handleClosedFileEvent:(NSAppleEventDescriptor *)event withReplyEvent:(NSAppleEventDescriptor *)replyEvent;

@end

@implementation ODBEditor

static ODBEditor	*_sharedODBEditor;

+ (id)sharedODBEditor:(NSString *)editorBundleIdentifier
{
	if (_sharedODBEditor == nil)
	{
		_sharedODBEditor = [[ODBEditor alloc] init];
	}
	[_sharedODBEditor setEditorBundleIdentifier:editorBundleIdentifier];
	return _sharedODBEditor;
}

- (id)init
{
	self = [super init];
	
	if (self != nil)
	{
		UInt32  packageType = 0;
		UInt32  packageCreator = 0;
		
		if (_sharedODBEditor != nil)
		{
			[self autorelease];
			[NSException raise: NSInternalInconsistencyException format: @"ODBEditor is a singleton - use [ODBEditor sharedODBEditor]"];
			
			return nil;
		}
		
		// our initialization
		
		CFBundleGetPackageInfo(CFBundleGetMainBundle(), &packageType, &packageCreator);
		_signature = packageCreator;
		
		_filesBeingEdited = [[NSMutableDictionary alloc] init];
		
		// setup our event handlers
		
		NSAppleEventManager *appleEventManager = [NSAppleEventManager sharedAppleEventManager];
		
		[appleEventManager setEventHandler: self andSelector: @selector(handleModifiedFileEvent:withReplyEvent:) forEventClass: kODBEditorSuite andEventID: kAEModifiedFile];
		
		[appleEventManager setEventHandler: self andSelector: @selector(handleClosedFileEvent:withReplyEvent:) forEventClass: kODBEditorSuite andEventID: kAEClosedFile];
	}
	
	return self;
}

- (void)dealloc
{
	NSAppleEventManager *appleEventManager = [NSAppleEventManager sharedAppleEventManager];
	
	[appleEventManager removeEventHandlerForEventClass: kODBEditorSuite andEventID: kAEModifiedFile];
	[appleEventManager removeEventHandlerForEventClass: kODBEditorSuite andEventID: kAEClosedFile];
	
	[_editorBundleIdentifier release];
	[_filesBeingEdited release];
	
	[super dealloc];
}

- (void)setEditorBundleIdentifier:(NSString *)bundleIdentifier
{
	[_editorBundleIdentifier autorelease];
	_editorBundleIdentifier = [bundleIdentifier copy];
}

- (NSString *)editorBundleIdentifier
{
	return _editorBundleIdentifier;
}

- (BOOL)editFile:(NSString *)path options:(NSDictionary *)options forClient:(id)client context:(void *)context
{
	return [self _editFile: path isEditingString: NO options: options forClient: client context: context];
}

@end

@implementation ODBEditor(Private)

- (BOOL)_launchExternalEditor
{
	BOOL success = NO;
	BOOL running = NO;
	NSWorkspace *workspace = [NSWorkspace sharedWorkspace];
	NSArray *runningApplications = [workspace launchedApplications];
	NSEnumerator *enumerator = [runningApplications objectEnumerator];
	NSDictionary *applicationInfo;
	
	while (nil != (applicationInfo = [enumerator nextObject]))
	{
		NSString	*bundleIdentifier = [applicationInfo objectForKey: @"NSApplicationBundleIdentifier"];
		
		if ([bundleIdentifier isEqualToString: _editorBundleIdentifier])
		{
			running = YES;
			// bring the app forward
			success = [workspace launchApplication: [applicationInfo objectForKey: @"NSApplicationPath"]];
			break;
		}
	}
	
	if (running == NO)
	{
		success = [workspace launchAppWithBundleIdentifier: _editorBundleIdentifier options: 0L additionalEventParamDescriptor: nil launchIdentifier: nil];
	}
	
	return success;
}

- (BOOL)_editFile:(NSString *)fileName isEditingString:(BOOL)editingStringFlag options:(NSDictionary *)options forClient:(id)client context:(NSDictionary *)context
{
	BOOL success = NO;
	OSStatus status = noErr;
	NSData *targetBundleID = [_editorBundleIdentifier dataUsingEncoding: NSUTF8StringEncoding];
	NSAppleEventDescriptor  *targetDescriptor = [NSAppleEventDescriptor descriptorWithDescriptorType: typeApplicationBundleID
																								data: targetBundleID];
	NSAppleEventDescriptor  *appleEvent = [NSAppleEventDescriptor appleEventWithEventClass: kCoreEventClass
																				   eventID: kAEOpenDocuments
																		  targetDescriptor: targetDescriptor
																				  returnID: kAutoGenerateReturnID
																		     transactionID: kAnyTransactionID];
	NSAppleEventDescriptor *replyDescriptor = nil;
	NSAppleEventDescriptor *errorDescriptor = nil;
	AEDesc reply = {typeNull, NULL};														
	NSString *customPath = [options objectForKey: ODBEditorCustomPathKey];
	
	[self _launchExternalEditor];
	
	// add the file
	fileName = [fileName stringByResolvingSymlinksInPath];
	
	[appleEvent setParamDescriptor: [NSAppleEventDescriptor descriptorWithFilePath: fileName] forKeyword: keyDirectObject];
	
	// add our signature
	
	[appleEvent setParamDescriptor: [NSAppleEventDescriptor descriptorWithTypeCode: _signature] forKeyword: keyFileSender];
	
	// custom path
	
	if (customPath != nil) {
		[appleEvent setParamDescriptor: [NSAppleEventDescriptor descriptorWithString: customPath] forKeyword: keyFileCustomPath];
    }
	
	// send the event
	
	status = AESend([appleEvent aeDesc], &reply, kAEWaitReply, kAENormalPriority, kAEDefaultTimeout, NULL, NULL);		
	
	if (status == noErr)
	{
		replyDescriptor = [[[NSAppleEventDescriptor alloc] initWithAEDescNoCopy: &reply] autorelease];
		errorDescriptor = [replyDescriptor paramDescriptorForKeyword: keyErrorNumber];
		
		if (errorDescriptor != nil)
		{
			status = [errorDescriptor int32Value];
		}
		
		if (status == noErr)
		{
			// save off some information that we'll need when we get called back
			
			NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
			
			[dictionary setObject: [NSValue valueWithNonretainedObject: client] forKey: ODBEditorNonRetainedClient];
			if (context != NULL)
				[dictionary setObject: [NSValue valueWithPointer: context] forKey: ODBEditorClientContext];
			[dictionary setObject: fileName forKey: ODBEditorFileName];
			[dictionary setObject: [NSNumber numberWithBool: editingStringFlag] forKey: ODBEditorIsEditingString];
			
			[_filesBeingEdited setObject: dictionary forKey: fileName];
		}
	}
	
	success = (status == noErr);
	return success;
}

- (void)handleModifiedFileEvent:(NSAppleEventDescriptor *)event withReplyEvent:(NSAppleEventDescriptor *)replyEvent
{
	NSAppleEventDescriptor  *descriptor = [[event paramDescriptorForKeyword: keyDirectObject] coerceToDescriptorType: typeFileURL];
	NSString *urlString = [[[NSString alloc] initWithData: [descriptor data] encoding: NSUTF8StringEncoding] autorelease];
	if (nil == urlString)
	{
		NSLog(@"handleModifiedFileEvent: No URL given.");
		return;
	}
	NSString *fileName = [[[NSURL URLWithString: urlString] path] stringByResolvingSymlinksInPath];
	NSDictionary *dictionary = nil;
	dictionary = [_filesBeingEdited objectForKey: fileName];
	if (nil == dictionary)
	{
		NSLog(@"handleModifiedFileEvent: Got ODB editor event for unknown file.");
		return;
	}
	id  client = [[dictionary objectForKey: ODBEditorNonRetainedClient] nonretainedObjectValue];
	[client odbEditor: self didModifyFile: fileName newFileLocation: nil context: nil];
}

- (void)handleClosedFileEvent:(NSAppleEventDescriptor *)event withReplyEvent:(NSAppleEventDescriptor *)replyEvent
{
	NSAppleEventDescriptor *descriptor = [[event paramDescriptorForKeyword: keyDirectObject] coerceToDescriptorType: typeFileURL];
	NSString *urlString = [[[NSString alloc] initWithData: [descriptor data] encoding: NSUTF8StringEncoding] autorelease];
	if (nil == urlString)
	{
		NSLog(@"handleClosedFileEvent: No URL given.");
		return;
	}
	NSString *fileName = [[[NSURL URLWithString: urlString] path] stringByResolvingSymlinksInPath];
	NSDictionary *dictionary = nil;
	dictionary = [_filesBeingEdited objectForKey: fileName];
	if (nil == dictionary)
	{
		NSLog(@"handleClosedFileEvent: Got ODB editor event for unknown file.");
		return;
	}
	id client = [[dictionary objectForKey: ODBEditorNonRetainedClient] nonretainedObjectValue];
	void *context = [[dictionary objectForKey: ODBEditorClientContext] pointerValue];
	[client odbEditor: self didClosefile: fileName context: context];
	[_filesBeingEdited removeObjectForKey: fileName];
}

@end

