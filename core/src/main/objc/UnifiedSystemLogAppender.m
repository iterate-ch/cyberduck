/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

#import "UnifiedSystemLogAppender.h"
#import <Foundation/Foundation.h>
#import <JavaNativeFoundation/JNFString.h>
#import <os/log.h>

JNIEXPORT void JNICALL Java_ch_cyberduck_core_logging_UnifiedSystemLogAppender_log(JNIEnv *env, jobject this, jint type, jstring message)
{
    static os_log_t category;
    if(!category) {
        NSString* appName = [[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleName"];
        NSString* appBundleIdentifier = [[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleIdentifier"];
        if (!appName || !appBundleIdentifier) {
            category = OS_LOG_DEFAULT;
        }
        else {
            category = os_log_create([appBundleIdentifier cStringUsingEncoding:kUnicodeUTF8Format], [appName cStringUsingEncoding:kUnicodeUTF8Format]);
        }
    }
    os_log_with_type(category, type, "%{public}@", (CFStringRef)JNFJavaToNSString(env, message));
}
