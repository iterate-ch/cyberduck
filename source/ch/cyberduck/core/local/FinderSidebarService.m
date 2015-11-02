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

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_FinderSidebarService_addItem(JNIEnv *env, jobject this, jstring file, jstring name) {
    LSSharedFileListRef list = LSSharedFileListCreate(kCFAllocatorDefault, kLSSharedFileListFavoriteItems, NULL);
    if (!list) {
		NSLog(@"Error getting shared file list reference");
        return NO;
    }
    NSURL* url = [NSURL fileURLWithPath:JNFJavaToNSString(env, file)];
    LSSharedFileListItemRef item = LSSharedFileListInsertItemURL(list,
                                                                 kLSSharedFileListItemLast,
                                                                 NULL, NULL,
                                                                 (CFURLRef)url,
                                                                 NULL, NULL);
    CFRelease(list);
    if(!item) {
        NSLog(@"Error getting shared file list item reference");
        return NO;
    }
    CFRelease(item);
    return YES;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_local_FinderSidebarService_removeItem(JNIEnv *env, jobject this, jstring file, jstring name) {
    OSStatus err;
    LSSharedFileListRef list = LSSharedFileListCreate(kCFAllocatorDefault, kLSSharedFileListFavoriteItems, NULL);
    if (!list) {
        NSLog(@"Error getting shared file list reference");
        return NO;
    }
    UInt32 seed;
    CFArrayRef items = LSSharedFileListCopySnapshot(list, &seed);
    if (!items) {
        NSLog(@"Error getting shared file list items snapshot copy reference");
        return NO;
    }
    BOOL found = NO;
    for (CFIndex i = 0; i < CFArrayGetCount(items); i++) {
        NSURL *url;
        LSSharedFileListItemRef item = (LSSharedFileListItemRef)CFArrayGetValueAtIndex(items, i);
        if (noErr == (err = LSSharedFileListItemResolve(item, kLSSharedFileListNoUserInteraction|kLSSharedFileListDoNotMountVolumes, (CFURLRef*)&url, NULL))) {
            NSString *itemPath = [url path];
            [url release];
            if ([itemPath isEqualToString:JNFJavaToNSString(env, file)]) {
                err = LSSharedFileListItemRemove(list, item);
                found = YES;
                break;
            }
        }
    }
    CFRelease(items);
    CFRelease(list);
	return (err == noErr) && (found);
}