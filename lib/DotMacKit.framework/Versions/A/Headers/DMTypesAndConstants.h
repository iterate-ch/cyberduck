#ifndef __DMTYPESANDCONSTANTS_H__
#define __DMTYPESANDCONSTANTS_H__

/*
    DMTypesAndConstants.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <Foundation/Foundation.h>

/*! @header DMTypesAndConstants
 *  @abstract This header provides access to useful DotMac Kit constants, including 
 *	.Mac service constants, resource attribute dictionary keys, status constants,
 *	and transaction state constants.
 */


#pragma mark -
#pragma mark Auto-launch options

typedef enum {
    kDMLaunchDefault       = 0,  // Only one instance, launch in foreground
    kDMLaunchAlways        = 1,  // Always launch a new instance of application (applies to bundled Apps only)
    kDMLaunchInBackground  = 2   // Don't bring launched apps to foreground (applies to bundled Apps only)
} DMAutoLaunchOptions;


#pragma mark -
#pragma mark .Mac Service constants 

/*!
 * @const kDMiDiskService
 * @abstract Key for the .Mac iDisk service (NSString).
 */
extern NSString *kDMiDiskService;

/*!
 * @const kDMEmailService
 * @abstract Key for the .Mac email service (NSString).
 */
extern NSString *kDMEmailService;

/*!
 * @const kDMBackupService
 * @abstract Key for the .Mac Backup service (NSString).
 */
extern NSString *kDMBackupService;

/*!
 * @const kDMWebHostingService
 * @abstract Key for the .Mac web hosting service (NSString).
 */
extern NSString *kDMWebHostingService;

/*!
 * @const kDMiSyncService
 * @abstract Key for the .Mac iSync service (NSString).
 */
extern NSString *kDMiSyncService;
 

#pragma mark -
#pragma mark Resource attribute keys 

extern NSString *kDMDateCreated;

/*!
 * @const kDMContentLength
 * @abstract Key for a resource's content length in bytes (NSNumber).
 */
extern NSString *kDMContentLength;

/*!
 * @const kDMLastModified
 * @abstract Key for a resource's last modified date (NSDate).
 */
extern NSString *kDMLastModified;

/*!
 * @const kDMDisplayName
 * @abstract Key for a resource's display name (NSString).
 */
extern NSString *kDMDisplayName;

/*!
 * @const kDMURI
 * @abstract Key for a resource's uri (NSString).
 */
extern NSString *kDMURI;

/*!
 * @const kDMContentType
 * @abstract Key for a resource's content type (NSString).
 */
extern NSString *kDMContentType;

/*!
 * @const kDMIsCollection
 * @abstract Key for a resource's collection status (NSNumber boolean).
 */
extern NSString *kDMIsCollection;

/*!
 * @const kDMIsLocked
 * @abstract Key for a resource's locked status (NSNumber boolean).
 */
extern NSString *kDMIsLocked;

/*!
 * @const kDMLockOwner
 * @abstract Key for a resource's lock owner (NSString).
 */
extern NSString *kDMLockOwner;

/*!
 * @const kDMLockToken
 * @abstract Key for a resource's lock token (NSString).
 */
extern NSString *kDMLockToken;

/*!
 * @const kDMLockTimeout
 * @abstract Key for a resource's lock timeout in seconds (NSNumber).
 */
extern NSString *kDMLockTimeout;


#pragma mark -
#pragma mark Quota attribute keys 

/*!
 * @const kDMiDiskQuotaInBytes
 * @abstract Key for an iDisk's quota in bytes (NSNumber).
 */
extern NSString *kDMiDiskQuotaInBytes;

/*!
 * @const kDMiDiskSpaceUsedInBytes
 * @abstract Key for for an iDisk's space used in bytes (NSNumber).
 */
extern NSString *kDMiDiskSpaceUsedInBytes;


#pragma mark -
#pragma mark Principal types

extern NSString *kDMMemberAccount;
extern NSString *kDMSecondaryUser; 
extern NSString *kDMGroup;


#pragma mark -
#pragma mark Principal property keys

extern NSString *kDMPrincipalName; 
extern NSString *kDMPrincipalOwner;
extern NSString *kDMPrincipalType;


