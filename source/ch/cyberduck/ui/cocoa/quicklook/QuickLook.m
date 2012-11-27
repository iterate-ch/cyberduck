/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

#import <QuickLook.h>
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation/JNFString.h>
#import "QLPreviewPanel.h"
#define QLPreviewPanel NSClassFromString(@"QLPreviewPanel")

// First, load the private Quick Look framework if available (10.5+)
#define QUICK_LOOK_AVAILABLE [[NSBundle bundleWithPath:@"/System/Library/PrivateFrameworks/QuickLookUI.framework"] load]

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_quicklook_DeprecatedQuickLook_selectNative(JNIEnv *env, jobject this, jobjectArray paths)
{
	if(QUICK_LOOK_AVAILABLE) {
		NSMutableArray* URLs = nil;
		URLs = [NSMutableArray arrayWithCapacity:(*env)->GetArrayLength(env, paths)];
		int i;
		for(i = 0; i < (*env)->GetArrayLength(env, paths); i++) {
			[URLs addObject:[NSURL fileURLWithPath:JNFJavaToNSString(env, (jstring)(*env)->GetObjectArrayElement(env, paths, i))]];
		}
		[[QLPreviewPanel sharedPreviewPanel] setURLs:URLs currentIndex:0 preservingDisplayState:YES];
	}
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_ui_cocoa_quicklook_DeprecatedQuickLook_isAvailableNative
  (JNIEnv *env, jobject this)
{
    return QUICK_LOOK_AVAILABLE;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_ui_cocoa_quicklook_DeprecatedQuickLook_isOpenNative
  (JNIEnv *env, jobject this)
{
	if(QUICK_LOOK_AVAILABLE) {
		return [[QLPreviewPanel sharedPreviewPanel] isOpen];
	}
	return NO;
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_quicklook_DeprecatedQuickLook_openNative
  (JNIEnv *env, jobject this)
{
	if(QUICK_LOOK_AVAILABLE) {
		if(![[QLPreviewPanel sharedPreviewPanel] isOpen]) {
			// And then display the panel
			[[QLPreviewPanel sharedPreviewPanel] makeKeyAndOrderFrontWithEffect:2];
		}
	}
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_quicklook_DeprecatedQuickLook_closeNative
  (JNIEnv *env, jobject this)
{
	if(QUICK_LOOK_AVAILABLE) {
		if([[QLPreviewPanel sharedPreviewPanel] isOpen]) {
			// If the user presses space when the preview panel is open then we close it
			[[QLPreviewPanel sharedPreviewPanel] closeWithEffect:2];
		}
	}
}