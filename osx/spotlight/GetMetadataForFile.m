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

#include <CoreFoundation/CoreFoundation.h>
#include <CoreServices/CoreServices.h> 

#import <Cocoa/Cocoa.h>

Boolean GetMetadataForFile(void* thisInterface, 
						   CFMutableDictionaryRef attributes, 
						   CFStringRef contentTypeUTI,
						   CFStringRef file)
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSDictionary *bookmark = [NSDictionary dictionaryWithContentsOfFile:(NSString *)file];
	if(bookmark) 
	{
		if(nil != [bookmark objectForKey:@"Hostname"]) {
			[(NSMutableDictionary *)attributes setObject:[bookmark objectForKey:@"Hostname"]
												  forKey:@"ch_sudo_cyberduck_hostname"];
		}
		if(nil != [bookmark objectForKey:@"Nickname"]) {
			[(NSMutableDictionary *)attributes setObject:[bookmark objectForKey:@"Nickname"]
												  forKey:@"ch_sudo_cyberduck_nickname"];
		}
	}
	[pool drain];
	
    return TRUE;
}
