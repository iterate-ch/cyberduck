#ifndef __DMIDISKSESSION_H__
#define __DMIDISKSESSION_H__

/*
    DMiDiskSession.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <DotMacKit/DMTypesAndConstants.h>

/*! @header DMiDiskSession 
 *  @abstract Defines access to the DMiDiskSession object, which provides efficient read/write 
 *	access to the iDisk belonging to the associated .Mac account.
 *  @discussion Please note: All server-side paths passed as arguments to DMiDiskSession methods 
 *	should start with a forward slash, omitting the iDisk account name at the beginning of the 
 *	path--for example, if uploading a local file to a user's Public folder, the second path 
 *	passed to the putLocalFileAtPath:toPath: method would be of the format '/Public/filename'.
 */
@class DMAccount;
@class DMTransaction;


/*!
 * @class DMiDiskSession
 * @abstract A DMiDiskSession object provides efficient read/write access to the iDisk belonging
 *	to the associated .Mac account.  
 */
@interface DMiDiskSession : NSObject {
    DMAccount *_account;
    id _recent_lock_tokens;
    id _delegate;
    BOOL _use_synchronous;
    NSTimeInterval _lockDuration;
    id _session_data;
    BOOL _checked;
    BOOL _checkPreAuth;
    NSLock *_auth_lock;
    id _my_private_ivars;
}

#pragma mark -
#pragma mark Factories and Initers

/*!
 * @method iDiskSessionWithAccount:
 * @abstract Returns a new autoreleased session corresponding to the provided account's iDisk.
 * @discussion This method will return an auto-released iDisk session instance that can be used to 
 *	perform I/O transactions with the .Mac iDisk servers.  It requires a DMMemberAccount
 * 	argument, the credentials of which will be used to access the given iDisk.
 * @param account The .Mac account, containing the credentials needed for iDisk access.
 * @result A new autoreleased DMiDiskSession object.
 */
+ (id)iDiskSessionWithAccount: (DMAccount *)account;

/*!
 * @method initWithAccount:
 * @abstract Initialize a new allocated session corresponding to the provided account's iDisk.
 * @discussion This method will return an iDisk session instance that can be used to perform 
 *	I/O transactions with the .Mac iDisk servers. It requires a DMAccount argument, the 
 *	credentials of which will be used to access the given iDisk.
 * @param account The .Mac account, containing the credentials needed for iDisk access.
 * @result A newly initialized and allocated DMiDiskSession object.
 */
- (id)initWithAccount: (DMAccount *)account;


#pragma mark -
#pragma mark Dealing with access and the account

/*!
 * @method validateAccess
 * @abstract Used to check that the credentials set for the session can access the iDisk.
 * @discussion Use this method to check that the client machine and the account associated with 
 *	this iDisk session have access to the iDisk servers.  The returned integer corresponds 
 *	to one of the status constants defined in DMTypesAndConstants.h.
 * @result A status constant, as defined in DMTypesAndConstants.h.
 */
- (int)validateAccess; 

/*!
 * @method account
 * @abstract Used to access the account object associated with this session's iDisk.
 * @discussion  Returns the account object containing the credentials being used to access this 
 *	session's iDisk.
 * @result Returns the account object associated with this session's iDisk.
 */
- (DMAccount *)account;


#pragma mark -
#pragma mark Dealing with delegates

/*!
 * @method setDelegate:
 * @abstract Used to set the delegate object which will receive transaction status messages.
 * @discussion Sets the delegate object that will receive status messages from asynchronous 
 *	DMiDiskSession transactions as they complete.  The object passed in this method's delegate 
 *	parameter must conform to the informal DMTransactionDelegate protocol.  Delegates receive 
 *	their messages on the run loop originally used to create the given DMTransaction.  It is 
 *	safe to call this method to change the delegate as needed--since an in-flight DMTransaction 
 *	object uses the settings that were in place at the time it was created, calling this 
 *	method will not affect any transaction already in progress.
 * @param delegate The delegate object which will receive status messages.
 */
