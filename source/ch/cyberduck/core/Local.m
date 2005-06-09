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
#import <Carbon/Carbon.h>
#import <CoreServices/CoreServices.h>
#import <Cocoa/Cocoa.h>
#import <IconFamily.h>

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

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Local_setIconFromExtension(JNIEnv *env, jobject this, jstring path, jstring icon)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSImage *image = [[NSWorkspace sharedWorkspace] iconForFileType:convertToNSString(env, icon)];
	[image setScalesWhenResized:YES];
	[image setSize:NSMakeSize(128.0, 128.0)];
	id iconFamily = [IconFamily iconFamilyWithThumbnailsOfImage:image];
	[iconFamily setAsCustomIconForFile:convertToNSString(env, path)];
	[pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_Local_setIconFromFile(JNIEnv *env, jobject this, jstring path, jstring icon)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	id iconFamily = [IconFamily iconFamilyWithThumbnailsOfImage:[NSImage imageNamed:convertToNSString(env, icon)]];
	[iconFamily setAsCustomIconForFile:convertToNSString(env, path)];
	[pool release];
}