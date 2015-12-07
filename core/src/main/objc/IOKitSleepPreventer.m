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

#import "IOKitSleepPreventer.h"
#import <JavaNativeFoundation/JNFString.h>
#import <IOKit/pwr_mgt/IOPMLib.h>

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_IOKitSleepPreventer_createAssertion
  (JNIEnv *env, jobject this, jstring reason)
{
     IOPMAssertionID assertionID;
     // Prevents the system from sleeping automatically due to a lack of user activity.
    IOReturn success = IOPMAssertionCreateWithName(kIOPMAssertionTypeNoIdleSleep,
                                              kIOPMAssertionLevelOn, (CFStringRef)JNFJavaToNSString(env, reason), &assertionID);
    if (success == kIOReturnSuccess) {
       return JNFNSToJavaString(env, [NSString stringWithFormat: @"%u", assertionID]);
    }
    return nil;
}


JNIEXPORT void JNICALL Java_ch_cyberduck_core_IOKitSleepPreventer_releaseAssertion
  (JNIEnv *env, jobject this, jstring assertionID)
{
    NSString* id = JNFJavaToNSString(env, assertionID);
    IOPMAssertionRelease([id intValue]);
}
