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

#import <FinderSidebarService.h>
#import <CoreServices/CoreServices.h>
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>

NSString* getBundleName() {
    NSBundle* mainBundle = [NSBundle mainBundle];
    NSDictionary* infoDictionary = [mainBundle infoDictionary];
    NSString* bundleName = [infoDictionary objectForKey:@"CFBundleName"];
    if (!bundleName){
        return @"Cyberduck";
    }
    return bundleName;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_FinderSidebarService_containsItem(JNIEnv *env, jobject this, jstring file, jstring sharedListName) {
JNF_COCOA_ENTER(env);
    LSSharedFileListRef list = LSSharedFileListCreate(kCFAllocatorDefault, (CFStringRef)JNFJavaToNSString(env, sharedListName), NULL);
    if (!list) {
        NSLog(@"Error getting shared file list reference");
        return NO;
    }
    CFArrayRef items = LSSharedFileListCopySnapshot(list, NULL);
    if (!items) {
        NSLog(@"Error getting shared file list items snapshot copy reference");
        return NO;
    }
    NSString *reference = JNFJavaToNSString(env, file);
    BOOL found = NO;
    for (CFIndex i = 0; i < CFArrayGetCount(items); i++) {
        LSSharedFileListItemRef itemRef = (LSSharedFileListItemRef)CFArrayGetValueAtIndex(items, i);
        CFStringRef propertyRef = LSSharedFileListItemCopyProperty(itemRef, (CFStringRef)@"Reference");
        if (propertyRef) {
            if(CFEqual(propertyRef, (CFStringRef)reference)) {
                found = YES;
            }
            CFRelease(propertyRef);
        }
        if (found) {
            break;
        }
    }
    CFRelease(items);
    CFRelease(list);
	return found;
JNF_COCOA_EXIT(env);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_FinderSidebarService_addItem(JNIEnv *env, jobject this, jstring file, jstring displayName, jstring sharedListName) {
JNF_COCOA_ENTER(env);
    LSSharedFileListRef list = LSSharedFileListCreate(kCFAllocatorDefault, (CFStringRef)JNFJavaToNSString(env, sharedListName), NULL);
    if (!list) {
		NSLog(@"Error getting shared file list reference");
        return NO;
    }
    NSURL* url = [NSURL fileURLWithPath:JNFJavaToNSString(env, file)].URLByStandardizingPath;
    LSSharedFileListItemRef itemRef = LSSharedFileListInsertItemURL(list,
                                                                    kLSSharedFileListItemLast,
                                                                    (CFStringRef)JNFJavaToNSString(env, displayName),
                                                                    NULL,
                                                                    (CFURLRef)url,
                                                                    (CFDictionaryRef)[NSDictionary dictionaryWithObject:JNFJavaToNSString(env, file) forKey:@"Reference"],
                                                                    NULL);
    CFRelease(list);
    if (!itemRef) {
        NSLog(@"Error getting shared file list item reference");
        return NO;
    }
    CFRelease(itemRef);
    return YES;
JNF_COCOA_EXIT(env);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_FinderSidebarService_removeItem(JNIEnv *env, jobject this, jstring file, jstring sharedListName) {
JNF_COCOA_ENTER(env);
    LSSharedFileListRef list = LSSharedFileListCreate(kCFAllocatorDefault, (CFStringRef)JNFJavaToNSString(env, sharedListName), NULL);
    if (!list) {
        NSLog(@"Error getting shared file list reference");
        return NO;
    }
    CFArrayRef items = LSSharedFileListCopySnapshot(list, NULL);
    if (!items) {
        NSLog(@"Error getting shared file list items snapshot copy reference");
        return NO;
    }
    NSString *reference = JNFJavaToNSString(env, file);
    OSStatus err;
    for (CFIndex i = 0; i < CFArrayGetCount(items); i++) {
        LSSharedFileListItemRef itemRef = (LSSharedFileListItemRef)CFArrayGetValueAtIndex(items, i);
        CFStringRef propertyRef = LSSharedFileListItemCopyProperty(itemRef, (CFStringRef)@"Reference");
        if (propertyRef) {
            if(CFEqual(propertyRef, (CFStringRef)reference)) {
                if(noErr == (err = LSSharedFileListItemRemove(list, itemRef))) {
                    break;
                }
                else {
                    NSLog(@"Error removing shared file list item. %s", [NSError errorWithDomain:NSOSStatusErrorDomain code:err userInfo:nil].description.UTF8String);
                }
            }
            CFRelease(propertyRef);
        }
    }
    CFRelease(items);
    CFRelease(list);
	return err == noErr;
JNF_COCOA_EXIT(env);
}
