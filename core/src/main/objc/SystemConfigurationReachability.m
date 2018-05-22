/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

#import "SystemConfigurationReachability.h"
#import <JavaNativeFoundation/JNFString.h>
#import <Foundation/Foundation.h>
#import <SystemConfiguration/SystemConfiguration.h>
#import <netinet/in.h>

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_diagnostics_SystemConfigurationReachability_isReachable
  (JNIEnv *env, jobject this, jstring urlString)
{
	return [SystemConfigurationReachability isReachable:JNFJavaToNSString(env, urlString)];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_diagnostics_SystemConfigurationReachability_diagnose
  (JNIEnv *env, jobject this, jstring urlString)
{
	[SystemConfigurationReachability diagnose:JNFJavaToNSString(env, urlString)];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_diagnostics_SystemConfigurationReachability_monitor
  (JNIEnv *env, jobject this, jstring urlString)
{
	return [SystemConfigurationReachability monitor:JNFJavaToNSString(env, urlString)];
}

NSString *kReachabilityChangedNotification = @"kNetworkReachabilityChangedNotification";

static void _ReachabilityCallback(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void* info) {
    NSString* urlString = (NSString *)info;
    // Post a notification to notify the client that the network reachability changed
    [[NSNotificationCenter defaultCenter] postNotificationName: kReachabilityChangedNotification object: urlString];
    // Delete callback and unschedule
    SCNetworkReachabilitySetCallback(target, NULL, NULL);
    SCNetworkReachabilityUnscheduleFromRunLoop(target, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode);
    CFRelease(target);
}

@implementation SystemConfigurationReachability

+ (void)diagnose:(NSString*)urlString {
	CFURLRef url;
	CFNetDiagnosticRef diagnostics;
	CFNetDiagnosticStatus status;
	url = CFURLCreateWithString(kCFAllocatorDefault, (CFStringRef)urlString, NULL);
	if(url) {
		diagnostics = CFNetDiagnosticCreateWithURL(kCFAllocatorDefault, url);
		CFRelease(url);
		if(diagnostics) {
			status = CFNetDiagnosticDiagnoseProblemInteractively(diagnostics);
			CFRelease(diagnostics);
		}
        else {
            NSLog(@"Error creating diagnostics instance for %@", urlString);
        }
	}
}

+ (BOOL)monitor:(NSString*)urlString {
	NSURL *url = [NSURL URLWithString:urlString];
	SCNetworkReachabilityRef target = SCNetworkReachabilityCreateWithName(kCFAllocatorDefault, [[url host] cStringUsingEncoding:NSASCIIStringEncoding]);
	if(NULL == target) {
        NSLog(@"Error creating reachability instance for %@", urlString);
	    return NO;
	}
    SCNetworkReachabilityContext context = {
        .version = 0,
        .info = (void *)CFBridgingRetain(urlString),
        .release = CFRelease
    };
	if(SCNetworkReachabilitySetCallback(target, _ReachabilityCallback, &context)) {
		if(SCNetworkReachabilityScheduleWithRunLoop(target, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode)) {
			return YES;
		}
        else {
            NSLog(@"Error scheduling reachability run loop for %@", urlString);
            SCNetworkReachabilitySetCallback(target, NULL, NULL);
        }
	}
    NSLog(@"Error setting reachability callback for %@", urlString);
	return NO;
}

+ (BOOL)isReachable:(NSString*)urlString {
	NSURL *url = [NSURL URLWithString:urlString];
	SCNetworkReachabilityRef target = SCNetworkReachabilityCreateWithName(kCFAllocatorDefault, [[url host] cStringUsingEncoding:NSASCIIStringEncoding]);
	if(NULL == target) {
        NSLog(@"Error creating reachability instance for %@", urlString);
	    return NO;
	}
	SCNetworkConnectionFlags flags;
	if(!SCNetworkReachabilityGetFlags(target, &flags)) {
        NSLog(@"Error getting reachability flags for %@", urlString);
        CFRelease(target);
        return NO;
    }
	CFRelease(target);
	return (flags & kSCNetworkFlagsReachable) && !(flags & kSCNetworkFlagsConnectionRequired);
}

@end
