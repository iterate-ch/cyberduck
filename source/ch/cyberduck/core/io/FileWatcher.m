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
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import <FileWatcher.h>
#import <UKKQueue.h>

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

JNIEXPORT void JNICALL Java_ch_cyberduck_core_io_FileWatcher_addPath(JNIEnv *env, jobject instance, jstring local)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[UKKQueue sharedUKKQueue] addPathToQueue:convertToNSString(env, local)];
	[pool release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_io_FileWatcher_removePath(JNIEnv *env, jobject instance, jstring local)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	[[UKKQueue sharedUKKQueue] removePathFromQueue:convertToNSString(env, local)];
	[pool release];
}
