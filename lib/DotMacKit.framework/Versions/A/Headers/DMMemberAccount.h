#ifndef __DMMEMBERACCOUNT_H__
#define __DMMEMBERACCOUNT_H__

/*
    DMMemberAccount.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <DotMacKit/DMTypesAndConstants.h>
#import <DotMacKit/DMAccount.h>
#import <DotMacKit/DMGroup.h>
#import <DotMacKit/DMTransactionGenerator.h>

/*! @header DMMemberAccount 
 *  @abstract Defines access to DMMemberAccount objects, which correspond to particular 
 *	.Mac member accounts, providing account validation capabilities, access to
 *	specific account properties, and a mechanism to manage secondary users and groups.
 */
@class DMTransaction;

/*!
 * @class DMMemberAccount
 * @abstract A DMMemberAccount object corresponds to a .Mac member account, providing account
 *	validation capabilities as well as access to specific account properties.  
 */
@interface DMMemberAccount : DMAccount <DMTransactionGenerator> {
    id _delegate;
    BOOL _use_synchronous;
    id _info_session;
	id _info_session2;
    NSURL *_info_url;
	NSURL *_info_url2;
    NSLock *_info_lock;
    NSString *_user_agent;
    id _my_private_ivars;
}


#pragma mark -
#pragma mark Factories and Initers

/*!
 * @method accountFromPreferencesWithApplicationID:
 * @abstract Returns a new autoreleased account object using the .Mac credentials found in the
 *	system preferences.
 * @discussion Returns an initialized, auto-released instance of DMMemberAccount keyed to a 
 *	specific application's identification string. The instance returned will have the 
 *	.Mac name and password from the system pre-applied. Returns nil if no account is set 
 *	in the preferences. Pass the unique application identifying string obtained from
 *	http://developer.apple.com/datatype/creatorcode.html
 * @param creatorCode The application's 4-character id string.
 * @result A new autoreleased DMMemberAccount object.
 */
+ (id)accountFromPreferencesWithApplicationID: (NSString *)creatorCode;

/*!
 * @method accountWithName:password:applicationID:
 * @abstract Returns a new autoreleased account object using the provided .Mac credentials.
 * @discussion Returns an initialized, auto-released instance of DMMemberAccount keyed to a 
 *	specific application's identification string.  The instance returned will use the provided 
 *	name and password, rather than any credentials stored in the .Mac preferences on the system.  
 *	(To have the credentials stored on the system pre-applied, use the 
 *	accountFromPreferencesWithApplicationID: method instead.) Pass the unique application 
 *	identifying string obtained from http://developer.apple.com/datatype/creatorcode.html
 * @param name A username string.
 * @param password A password string. (Pass nil for unauthenticated Public folder access.)
 * @param creatorCode The application's 4-character id string.
 * @result A new autoreleased DMMemberAccount object.
 */
+ (id)accountWithName: (NSString *)name 
                    password: (NSString *)password
                    applicationID: (NSString *)creatorCode;

/*!
 * @method initFromPreferencesWithApplicationID:
 * @abstract Returns a newly allocated account object using the .Mac credentials found in the
 *	system preferences.
 * @discussion Returns an initialized, allocated instance of DMMemberAccount keyed to a 
 *	specific application's identification string. The instance returned will have the 
 *	.Mac name and password from the system pre-applied. Returns nil if no account is set 
 *	in the preferences. Pass the unique application identifying string obtained from
 *	http://developer.apple.com/datatype/creatorcode.html
 * @param creatorCode The application's 4-character id string.
 * @result A newly initialized, allocated DMMemberAccount object.
 */
- (id)initFromPreferencesWithApplicationID: (NSString *)creatorCode;

/*!
 * @method initWithName:password:applicationID:
 * @abstract Returns a newly allocated account object using the provided .Mac credentials.
 * @discussion Returns an initialized, allocated instance of DMMemberAccount keyed to a 
 *	specific application's identification string.  The instance returned will use the provided 
 *	name and password, rather than any credentials stored in the .Mac preferences on the system.  
 *	(To have the credentials stored on the system pre-applied, use the 
 *	initFromPreferencesWithApplicationID: method instead.) Pass the unique application 
 *	identifying string obtained from http://developer.apple.com/datatype/creatorcode.html 
 * @param name A username string.
 * @param password A password string. (Pass nil for unauthenticated Public folder access.)
 * @param theID The application's 4-character id string.
 * @result A newly initialized, allocated DMMemberAccount object.
 */
