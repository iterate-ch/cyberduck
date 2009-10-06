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

#import "URLSchemeHandlerConfiguration.h"
#import <Foundation/Foundation.h>
#import "ApplicationServices/ApplicationServices.h"

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

jstring convertToJString(JNIEnv *env, NSString *nsString) 
{
    if(nsString == nil) {
        return NULL;
    }
    const char *unichars = [nsString UTF8String];
    return (*env)->NewStringUTF(env, unichars);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_urlhandler_URLSchemeHandlerConfiguration_setDefaultHandlerForURLScheme
  (JNIEnv *env, jobject this, jstring scheme, jstring bundleIdentifier)
{
	LSSetDefaultHandlerForURLScheme(
		(CFStringRef)convertToNSString(env, scheme), 
		(CFStringRef)convertToNSString(env, bundleIdentifier)
	);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_urlhandler_URLSchemeHandlerConfiguration_getDefaultHandlerForURLScheme
  (JNIEnv *env, jobject this, jstring scheme)
{
    NSString *bundleIdentifier = nil;
	bundleIdentifier = (NSString *)LSCopyDefaultHandlerForURLScheme((CFStringRef)convertToNSString(env, scheme));
    if(nil == bundleIdentifier) {
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, [bundleIdentifier UTF8String]);
    if(bundleIdentifier) {
        [bundleIdentifier release];
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL Java_ch_cyberduck_ui_cocoa_urlhandler_URLSchemeHandlerConfiguration_getAllHandlersForURLScheme
  (JNIEnv *env, jobject this, jstring scheme)
{
    NSArray *handlers = nil;
	handlers = (NSArray *)LSCopyAllHandlersForURLScheme(
		(CFStringRef)convertToNSString(env, scheme)
	);
    if(nil == handlers) {
        handlers = [NSArray array];
    }
    jobjectArray result = (jobjectArray)(*env)->NewObjectArray(env,
        [handlers count], (*env)->FindClass(env, "java/lang/String"), (*env)->NewStringUTF(env, "")
    );
    jint i;
    for(i = 0; i < [handlers count]; i++) {
        (*env)->SetObjectArrayElement(env, result, i, convertToJString(env, [handlers objectAtIndex:i]));
    }
    if(handlers) {
        [handlers release];
    }
    return result;
}
