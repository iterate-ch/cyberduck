#ifndef __DMTRANSACTION_H__
#define __DMTRANSACTION_H__

/*
    DMTransaction.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <DotMacKit/DMTypesAndConstants.h>

/*! @header DMTransaction 
 *  @abstract This header defines methods used to interrogate DMTransaction objects regarding
 * 	success/error status, the http status code received, payload data, bytes transferred
 * 	and more.
 */
@class DMiDiskSession;

/*!
 * @class DMTransaction
 * @abstract DMTransaction objects handle http requests both synchronously and asynchronously.  
 */
@interface DMTransaction : NSObject {
    id _request;
    id _delegate;
    id _result;
    BOOL _started;
    BOOL _finished;
    BOOL _aborted;
    BOOL _use_synchronous;
    BOOL _is_upload;
    BOOL _force;
    BOOL _tracking;
    BOOL _data;
    BOOL _result_required;
    id _pre_handler_obj;
    SEL _pre_handler_sel;
    id _post_handler_obj;
    SEL _post_handler_sel;
    id _response_handler;
    id _listener;
    NSString *_identifier;
    NSString *_notes;
    id _my_private_ivars;
}

#pragma mark -
#pragma mark Checking transaction status

/*!
 * @method isSuccessful
 * @abstract Returns a BOOL indicating transaction success.
 * @discussion Returns YES if the transaction has finished successfully, no network errors 
 *	occurred, and a valid http status code was returned.  Otherwise, returns NO.
 * @result Returns a BOOL indicating transaction success.
 */
- (BOOL)isSuccessful;

/*!
 * @method isFinished
 * @abstract Returns a BOOL indicating whether the transaction is finished.
 * @discussion This method will return YES if the given transaction is no longer active, 
 *	due to success, error or cancellation.  Otherwise, NO is returned.
 * @result Returns a BOOL indicating whether the transaction is finished.
 */
- (BOOL)isFinished;

/*!
 * @method transactionState
 * @abstract Used to get the state of the transaction.
 * @discussion Returns an integer constant that describes the current state of the transaction--
 *	kDMTransactionNotStarted, kDMTransactionActive, kDMTransactionSuccessful, 
 *	kDMTransactionAborted or kDMTransactionHadError is returned. (These values
 *	are defined in DMTypesAndConstants.h.)
 * @result A transaction state constant, as defined in DMTypesAndConstants.h.
 */
- (int)transactionState;

/*!
 * @method httpStatusCode
 * @abstract Used to get the http status code for the transaction.
 * @discussion Returns the HTTP status code for the request if applicable. Check 
 *	RFC 2068 (HTTP/1.1) for a complete list of all of the applicable status codes.
 *	[Note: kDMUndefined will be returned if the transaction has not yet 
 *	completed, if an http status code is not applicable for the given transaction 
 *	type, or if the transaction failed before an HTTP status code could even be 
 *	received, as might be the case if the client computer could not contact the 
 *	.Mac environment for some reason.]
 * @result An http status code.
 */
- (int)httpStatusCode;

/*!
 * @method errorType
 * @abstract Used to get the errorType, if any, for the transaction.
 * @discussion If an error occurred while generating the response associated with this 
 *	object, this method will return a specific error type, as defined in 
 *	DMTypesAndConstants.h.  If an unknown error occurred, kDMUnknownError is returned.  
 *	However, if no error occurred or the response has not yet completed when this 
 *	method is called, kDMUndefined will be returned.  [Note: If the http status 
 *	code that was received is considered a failure code for the given transaction 
 *	type, kDMUnknownError is returned--the http status code can be retrieved using 
 *	the httpStatusCode method of this class.]
 * @result A status code as defined in DMTypesAndConstants.h.
 */
- (int)errorType;


#pragma mark -
#pragma mark Cancelling a transaction

/*!
 * @method abort
 * @abstract Halts the progress of the transaction.
 * @discussion Stops the current transaction if it is running asychronously, halting its progress. 
 *	If the transaction is already complete (or resulted in an error) this method has no effect.
 */
- (void)abort;


#pragma mark -
#pragma mark Getting the result

/*!
 * @method result
 * @abstract Returns the result, if any, for the transaction.
 * @discussion Returns the result object of the given transaction, if currently available and 
 *	applicable.  Otherwise returns nil.  [Note: If this method is being used to obtain 
 *	the payload body of a GET request, and it is called before the transaction has finished, 
 *	the returned object will represent only the data received up until that point.]
 * @result Returns the result object appropriate to the specific type of transaction being made.
 */
- (id)result;


#pragma mark -
#pragma mark Convenient API

/*!
 * @method contentLength
 * @abstract Used to get the length of the content being uploaded or downloaded by the 
 *	transaction.
 * @discussion Returns the length in bytes of the content being downloaded or uploaded.  
 *	This returned length does not change over the life of the given transaction, 
 *	except in the case where this method is called immediately after a GET request 
 *	is started, before the content length has been received from the server--this 
 *	method returns kDMUndefined if called too early.
 * @result Returns the number of bytes being uploaded or downloaded by the transaction.
 */
- (SInt64)contentLength;

/*!
 * @method bytesTransferred
 * @abstract Used to get the number of bytes transferred by the transaction.
 * @discussion Most useful for asynchronous operations, this method returns the number 
 *	of bytes downloaded or uploaded thus far.
 * @result Returns the number of bytes transferred.
 */
- (SInt64)bytesTransferred;

/*!
 * @method uri
 * @abstract Used to get the transaction's target uri.
 * @discussion Returns the target uri sent in this transaction's originating request.
 * @result Returns the target uri.
 */
- (NSString *)uri;

@end

#endif
