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
#import "ODBEditor.h"

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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_odb_ODBEditor_edit(
										JNIEnv *env, 
										jobject this, 
										jstring path,
										jstring bundleIdentifier)
{
	Editor *editor = [[Editor alloc] init: env
						  withEditorClass: (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this)) 
						 withEditorObject: (*env)->NewGlobalRef(env, this)];
	[editor odbEdit:nil path:convertToNSString(env, path) withEditor:convertToNSString(env, bundleIdentifier)];
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

- (IBAction) odbEdit:(id) sender path:(NSString *)path withEditor:(NSString *)editor
{
    [[ODBEditor sharedODBEditor:editor] editFile:path options:nil forClient:self context:nil];
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