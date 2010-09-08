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

#import <Cocoa/Cocoa.h>

//A bookmark collection
@interface CDTransmitImportFavoriteCollection : NSObject {
	NSString *_name;
	NSArray *_favorites;
}

@property (readonly) NSArray *favorites; //The root favorite collection has favorite collections here, the rest has favorites themselves here
@property (readonly) NSString *name; //Name of the collection

@end

//The favortite class
@interface CDTransmitImportFavorite : NSObject {
	NSString *nickname; //Favorite's name
	
	NSString *username; //Username
	NSString *password; //Password might be stored here - however, it was always nil in all my examples
	
	NSString *protocol; //Protocol to be used - FTP, SFTP, S3, FTPSSL (FTP with implicit SSL), FTPTLS (FTP with TLS/SSL), WebDAV, WebDAVS
	NSString *server; //Server URL
	int port; //Port to be used. When default, is 0
	
	NSString *initialPath; //Initial path for the server
}

@property (readonly) NSString *nickname, *username, *password, *protocol, *server, *initialPath;
@property (readonly) int port;

@end