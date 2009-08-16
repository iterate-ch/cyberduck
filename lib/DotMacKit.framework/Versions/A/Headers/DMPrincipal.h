#ifndef __DMPRINCIPAL_H__
#define __DMPRINCIPAL_H__

/*
    DMPrincipal.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <Foundation/Foundation.h>

@protocol DMPrincipal

/*!
 * @method principalIDWithName:andOwner:
 * @abstract Returns a principal ID string identifying the principal with the given name 
 *	and owner (the owner is the name of the .Mac member that owns the given principal; when
 *	calling this method for a DMMemberAccount, the name and owner strings are the same).
 * @result An NSString.
 */
+ (NSString *)principalIDWithName: (NSString *)name andOwner: (NSString *)memberName;

/*!
 * @method principalIDWithName:andOwner:
 * @abstract Returns a principal ID string identifying a principal, such as a .Mac member,
 *	secondary user or group.
 * @result An NSString.
 */
- (NSString *)principalID;

@end

#endif