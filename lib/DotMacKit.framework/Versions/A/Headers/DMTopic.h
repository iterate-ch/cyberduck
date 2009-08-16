#ifndef __DMTOPIC_H__
#define __DMTOPIC_H__

/*
    DMTopic.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMTopic 
 *	DMTopics have DMMessages published to them using a DMMessagePublisher session.  Users of a 
 *	DMMessageSubscriber session can subscribe to a DMTopic object in order to be informed when new 
 *	messages are published to it.  DMTopic objects can only be created using the factory methods of a 
 *	DMMessagePublisher or a DMMessageSubscriber session, and they inherit the credentials of their 
 *	parent message session.  A DMTopic object can be passed to DMSecurity protocol methods that take 
 *	an 'entity' argument, in order to set access privileges on the given topic.  When a .Mac member 
 *	is granted access to a DMTopic, an invitation is automatically generated and can be accessed 
 *	using a DMMessageSubscriber session.  
 */

#import <Foundation/Foundation.h>
#import <DotMacKit/DMTransactionGenerator.h>

@class DMTransaction;
@class DMMessage;
@class DMMemberAccount;
@class DMMessageCache;

@interface DMTopic : NSObject <DMTransactionGenerator> {
	NSString *_name;
	NSString *_owner;
	DMMemberAccount *_credentials;
	NSString *_app_id;
	NSString *_topic_id;
	NSString *_launchables_id;
	id _message_session;
	DMMessageCache *_message_cache;
	id _transaction_delegate;
	BOOL _use_synchronous;
	BOOL _isAutoArrivalEnabled;
}


#pragma mark -
#pragma mark Convenience API

/*!
 * @method name
 * @abstract Returns the name of the topic.
 * @result An NSString topic name.
 */
- (NSString *)name;

/*!
 * @method owner
 * @abstract Returns the name of the .Mac member who owns and hosts the topic.
 * @result An NSString .Mac member name.
 */
- (NSString *)owner;

/*!
 * @method applicationID
 * @abstract Returns the creator code of the application that created the topic.
 * @result An NSString 4-character creator code.
 */
- (NSString *)applicationID;

/*!
 * @method credentials
 * @abstract Returns the .Mac member object containing the credentials that will be used to access 
 *	or modify this DMTopic object.
 * @result A DMMemberAccount object.
 */
- (DMMemberAccount *)credentials;

/*!
 * @method exists
 * @abstract The returned DMTransaction object’s result method returns an NSNumber, whose Boolean value 
 *	can be retrieved using NSNumber’s boolValue method.  This value will be YES if the DMTopic exists 
 *	and NO otherwise. If an error is encountered that prevents the existence check, the returned 
 *	DMTransaction’s -errorType method can be used to identify the error.
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)exists;


#pragma mark -
#pragma mark Retrieving messages from a topic

/*!
 * @method newMessages
 * @abstract Returns all DMMessages in this DMTopic that have not yet been accessed by the caller.   
 *	(The caller is defined as a combination of the .Mac credentis set for the session that created this 
 *	DMTopic, the machine and local OS X user that the calling application is running under and the 
 *	calling application itself.)  This method will most often be called after a DMMessageSubscriber 
 *	session’s subscription delegate is informed of a change in this DMTopic.  Returns an NSArray of 
 *	DMMessages ordered by publication time, starting with the earliest.
 * @result An NSArray containing all DMMessages in this DMTopic that have not yet been accessed or
 *	seen by the caller.
 */
- (NSArray *)newMessages;

/*!
 * @method messages
 * @abstract Returns all DMMessages found in this DMTopic on .Mac, as an NSArray of DMMessages. 
 *	Messages are ordered by publication time, starting with the earliest.
 * @result An NSArray containing all non-expired DMMessages found in this DMTopic on .Mac.
 */
- (NSArray *)messages;

/*!
 * @method messageWithID:
 * @abstract Retrieves a DMMessage using its globally unique identifier. Returns a DMMessage if the 
 *	provided message identifier is valid, and nil otherwise.
 * @result A DMMessage corresponding to the provided message ID.
 */
- (DMMessage *)messageWithID: (NSString *)messageID;

/*!
 * @method newestMessage
 * @abstract Returns the most recent DMMessage in the topic, based on publication time. 
 * @result The most recent DMMessage in the topic, based on publication time.
 */
- (DMMessage *)newestMessage;

/*!
 * @method oldestMessage
 * @abstract Returns the oldest DMMessage in the topic, based on publication time. 
 * @result The oldest DMMessage in the topic, based on publication time.
 */
- (DMMessage *)oldestMessage;


#pragma mark -
#pragma mark Removing messages from a topic

/*!
 * @method removeMessageWithID:
 * @abstract Removes the identified DMMessage from this DMTopic on .Mac.
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeMessageWithID: (NSString *)identifier;

/*!
 * @method removeMessages
 * @abstract Removes all DMMessages from this DMTopic on .Mac.
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeMessages;


#pragma mark -
#pragma mark Resetting the ttl of a message

/*!
 * @method resetTimeToLive:forMessageWithID:
 * @abstract Resets the time-to-live values of the DMMessage specified by the given identifier.
 *	The provided time-to-live value, an NSTimeInterval with precision in seconds, cannot be greater 
 *	than 30 days.
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)resetTimeToLive: (NSTimeInterval)ttl forMessageWithID: (NSString *)messageID;


#pragma mark -
#pragma mark Auto-arrival of message payloads

/*!
 * @method setIsAutoArrivalEnabledForMessagePayloads:
 * @abstract Sets whether or not payloads are automatic downloaded (enabled by default).
 * @discussion By default, auto-arrival is enabled for all message payloads.  This method can be called 
 *	regardless of whether a subscription to the given topic already exists. This method sets whether 
 *	future messages received for the topic should have their payloads downloaded automatically 
 *	(a message’s meta data—everything but the payload—always arrives automatically). 
 *	[Note: Any auto-arrival default set by this method for a topic is automatically cleared from the 
 *	local cache if an ‘unsubscribe’ call on the given topic is ever made in the future.]  If this method 
 *	is being called around the same time that a subscription to the given topic is being created, it is 
 *	recommended that this method be called first, before the call to DMMessageSubscriber’s 
 *	-subscribeToTopic: method.  This ensures that the desired auto-arrival behavior is in place for the 
 *	given topic before any topic-related callbacks are made into the DMMessageSubscriber’s subscription 
 *	delegate.
 */
- (void)setIsAutoArrivalEnabledForMessagePayloads: (BOOL)isEnabled;

/*!
 * @method isAutoArrivalEnabledForMessagePayloads
 * @abstract Returns a BOOL value indicating whether auto-arrival is currently enabled for the payloads 
 *	of new messages arriving for the given DMTopic object (enabled by default).
 * @result A BOOL value, YES for enabled, no for disabled.
 */
- (BOOL)isAutoArrivalEnabledForMessagePayloads;

@end

#endif
