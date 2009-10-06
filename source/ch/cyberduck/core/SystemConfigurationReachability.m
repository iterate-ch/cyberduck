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

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationReachability_isReachable
  (JNIEnv *env, jobject this, jstring urlString)
{
	return [Host isReachable:convertToNSString(env, urlString)];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_core_SystemConfigurationReachability_diagnose
  (JNIEnv *env, jobject this, jstring urlString)
{
	[Host diagnose:convertToNSString(env, urlString)];
}

@implementation Host

+ (void)diagnose:(NSString*)urlString {
	CFURLRef url;
	CFNetDiagnosticRef myDiagnostics;
	CFNetDiagnosticStatus myStatus;
	
	//First get the CFURLRef
	url = CFURLCreateWithString(kCFAllocatorDefault, (CFStringRef)urlString, NULL);
	
	if(url) {
		//Now create the CFNetDiagnosticRef then release url since we are done with it
		myDiagnostics = CFNetDiagnosticCreateWithURL(kCFAllocatorDefault, url);
		CFRelease(url);
		
		if(myDiagnostics) {
			//Call the interactive diagnose call
			myStatus = CFNetDiagnosticDiagnoseProblemInteractively(myDiagnostics);
			CFRelease(myDiagnostics);
		}
	}
}

+ (BOOL)isReachable:(NSString*)urlString {
	SCNetworkConnectionFlags flags;
	NSURL * url = [NSURL URLWithString:urlString];
	
	if (!SCNetworkCheckReachabilityByName([[url host] cStringUsingEncoding:NSASCIIStringEncoding], &flags))
		return NO;
	return (flags & kSCNetworkFlagsReachable) && !(flags & kSCNetworkFlagsConnectionRequired);
}

@end