- (id)initWithName: (NSString *)name
                    password: (NSString *)password
                    applicationID: (NSString *)creatorCode;


#pragma mark -
#pragma mark Checking account credentials

/*!
 * @method validateCredentials
 * @abstract Used to check that the credentials set for this account are valid.
 * @discussion Use this method to check that the given account contains valid .Mac credentials.  
 *	The returned integer will correspond to one of the status constants defined in 
 *	DMTypesAndConstants.h.
 * @result A status constant, as defined in DMTypesAndConstants.h.
 */
- (int)validateCredentials;

/*!
 * @method matchesSystemCredentials
 * @abstract Used to check that the credentials set for this account are valid.
 * @discussion Returns YES if the name and password currently set for this DMMemberAccount 
 *		object match the .Mac credentials stored on the system.  Returns NO otherwise.
 * @result A BOOL value indicating whether this account's credentials match the credentials 
 *		currently stored in the .Mac system preferences.
 */
- (BOOL)matchesSystemCredentials;


#pragma mark -
#pragma mark Requesting account properties

/*!
 * @method servicesAvailableForAccount
 * @abstract Retrieves a listing of the .Mac services available to this account.
 * @discussion The returned DMTransaction object's result method returns an NSArray of NSString 
 *	entries corresponding to available services for the .Mac account represented by this 
 *	DMMemberAccount instance.  If a given .Mac service is enabled for this account, then the 
 *	array will contain a string equal to a corresponding NSString service constant defined in
 *	DMTypesAndConstants.h.  [Note: If the account credentials do not exist or some other error 
 *	occurs, the returned DMTransaction's errorType method can be used to identify the error.]
 * @result Returns a DMTransaction whose result method returns an NSArray containing service names.
 */
- (DMTransaction *)servicesAvailableForAccount;

/*!
 * @method daysLeftUntilExpiration
 * @abstract Retrieves a listing of the .Mac services available to this account.
 * @discussion The returned DMTransaction object's result method returns an NSNumber representing 
 *	the number of full days left until the given account expires.  [Note: If the account 
 *	credentials do not exist or some other error occurs, the returned DMTransaction's errorType
 *	method can be used to identify the error.]
 * @result Returns a DMTransaction whose result method returns an NSNumber object.
 */
- (DMTransaction *)daysLeftUntilExpiration;  

/*!
 * @method canUpgradeAccount
 * @abstract Retrieves a listing of the .Mac services available to this account.
 * @discussion The returned DMTransaction object's result method returns an NSNumber, whose Boolean 
 *	value can be retrieved using NSNumber's boolValue method.  This value will be YES if the 
 *	DMMemberAccount object's credentials are associated with an existing account that can be 
 *	upgraded, such as a trial or email-only account, or if the account has expired and needs to 
 *	be renewed.  Otherwise, the resulting value will be NO.  [Note: If the account credentials do 
 *	not exist or some other error occurs, the returned DMTransaction's errorType method can be 
 *	used to identify the error.]
 * @result Returns a DMTransaction whose result method returns an NSNumber object that represents a
 *	BOOL value. 
 */
- (DMTransaction *)canUpgradeAccount;


#pragma mark -
#pragma mark Upgrading/Creating accounts

/*!
 * @method upgradeAccount
 * @abstract Opens the .Mac upgrade page in the user's default browser.
 * @discussion This method will launch the user's default web browser and open the appropriate .Mac 
 *	membership upgrade page, which is localized based on system preferences.
 */
- (void)upgradeAccount;

/*!
 * @method accountUpgradeURL
 * @abstract Returns the URL for the .Mac upgrade page.
 * @discussion This method provides access to the same localized URL that the upgradeAccount method 
 *	loads in the default browser. [Note: The URL returned by this method should not be 
 *	hard-coded within an application, as it may change in the future.]
 */
- (NSURL *)accountUpgradeURL;

/*!
 * @method signUpNewMember
 * @abstract Opens the .Mac sign-up page in the user's default browser.
 * @discussion This method will launch the user's default web browser and open the .Mac new member 
 *	sign-up page, which is localized based on system preferences.  This method can be used when 
 *	the user of the given application is not yet a .Mac member and cannot fully enjoy the 
 *	application's features without a .Mac account.
 */
+ (void)signUpNewMember;

