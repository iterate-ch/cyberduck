#ifndef __DMMESSAGESUBSCRIBER_H__
#define __DMMESSAGESUBSCRIBER_H__

/*
    DMMessageSubscriber.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/


/*! @header DMMessageSubscriber 
 *  @abstract Defines the DMMessageSubscriber session object, which is used to subscribe to specific 
 *	topics and to access new messages in topics of interest.  The .Mac credentials held by the 
 *	DMMessageSubscriber session represent the subscriber.  The subscriber can subscribe to topics 
 *	that it itself owns and to accessible topics owned by other .Mac members.
 *	[Note: Only .Mac members can publish or subscribe to messages.  Currently, secondary users cannot.]
 */

#import <Foundation/Foundation.h>
#import <DotMacKit/DMTransactionGenerator.h>
#import <DotMacKit/DMSecurity.h>
#import <DotMacKit/DMTypesAndConstants.h>

@class DMTopic;
@class DMTransaction;
@class DMMemberAccount;
@class DMMessage;

@interface DMMessageSubscriber : NSObject <DMTransactionGenerator, DMSecurity> {
	DMMemberAccount *_credentials;
	BOOL _use_synchronous;
	id _transaction_delegate;
	id _subscription_delegate;
	id _listener;
	NSLock *_listener_lock;
	id _message_cache;
	NSConditionLock *_topicUpdateLock;
	NSMutableDictionary *_topicsInQueue; 
	NSMutableArray *_topicQueue;
	int _numTopicsInQueue;
	
	NSLock *_activityCheckerLock;
	int _numUpdatesInProgress;
	BOOL _subscriberHalted;
	
	NSLock *_observerLock;
	NSMutableDictionary *_registeredProcessObservers;
	NSMutableDictionary *_registeredSystemObservers;
	NSMutableDictionary *_registeredRemoteObservers;
	
	NSString *_subscriberInstanceStr;
	NSString *_subscriberID;
	NSData *_subscriberIDData;
	
	DMTopic *_invitationsTopic;
	NSLock *_invitationsLock;
	
	BOOL _isMasterSubscriber;
}


#pragma mark -
#pragma mark Factories and Initers

/*!
 * @method messageSubscriberWithCredentials:
 * @abstract Returns a new, autoreleased DMMessageSubscriber session that will use the provided object’s  
 * credentials when subscribing to DMTopics or accessing a DMTopic’s messages.  The provided .Mac 
 * member credentials can be used to subscribe to accessible topics owned by any .Mac member.
 * @result A new, autoreleased DMMessageSubscriber.
 */
+ (id)messageSubscriberWithCredentials: (DMMemberAccount *)credentials;

/*!
 * @method initMessageSubscriberWithCredentials:
 * @abstract Returns newly initialized DMMessageSubscriber session that will use the provided object’s  
 * credentials when subscribing to DMTopics or accessing a DMTopic’s messages.  The provided .Mac 
 * member credentials can be used to subscribe to accessible topics owned by any .Mac member.
 * @result A new, autoreleased DMMessageSubscriber.
 */
- (id)initMessageSubscriberWithCredentials: (DMMemberAccount *)credentials;


#pragma mark -
#pragma mark Convenience API

/*!
 * @method credentials
 * @abstract Returns the .Mac member object containing the credentials set for this 
 *	DMMessageSubscriber session.  These credentials are used to determine which topics can be 
 *	subscribed to using this session.
 * @result A DMMemberAccount object.
 */
- (DMMemberAccount *)credentials;


#pragma mark -
#pragma mark Working with existing topics

/*!
 * @method topicNamed:
 * @abstract Returns an autoreleased DMTopic instance with the provided name.  The returned topic's owner 
 *	will be the .Mac member name specified by this DMMessageSubscriber’s credentials and the topic’s 
 *	application ID will be set to the calling application’s creator code.  This method is used to obtain 
 *	a DMTopic object that represents an existing topic on .Mac.  The credentials set for this 
 *	DMMessageSubscriber instance will be used to access the topic represented by the returned DMTopic object.  
 *	To access messages in a topic, a DMTopic object is required.  If you are trying to obtain a DMTopic 
 *	object that represents a topic not owned by this DMMessageSubscriber's credentials, use either the 
 *	-topicNamed:andOwnedBy: method or the –topicNamed:andOwnedBy:withApplicationID: method, as described below.
 * @result An autoreleased DMTopic instance with the provided name.
 */
