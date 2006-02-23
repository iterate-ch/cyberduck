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

#endif
