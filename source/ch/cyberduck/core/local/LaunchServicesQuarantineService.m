/*
 *  Copyright (c) 2012 David Kocher. All rights reserved.
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

#import <LaunchServicesQuarantineService.h>
#import <ApplicationServices/ApplicationServices.h>
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_LaunchServicesQuarantineService_setQuarantine(JNIEnv *env, jobject this, jstring path, jstring originUrl, jstring dataUrl)
{
	NSURL* url = [NSURL fileURLWithPath:JNFJavaToNSString(env, path)];
	FSRef ref;
	if(CFURLGetFSRef((CFURLRef) url, &ref)) {
        NSMutableDictionary* attrs = [[NSMutableDictionary alloc] init];
        // Write quarantine attributes
        [attrs setValue:(NSString*)kLSQuarantineTypeOtherDownload forKey:(NSString*)kLSQuarantineTypeKey];
        [attrs setValue:JNFJavaToNSString(env, originUrl) forKey:(NSString*)kLSQuarantineOriginURLKey];
        [attrs setValue:JNFJavaToNSString(env, dataUrl) forKey:(NSString*)kLSQuarantineDataURLKey];

        if(LSSetItemAttribute(&ref, kLSRolesAll, kLSItemQuarantineProperties, (CFDictionaryRef*) attrs) != noErr) {
            NSLog(@"Error writing quarantine attribute");
    		return FALSE;
        }
        [attrs release];
        return TRUE;
	}
    return FALSE;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_LaunchServicesQuarantineService_setWhereFrom(JNIEnv *env, jobject this, jstring path, jstring dataUrl)
{
	typedef OSStatus (*MDItemSetAttribute_type)(MDItemRef, CFStringRef, CFTypeRef);
	static MDItemSetAttribute_type mdItemSetAttributeFunc = NULL;
	static bool didSymbolLookup = false;
	if (!didSymbolLookup) {
		didSymbolLookup = true;
		CFBundleRef metadataBundle = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.Metadata"));
		if (!metadataBundle) {
    		return FALSE;
		}
		mdItemSetAttributeFunc = (MDItemSetAttribute_type)CFBundleGetFunctionPointerForName(metadataBundle, CFSTR("MDItemSetAttribute"));
	}
	if (!mdItemSetAttributeFunc) {
		return FALSE;
	}
	MDItemRef mdItem = MDItemCreate(NULL, (CFStringRef)JNFJavaToNSString(env, path));
	if (!mdItem) {
		return FALSE;
	}
	mdItemSetAttributeFunc(mdItem, kMDItemWhereFroms, (CFMutableArrayRef)[NSMutableArray arrayWithObject:JNFJavaToNSString(env, dataUrl)]);
	CFRelease(mdItem);
    return TRUE;
}
