#ifndef __DMACCOUNT_H__
#define __DMACCOUNT_H__

/*
    DMAccount.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMAccount 
 *  @abstract This header defines the base class used by DMMemberAccount objects, but
 *	is not meant for direct usage.
 */
 
#import <Foundation/Foundation.h>
#import <DotMacKit/DMCredential.h>
#import <DotMacKit/DMPrincipal.h>

@interface DMAccount : NSObject <DMCredential, DMPrincipal> {
    id _identity;
	NSString *_principalID;
}

#pragma mark -
#pragma mark Convenience API

/*!
 * @method applicationID
 * @abstract Returns the application ID set for this DMAccount object.
 * @result The application ID string.
 */
- (NSString *)applicationID;
 
/*!
 * @method setApplicationID:
 * @abstract Sets the calling application's identifying creator code.
 * @param name The unique 4-character application id (a creator code).
 */
- (void)setApplicationID: (NSString *)creatorCode;

/*!
 * @method applicationName
 * @abstract Returns the human-readable application name set for this DMAccount object.
 * @result The human-readable application name.
 */
- (NSString *)applicationName;

/*!
 * @method setApplicationName:
 * @abstract Sets the calling application's human-readable name.
 * @discussion This method sets the human-readable name of the calling application.  Once 
 *	set, it will be included in the user-agent header sent with all http requests.
 * @param name The human-readable application name.
 */
- (void)setApplicationName: (NSString *)name;

@end

#endif