- (DMTopic *)topicNamed: (NSString *)name;

/*!
 * @method topicNamed:andOwnedBy:
 * @abstract Returns an autoreleased DMTopic instance with the provided name and owner, where the owner is the
 *	name of the .Mac member who hosts the given topic.  The returned topic's application ID will be set to the 
 *	calling application’s creator code.  This method is used to obtain a DMTopic object that represents an existing 
 *	topic on .Mac.  The credentials set for this DMMessageSubscriber instance will be used to access the topic 
 *	represented by the returned DMTopic object.  To access messages in a topic, a DMTopic object is required.  
 *	If you are trying to obtain a DMTopic object that represents a topic that was created by an application 
 *	different from the calling application, use the -topicNamed:andOwnedBy:withApplicationID: method, as described 
 *	below.
 * @result An autoreleased DMTopic instance with the provided name and owner.
 */
- (DMTopic *)topicNamed: (NSString *)name andOwnedBy: (NSString *)memberName;

/*!
 * @method topicNamed:andOwnedBy:withApplicationID:
 * @abstract Returns an autoreleased DMTopic instance with the provided name, owner and application ID, where the
 *	owner is the name of the .Mac member who hosts the given topic and the application ID is the creator code of 
 *	the application that originally created the topic.  This method is used to obtain a DMTopic object that 
 *	represents an existing topic on .Mac.  The credentials set for this DMMessageSubscriber instance will be used to 
 *	access the topic represented by the returned DMTopic object.  To access messages in a topic, a DMTopic object 
 *	is required.
 * @result An autoreleased DMTopic instance with the provided name, owner and application ID.
 */
- (DMTopic *)topicNamed: (NSString *)name andOwnedBy: (NSString *)memberName withApplicationID: (NSString *)creatorCode;


#pragma mark -
#pragma mark Managing subscriptions to topics

/*!
 * @method subscribeToTopic:
 * @abstract Creates a persistent subscription to the given topic. 
 * @discussion This subscription is specific to this session's .Mac member credentials, the machine and local 
 *	OS X user that the calling application is running under and the calling application itself.  As an argument, 
 *	this method takes a DMTopic to which this session's .Mac member credentials have access.  Calling this method 
 *	creates an entry for the given subscription in the local user preferences, which will cause the subscription 
 *	to be reactivated every time the calling application runs and creates a DMMessageSubscriber object. Therefore, 
 *	-subscribeToTopic: only needs to be called once for a given topic by an application. Calling 
 *	-unsubscribeFromTopic: removes the persistent subscription from the preferences.  
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)subscribeToTopic: (DMTopic *)topic;

/*!
 * @method unsubscribeFromTopic:
 * @abstract Removes an existing subscription to the given topic. Removes a subscription specific to the 
 *	.Mac member for this session, the machine and local OS X user that the calling application is running 
 *	under and the calling application itself.  Calling this method deletes the entry for the given 
 *	subscription from the local user preferences.  As an argument, this method takes a DMTopic object 
 *	to which this session's .Mac member credentials have access.  
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)unsubscribeFromTopic: (DMTopic *)topic;

/*!
 * @method subscribedTopics
 * @abstract Lists the topics to which this session's .Mac member has subscribed (where the subscription was 
 *	originally started on this machine for the current OS X user and the .Mac member credentials set for this 
 *	session).  Each topic listed may be owned and hosted by any .Mac member.
 * @result an NSArray of all DMTopic objects for which subscriptions exist.
 */
- (NSArray *)subscribedTopics;


#pragma mark -
#pragma mark Setting the subscription delegate

/*!
 * @method setSubscriptionDelegate:
 * @abstract Sets the delegate object for this session that will receive callbacks regarding topics of 
 *	interest.  The callbacks relate either to changes to subscribed topics or to new topic invitations sent 
 *	by other .Mac members. The object passed in this method's delegate parameter must conform to the 
 *	informal DMSubscriptionDelegate protocol.  Delegates receive their messages on the same run loop originally 
 *	used in calling this –setSubscriptionDelegate: method.  
 */
