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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_FinderLocal_setQuarantine(JNIEnv *env, jobject this, jstring path, jstring originUrl, jstring dataUrl)
{
	NSURL* url = [NSURL fileURLWithPath:convertToNSString(env, path)];
	FSRef ref;
	CFURLGetFSRef((CFURLRef) url, &ref);

	NSMutableDictionary* attrs = [[NSMutableDictionary alloc] init];
	// Write quarantine attributes
	[attrs setValue:(NSString*)kLSQuarantineTypeOtherDownload forKey:(NSString*)kLSQuarantineTypeKey];
	[attrs setValue:convertToNSString(env, originUrl) forKey:(NSString*)kLSQuarantineOriginURLKey];
	[attrs setValue:convertToNSString(env, dataUrl) forKey:(NSString*)kLSQuarantineDataURLKey];
		
	if(LSSetItemAttribute(&ref, kLSRolesAll, kLSItemQuarantineProperties, (CFDictionaryRef*) attrs) != noErr) {
		NSLog(@"Error writing quarantine attribute");
	}
	[attrs release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_FinderLocal_setWhereFrom(JNIEnv *env, jobject this, jstring path, jstring dataUrl)
{
	// From mozilla/camino/src/download/nsDownloadListener.mm
	typedef OSStatus (*MDItemSetAttribute_type)(MDItemRef, CFStringRef, CFTypeRef);
	static MDItemSetAttribute_type mdItemSetAttributeFunc = NULL;
	static bool didSymbolLookup = false;
	if (!didSymbolLookup) {
		didSymbolLookup = true;
		CFBundleRef metadataBundle = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.Metadata"));
		if (!metadataBundle) {
			return;
		}
		mdItemSetAttributeFunc = (MDItemSetAttribute_type)CFBundleGetFunctionPointerForName(metadataBundle, CFSTR("MDItemSetAttribute"));
	}
	if (!mdItemSetAttributeFunc) {
		return;
	}
	MDItemRef mdItem = MDItemCreate(NULL, (CFStringRef)convertToNSString(env, path));
	if (!mdItem) {
	   return;
	}
	mdItemSetAttributeFunc(mdItem, kMDItemWhereFroms, (CFMutableArrayRef)[NSMutableArray arrayWithObject:convertToNSString(env, dataUrl)]);
	CFRelease(mdItem);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_model_FinderLocal_applicationForExtension(
										JNIEnv *env,
										jobject this,
                                        jstring extension)
{
    CFURLRef url; // Path of the application bundle
	// Locates the preferred application for opening items with a specified file type,
	// creator signature, filename extension, or any combination of these characteristics.
    OSStatus err = LSGetApplicationForInfo(kLSUnknownType, kLSUnknownCreator,
        (CFStringRef)convertToNSString(env, extension),
        kLSRolesAll, NULL, &url);
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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_model_FinderLocal_kind(JNIEnv *env, jobject this, jstring extension)
{
	NSString *kind = nil;
	LSCopyKindStringForTypeInfo(kLSUnknownType, kLSUnknownCreator, 
		(CFStringRef)convertToNSString(env, extension), (CFStringRef *)&kind);
	if(!kind) {
		kind = NSLocalizedString(@"Unknown", @"");
	}
	jstring result = (*env)->NewStringUTF(env, [kind UTF8String]);
	if(kind) {
		[kind release];
	}
	return result;
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_model_FinderLocal_resolveAlias(JNIEnv *env, jobject this, jstring absolute)
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