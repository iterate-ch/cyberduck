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

#import <FinderLocal.h>
#import <ApplicationServices/ApplicationServices.h>
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_local_FinderLocal_resolveAlias(JNIEnv *env, jobject this, jstring absolute)
{
    NSString *path = JNFJavaToNSString(env, absolute);
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
	return JNFNSToJavaString(env, resolvedPath);
}