- (void)setDelegate: (id)delegate;

/*!
 * @method delegate
 * @abstract Used to get the currently set delegate object that receives transaction status messages.
 * @result Returns the object that is currently set to receive asynchronous transaction status 
 *	messages.
 */
- (id)delegate;


#pragma mark -
#pragma mark Dealing with synchronicity

/*!
 * @method setIsSynchronous:
 * @abstract Used to set the synchronicity state of this session.
 * @discussion Passing YES for useSynchronous will cause this DMiDiskSession object's transaction 
 *	methods to block until they have either completed their transaction or failed with an error.  
 *	Passing NO will cause the methods to return immediately, using any previously set delegate 
 *	object to signal completion or error.  It is safe to call this method to switch between 
 *	asynchronous and synchronous modes as needed--since an in-flight DMTransaction object uses 
 *	the settings that were in place at the time it was created, calling this method will not 
 *	affect any transaction already in progress.  [Note: DMiDiskSession's default mode is asynchronous.]
 * @param useSynchronous Boolean value specifying the state of synchronicity for this session.
 */
- (void)setIsSynchronous: (BOOL)useSynchronous;

/*!
 * @method isSynchronous
 * @abstract Used to set the synchronicity state of this session.
 * @discussion Gets the current state of synchronicity for this session. Returns either YES 
 *	for synchronous or NO for asynchronous. [Note: DMiDiskSession's default 
 *	mode is asynchronous.]
 * @result Returns the synchronicity state of this session.
 */
- (BOOL)isSynchronous;


#pragma mark -
#pragma mark Getting the storage quota

/*!
 * @method quotaAttributes
 * @abstract Gets the user's iDisk quota and amount of space currently used.
 * @discussion Use this method to get the user's iDisk quota value along with the amount of storage 
 *	space that is currently used.  The returned DMTransaction object's result method
 *	returns an NSDictionary containing two NSNumber values, which can be retrieved 
 *	using the following key constants: kDMiDiskQuotaInBytes and kDMiDiskSpaceUsedInBytes.
 * @result Returns a DMTransaction whose result method returns an NSDictionary with the user's 
 *	iDisk quota and amount of space currently used.
 */
- (DMTransaction *)quotaAttributes;


#pragma mark -
#pragma mark Downloading data

/*!
 * @method getDataAtPath:
 * @abstract Gets data from the provided iDisk path.
 * @discussion If the transaction is successful, the returned DMTransaction object's result method 
 *	returns an NSData object containing the desired resource (this NSData object is
 *	memory-mapped from a file in /tmp, allowing an application to download a large amount 
 *	of data without bringing it into real memory until it is actually needed--clean-up of the 
 *	temporary file happens automatically).  [Note: If the transaction is asynchronous and is not 
 *	yet complete, the result method will return the data that has been downloaded so far.] The 
 *	returned DMTransaction object can be used to get the result of the transaction, status, 
 *	payload, and many other pieces of information about the actual transaction.
 * @param sourcePath A remote iDisk path.
 * @result Returns a DMTransaction whose result method returns an NSData object containing data from
 *		the resource of the provided iDisk path.
 */
- (DMTransaction *)getDataAtPath: (NSString *)sourcePath;

/*!
 * @method getDataAtPath:ifModifiedSince:
 * @abstract Gets data from the provided iDisk path.
 * @discussion If the transaction is successful, the returned DMTransaction object's result method 
 *	returns an NSData object containing the desired resource (this NSData object is memory-mapped 
 *	from a file in /tmp, allowing an application to download a large amount of data without 
 *	bringing it into real memory until it is actually needed--clean-up of the temporary file 
 *	happens automatically). [Note: If the transaction is asynchronous and is not yet complete,
 * 	the result method will return the data that has been downloaded so far.]  
 *	The returned DMTransaction object can be used to get the result of the transaction, status, 
 *	payload, and many other pieces of information about the actual transaction.
 * @param sourcePath A remote iDisk path.
 * @param lastModifiedDate The modified date which determines whether the server will return data 
 *	for this get request.
 * @result Returns a DMTransaction whose result method returns an NSData object containing data from
 *	the resource referenced by the provided iDisk path.
 */
