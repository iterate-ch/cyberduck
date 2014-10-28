/*
 *  Copyright (c) 2014 David Kocher. All rights reserved.
 *  http://cyberduck.io/
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
 *  dkocher@cyberduck.io
 */

#import <Sandbox.h>
#import <Cocoa/Cocoa.h>
#import <Security/SecRequirement.h>
#import <Security/SecStaticCode.h>
#import <objc/runtime.h>

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_sparkle_Sandbox_isSandboxed(JNIEnv *env, jobject this)
{
    BOOL isSandboxed = NO;
    NSURL *bundleURL = [[NSBundle mainBundle] bundleURL];
    SecStaticCodeRef staticCode = NULL;
    SecStaticCodeCreateWithPath((CFURLRef)bundleURL, kSecCSDefaultFlags, &staticCode);
    if (!staticCode) {
        return NO;
    }
    static SecRequirementRef sandboxRequirement = NULL;
    SecRequirementCreateWithString(CFSTR("entitlement[\"com.apple.security.app-sandbox\"] exists"), kSecCSDefaultFlags, &sandboxRequirement);
    if (!sandboxRequirement) {
        return NO;
    }
    OSStatus codeCheckResult = SecStaticCodeCheckValidityWithErrors(staticCode, kSecCSBasicValidateOnly, sandboxRequirement, NULL);
    if (codeCheckResult == errSecSuccess) {
        isSandboxed = YES;
    }
    CFRelease(staticCode);
    CFRelease(sandboxRequirement);
    return isSandboxed;
}
