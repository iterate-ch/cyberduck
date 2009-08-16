#ifndef __DMGROUP_H__
#define __DMGROUP_H__

/*
    DMGroup.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMGroup 
 *  @abstract Defines DMGroup objects, which represent .Mac member-owned groups containing other .Mac 
 *  members, secondary users and even other groups as members.  A DMGroup’s principal ID can be 
 *  used to refer to it as a security principal in DMSecurity protocol methods.  A DMGroup object can 
 *  be obtained only through a DMMemberAccount object.  
 */

#import <Foundation/Foundation.h>
#import <DotMacKit/DMTransactionGenerator.h>
#import <DotMacKit/DMPrincipal.h>

@class DMTransaction;


@interface DMGroup : NSObject <DMPrincipal, DMTransactionGenerator> {
    NSString *_name;
	id _owner;
	id _delegate;
	BOOL _use_synchronous;
	NSString *_principalID;
}

/*!
 * @method name
 * @abstract Returns the name of the .Mac member-owned group represented by this DMGroup object. 
 * @result An NSString.
 */
- (NSString *)name;

/*!
 * @method owner
 * @abstract Returns the name of the .Mac member who own this group.
 * @result An NSString.
 */
- (NSString *)owner;

/*!
 * @method addMembers:
 * @abstract Adds members to the given .Mac member-owned group. If the group already exists, then the 
 * identified principals are added as members.  If the group does not already exist, this call fails.  The 
 * members argument is an NSArray of principal ID strings that identify principals corresponding to 
 * DMMemberAccounts, DMSecondaryUsers and/or other DMGroup objects. 
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)addMembers: (NSArray *)members;

/*!
 * @method removeMembers:
 * @abstract Removes members from the given .Mac member-owned group.  The members argument is an 
 * NSArray of principal ID strings that identify principals corresponding to DMMemberAccounts, 
 * DMSecondaryUsers and/or other DMGroup objects. 
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeMembers: (NSArray *)members;

/*!
 * @method members
 * @abstract Lists all members of the given .Mac member-owned group. Returns a DMTransaction object
 * whose -isSuccessful method can be used to test for success and whose -result method, upon 
 * success, returns an NSArray of principal ID strings (in DMTransaction -result:) that identify 
 * principals corresponding to DMMemberAccounts, DMSecondaryUsers and/or other DMGroup objects. 
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)members;

@end

#endif