#pragma mark -
#pragma mark Logging constants

/*!
 * @const kDMLoggingLevel
 * @abstract Key used to set the framework's logging level in the standard user defaults (NSString).
 */
extern NSString *kDMLoggingLevel;

/*!
 * @const kDMErrorLogging
 * @abstract When this string is set as the value corresponding to the kDMLoggingLevel key in
 *	the standard user defaults, errors are logged to stderr (NSString).
 */
extern NSString *kDMErrorLogging;

/*!
 * @const kDMDebugLogging
 * @abstract When this string is set as the value corresponding to the kDMLoggingLevel key in
 *	the standard user defaults, debugging information and errors are logged to stderr (NSString).
 */
extern NSString *kDMDebugLogging;


#pragma mark -
#pragma mark Status constants

/*!
 * @defined kDMSuccess
 * @abstract Status constant indicating operation success.
 */
#define kDMSuccess (1)

/*!
 * @defined kDMInvalidCredentials
 * @abstract Status constant indicating failure due to invalid credentials.
 */
#define kDMInvalidCredentials (101)

/*!
 * @defined kDMInsufficientStorage
 * @abstract Status constant indicating failure due to insufficient storage.
 */
#define kDMInsufficientStorage (102)

/*!
 * @defined kDMNetworkError
 * @abstract Status constant indicating failure due to a network error or lack of
 *	network availability.
 */
#define kDMNetworkError (103)

/*!
 * @defined kDMResourceNotFound
 * @abstract Status constant indicating failure due to a required resource that was not found.
 */
#define kDMResourceNotFound (104)

/*!
 * @defined kDMResourceExists
 * @abstract Status constant indicating failure due to the pre-existence of a given resource.
 */
#define kDMResourceExists (105)

/*!
 * @defined kDMServiceBusy
 * @abstract Status constant indicating failure due to the current busyness of a given service.
 *	When this error is encountered, a subsequent retry after a delay is recommended.
 */
#define kDMServiceBusy (106)

/*!
 * @defined kDMInvalidParameter
 * @abstract Status constant an error due to an invalide user-provided parameter.
 */
#define kDMInvalidParameter (107)

/*!
 * @defined kDMUnknownError
 * @abstract Status constant indicating failure due to an unknown error.
 */
#define kDMUnknownError (100)


#pragma mark -
#pragma mark Transaction state constants

/*!
 * @defined kDMTransactionNotStarted
 * @abstract State constant indicating that the transaction has not started.
 */
#define kDMTransactionNotStarted (1)

/*!
 * @defined kDMTransactionActive
 * @abstract State constant indicating that the transaction is in progress.
 */
#define kDMTransactionActive (2)

/*!
 * @defined kDMTransactionSuccessful
 * @abstract State constant indicating that the transaction was successful.
 */
#define kDMTransactionSuccessful (3)

/*!
 * @defined kDMTransactionAborted
 * @abstract State constant indicating that the transaction was cancelled.
 */
#define kDMTransactionAborted (4)

/*!
 * @defined kDMTransactionHadError
 * @abstract State constant indicating that the transaction ended with an error.
 */
#define kDMTransactionHadError (5)


#pragma mark -
#pragma mark Miscellaneous

/*!
 * @defined kDMUndefined
 * @abstract Constant used throughout the DotMac Kit wherever an "Undefined" value is appropriate.
 */
#define kDMUndefined (-1)


#pragma mark -
#pragma mark Access privilege constants

/*!
 * @defined kDMRead
 * @abstract Read permission. This also implies kDMReadChildren.
 */
extern NSString *kDMRead;

/*!
 * @defined kDMWrite
 * @abstract Write permission. This also implies kDMModify.
 */
extern NSString *kDMWrite;

/*!
 * @defined kDMModify
 * @abstract Permission to modify the resource but not to delete or rename. Does not apply to collections/directories
 */
extern NSString *kDMModify;

/*!
 * @defined kDMReadChildren
 * @abstract Permission to read a resource inside the directory/collection but cannot list the directory/collection. 
 *			 Only applies to collections/directories.
 */
extern NSString *kDMReadChildren;

#endif
