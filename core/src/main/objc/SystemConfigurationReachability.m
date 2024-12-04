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
#import <Foundation/Foundation.h>
#import <SystemConfiguration/SystemConfiguration.h>

NSString *kReachabilityChangedNotification = @"kNetworkReachabilityChangedNotification";

static void _ReachabilityCallbackFunction(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void* context) {
    // Post a notification to notify the client that the network reachability changed
    [[NSNotificationCenter defaultCenter] postNotificationName: kReachabilityChangedNotification
                                                        object: context
                                                      userInfo: nil];
}

@implementation SystemConfigurationReachability

- (id)initWithUrl:(NSString *) url {
    self->url = [[NSURL alloc] initWithString:url];
    self->target = SCNetworkReachabilityCreateWithName(kCFAllocatorDefault, [[[NSURL URLWithString:url] host] cStringUsingEncoding:NSASCIIStringEncoding]);
    return self;
}

-(void)dealloc {
    if(url != nil) {
        [url release];
    }
    if(target != NULL) {
        CFRelease(target);
    }
    [super dealloc];
}

- (void)diagnoseInteractively {
	CFNetDiagnosticRef diagnostics;
	CFNetDiagnosticStatus status;
    diagnostics = CFNetDiagnosticCreateWithURL(kCFAllocatorDefault, (CFURLRef)url);
    if(diagnostics) {
        status = CFNetDiagnosticDiagnoseProblemInteractively(diagnostics);
        CFRelease(diagnostics);
    }
    else {
        NSLog(@"Error creating diagnostics instance for %@", url);
    }
}

- (BOOL)startReachabilityMonitor {
    if(target != NULL) {
        SCNetworkConnectionFlags flags;
        if(!SCNetworkReachabilityGetFlags(target, &flags)) {
            NSLog(@"Error getting reachability flags for %@", url);
            return NO;
        }
        SCNetworkReachabilityContext context = {
            .version = 0,
            .info = (void *)CFBridgingRetain(self),
            .release = CFRelease
        };
        if(SCNetworkReachabilitySetCallback(target, _ReachabilityCallbackFunction, &context)) {
            if(SCNetworkReachabilityScheduleWithRunLoop(target, CFRunLoopGetMain(), kCFRunLoopDefaultMode)) {
                return YES;
            }
            else {
                NSLog(@"Error scheduling reachability run loop for %@", url);
                SCNetworkReachabilitySetCallback(target, NULL, NULL);
            }
        }
        NSLog(@"Error setting reachability callback for %@", url);
        return NO;
	}
	return NO;
}

- (BOOL)stopReachabilityMonitor {
    if(target != NULL) {
        // Delete callback and unschedule
        if(!SCNetworkReachabilitySetCallback(target, NULL, NULL)) {
            return NO;
        }
        if(!SCNetworkReachabilityUnscheduleFromRunLoop(target, CFRunLoopGetMain(), kCFRunLoopDefaultMode)) {
            return NO;
        }
        return YES;
    }
    return NO;
}

- (SCNetworkReachabilityFlags)getFlags {
	SCNetworkReachabilityFlags flags;
	if(!SCNetworkReachabilityGetFlags(target, &flags)) {
        NSLog(@"Error getting reachability flags for %@", url);
        return NO;
    }
    return flags;
}

@end