- (DMTransaction *)getDataAtPath: (NSString *)sourcePath
                    ifModifiedSince: (NSDate *)lastModifiedDate;

/*!
 * @method getDataAtPath:ifModifiedSince:withByteRange:
 * @abstract Get data from the provided iDisk path.
 * @discussion If the transaction is successful, the returned DMTransaction object's result method 
 *	returns an NSData object containing the desired resource (this NSData object is memory-mapped 
 *	from a file in /tmp, allowing an application to download a large amount of data without
 * 	bringing it into real memory until it is actually needed--clean-up of the temporary file 
 *	happens automatically). The NSRange object, passed as the byteRange argument, 
 *	should have its location set as the index of the first byte to be retrieved and its length 
 *	should be set as the total number of bytes to be retrieved, including the first byte.  
 *	You can also pass nil for lastModifiedDate if you are not interested in when the server 
 *	resource was last modified. [Note: If the transaction is aysnchronous and is not yet
 * 	complete, the result method will return the data that has been downloaded so far.]
 * @param sourcePath A remote iDisk path.
 * @param lastModifiedDate The modified date which determines whether the server will return data 
 *	for this get request.
 * @param byteRange The byte range of the given resource to be retrieved by this get request.
 * @result Returns a DMTransaction whose result method returns an NSData object containing data from
 *	the resource of the provided iDisk path.
 */
- (DMTransaction *)getDataAtPath: (NSString *)sourcePath
                    ifModifiedSince: (NSDate *)lastModifiedDate
                    withByteRange: (NSRange)byteRange;

#pragma mark -
#pragma mark Uploading data

/*!
 * @method putData:toPath:
 * @abstract Put data to the provided iDisk path.
 * @discussion This method places the contents of the data argument into the resource at the 
 *	location specified by the destination path.  [Note: Call will fail if any sublevels 
 *	of destinationPath (excluding the object itself) do not exist, and the call may return 
 *	nil if an invalid data argument is passed.] 
 * @param data The data to be uploaded.
 * @param destinationPath A remote iDisk destination path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)putData: (NSData *)data 
                    toPath: (NSString *)destinationPath; 

/*!
 * @method putData:toPath:withByteRange:
 * @abstract Put data to the provided iDisk path.
 * @discussion This method places the contents of the data argument into the resource at the 
 *	location specified by the byteRange argument. The NSRange object, passed as the byteRange 
 *	argument, should have its location set as the index of the first byte to be written to 
 *	and its length should be set as the total number of bytes, including the first byte.  
 *	If the end of the specified range is beyond the end of the file, then the length of the 
 *	resource is extended to accommodate all of the bytes. [Note: Call will fail if any 
 *	sublevels of destinationPath (excluding the object itself) do not exist, and the call 
 *	may return nil if an invalid data argument is passed.]
 * @param data The data to be uploaded.
 * @param destinationPath A remote iDisk destination path.
 * @param byteRange The byte range of the remote resource to which data will be written.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction. 
 */
- (DMTransaction *)putData: (NSData *)data 
                    toPath: (NSString *)destinationPath 
                    withByteRange: (NSRange)byteRange;

