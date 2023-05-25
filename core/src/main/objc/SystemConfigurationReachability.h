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

#import <Foundation/Foundation.h>
#import <CoreServices/CoreServices.h>
#import <SystemConfiguration/SCNetworkReachability.h>

@interface SystemConfigurationReachability : NSObject {
@private
	NSURL *url;
    SCNetworkReachabilityRef target;
}

- (id)initWithUrl:(NSString *) url;

- (void)diagnoseInteractively;

- (BOOL)startReachabilityMonitor;
- (BOOL)stopReachabilityMonitor;

- (SCNetworkReachabilityFlags)getFlags;

@end
