#ifndef __DMUTILITIES_H__
#define __DMUTILITIES_H__

//
//  DMUtilities.h
//  DotMacKit
//
//  Copyright (c) 2005 Apple Computer, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DMUtilities : NSObject {
}

/*!
 * @method principalPropertiesFromPrincipalID:
 * @abstract Returns an NSDictionary representing the properties of the principal specified by principalID.
 * @discussion This utility method is used to parse a principal ID string returned by a DMPrincipal method.  
 *	This method returns a dictionary with values for the following keys: kDMPrincipalName, kDMPrincipalOwner 
 *	and kDMPrincipalType.  The kDMPrincipalOwner key corresponds to the name of the .Mac member who owns the 
 *	given principal (this is the same as the principal name if the principal represents a DMMemberAccount) 
 *	and the kDMPrincipalType key corresponds to one of the following: kDMMemberAccount, kDMSecondaryUser or 
 *	kDMGroup. (These constants are defined in DMTypesAndConstants.h.) 
 * @result An NSDictionary.
 */
+ (NSDictionary *)principalPropertiesFromPrincipalID: (NSString *)principalID;
						
@end

#endif