/*!
 * @method putLocalFileAtPath:toPath:
 * @abstract Put the data in the local file to the provided iDisk path.
 * @discussion This method places the contents of a local file into a new server-side file at 
 *	the given destination path.  If the localPath parameter refers to a symlink, the 
 *	symlink will not be preserved--rather, the resource it references will be uploaded.  
 *	[Note: Call will fail if all sublevels of destinationPath (excluding the object itself) 
 *	do not already exist, and the call may return nil if an invalid localPath argument is passed.] 
 * @param localPath The path to a local file to be uploaded.
 * @param destinationPath A remote iDisk destination path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)putLocalFileAtPath: (NSString *)localPath 
                    toPath: (NSString *)destinationPath;


#pragma mark -
#pragma mark Creating collections

/*!
 * @method makeCollectionAtPath:
 * @abstract Create a collection at the provided iDisk path.
 * @discussion This method creates a collection (directory) at the location specified in thePath. 
 *	If any of the levels above the one being created does not exist, then the create will fail. 
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)makeCollectionAtPath: (NSString *)thePath;

 
#pragma mark -
#pragma mark Deleting resources

/*!
 * @method deleteResourceAtPath:
 * @abstract Deletes the resource at the provided iDisk path.
 * @discussion Deletes the resource referenced by thePath if it is not locked by another client and 
 *	the account who owns the credentials used has write access to thePath.  If the delete request 
 *	fails because the specified resource or its parent is locked, the returned DMTransaction 
 *	object's result method will return an NSArray of NSString's representing the exact uri's 
 *	which are currently locked.  [Note: Unlike on most file systems, a non-empty collection 
 *	(directory) can be deleted.  Issuing a delete targeted at a collection will delete the 
 *	collection and all of it contents without warning (assuming you have the correct access 
 *	privileges).]
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)deleteResourceAtPath: (NSString *)thePath;


#pragma mark -
#pragma mark Moving and Copying resources

/*!
 * @method moveResourceAtPath:toPath:
 * @abstract Moves the remote resource specified in sourcePath to destinationPath on the iDisk.
 * @discussion Moves the resource identified by sourcePath if it is not locked by another client 
 *	and the account who owns the credentials used has write access to destinationPath.  This 
 *	method will overwrite the resource at destinationPath if that resource is not locked by 
 *	another client. 
 * @param sourcePath A remote iDisk source path.
 * @param destinationPath A remote iDisk destination path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)moveResourceAtPath: (NSString *)sourcePath
                    toPath: (NSString *)destinationPath;

/*!
 * @method copyResourceAtPath:toPath:
 * @abstract Copies the remote resource specified in sourcePath to destinationPath on the iDisk.
 * @discussion Copies the resource identified by sourcePath if the account who owns the credentials 
 *	used has write access to destinationPath.  This method will overwrite the resource at 
 *	destinationPath if that resource is not locked by another client.  
 * @param sourcePath A remote iDisk source path.
 * @param destinationPath A remote iDisk destination path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)copyResourceAtPath: (NSString *)sourcePath
                    toPath: (NSString *)destinationPath;


#pragma mark -
#pragma mark Locking resources

/*!
 * @method lockResourceAtPath:
 * @abstract Lock the resource at the given iDisk path.
 * @discussion Locks the resource referenced by thePath so that only this session can execute write 
 *	operations (e.g., PUT, DELETE) on that resource.  The iDisk server reserves the right to 
 *	grant a lock duration/timeout that differs from the requested lock duration--when this method 
 *	is successful, the returned DMTransaction object's result method will return an NSDictionary 
 *	containing an NSNumber value (accessible using the kDMLockTimeout key) which is the lock 
 *	timeout that was granted by the server.  The lock will expire after the time specified by 
 *	the granted lock timeout (in seconds) has passed.  If the lock request fails because the
 * 	specified resource or its parent is already locked, the returned DMTransaction object's
 * 	result method will return an NSArray of NSString's representing the exact uri's which are
 *	currently locked.  To maintain a lock, issue a relock request on the same resource before 
 *	the original lock has expired.  [Note: The lock created is a cooperative, 'write' lock.]
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)lockResourceAtPath: (NSString *)thePath;

/*!
 * @method lockResourceAtPath:withDuration:
 * @abstract Lock the resource at the given iDisk path with the provided duration.
 * @discussion Locks the resource referenced by thePath so that only this session can execute write 
 *	operations (e.g., PUT, DELETE) on that resource.  The iDisk server reserves the right to 
 *	grant a lock duration/timeout that differs from the requested lock duration--when this method 
 *	is successful, the returned DMTransaction object's result method will return an NSDictionary 
 *	containing an NSNumber value (accessible using the kDMLockTimeout key) which is the lock 
 *	timeout that was granted by the server.  The lock will expire after the time specified by the 
 *	granted lock timeout (in seconds) has passed.  If the lock request fails because the 
 *	specified resource or its parent is already locked, the returned DMTransaction object's 
 *	result method will return an NSArray of NSString's representing the exact uri's which are 
 *	currently locked.  To maintain a lock, issue a relock request on the same resource before 
 *	the original lock has expired.  [Note: The lock created is a cooperative, 'write' lock.]
 * @param thePath A remote iDisk path.
 * @param lockDuration The lock duration, in seconds.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)lockResourceAtPath: (NSString *)thePath
                    withDuration: (NSTimeInterval)lockDuration;

/*!
 * @method relockResourceAtPath:
 * @abstract Relock the resource at the given iDisk path.
 * @discussion Refreshes the lock on the resource referenced by thePath.  The iDisk server reserves 
 *	the right to grant a lock duration/timeout that differs from the requested lock 
 *	duration--when this method is successful, the returned DMTransaction object's result method 
 *	will return an NSDictionary containing an NSNumber value (accessible using the kDMLockTimeout 
 *	key) which is the lock timeout that was granted by the server.  Once refreshed, the lock will 
 *	expire after the time specified by the granted lock timeout (in seconds) has passed.  
 *	Attempting to relock a resource that is not locked will always fail.  If the relock request 
 *	fails because the specified resource or its parent was originally locked by another client, 
 *	the returned DMTransaction object's result method will return an NSArray of NSString's 
 *	representing the exact uri's which are currently locked.  [Note: The lock created is a 
 *	cooperative, 'write' lock.]
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)relockResourceAtPath: (NSString *)thePath;

/*!
 * @method relockResourceAtPath:withDuration:
 * @abstract Relock the resource at the given iDisk path with the provided duration.
 * @discussion Refreshes the lock on the resource referenced by thePath.  The iDisk server reserves 
 *	the right to grant a lock duration/timeout that differs from the requested lock 
 *	duration--when this method is successful, the returned DMTransaction object's result method 
 *	will return an NSDictionary containing an NSNumber value (accessible using the kDMLockTimeout 
 *	key) which is the lock timeout that was granted by the server.  Once refreshed, the lock will 
 *	expire after the time specified by the granted lock timeout (in seconds) has passed.  
 *	Attempting to relock a resource that is not locked will always fail.  If the relock request 
 *	fails because the specified resource or its parent was originally locked by another client, 
 *	the returned DMTransaction object's result method will return an NSArray of NSString's 
 *	representing the exact uri's which are currently locked.  [Note: The lock created is a 
 *	cooperative, 'write' lock.]
 * @param thePath A remote iDisk path.
 * @param lockDuration The lock duration, in seconds.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)relockResourceAtPath: (NSString *)thePath
                    withDuration: (NSTimeInterval)lockDuration;

/*!
 * @method unlockResourceAtPath:
 * @abstract Unlock the resource at the given iDisk path.
 * @discussion Releases any lock on the resource referenced by thePath.  Attempting to unlock a 
 *	resource that is not locked will fail.
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)unlockResourceAtPath: (NSString *)thePath;


#pragma mark -
#pragma mark Dealing with lock duration

/*!
 * @method setDefaultLockDuration:
 * @abstract Set the default lock duration used by this session's lock and relock requests.
 * @discussion This method sets the default lock duration, in seconds, used by all lock-related 
 *	DMiDiskSession methods that do not specify a transaction-specific lock duration of 
 *	their own.  If this method is not called, the default lock duration is 5 minutes, 
 *	given in seconds.
 * @param lockDuration The lock duration, in seconds. 
 */
