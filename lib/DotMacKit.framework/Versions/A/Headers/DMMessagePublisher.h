#ifndef __DMMESSAGEPUBLISHER_H__
#define __DMMESSAGEPUBLISHER_H__

/*
    DMMessagePublisher.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMMessagePublisher 
 *  @abstract Defines the DMMessagePublisher session object (a subclass of DMSession), which is used to  
 *	publish DMMessages to DMTopics.  The .Mac member credential held by the DMMessagePublisher  
 *	session represents the member who will be publishing messages to topics.  The publishing member  
 *	can publish messages to topics that it itself owns or to topics owned by other .Mac  
 *	members.  The DMMessagePublisher session conforms to the DMSecurity protocol and  
 *	DMTopics are considered securable ‘entities’ that can be passed to DMSecurity methods for  
 *	this session. [Note: Only .Mac members can publish or subscribe to messages.  Currently, secondary 
 *	users cannot.] 
 */

#import <Foundation/Foundation.h>
#import <DotMacKit/DMTransactionGenerator.h>
#import <DotMacKit/DMSecurity.h>

@class DMTopic;
@class DMTransaction;
@class DMMemberAccount;
@class DMMessage;

@interface DMMessagePublisher : NSObject <DMTransactionGenerator, DMSecurity> {
	DMMemberAccount *_credentials;
	BOOL _use_synchronous;
	id _transaction_delegate;
	id _message_cache;
	NSTimeInterval _defaultTTL;
}


#pragma mark -
#pragma mark Factories and Initers

/*!
 * @method messagePublisherWithCredentials:
 * @abstract Returns a new, autoreleased DMMessagePublisher session that will use the provided object’s 
 * credentials when publishing DMMessages or otherwise accessing or modifying DMTopics.  The publishing
 * member can publish messages to topics that it itself owns or to topics owned by other .Mac members
 * @result A new, autoreleased DMMessagePublisher session.
 */
+ (id)messagePublisherWithCredentials: (DMMemberAccount *)credentials;

/*!
 * @method initMessagePublisherWithCredentials:
 * @abstract Returns a newly initialized DMMessagePublisher session that will use the provided object’s 
 * credentials when publishing DMMessages or otherwise accessing or modifying DMTopics. The publishing 
 *	member can publish messages to topics that it itself owns or to topics owned by other .Mac members. 
 * @result A newly initialized DMMessagePublisher session.
 */
- (id)initMessagePublisherWithCredentials: (DMMemberAccount *)credentials;


#pragma mark -
#pragma mark Convenience API

/*!
 * @method credentials
 * @abstract Returns the .Mac member object whose  set for this DMMessagePublisher session. These 
 *	credentials are used to determine which topics can have messages published to them or be otherwise 
 *	modified using this session.
 * @result A DMMemberAccount object.
 */
- (DMMemberAccount *)credentials;


#pragma mark -
#pragma mark Creating and Removing topics

/*!
 * @method createTopicNamed:
 * @abstract Adds a new, empty DMTopic to .Mac.  The new topic will be owned by the .Mac member specified 
 *	by this DMMessagePublisher’s credentials.  
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)createTopicNamed: (NSString *)topicName;

/*!
 * @method removeTopic:
 * @abstract Removes the named DMTopic from .Mac, including all of the messages it contains.  A topic can 
 *	be removed only by its owner, using the same application that originally created it.  Therefore, the 
 *	.Mac member name specified by this DMMessagePublisher’s credentials must be the owner of the 
 *	topic and the calling application must be the same application used to originally create the topic.  
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)removeTopicNamed: (NSString *)topicName;


#pragma mark -
#pragma mark Working with existing topics

/*!
 * @method topicNamed:
 * @abstract Returns an autoreleased DMTopic instance with the provided name.  The returned topic's owner 
 *	will be the .Mac member name specified by this DMMessagePublisher’s credentials and the topic’s 
 *	application ID will be set to the calling application’s creator code.  This method is used to obtain 
 *	a DMTopic object that represents an existing topic on .Mac.  The credentials set for this 
 *	DMMessagePublisher instance will be used to access or modify the topic represented by the returned DMTopic 
 *	object.  To publish messages to a topic or to list the messages in a topic, a DMTopic object is required.  
 *	If you are trying to obtain a DMTopic object that represents a topic not owned by this DMMessagePublisher's 
 *	credentials, use either the -topicNamed:andOwnedBy: method or the –topicNamed:andOwnedBy:withApplicationID: 
 *	method, as described below.
 * @result An autoreleased DMTopic instance with the provided name.
 */
