#ifndef __DMSUBSCRIPTIONDELEGATE_H__
#define __DMSUBSCRIPTIONDELEGATE_H__

/*
 *  DMSubscriptionDelegate.h
 *  Copyright (C) 2005 Apple Computer, Inc. All rights reserved.
 *
 */

/*! @header DMSubscriptionDelegate
 *  @abstract Defines the informal DMSubscriptionDelegate protocol.
 */

/*!
 * @category NSObject(DMSubscriptionDelegate)
 * @abstract This category on NSObject defines an informal delegate protocol.
 */
@interface NSObject (DMSubscriptionDelegate)

/*!
 * @method topicsHaveChanged:
 * @abstract This method is called when new messages are published to topics of interest—-that is, 
 *	topics to which the active DMMessageSubscriber session’s .Mac member is subscribed.  The method takes 
 *	an NSArray of DMTopic objects.  [Note: After receiving this delegate message, clients can call a DMTopic’s   
 *	-newMessages method to retrieve all new, unseen messages.
 */
- (void)topicsHaveChanged: (NSArray *)topics;

/*!
 * @method invitationsReceivedForTopics:
 * @abstract This method is called when new invitations to subscribe to another .Mac member’s topics are 
 *	received.  Only new invitations that have not yet been seen by the current combination of .Mac subscriber, 
 *	running application and local OS X user account are returned.  [Note: An invitation to subscribe to a topic 
 *	is automatically sent to the invited .Mac member when that member is granted access to the topic by the 
 *	topic’s owner using the DMSecurity protocol methods implemented by the DMMessagePublisher class.]
 */
- (void)invitationsReceivedForTopics: (NSArray *)topics;

@end

#endif