- (void)setDefaultLockDuration: (NSTimeInterval)lockDuration;   

/*!
 * @method defaultLockDuration
 * @abstract Get the default lock duration used by this session's lock and relock requests.
 * @discussion This method returns the default lock duration, in seconds, used by all lock-related 
 *	DMiDiskSession methods that do not specify a transaction-specific lock duration of 
 *	their own.  The default lock duration is 5 minutes, given in seconds. 
 * @result Returns an NSTimeInterval, which is the session's default lock duration, in seconds.  
 */
- (NSTimeInterval)defaultLockDuration;


#pragma mark -
#pragma mark Getting info on objects and collections

/*!
 * @method resourceExistsAtPath:
 * @abstract Determines whether the resource at the provided iDisk path exists.
 * @discussion A method which always operates synchronously, resourceExistsAtPath: returns YES 
 *	if the resource specified by thePath exists on the iDisk server, and returns NO otherwise.  
 *	[Note: To check for the existence of a resource asynchronously, use the 
 *	basicAttributesAtPath: method.]
 * @param thePath A remote iDisk path.
 * @result Returns a BOOL indicating whether the resource at the provided iDisk path exists.
 */
- (BOOL)resourceExistsAtPath: (NSString *)thePath;

/*!
 * @method basicAttributesAtPath:
 * @abstract Provides access to the basic attributes of a given resource if it exists.
 * @discussion Performs a lightweight access against a single file or collection (directory) 
 *	resource referenced by thePath. Use this method when only basic information such as the 
 *	last modified date or content length is needed, as it is lighter weight than an 
 *	extendedAttributesAtPath: call.  If a resource exists at thePath and the transaction 
 *	is successful, the returned DMTransaction object's result method returns an NSDictionary.  
 *	That NSDictionary contains resource attributes whose values, if present and applicable, 
 *	can be retrieved using the following NSString key constants: kDMContentLength and
 *	kDMLastModified.  The value corresponding to the kDMContentLength key is an 
 *	NSNumber (in bytes) and the value for the kDMLastModified key is an NSDate.  
 *	[Note: kDMContentLength is not returned for collections.]
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)basicAttributesAtPath: (NSString *)thePath;

/*!
 * @method extendedAttributesAtPath:
 * @abstract Provides access to the extended attributes of a given resource if it exists.
 * @discussion This method provides the attributes of the file or collection (directory) resource 
 *	specified by thePath.  If a resource exists at thePath and the request is successful, 
 *	the returned DMTransaction object's result method returns an NSDictionary.  That 
 *	NSDictionary contains resource attributes whose values, if present and applicable, 
 *	can be retrieved using the following NSString key constants: kDMContentLength,
 * 	kDMLastModified, kDMURI, kDMDisplayName, kDMContentType, kDMIsCollection, kDMIsLocked,
 *	kDMLockOwner, kDMLockToken and kDMLockTimeout. See the DMTypesAndConstants.h for the value 
 *	types which correspond to these keys.  [Note: kDMContentLength is not returned for
 * 	collections.]
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction,
 * 	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)extendedAttributesAtPath: (NSString*)thePath; 

/*!
 * @method listCollectionAtPath:
 * @abstract Provides a shallow listing of the contents of the requested collection.
 * @discussion This method retrieves the contents of a collection (directory) on the server. 
 *	The collection contents retrieved by this method are shallow and therefore do not 
 *	include the contents of any subdirectories.  If the request is successful, the 
 *	returned DMTransaction object's result method returns an NSArray of NSDictionaries.  
 *	Each dictionary contained in that array has the same format as the resource 
 *	attributes dictionary that is retrieved after calling DMiDiskSession's
 *	extendedAttributesAtPath: method.
 * @param thePath A remote iDisk path.
 * @result Returns a DMTransaction object that can be used to get the result of the transaction, 
 *	status, payload, and many other pieces of information about the actual transaction.
 */
