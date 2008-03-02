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

#import "Growl.h"

// Simple utility to convert java strings to NSStrings
NSString *convertToNSString(JNIEnv *env, jstring javaString)
{
    NSString *converted = nil;
    const jchar *unichars = NULL;
    
    if (javaString == NULL) {
        return nil; 
    }                   
    unichars = (*env)->GetStringChars(env, javaString, NULL);
    if ((*env)->ExceptionOccurred(env)) {
        return @"";
    }
    converted = [NSString stringWithCharacters:unichars length:(*env)->GetStringLength(env, javaString)]; // auto-released
    (*env)->ReleaseStringChars(env, javaString, unichars);
    return converted;
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_GrowlNative_register(
                                                                     JNIEnv *env, 
                                                                     jobject this)
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    [[Growl defaultInstance] registerGrowl];
    [pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_GrowlNative_notify(
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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_GrowlNative_notifyWithImage(
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

#define GROWL_DOWNLOAD_COMPLETE             NSLocalizedStringFromTable(@"Download complete", @"Growl", @"Growl Notification")
#define GROWL_UPLOAD_COMPLETE               NSLocalizedStringFromTable(@"Upload complete", @"Growl", @"Growl Notification")
#define GROWL_SYNCHRONIZATION_COMPLETE      NSLocalizedStringFromTable(@"Synchronization complete", @"Growl", @"Growl Notification")
#define GROWL_CONNECTION_OPENED             NSLocalizedStringFromTable(@"Connection opened", @"Growl", @"Growl Notification")
#define GROWL_CONNECTION_FAILED             NSLocalizedStringFromTable(@"Connection failed", @"Growl", @"Growl Notification")
#define GROWL_DOWNLOAD_FAILED               NSLocalizedStringFromTable(@"Download failed", @"Growl", @"Growl Notification")
#define GROWL_UPLOAD_FAILED                 NSLocalizedStringFromTable(@"Upload failed", @"Growl", @"Growl Notification")
#define GROWL_QUEUED                        NSLocalizedStringFromTable(@"Transfer queued", @"Growl", @"Growl Notification")
#define GROWL_RENDEZVOUS_FOUND_SERVICE      @"Bonjour"

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
    return [super init];
}

- (void)dealloc
{
    [instance release];
    [super dealloc];
}

- (void)registerGrowl
{
    [GrowlApplicationBridge setGrowlDelegate:self];
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImageName:(NSString *) image
{
    [self notifyGrowl: title withDescription:description withImage:[NSImage imageNamed:image]];
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImage:(NSImage *) image
{
    [GrowlApplicationBridge notifyWithTitle:NSLocalizedStringFromTable(title, @"Growl", @"Growl Notification")
                                description:description
                           notificationName:title
                                   iconData:[image TIFFRepresentation]
                                   priority:0
                                   isSticky:NO
                               clickContext:nil];
}

- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description
{
    [GrowlApplicationBridge notifyWithTitle:NSLocalizedStringFromTable(title, @"Growl", @"Growl Notification")
                                description:description
                           notificationName:title
                                   iconData:nil
                                   priority:0
                                   isSticky:NO
                               clickContext:nil];
}

#pragma mark Growl Delegate methods

- (NSString *)applicationNameForGrowl {
    return [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"];
}

- (NSDictionary *)registrationDictionaryForGrowl {

    NSArray *allNotifications = [NSArray arrayWithObjects:
        GROWL_DOWNLOAD_COMPLETE,
        GROWL_UPLOAD_COMPLETE,
        GROWL_SYNCHRONIZATION_COMPLETE,
        GROWL_CONNECTION_OPENED,
        GROWL_CONNECTION_FAILED,
        GROWL_DOWNLOAD_FAILED,
        GROWL_UPLOAD_FAILED,
        GROWL_RENDEZVOUS_FOUND_SERVICE,
        GROWL_QUEUED,
        nil];
    NSArray *defaultNotifications = [NSArray arrayWithObjects:
        GROWL_DOWNLOAD_COMPLETE,
        GROWL_UPLOAD_COMPLETE,
        GROWL_SYNCHRONIZATION_COMPLETE,
        GROWL_CONNECTION_OPENED,
        GROWL_CONNECTION_FAILED,
        GROWL_DOWNLOAD_FAILED,
        GROWL_UPLOAD_FAILED,
        GROWL_RENDEZVOUS_FOUND_SERVICE,
        GROWL_QUEUED,
        nil];
    
    NSDictionary *registrationDict = [NSDictionary dictionaryWithObjectsAndKeys:
        allNotifications, GROWL_NOTIFICATIONS_ALL,
        defaultNotifications, GROWL_NOTIFICATIONS_DEFAULT,
        nil];
    
    return registrationDict;
}

@end
