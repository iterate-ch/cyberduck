/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

#include <jni.h>
/* Header for class ch_cyberduck_ui_cocoa_growl_Growl */

#ifndef _Included_ch_cyberduck_ui_cocoa_growl_Growl
#define _Included_ch_cyberduck_ui_cocoa_growl_Growl
#ifdef __cplusplus
extern "C" {
#endif
	/* Inaccessible static: log */
	/* Inaccessible static: instance */
	/* Inaccessible static: class_00024ch_00024cyberduck_00024ui_00024cocoa_00024growl_00024Growl */
	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    launch
	 * Signature: ()V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_launch
	(JNIEnv *, jobject);
	
	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    notify
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notify
		(JNIEnv *, jobject, jstring, jstring);

	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    notify
	 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notifyWithImage
		(JNIEnv *, jobject, jstring, jstring, jstring);
	
#ifdef __cplusplus
}
#endif
#endif

#import <Cocoa/Cocoa.h>

static id instance;

@interface Growl : NSObject {
	BOOL registered;
}

+ (id)defaultInstance;

- (void)launchGrowl;
- (void)registerGrowl:(void*)context;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImageName:(NSString *) image;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImage:(NSImage *) image;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description;

@end