- (DMTransaction *)listCollectionAtPath: (NSString *)thePath;

@end


#pragma mark

/*!
 * @category DMiDiskSession(DMiDiskFileManager)
 * @abstract The DMiDiskFileManager category adds NSFileManager-style methods to DMiDiskSession.
 */
@interface DMiDiskSession (DMiDiskFileManager)

/*! @functiongroup DMiDiskFileManager category methods */

#pragma mark -
#pragma mark Creating resources

/*!
 * @method createDirectoryAtPath:attributes:
 * @abstract Create a directory (collection) at the provided iDisk path.
 * @discussion This method will create a new directory at the given path.  It has the same usage 
 *	as the corresponding method of NSFileManager.  However, it does not support the setting of
 * 	attributes and if a non-nil attributes argument is passed to this method, it will be
 * 	ignored.
 * @param path A remote iDisk path.
 * @param attributes The attributes argument is ignored.
 * @result Returns a BOOL indicating whether the method was successful.
 */
- (BOOL)createDirectoryAtPath:(NSString *)path attributes:(NSDictionary *)attributes;

/*!
 * @method createFileAtPath:contents:attributes:
 * @abstract Create a file at the given iDisk path containing the provided data.
 * @discussion This method creates a file at path that contains contents, behavior which matches 
 *	the style of the corresponding method of NSFileManager. However, it does not support the
 * 	setting of attributes and if a non-nil attributes argument is passed to this method, 
 *	it will be ignored.
 * @param path A remote iDisk path.
 * @param contents The data to be written to the remote iDisk path.
 * @param attributes The attributes argument is ignored.
 * @result Returns a BOOL indicating whether the method was successful.
 */
