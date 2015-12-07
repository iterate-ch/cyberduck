/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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
 *  Created by August Mueller on Wed Feb 04 2005.
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import "LaunchServicesSchemeHandler.h"
#import <Foundation/Foundation.h>
#import "ApplicationServices/ApplicationServices.h"
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT void JNICALL Java_ch_cyberduck_core_urlhandler_LaunchServicesSchemeHandler_setDefaultHandler
  (JNIEnv *env, jobject this, jstring scheme, jstring bundleIdentifier)
{
	LSSetDefaultHandlerForURLScheme(
		(CFStringRef)JNFJavaToNSString(env, scheme),
		(CFStringRef)JNFJavaToNSString(env, bundleIdentifier)
	);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_urlhandler_LaunchServicesSchemeHandler_getDefaultHandler
  (JNIEnv *env, jobject this, jstring scheme)
{
    NSString *bundleIdentifier = nil;
	bundleIdentifier = (NSString *)LSCopyDefaultHandlerForURLScheme((CFStringRef)JNFJavaToNSString(env, scheme));
    if(nil == bundleIdentifier) {
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, [bundleIdentifier UTF8String]);
    if(bundleIdentifier) {
        [bundleIdentifier release];
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL Java_ch_cyberduck_core_urlhandler_LaunchServicesSchemeHandler_getAllHandlers
  (JNIEnv *env, jobject this, jstring scheme)
{
    NSArray *handlers = [(NSArray *)LSCopyAllHandlersForURLScheme(
		(CFStringRef)JNFJavaToNSString(env, scheme)) autorelease];
    if(nil == handlers) {
        handlers = [NSArray array];
    }
    jobjectArray result = (jobjectArray)(*env)->NewObjectArray(env,
        [handlers count], (*env)->FindClass(env, "java/lang/String"), (*env)->NewStringUTF(env, "")
    );
    jint i;
    for(i = 0; i < [handlers count]; i++) {
        (*env)->SetObjectArrayElement(env, result, i, JNFNSToJavaString(env, [handlers objectAtIndex:i]));
    }
    return result;
}
