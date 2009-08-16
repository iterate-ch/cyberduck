#ifndef __DMTRANSACTIONGENERATOR_H__
#define __DMTRANSACTIONGENERATOR_H__

/*
    DMTransactionGenerator.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMTransactionGenerator
 *  @abstract Defines the DMTransactionGenerator interface.
 */

#import <Foundation/Foundation.h>


@protocol DMTransactionGenerator

#pragma mark -
#pragma mark Dealing with delegates

/*!
 * @method setTransactionDelegate:
 * @abstract Used to set the delegate object which will receive transaction status 
 *	messages.
 * @discussion Sets the delegate object that will receive status messages 
 *	from asynchronous transactions as they complete.  The object passed 
 *	in this method's delegate parameter must conform to the informal 
 *	DMTransactionDelegate protocol.  Delegates receive their messages on the run 
 *	loop originally used to create the given DMTransaction.  It is safe to call 
 *	this method to change the delegate as needed--since an in-flight DMTransaction 
 *	object uses the settings that were in place at the time it was created, calling 
 *	this method will not affect any transaction already in progress.
 * @param delegate The delegate object which will receive asynchronous status messages.
 */
- (void)setTransactionDelegate: (id)delegate;

/*!
 * @method transactionDelegate
 * @abstract Used to get the currently set delegate object that receives transaction 
 *	status messages for this object.
 * @result Returns the object that is currently set to receive asynchronous transaction 
 *	status messages.
 */
- (id)transactionDelegate;


#pragma mark -
#pragma mark Dealing with synchronicity

/*!
 * @method setIsSynchronous:
 * @abstract Used to set the synchronicity state for transactions.
 * @discussion Passing YES for useSynchronous will cause the object's transaction 
 *	methods to block until they have either completed their transaction 
 *	or failed with an error. Passing NO will cause the methods to return immediately, 
 *	using any previously set delegate object to signal completion or error.  It is 
 *	safe to call this method to switch between asynchronous and synchronous modes as
 * 	needed--since an in-flight DMTransaction object uses the settings that were in 
 *	place at the time it was created, calling this method will not affect any 
 *	transaction already in progress.  [Note: The default mode is asynchronous.]
 * @param useSynchronous Boolean value specifying the state of synchronicity for this 
 *	session.
 */
- (void)setIsSynchronous: (BOOL)useSynchronous;

/*!
 * @method isSynchronous
 * @abstract Used to set the synchronicity state for transactions.
 * @discussion Gets the current state of synchronicity for this object. Returns either 
 *	YES for synchronous or NO for asynchronous. [Note: The default mode is 
 *	asynchronous.]
 * @result Returns the synchronicity state for transactions.
 */
- (BOOL)isSynchronous;

@end

#endif