- (BOOL)createFileAtPath:(NSString *)path contents:(NSData *)contents
                    attributes:(NSDictionary *)attributes;


#pragma mark -
#pragma mark Uploading/Downloading/Copying/Moving resources

/*!
 * @method copyPath:toPath:handler:
 * @abstract Copies the resource at the source path to the destination path.
 * @discussion This method will create a copy of the source resource at the given destination 
 *	path, but will not overwrite an existing resource.  Each path argument can be either a 
 *	local or a remote path--passing a local source path and remote destination path leads to 
 *	an upload of a local resource, while passing a remote source path and local destination 
 *	path leads to a download of a remote resource. If two remote paths are provided as
 *	arguments, this method issues a single, server-side COPY request.  This method has 
 *	usage similar to that of the corresponding method of NSFileManager.  However, it does 
 *	not support the usage of a callback handler and if a non-nil handler argument is 
 *	passed to this method, it will be ignored. Only the data forks of files and 
 *	directories are copied.  In the case of an upload, local symlinks are not preserved--
 *	when a symlink is encountered, the resource it references will be uploaded.  
 *	[Note: If two local paths are provided as arguments, this method calls 
 *	NSFileManager's copyPath:toPath:handler: method with whatever handler argument was 
 *	provided.]
 * @param source A local or remote source path.
 * @param destination A local or remote destination path.
 * @param handler The handler argument is ignored.
 * @result Returns a BOOL indicating whether the method was successful.
 */
- (BOOL)copyPath:(NSString *)source toPath:(NSString *)destination handler:(id)handler;

