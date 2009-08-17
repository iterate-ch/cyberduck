#ifndef __DMTRANSACTIONDELEGATE_H__
#define __DMTRANSACTIONDELEGATE_H__

/*
    DMTransactionDelegate.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <DotMacKit/DMTypesAndConstants.h>

/*! @header DMTransactionDelegate
 *  @abstract This header defines the informal DMTransactionDelegate protocol implemented by 
 *	objects to receive informational callbacks about asynchronous DMTransaction 
 *	operations in progress.
 */
@class DMTransaction;

/*!
 * @category NSObject(DMTransactionDelegate)
 * @abstract The DMTransactionDelegate category on NSObject defines methods that can be 
 *	implemented by objects to receive informational callbacks about asynchronous 
 * 	DMTransaction operations in progress.
 */
@interface NSObject (DMTransactionDelegate)

/*!
 * @method transactionSuccessful:
 * @abstract Called when an asynchronous transaction succeeds.
 * @discussion This delegate method is called when a transaction successfully completes.  
 *	If the transaction does not complete successfully, then transactionHadError: 
 *	is called instead.  Takes one parameter, which is a reference to the 
 *	DMTransaction object that completed.
 * @param theTransaction The transaction object.
 */
- (void)transactionSuccessful: (DMTransaction *)theTransaction;

/*!
 * @method transactionHadError:
 * @abstract Called when an asynchronous transaction fails.
 * @discussion This delegate method is called when a transaction encounters an error 
 *	condition. If the transaction does complete successfully, then 
 *	transactionSuccessful: is called instead.  Takes one parameter, which is a 
 *	reference to the DMTransaction object that experienced the error.
 * @param theTransaction The transaction object.
 */
- (void)transactionHadError: (DMTransaction *)theTransaction;

/*!
 * @method transactionAborted:
 * @abstract Called when an asynchronous transaction is cancelled.
 * @discussion This delegate method is called after the DMTransaction object's abort 
 *	method is called and stops a currently-running asynchronous transaction.
 * @param theTransaction The transaction object.
 */
- (void)transactionAborted: (DMTransaction *)theTransaction;

@end

#endif
