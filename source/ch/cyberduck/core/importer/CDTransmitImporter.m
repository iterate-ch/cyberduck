/*
 *  Copyright (c) 2010 FuelCollective. All rights reserved.
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

#import "CDTransmitImporter.h"

@implementation CDTransmitImportFavorite

@synthesize nickname, username, password, protocol, server, initialPath;
@synthesize port;

-(void)dealloc {
	[nickname release];
	[username release];
	[password release];
	[protocol release];
	[server release];
	[initialPath release];
	
	[super dealloc];
}

-(id)initWithCoder:(NSKeyedUnarchiver*)unarchiver{
	self = [self init];
	
	// There are more attributes, but the rest is Transmit-bound	
	nickname = [[unarchiver decodeObjectForKey:@"nickname"] retain];
	initialPath = [[unarchiver decodeObjectForKey:@"initialRemotePath"] retain];
	password = [[unarchiver decodeObjectForKey:@"password"] retain];
	port = [unarchiver decodeIntForKey:@"port"];
	protocol = [[unarchiver decodeObjectForKey:@"protocol"] retain];
	server = [[unarchiver decodeObjectForKey:@"server"] retain];
	username = [[unarchiver decodeObjectForKey:@"username"] retain];
	
	return self;
}

@end

@implementation CDTransmitImportFavoriteCollection

@synthesize name = _name;
@synthesize favorites = _favorites ;

-(void)dealloc{
	[_name release];
	[_favorites release];
	[super dealloc];
}
-(id)initWithCoder:(NSKeyedUnarchiver*)unarchiver{
	self = [self init];
	
	_name = [[unarchiver decodeObjectForKey:@"name"] retain];
	_favorites = [[unarchiver decodeObjectForKey:@"contents"] retain];

	return self;
}
@end