- (void)setSubscriptionDelegate: (id)delegate;

/*!
 * @method subscriptionDelegate
 * @abstract Retrieves the delegate object for this session that is set to receive callbacks regarding topics of 
 * interest. The callbacks relate either to changes to subscribed topics or to new topic invitations sent 
 * by other .Mac members.  
 * @result A delegate object conforming to the informal DMSubscriptionDelegate protocol, if set. 
 */
- (id)subscriptionDelegate;


#pragma mark -
#pragma mark Launching apps on topic changes

/*!
 * @method addLaunchableAppForTopics:withBundleIdentifier:arguments:options:
 * @abstract Registers a bundled application that will be launched when any of the specified DMTopics change 
 *	(the topics parameter is an NSArray of DMTopics).  You can have a single application that launches on 
 *	changes of multiple topics.  The specified bundle identifier will be used to resolve the application's 
 *	path for launching—-if the bundle identifier is nil, the calling application’s bundle identifier will be 
 *	used.  You can specify launch arguments in the args parameter that will be passed to the application when 
 *	it is launched.  Possible values for the options parameter can be found in DMTypesAndConstants.h.   
 */
- (void)addLaunchableAppForTopics: (NSArray *)topics withBundleIdentifier: (NSString *)bundleIdentifier
            arguments: (NSString *)args options: (DMAutoLaunchOptions)options;
            
/*!
 * @method addLaunchableToolForTopics:withPath:arguments:options:
 * @abstract This method registers a tool that will be launched when any of the specified DMTopics change 
 *	(the topics parameter is an NSArray of DMTopics).  You can have a single tool that launches on changes 
 *	of multiple topics.  The path parameter is a fully-qualified path to the tool.  You can specify launch 
 *	arguments in the args parameter that will be passed in unchanged to the tool when it is launched.  
 *	Possible values for the options parameter can be found in DMTypesAndConstants.h. 
 */
- (void)addLaunchableToolForTopics: (NSArray *)topics withPath: (NSString *)fullPath
            arguments: (NSString *)args options: (DMAutoLaunchOptions)options;
            
/*!
 * @method removeLaunchableAppForTopics:withBundleIdentifier:
 * @abstract Removes the identified launchable application for the specified topic(s).
 *	The topics argument is an NSArray of DMTopic objects for which the given app should no longer be launched.
 *	The bundle identifier argument is the identifier of the application that was previously added for the given 
 *	topic(s)-—if the bundle identifier is nil, the calling application’s bundle identifier will be used.
 */
- (void)removeLaunchableAppForTopics: (NSArray *)topics withBundleIdentifier: (NSString *)bundleIdentifier;
            
/*!
 * @method removeLaunchableToolForTopics:withPath:
 * @abstract Removes the launchable tool for the specified topic(s). The topics argument is an NSArray of 
 *	DMTopic objects for which the given tool should no longer be launched.  The path argument is a 
 *	fully-qualified path to the tool that was previously added for the given topics. 
 * @result no return value
 */
- (void)removeLaunchableToolForTopics: (NSArray *)topics withPath: (NSString *)fullPath;

/*!
 * @method removeLaunchablesForTopics:
 * @abstract Removes all launchable applications and tools for the specified topic(s). Takes an NSArray of 
 * DMTopic objects for which all applications and tools should no longer be launched. 
 */
- (void)removeLaunchablesForTopics: (NSArray *)topics;


#pragma mark -
#pragma mark Handling topic invitations

/*!
 * @method invitationsForTopics:
 * @abstract Lists all unsubscribed DMTopics owned by other .Mac members to which the .Mac member for this 
 *	session has been invited to subscribe.  Returns an NSArray of DMTopic objects.
 *	To be notified upon receipt of new invitations immediately, register a subscription delegate that 
 *	implements the -invitationsReceivedForTopics: method.  An invitation to a topic can be accepted by 
 *	subscribing to the given topic.
 *	[Note: An invitation for a topic is automatically sent to a .Mac member when that member is granted access 
 *	to the topic using the DMSecurity protocol methods implemented by the DMMessagePublisher session.]
 * @result An NSArray of DMTopic objects
 */
- (NSArray *)invitationsForTopics;

@end

#endif