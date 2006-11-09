/* =============================================================================
	FILE:		UKKQueue.h
	PROJECT:	Filie
    
    COPYRIGHT:  (c) 2003 M. Uli Kusterer, all rights reserved.
    
	AUTHORS:	M. Uli Kusterer - UK
    
    LICENSES:   GPL, Modified BSD

	REVISIONS:
		2003-12-21	UK	Created.
   ========================================================================== */

// -----------------------------------------------------------------------------
//  Headers:
// -----------------------------------------------------------------------------

#import <Foundation/Foundation.h>
#include <sys/types.h>
#include <sys/event.h>


// -----------------------------------------------------------------------------
//  Constants:
// -----------------------------------------------------------------------------

// Flags for notifyingAbout:
#define UKKQueueNotifyAboutRename					NOTE_RENAME		// Item was renamed.
#define UKKQueueNotifyAboutWrite					NOTE_WRITE		// Item contents changed (also folder contents changed).
#define UKKQueueNotifyAboutDelete					NOTE_DELETE		// item was removed.
#define UKKQueueNotifyAboutAttributeChange			NOTE_ATTRIB		// Item attributes changed.
#define UKKQueueNotifyAboutSizeIncrease				NOTE_EXTEND		// Item size increased.
#define UKKQueueNotifyAboutLinkCountChanged			NOTE_LINK		// Item's link count changed.
#define UKKQueueNotifyAboutAccessRevocation			NOTE_REVOKE		// Access to item was revoked.

// Notifications this sends:
//  (object is the file path registered with, and these are sent via the workspace notification center)
#define UKKQueueFileRenamedNotification				@"UKKQueueFileRenamedNotification"
#define UKKQueueFileWrittenToNotification			@"UKKQueueFileWrittenToNotification"
#define UKKQueueFileDeletedNotification				@"UKKQueueFileDeletedNotification"
#define UKKQueueFileAttributesChangedNotification   @"UKKQueueFileAttributesChangedNotification"
#define UKKQueueFileSizeIncreasedNotification		@"UKKQueueFileSizeIncreasedNotification"
#define UKKQueueFileLinkCountChangedNotification	@"UKKQueueFileLinkCountChangedNotification"
#define UKKQueueFileAccessRevocationNotification	@"UKKQueueFileAccessRevocationNotification"


// -----------------------------------------------------------------------------
//  UKKQueue:
// -----------------------------------------------------------------------------

@interface UKKQueue : NSObject
{
	int				queueFD;		// The actual queue ID.
	NSMutableArray* watchedPaths;   // List of NSStrings containing the paths we're watching.
	NSMutableArray* watchedFDs;		// List of NSNumbers containing the file descriptors we're watching.
	id				delegate;		// Gets messages about changes instead of notification center, if specified.
	id				delegateProxy;	// Proxy object to which we send messages so they reach delegate on the main thread.
	BOOL			alwaysNotify;	// Send notifications even if we have a delegate? Defaults to NO.
}

+ (id)sharedUKKQueue;

-(int)  queueFD;		// I know you unix geeks want this...

// High-level file watching:
-(void) addPathToQueue: (NSString*)path;
-(void) addPathToQueue: (NSString*)path notifyingAbout: (u_int)fflags;
-(void) removePathFromQueue: (NSString*)path;

-(id)	delegate;
-(void)	setDelegate: (id)newDelegate;

-(BOOL)	alwaysNotify;
-(void)	setAlwaysNotify: (BOOL)n;

// private:
-(void)		watcherThread: (id)sender;
-(void)		postNotification: (NSString*)nm forFile: (NSString*)fp; // Message-posting bottleneck.

@end


// -----------------------------------------------------------------------------
//  Methods delegates need to provide:
// -----------------------------------------------------------------------------

@interface NSObject (UKKQueueDelegate)

-(void) kqueue: (UKKQueue*)kq receivedNotification: (NSString*)nm forFile: (NSString*)fpath;

@end
