#ifndef __DMSECURITY_H__
#define __DMSECURITY_H__

/*
    DMSecurity.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMSecurity 
 *  @abstract Defines the DMSecurity protocol, which is used to get and set access privileges for securable 
 *  entities (such as DMTopics and NSStrings paths representing iDisk resources).  Access is granted to 
 *  principals corresponding to DMMemberAccounts, DMSecondaryUsers and DMGroups.  [Principals are represented 
 *  by principal IDÕs, which can be obtained by calling the +principalIDWithName:andOwner method on a 
 *  DMMemberAccount, DMSecondaryUser or DMGroup object. If you already have an instance of one of these 
 *  objects you can call its ÐprincipalID instance method instead.]    
 */

#import <Foundation/Foundation.h>

@class DMTransaction;

@protocol DMSecurity

/*!
 * @method accessToEntity:
 * @abstract Retrieves access privileges set on the specified entity for an identified principal. 
 * @discussion Takes an entity (valid entities include DMTopics and NSString paths representing iDisk 
 *	resources) and also takes a principal ID string that corresponds to a DMMemberAccount, 
 *	DMSecondaryUser or DMGroup.  Returns a DMTransaction object whose -isSuccessful method can be 
 *	used to test for success and whose -result method, upon success, returns an NSArray of access 
 *	privileges (kDMRead, kDMWrite, kDMModify, kDMReadChildren) as defined in DMTypesAndConstants.h.
 * @result A DMTransaction object.
 */
- (DMTransaction *)accessToEntity: (id)entity forPrincipal: (NSString *)principalID;

/*!
 * @method accessToEntity:forPrincipals:
 * @abstract Retrieves access privileges set on the specified entity. 
 * @discussion Returns access privileges set on the specified entity for a list of principals (valid 
 *	entities include DMTopics and NSString paths representing iDisk resources).  Returns a DMTransaction 
 *	object whose -isSuccessful method can be used to test for success and whose -result method, upon 
 *	success, returns an NSDictionary, where each key in the dictionary is a principal ID string 
 *	corresponding to an NSArray value that contains a set of privilege strings for the given principal. 
 *	Valid privilege strings include kDMRead, kDMWrite, kDMModify and kDMReadChildren, as defined in
 *	DMTypesAndConstants.h.
 * @result A DMTransaction object.
*/
- (DMTransaction *)accessToEntity: (id)entity forPrincipals: (NSArray *)principalIDs;

/*!
 * @method setAccess:toEntity:forPrincipals:
 * @abstract Sets access privileges on the specified entity for the identified principal(s).
 *	Sets access privileges on the specified entity for the identified principal(s).  The access 
 *	argument is an NSArray of privilege strings. Valid privilege strings include kDMRead, kDMWrite, 
 *	kDMModify and kDMReadChildren, as defined in DMTypesAndConstants.h.  Any privilege not included 
 *	in the access array will be denied.  When the specified entity has children (such as a directory 
 *	on iDisk) any direct or indirect child will inherit the given privileges unless different access 
 *	is explicitly set on that child.  If the access privileges array is nil, this method will remove 
 *	all access for the given principals.  The entity argument is either a DMTopic object or an 
 *	NSString path representing an iDisk resource.  The principals argument is an NSArray of principal 
 *	ID strings that identify the unique principals corresponding to DMMemberAccounts, DMSecondaryUsers 
 *	and/or DMGroups.  The returned DMTransaction's -isSuccessful method can be used to test for success. 
 * @result A DMTransaction object. 
 */
- (DMTransaction *)setAccess: (NSArray *)access toEntity: (id)entity
			   forPrincipals: (NSArray *)principals;
            
/*!
 * @method resetAccessToChildrenOfEntity:
 * @abstract This method removes all access privileges explicitly set on the children of the specified 
 *	entity.  The returned DMTransaction's -isSuccessful method can be used to test for success.   
 * @result A DMTransaction object.
 */
- (DMTransaction *)resetAccessToChildrenOfEntity: (id)entity;

@end

#endif