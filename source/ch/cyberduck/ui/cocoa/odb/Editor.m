/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

// Simple JNI_OnLoad api
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    return JNI_VERSION_1_4;
}

// Simple utility to convert java strings to NSStrings
NSString *convertToNSString(JNIEnv *env, jstring javaString)
{
    NSString *converted = nil;
    const jchar *unichars = NULL;
	
    if (javaString == NULL) {
        return nil;	
    }                   
    unichars = (*env)->GetStringChars(env, javaString, nil);
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

//jclass editorClass = 0;
//jobject editorObject = 0;
//JNIEnv* globalenv = 0;

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_odb_Editor_edit(
										JNIEnv *env, 
										jobject this, 
										jstring path) 
{
	
	// save jni environment for access in other methods
//	globalenv = env;
//	editorObject = (*env)->NewGlobalRef(env, this);
//	editorClass = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this));
	
	Editor *editor = [[Editor alloc] init: env
						  withEditorClass: (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this)) 
						 withEditorObject: (*env)->NewGlobalRef(env, this)];
//	Editor *editor = [[Editor alloc] init];
	[editor odbEdit:nil path:convertToNSString(env, path)];
}

@implementation Editor

- (id)init:(JNIEnv*)jniEnv withEditorClass:(jclass)class withEditorObject:(jobject)obj
{
    self = [super init];
	if (self != nil) {
		env = jniEnv;
		editorClass = class;
		editorObject = obj;
	}
    return self;
}

- (void)dealloc
{
	(*env)->DeleteGlobalRef(env, editorObject);
	(*env)->DeleteGlobalRef(env, editorClass);
	env = NULL;
	[super dealloc];
}

- (IBAction) odbEdit:(id) sender path:(NSString *)path
{
    
    [[ODBEditor sharedODBEditor] editFile:path options: nil forClient:self context: NULL];
}

- (void)odbEditor:(ODBEditor *)editor didModifyFile:(NSString *)path newFileLocation:(NSString *)newPath  context:(NSDictionary *)context
{

	jmethodID didModifyFileMethod = (*env)->GetMethodID(env, editorClass, "didModifyFile", "(Ljava/lang/String;)V");
	if (didModifyFileMethod == 0) {
		NSLog( @"Editor -> GetMethodID:didModifyFile failed");
		return;
	}
	
	(*env)->CallVoidMethod(env, editorObject, didModifyFileMethod, convertToJString(env, path));	
}

- (void)odbEditor:(ODBEditor *)editor didClosefile:(NSString *)path context:(NSDictionary *)context 
{
	
	jmethodID didCloseFileMethod = (*env)->GetMethodID(env, editorClass, "didCloseFile", "(Ljava/lang/String;)V");
	if (didCloseFileMethod == 0) {
		NSLog( @"Editor -> GetMethodID:didCloseFile failed");
		return;
	}
	
	(*env)->CallVoidMethod(env, editorObject, didCloseFileMethod, convertToJString(env, path));	

	[self release];
}

@end