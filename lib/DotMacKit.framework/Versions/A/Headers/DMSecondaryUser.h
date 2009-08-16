#ifndef __DMSECONDARYUSER_H__
#define __DMSECONDARYUSER_H__

/*
    DMSecondaryUser.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMSecondaryUser 
 *  @abstract Defines the DMSecondaryUser object, which represents a secondary user owned by a .Mac 
 *   member.  A secondary user’s principal ID can be used to add it to a DMGroup object as a 
 *   member or to refer to it as a security principal in DMSecurity protocol methods.  
 */

#import <Foundation/Foundation.h>
#import <DotMacKit/DMCredential.h>
#import <DotMacKit/DMPrincipal.h>
#import <DotMacKit/DMTransactionGenerator.h>


@class DMTransaction;

@interface DMSecondaryUser : NSObject <DMCredential, DMPrincipal, DMTransactionGenerator> {
    id _identity;
	id _delegate;
	BOOL _use_synchronous;
	NSString *_principalID; 
}

/*!
 * @method secondaryUserWithName:password:owner:andApplicationID:
 * @abstract Returns an autoreleased instance of DMSecondaryUser owned by the .Mac member 
 *	specified in the owner argument and keyed to a specific application’s identification string.  
 *	Pass the unique 4-character application identifier obtained from 
 *	http://developer.apple.com/datatype/creatorcode.html. 
 * @result An autoreleased instance of DMSecondaryUser.
 */
+ (id)secondaryUserWithName: (NSString *)name password: (NSString *)password 
        owner: (NSString *)memberName applicationID: (NSString *)creatorCode;
        
/*!
 * @method secondaryUserWithName:password:owner:andApplicationID:
 * @abstract Returns a newly initialized instance of DMSecondaryUser owned by the .Mac member 
 * specified in the owner argument and keyed to a specific application’s identification string.  
 *	Pass the unique 4-character application identifier obtained from 
 *	http://developer.apple.com/datatype/creatorcode.html.   
 * @result A newly initialized instance of DMSecondaryUser.
 */
- (id)initWithName: (NSString *)name password: (NSString *)password
        owner: (NSString *)memberName applicationID: (NSString *)creatorCode;

@end

#endif
