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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_local_FinderLocal_kind(JNIEnv *env, jobject this, jstring extension)
{
	NSString *kind = nil;
	OSStatus status = LSCopyKindStringForTypeInfo(kLSUnknownType, kLSUnknownCreator,
		(CFStringRef)convertToNSString(env, extension), (CFStringRef *)&kind);
    if(noErr == status) {
        jstring result = (*env)->NewStringUTF(env, [kind UTF8String]);
        if(kind) {
            [kind release];
        }
        return result;
    }
	else {
        jstring result = (*env)->NewStringUTF(env, [NSLocalizedString(@"Unknown", @"") UTF8String]);
        return result;
	}
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_local_FinderLocal_resolveAlias(JNIEnv *env, jobject this, jstring absolute)
{
    NSString *path = convertToNSString(env, absolute);
    NSString *resolvedPath = nil;

    CFURLRef url = CFURLCreateWithFileSystemPath
                       (kCFAllocatorDefault, (CFStringRef)path, kCFURLPOSIXPathStyle, NO);
    if (url != NULL)
    {
        FSRef fsRef;
        if (CFURLGetFSRef(url, &fsRef))
        {
            Boolean targetIsFolder, wasAliased;
            OSErr err = FSResolveAliasFile (&fsRef, true, &targetIsFolder, &wasAliased);
            if ((err == noErr) && wasAliased)
            {
                CFURLRef resolvedUrl = CFURLCreateFromFSRef(kCFAllocatorDefault, &fsRef);
                if (resolvedUrl != NULL)
                {
                    resolvedPath = (NSString*) CFURLCopyFileSystemPath(resolvedUrl, kCFURLPOSIXPathStyle);
                    CFRelease(resolvedUrl);
                }
            }
        }
        CFRelease(url);
    }

    if (resolvedPath == nil)
    {
        resolvedPath = [NSString stringWithString:path];
    }
	return convertToJString(env, resolvedPath);
}