/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

#import "NSAppleEventDescriptor-Extensions.h"

@implementation NSAppleEventDescriptor(Extensions)

+ (NSAppleEventDescriptor *)descriptorWithFilePath:(NSString *)fileName
{
	NSURL   *url = [NSURL fileURLWithPath: fileName];
	
	return [self descriptorWithFileURL: url];
}

+ (NSAppleEventDescriptor *)descriptorWithFileURL:(NSURL *)fileURL
{
	NSString	*string = [fileURL absoluteString];
	NSData		*data = [string dataUsingEncoding: NSUTF8StringEncoding];
	
	return [self descriptorWithDescriptorType: typeFileURL data: data];
}

@end