/*!
 * @method signUpNewMemberWithApplicationID:
 * @abstract This method performs the same function as the signUpNewMember method, and additionally 
 *	ensures that the owner of the provided application ID (if a member of the .Mac Affiliate 
 *	Program) earns a commission whenever a user of their application becomes a paying 
 *	.Mac member.  The application id is the unique string obtained from 
 *	http://developer.apple.com/datatype/creatorcode.html.  For more information on the 
 *	.Mac Affiliate Program, please visit http://www.mac.com/1/affiliates/faq.html.
 * @discussion If you'd like direct access to the URL opened by this method, perhaps for use in
 *	your own user interface, use the signUpURLWithApplicationID: method instead.
 */
+ (void)signUpNewMemberWithApplicationID: (NSString *)appID;

/*!
 * @method signUpURL
 * @abstract Returns the URL for the .Mac sign-up page.
 * @discussion This method provides access to the same localized URL that the signUpNewMember method 
 *	loads in the default browser.  [Note: The URL returned by this method should not be 
 *	hard-coded within an application, as it may change in the future.]
 */
+ (NSURL *)signUpURL;

/*!
 * @method signUpURLWithApplicationID:
 * @abstract Returns the URL for the .Mac sign-up page, with commission tracking enabled if the 
 *	owner of the provided application ID is a member of the .Mac Affiliate Program.  The 
 *	application id is the unique string obtained from 
 *	http://developer.apple.com/datatype/creatorcode.html.  For more information on the 
 *	.Mac Affiliate Program, please visit http://www.mac.com/1/affiliates/faq.html. 
 * @discussion This method provides access to the same URL that the signUpNewMemberWithApplicationID: 
 *	method loads in the default browser.  [Note: The URL returned by this method should not be 
 *	hard-coded within an application, as it may change in the future.]  
 */
+ (NSURL *)signUpURLWithApplicationID: (NSString *)appID;


#pragma mark -
#pragma mark Managing secondary accounts

/*!
 * @method secondaryUserNames:
 * @abstract Lists all secondary users owned by this .Mac member.  
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success and 
 * whose -result method returns an NSArray of NSStrings on success. 
 */
- (DMTransaction *)secondaryUserNames;

/*!
 * @method createSecondaryUserWithName:andPassword:
 * @abstract Adds a secondary user to .Mac if a secondary user of the same name does not already
 * exist for this owning member account.  
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)createSecondaryUserWithName: (NSString *)name
								andPassword: (NSString *)password;
                
/*!
 * @method removeSecondaryUserNamed:
 * @abstract Removes the secondary user with the given name from .Mac.
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeSecondaryUserNamed: (NSString *)name;

/*!
 * @method resetPassword:forSecondaryUserNamed:
 * @abstract Resets the password of the secondary user with the given name. 
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)resetPassword: (NSString *)password
		   forSecondaryUserNamed: (NSString *)name;


#pragma mark -
#pragma mark Managing groups

/*!
 * @method groupNames:
 * @abstract Lists all groups owned by this .Mac member.  
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success 
 *	and whose -result method returns an NSArray of NSString group names upon success. 
 */
- (DMTransaction *)groupNames;

/*!
 * @method groupNamed:
 * @abstract Use this method to obtain a DMGroup object representing a known group that already exists.
 * @result A DMGroup owned by this .Mac member.
 */
- (DMGroup *)groupNamed: (NSString *)name;

/*!
 * @method createGroupWithName:andMembers:
 * @abstract This method can be used to create a new DMGroup object with the provided name and members 
 *	if a group of the same name does not already exist for the owner’s account on .Mac.
 * @discussion The members argument is an NSArray of principal ID strings identifying principals 
 *	corresponding to DMMemberAccounts, DMSecondaryUsers and/or DMGroup objects. 
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)createGroupWithName: (NSString *)name andMembers: (NSArray *)members;

/*!
 * @method removeGroupNamed:
 * @abstract Removes the DMGroup with the given name from .Mac. 
 * @result Returns a DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeGroupNamed: (NSString *)name;


#pragma mark -
#pragma mark Deprecated methods

/* Deprecated -- Use DMTransactionGenerator's -setTransactionDelegate: method instead. */
- (void)setDelegate: (id)delegate;

/* Deprecated -- Use DMTransactionGenerator's -transactionDelegate: method instead. */
- (id)delegate;


@end

#endif
