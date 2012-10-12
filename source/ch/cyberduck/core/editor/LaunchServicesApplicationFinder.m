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

#include <stdio.h>

#import <Local.h>
#import <ApplicationServices/ApplicationServices.h>
#import <Foundation/Foundation.h>

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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_editor_LaunchServicesApplicationFinder_find(
										JNIEnv *env,
										jobject this,
                                        jstring extension)
{
    CFURLRef url; // Path of the application bundle
	// Locates the preferred application for opening items with a specified file type,
	// creator signature, filename extension, or any combination of these characteristics.
    OSStatus err = LSGetApplicationForInfo(kLSUnknownType, kLSUnknownCreator,
        (CFStringRef)convertToNSString(env, extension),
        kLSRolesEditor, NULL, &url);
    if(err != noErr) {
        // kLSApplicationNotFoundErr
		// If no application suitable for opening items with the specified characteristics is found
		// in the Launch Services database
		return NULL;
    }
    NSString *result = [(NSURL *)url path];
    CFRelease(url);
	return convertToJString(env, result);
}


JNIEXPORT jobjectArray JNICALL Java_ch_cyberduck_core_editor_LaunchServicesApplicationFinder_findAll(
										JNIEnv *env,
										jobject this,
                                        jstring extension)
{
    NSArray *handlers = [(NSArray *)LSCopyAllRoleHandlersForContentType(
        UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (CFStringRef)convertToNSString(env, extension), NULL),
        kLSRolesEditor) autorelease];
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
    return result;
}