/*!
 * @method movePath:toPath:handler:
 * @abstract Moves the resource at the source path to the destination path.
 * @discussion This method will move the source resource to the given destination path, but will 
 *	not overwrite an existing resource.  Each path argument can be either a local or a remote
 * 	path--passing a local source path and remote destination path leads to an upload of a
 * 	local resource, while passing a remote source path and local destination path leads 
 *	to a download of a remote resource.  If two remote paths are provided as arguments, 
 *	this method issues a single, server-side MOVE request.  This method has usage similar 
 *	to that of the corresponding method of NSFileManager.  However, it does not support 
 *	the usage of a callback handler and if a non-nil handler argument is passed to this
 * 	method, it will be ignored.  Only the data forks of files and directories are moved.
 *	In the case of an upload, local symlinks are not preserved--when a symlink is encountered, 
 *	the resource it references will be uploaded. [Note: If two local paths are provided 
 *	as arguments, this method calls NSFileManager's copyPath:toPath:handler: method with 
 *	whatever handler argument was provided.]
 * @param source A local or remote source path.
 * @param destination A local or remote destination path.
 * @param handler The handler argument is ignored.
 * @result Returns a BOOL indicating whether the method was successful.
 */
- (BOOL)movePath:(NSString *)source toPath:(NSString *)destination handler:(id)handler;

/*!
 * @method contentsAtPath:
 * @abstract Retrieves the data contained in the file referenced by the provided iDisk path.
 * @discussion Returns the contents of the file specified in the path as an NSData object. If 
 *	the path specifies a directory, or if some other error occurs, this method returns nil. 
 *	[Note: Only the data fork of the given file is returned.]
 * @param path A remote iDisk path.
 * @result Returns an NSData object containing the contents of file referenced by the provided 
 *		iDisk path.
 */
- (NSData *)contentsAtPath:(NSString *)path;


#pragma mark -
#pragma mark Deleting resources

/*!
 * @method removeFileAtPath:handler:
 * @abstract Deletes the resource referenced by the provided iDisk path.
 * @discussion This method will remove the resource at the given path and has the same usage as 
 *	the corresponding method of NSFileManager.  However, it does not support the usage of a 
 *	callback handler and if a non-nil handler argument is passed to this method, it will be 
 *	ignored.
 * @param path A remote iDisk path.
 * @param handler The handler argument is ignored.
 * @result Returns a BOOL indicating whether the method was successful.
 */
- (BOOL)removeFileAtPath:(NSString *)path handler:(id)handler;


#pragma mark -
#pragma mark Getting info on objects and collections

/*!
 * @method fileExistsAtPath:
 * @abstract Determines whether the resource at the provided iDisk path exists.
 * @param path A remote iDisk path.
 * @result Returns YES if the file specified in path exists, or NO if it does not.
 */
- (BOOL)fileExistsAtPath:(NSString *)path;

/*!
 * @method fileExistsAtPath:isDirectory:
 * @abstract Determines whether the resource at the provided iDisk path exists, setting the
 *	BOOL referenced by the isDirectory argument to YES if the given resource is a 
 *	directory, and to NO if it is a file.
 * @param path A remote iDisk path.
 * @param isDirectory A reference to a BOOL, used to specify whether the given path references a 
 *	directory.
 * @result Returns YES if the file specified in path exists, or NO if it does not.
 */
- (BOOL)fileExistsAtPath:(NSString *)path isDirectory:(BOOL *)isDirectory;

/*!
 * @method directoryContentsAtPath:
 * @abstract Provides a shallow listing of the contents of the requested directory.
 * @discussion Searches the contents of the directory specified by path and returns an array 
 *	of strings identifying the directories and files contained in the path. The search is 
 *	shallow and therefore does not return the contents of any subdirectories.  This 
 *	method returns nil if the directory referenced by the path does not exist or there is 
 *	some other error accessing it. This method returns an empty array if the directory 
 *	exists but has no contents.
 * @param path A remote iDisk path.
 * @result Returns an NSArray containing the a contents listing of the specified directory.
 */
- (NSArray *)directoryContentsAtPath:(NSString *)path;

@end 

#endif
