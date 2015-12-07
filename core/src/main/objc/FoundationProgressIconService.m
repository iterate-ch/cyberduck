/*
 *  Copyright (c) 2012 David Kocher. All rights reserved.
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

#import <FoundationProgressIconService.h>
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT void JNICALL Java_ch_cyberduck_core_local_FoundationProgressIconService_progress(JNIEnv *env, jobject this, jstring file, jlong current, jlong size)
{
    if(NSClassFromString(@"NSProgress")) {
        id parent = [NSClassFromString(@"NSProgress") performSelector:@selector(currentProgress)];
        if(nil == parent) {
            parent = [NSClassFromString(@"NSProgress") performSelector:@selector(progressWithTotalUnitCount:) withObject:[NSNumber numberWithFloat:size]];
            [parent setPausable:NO];
            [parent setCancellable:NO];
            // Sets the receiver as the current progress object of the current thread.
            [parent becomeCurrentWithPendingUnitCount:size];
            [parent publish];
        }
        else {
            id progress = [NSClassFromString(@"NSProgress") performSelector:@selector(alloc)];
            NSDictionary* info = [NSDictionary dictionaryWithObjectsAndKeys:
                @"NSProgressFileOperationKindDownloading", @"NSProgressFileOperationKindKey",
                [NSURL fileURLWithPath:JNFJavaToNSString(env, file)], @"NSProgressFileURLKey",
                nil];
            [progress performSelector:@selector(initWithParent:userInfo:)
                          withObject:parent
                          withObject:info];
            [progress setKind:@"NSProgressKindFile"];
            [progress setPausable:NO];
            [progress setCancellable:NO];
            [progress setCompletedUnitCount:[[NSNumber numberWithFloat:current] integerValue]];
        }
    }
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_local_FoundationProgressIconService_cancel(JNIEnv *env, jobject this, jstring file)
{
    if(NSClassFromString(@"NSProgress")) {
        id parent = [NSClassFromString(@"NSProgress") performSelector:@selector(currentProgress)];
        if(nil == parent) {
            return;
        }
        // Balance the most recent previous invocation of becomeCurrentWithPendingUnitCount: on the same thread
        // by restoring the current progress object
        [parent resignCurrent];
    }
}