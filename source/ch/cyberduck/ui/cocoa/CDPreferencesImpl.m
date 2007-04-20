/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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
 *  Created by August Mueller on Wed Feb 04 2005.
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import "CDPreferencesImpl.h"

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

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDPreferencesImpl_addObserver
(JNIEnv *env, jobject this, jstring property, jobject observer) 
{
    NSLog(@"Java_ch_cyberduck_ui_cocoa_CDPreferencesImpl_addObserver");
    Observer *impl = [[Observer alloc] init: env
                          withObserverClass: (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, this)) 
                         withObserverObject: (*env)->NewGlobalRef(env, this)];
    [impl observe:convertToNSString(env, property)];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDPreferencesImpl_removeObserver
(JNIEnv *env, jobject this, jobject observer) 
{
    NSLog(@"Java_ch_cyberduck_ui_cocoa_CDPreferencesImpl_removeObserver");
}

@implementation Observer

- (id)init:(JNIEnv*)jniEnv withObserverClass:(jclass)class withObserverObject:(jobject)obj
{
    NSLog(@"init:(JNIEnv*)jniEnv withObserverClass:(jclass)class withObserverObject:(jobject)obj");
    self = [super init];
    if (self != nil) {
        env = jniEnv;
        observerClass = class;
        observerObject = obj;
    }
    return self;
}

- (void)observe:(NSString*)prop
{
    NSLog(@"observe:(NSString*)prop");
    property = prop;
    
    // Register key value observation
    NSUserDefaults* defaults;
    defaults = [NSUserDefaults standardUserDefaults];
    
    [defaults addObserver:self 
               forKeyPath:property
                  options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld
                  context:NULL];
}

- (void)observeValueForKeyPath:(NSString*)keyPath 
                      ofObject:(id)object 
                        change:(NSDictionary*)change 
                       context:(void *)context
{
    NSLog(@"observeValueForKeyPath");
    jmethodID preferenceDidChangeMethod = (*env)->GetMethodID(env, observerClass, "preferenceDidChangeMethod", "()V");
    if (preferenceDidChangeMethod == 0) {
        NSLog( @"CDPreferencesImpl -> GetMethodID:preferenceDidChangeMethod failed");
        return;
    }
    (*env)->CallVoidMethod(env, observerObject, preferenceDidChangeMethod);
    
    [self dealloc];
}

- (void)dealloc
{
    NSLog(@"dealloc");
    NSUserDefaults* defaults;
    defaults = [NSUserDefaults standardUserDefaults];
    
    [defaults removeObserver:self forKeyPath:property];
    
    (*env)->DeleteGlobalRef(env, observerObject);
    (*env)->DeleteGlobalRef(env, observerClass);
    env = NULL;
    
    [super dealloc];
}

@end