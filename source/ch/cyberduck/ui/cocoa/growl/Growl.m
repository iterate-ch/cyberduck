/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

#import "Growl.h"
#import "GrowlDefines.h"
#import <GrowlAppBridge/GrowlApplicationBridge.h>

// Simple utility to convert java strings to NSStrings
NSString *convertToNSString(JNIEnv *env, jstring javaString)
{
    NSString *converted = nil;
    const jchar *unichars = NULL;
	
    if (javaString == NULL) {
        return nil;	
    }                   
    unichars = (*env)->GetStringChars(env, javaString, nil);
    if ((*env)->ExceptionOccurred(env)) {
        return @"";
    }
    converted = [NSString stringWithCharacters:unichars length:(*env)->GetStringLength(env, javaString)]; // auto-released
    (*env)->ReleaseStringChars(env, javaString, unichars);
    return converted;
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_launch(
																  JNIEnv *env, 
																  jobject this)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[Growl defaultInstance] launchGrowl];
	[pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_register(
																	 JNIEnv *env, 
																	 jobject this)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[Growl defaultInstance] registerGrowl];
	[pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notify(
																	 JNIEnv *env, 
																	 jobject this,
																	 jstring title,
																	 jstring description)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[Growl defaultInstance] notifyGrowl:convertToNSString(env, title) 
						 withDescription:convertToNSString(env, description)];
	[pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notifyWithImage(
																			  JNIEnv *env, 
																			  jobject this,
																			  jstring title,
																			  jstring description,
																			  jstring image)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[Growl defaultInstance] notifyGrowl:convertToNSString(env, title) 
						 withDescription:convertToNSString(env, description)
						   withImageName:convertToNSString(env, image)];
	[pool release];
}

#define	GROWL_DOWNLOAD_COMPLETE				NSLocalizedString(@"Download complete", @"Growl Notification")
#define	GROWL_UPLOAD_COMPLETE				NSLocalizedString(@"Upload complete", @"Growl Notification")
#define GROWL_SYNCHRONIZATION_COMPLETE		NSLocalizedString(@"Synchronization complete", @"Growl Notification")
#define	GROWL_CONNECTION_OPENED				NSLocalizedString(@"Connection opened", @"Growl Notification")
#define	GROWL_CONNECTION_FAILED				NSLocalizedString(@"Connection failed", @"Growl Notification")
#define	GROWL_RENDEZVOUS_FOUND_SERVICE		NSLocalizedString(@"Rendezvous", @"Growl Notification")

@implementation Growl

+ (id)defaultInstance
{
	if(nil == instance) {
		instance = [[Growl alloc] init];
	}
	return instance;
}

- (id)init
{
	self = [super init];
	if (self) {
		[[NSDistributedNotificationCenter defaultCenter] addObserver:self 
															selector:@selector(registerGrowlWithContext:) 
																name:GROWL_IS_READY 
															  object:nil];
	}
	return self;
}

- (void)dealloc
{
	[instance release];
	[super dealloc];
}

- (void)launchGrowl
{
	if(NSClassFromString(@"GrowlAppBridge") != nil) 
	{ 
		[NSClassFromString(@"GrowlAppBridge") launchGrowlIfInstalledNotifyingTarget:self
																		   selector:@selector(registerGrowl:)
																			context:nil];
	}
}

- (void)registerGrowl
{
	[self registerGrowlWithContext: nil];
//	if(NSClassFromString(@"GrowlAppBridge") != nil) 
//	{
//		if([NSClassFromString(@"GrowlAppBridge") isGrowlRunning])
//		{
//			[self registerGrowlWithContext: nil];
//		}
//	}
}

- (void)registerGrowlWithContext:(void*)context
{
	NSArray *allNotifications = [NSArray arrayWithObjects:
		GROWL_DOWNLOAD_COMPLETE,
		GROWL_UPLOAD_COMPLETE,
		GROWL_SYNCHRONIZATION_COMPLETE,
		GROWL_CONNECTION_OPENED,
		GROWL_CONNECTION_FAILED,
		GROWL_RENDEZVOUS_FOUND_SERVICE,
		nil];
	NSArray *defaultNotifications = [NSArray arrayWithObjects:
		GROWL_DOWNLOAD_COMPLETE,
		GROWL_UPLOAD_COMPLETE,
		GROWL_SYNCHRONIZATION_COMPLETE,
		GROWL_CONNECTION_OPENED,
		GROWL_CONNECTION_FAILED,
		nil];
	
	NSDictionary *registrationDict = [NSDictionary dictionaryWithObjectsAndKeys:
		@"Cyberduck", GROWL_APP_NAME,
		allNotifications, GROWL_NOTIFICATIONS_ALL,
		defaultNotifications, GROWL_NOTIFICATIONS_DEFAULT,
		nil];
	
	[[NSDistributedNotificationCenter defaultCenter] postNotificationName:GROWL_APP_REGISTRATION
																   object:nil
																 userInfo:registrationDict];
	registered = YES;
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImageName:(NSString *) image
{
	[self notifyGrowl: title withDescription:description withImage:[NSImage imageNamed:image]];
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImage:(NSImage *) image
{
	if(registered) {
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
			title, GROWL_NOTIFICATION_NAME,
			title, GROWL_NOTIFICATION_TITLE,
			[image TIFFRepresentation], GROWL_NOTIFICATION_ICON,
			description, GROWL_NOTIFICATION_DESCRIPTION,
			@"Cyberduck", GROWL_APP_NAME,
			nil];
		
		[[NSDistributedNotificationCenter defaultCenter]
										postNotificationName: GROWL_NOTIFICATION
													  object: nil
													userInfo: event];
	}
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description
{
	if(registered) {
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
			title, GROWL_NOTIFICATION_NAME,
			title, GROWL_NOTIFICATION_TITLE,
			description, GROWL_NOTIFICATION_DESCRIPTION,
			@"Cyberduck", GROWL_APP_NAME,
			nil];
		
		[[NSDistributedNotificationCenter defaultCenter]
										postNotificationName: GROWL_NOTIFICATION
													  object: nil
													userInfo: event];
	}
}

@end