- (DMTopic *)topicNamed: (NSString *)name;

/*!
 * @method topicNamed:andOwnedBy:
 * @abstract Returns an autoreleased DMTopic instance with the provided name and owner, where the owner is the 
 *	name of the .Mac member who hosts the given topic.  The returned topic's application ID will be set to the 
 *	calling application’s creator code.  This method is used to obtain a DMTopic object that represents an 
 *	existing topic on .Mac.  The credentials set for this DMMessagePublisher instance will be used to access or 
 *	modify the topic represented by the returned DMTopic object.  To publish messages to a topic or to list the 
 *	messages in a topic, a DMTopic object is required.  If you are trying to obtain a DMTopic object that 
 *	represents a topic that was created by an application different from the calling application, use the 
 *	-topicNamed:andOwnedBy:withApplicationID: method, as described below.
 * @result An autoreleased DMTopic instance with the provided name and owner.
 */
- (DMTopic *)topicNamed: (NSString *)name andOwnedBy: (NSString *)memberName;

/*!
 * @method topicNamed:andOwnedBy:withApplicationID:
 * @abstract Returns an autoreleased DMTopic instance with the provided name, owner and application ID, where 
 *	the owner is the name of the .Mac member who hosts the given topic and the application ID is the creator code 
 *	of the application that originally created the topic.  This method is used to obtain a DMTopic object 
 *	that represents an existing topic on .Mac.  The credentials set for this DMMessagePublisher instance will be 
 *	used to access or modify the topic represented by the returned DMTopic object.  To publish messages to a topic 
 *	or to list the messages in a topic, a DMTopic object is required.
 * @result An autoreleased DMTopic instance with the provided name, owner and application ID.
 */
- (DMTopic *)topicNamed: (NSString *)name andOwnedBy: (NSString *)memberName withApplicationID: (NSString *)creatorCode;

/*!
 * @method topics:
 * @abstract Lists the topics that this DMMessagePublisher can publish messages to or remove messages 
 *	from.  Each topic listed may be owned by this session's .Mac member credentials or by another 
 *	.Mac member.  Returns a DMTransaction object whose -isSuccessful method can be used to test 
 *	for success and whose -result method, upon success, returns an NSArray of all accessible 
 *	DMTopic objects.  
 * @result An NSArray of all accessible DMTopic objects.
 */
- (DMTransaction *)topics;


#pragma mark -
#pragma mark Publishing a message to a topic

/*!
 * @method publishMessage:toTopic:
 * @abstract Publishes a new DMMessage to a DMTopic that already exists on .Mac (an existing topic will have 
 *	been previously created on .Mac by the topic’s owner using DMMessagePublisher’s -createTopicNamed: method).
 *	The default time-to-live for a newly published DMMessage is 7 days.  This default can be overridden using 
 *	the session’s -setDefaultTimeToLive: method.  Time-to-live can also be set on a message-by-message basis 
 *	using DMMessage’s -setTimeToLive: method.  [Note: If .Mac is too busy to handle the message publication 
 *	request, kDMServiceBusy will be returned by the transaction’s -errorType method, and publication should be 
 *	retried later.  If the given DMMessage has already been published to .Mac, kDMResourceExists will be 
 *	returned by the transaction’s -errorType method.]
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)publishMessage: (DMMessage *)message toTopic: (DMTopic *)topic;


#pragma mark -
#pragma mark Time-to-live defaults

/*!
 * @method defaultTimeToLive:
 * @abstract Returns the default time-to-live that will be used by all DMMessages published by this session.  
 * The default time-to-live value is 7 days.  Returns an NSTimeInterval (precision in seconds).  
 * @result An NSTimeInterval (precision in seconds).
 */
- (NSTimeInterval)defaultTimeToLive;

/*!
 * @method setDefaultTimeToLive:
 * @abstract Sets the default time-to-live that will be used by all DMMessages published by this session.
 *	The default time-to-live value cannot exceed 30 days. Takes an NSTimeInterval (precision in seconds).
 *	If the provided ttl value not in the valid range, kDMInvalidParameter will be returned; otherwise, this 
 *	method returns kDMSuccess.
 * @result An integer status code
 */
- (int)setDefaultTimeToLive: (NSTimeInterval)defaultTTL;

@end

#endif