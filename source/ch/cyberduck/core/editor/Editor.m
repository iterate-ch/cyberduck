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

#import "Editor.h"
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation/JNFString.h>
#import "ODBEditor.h"

@interface Editor : NSObject
{
	jclass		editorClass;
	jobject		editorObject;
	JNIEnv*		env;
}

- (id)init:(JNIEnv*)env withEditorClass:(jclass)editorClass withEditorObject:(jobject)editorObject;
- (BOOL)odbEdit:(id) sender path:(NSString *)path url:(NSString *)url withEditor:(NSString *)editor;

@end

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_editor_ODBEditor_edit(
										JNIEnv *env, 
										jobject this, 
										jstring local,
										jstring url,
										jstring bundleIdentifier)
{
	Editor *editor = [[Editor alloc] init: env
						  withEditorClass: (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this)) 
						 withEditorObject: (*env)->NewGlobalRef(env, this)];
	if([editor odbEdit:nil path:JNFJavaToNSString(env, local) url:JNFJavaToNSString(env, url) withEditor:JNFJavaToNSString(env, bundleIdentifier)]) {
	    return true;
	}
	return false;
}

@implementation Editor

- (id)init:(JNIEnv*)jniEnv withEditorClass:(jclass)class withEditorObject:(jobject)obj
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    self = [super init];
	if (self != nil) {
		env = jniEnv;
		editorClass = class;
		editorObject = obj;
	}
	[pool drain];
    return self;
}

- (void)dealloc
{
	(*env)->DeleteGlobalRef(env, editorObject);
	(*env)->DeleteGlobalRef(env, editorClass);
	env = NULL;
	[super dealloc];
}

- (BOOL)odbEdit:(id) sender path:(NSString *)path url:(NSString *)url withEditor:(NSString *)editor
{
    NSDictionary* options = [NSDictionary dictionaryWithObjectsAndKeys:
                                url, ODBEditorCustomPathKey,
                                nil
                            ];
    return [[ODBEditor sharedODBEditor:editor] editFile:path options:options forClient:self context:nil];
}

- (void)odbEditor:(ODBEditor *)editor didModifyFile:(NSString *)path newFileLocation:(NSString *)newPath  context:(NSDictionary *)context
{
	jmethodID didModifyFileMethod = (*env)->GetMethodID(env, editorClass, "didModifyFile", "()V");
	if (didModifyFileMethod == 0) {
		NSLog( @"Editor -> GetMethodID:didModifyFile failed");
		return;
	}
	(*env)->CallVoidMethod(env, editorObject, didModifyFileMethod);	
}

- (void)odbEditor:(ODBEditor *)editor didClosefile:(NSString *)path context:(NSDictionary *)context 
{
	jmethodID didCloseFileMethod = (*env)->GetMethodID(env, editorClass, "didCloseFile", "()V");
	if (didCloseFileMethod == 0) {
		NSLog( @"Editor -> GetMethodID:didCloseFile failed");
		return;
	}
	(*env)->CallVoidMethod(env, editorObject, didCloseFileMethod);	

	[self release];
}

@end