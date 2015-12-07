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

#import <LaunchServicesFileDescriptor.h>
#import <ApplicationServices/ApplicationServices.h>
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_local_LaunchServicesFileDescriptor_kind(JNIEnv *env, jobject this, jstring extension)
{
	NSString *kind = nil;
	OSStatus status = LSCopyKindStringForTypeInfo(kLSUnknownType, kLSUnknownCreator,
		(CFStringRef)JNFJavaToNSString(env, extension), (CFStringRef *)&kind);
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