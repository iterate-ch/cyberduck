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
#import <ApplicationServices/ApplicationServices.h>
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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_setQuarantine(JNIEnv *env, jobject this, jstring path, jstring originUrl, jstring dataUrl)
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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_setWhereFrom(JNIEnv *env, jobject this, jstring path, jstring dataUrl)
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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_applicationForExtension(
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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_setIconFromExtension(JNIEnv *env, jobject this, jstring path, jstring icon)
{
	NSImage *image = [[NSWorkspace sharedWorkspace] iconForFileType:convertToNSString(env, icon)];
	[image setScalesWhenResized:YES];
	[image setSize:NSMakeSize(128.0, 128.0)];
	NSWorkspace *workspace = [NSWorkspace sharedWorkspace];
	if([workspace respondsToSelector:@selector(setIcon:forFile:options:)]) {
		[workspace setIcon:image forFile:convertToNSString(env, path) options:NSExcludeQuickDrawElementsIconCreationOption];
	}
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_setIconFromFile(JNIEnv *env, jobject this, jstring path, jstring icon)
{
	NSWorkspace *workspace = [NSWorkspace sharedWorkspace];
	if([workspace respondsToSelector:@selector(setIcon:forFile:options:)]) {
		[workspace setIcon:[NSImage imageNamed:convertToNSString(env, icon)] forFile:convertToNSString(env, path) options:NSExcludeQuickDrawElementsIconCreationOption];
	}
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_removeCustomIcon(JNIEnv *env, jobject this, jstring path)
{
	[IconFamily removeCustomIconFromFile:convertToNSString(env, path)];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_model_CDLocal_kind(JNIEnv *env, jobject this, jstring extension)
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