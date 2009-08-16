#ifndef __DMMESSAGE_H__
#define __DMMESSAGE_H__

/*
    DMMessage.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.
    
    Public header file.
*/

#import <Foundation/Foundation.h>
#import <DotMacKit/DMTransactionGenerator.h>

/*! @header DMMessage 
 *  @abstract Defines the DMMessage class.  Once published to a DMTopic, a DMMessage object becomes 
 * immutable.  Use DMMessage’s -isPublished method to determine whether a message has been 
 * published to .Mac.
 */

@class DMTransaction;
@class DMTopic;
 
@interface DMMessage : NSObject <NSMutableCopying, DMTransactionGenerator> {
	NSString *_msg_string;	
	NSDictionary *_properties;
	id _payload;
	NSString *_payload_type;
	NSString *_msg_id;
	DMTopic *_topic; 
	NSString *_publisher;
	NSDate *_publication_date;
	NSDate *_expiration_date;
	NSTimeInterval _ttl;
	unsigned _payload_size;
	BOOL _use_synchronous;
	id _transaction_delegate;
	BOOL _is_published;
	BOOL _isTTLAlreadySet;
	id _enc_properties;
	id _enc_payload;
}


#pragma mark -
#pragma mark Factories and Initers

/*!
 * @method message:
 * @abstract Returns an autoreleased DMMessage instance containing the provided message string. This 
 *	is a convenience method for creating a DMMessage containing just the provided message string and
 *	no payload object or properties dictionary.  The message string cannot exceed 1K in size.
 * @result An autoreleased DMMessage instance containing the provided message string.
 */
+ (id)message: (NSString *)messageString;

/*!
 * @method message:withProperties:andPayload:ofType:
 * @abstract Returns an autoreleased DMMessage instance containing a payload object, payload type, 
 *	and an optional message string and properties dictionary (actually, all parameters are optional, 
 *	but at least one of them must be non-nil).  The message string cannot exceed 1K in size and the 
 *	optional dictionary cannot exceed 100K in size.  [Note: The dictionary can contain only NSString 
 *	keys and values and the 100K limit includes both keys and values.]  The payload object must 
 *	conform to the NSCoding protocol—its size is limited to 5MB and is further limited by the space 
 *	available to the .Mac member who owns and hosts the parent topic.  The payload type is an NSString 
 *	that represents the file extension that would be used if the payload object were serialized to a 
 *	file (‘txt’, ‘mov’, ‘plist’, ‘html’, or ‘jpg’ for instance).
 * @result An autoreleased DMMessage instance.
 */
+ (id)message: (NSString *)messageString
            withProperties: (NSDictionary *)properties
            andPayload: (id)object
            ofType: (NSString *)payloadType;
                            
/*!
 * @method initMessage:
 * @abstract Returns a newly initialized DMMessage instance containing the provided message string. 
 *	This is a convenience method for creating a DMMessage containing just the provided message string 
 *	and no payload object or properties dictionary.  The message string cannot exceed 1K in size.
 * @result A newly initialized DMMessage instance containing the provided message string.
 */
- (id)initMessage: (NSString *)messageString;

/*!
 * @method initMessage:withProperties:andPayload:ofType:
 * @abstract Returns a newly initialized DMMessage instance containing a payload object, payload type, 
 *	and an optional message string and properties dictionary (actually, all parameters are optional, but 
 *	at least one of them must be non-nil).  The message string cannot exceed 1K in size and the optional 
 *	dictionary cannot exceed 100K in size.  [Note: The dictionary can contain only NSString keys and 
 *	values and the 100K limit includes both keys and values.]  The payload object must conform to the 
 *	NSCoding protocol—its size is limited to 5MB and is further limited by the space available to the 
 *	.Mac member who owns the parent topic.  The payload type is an NSString that represents the file 
 *	extension that would be used if the payload object were serialized to a file (‘txt’, ‘mov’, ‘plist’, 
 *	‘html’, or ‘jpg’ for instance).
 * @result A newly initialized DMMessage instance.
 */
- (id)initMessage: (NSString *)messageString
            withProperties: (NSDictionary *)properties
            andPayload: (id)object
            ofType: (NSString *)payloadType;


#pragma mark -
#pragma mark Convenience API
            
/*!
 * @method messageID
 * @abstract Returns this DMMessage's globally unique identifier string. 
 * @result An NSString message identifier. 
 */
- (NSString *)messageID;

/*!
 * @method publisher
 * @abstract Returns the name of the .Mac member who published this DMMessage to .Mac. 
 *	Returns nil if this DMMessage has not yet been published. 
 * @result An NSString member name. 
 */
- (NSString *)publisher;

/*!
 * @method topic
 * @abstract Returns the topic that contains this DMMessage. Returns nil if this DMMessage has not 
 *	yet been published to a topic. 
 * @result The DMTopic that contains this DMMessage. 
 */
- (DMTopic *)topic;

/*!
 * @method messageString
 * @abstract Returns the optional message string set for the DMMessage. 
 * @result An NSString.
 */
- (NSString *)messageString;

/*!
 * @method setMessageString:
 * @abstract Sets the optional message string for the DMMessage. The message string cannot exceed 1K in 
 *	size. An integer status constant is returned. [Note: Calling this method on an already-published 
 *	message will result in kDMResourceExists being returned; otherwise, the method returns kDMSuccess. 
 *	The -isPublished method can be used to determine whether this DMMessage has already been published.] 
 * @result a status constant defined in DMTypesAndConstants under Publish & Subscribe error types 
 */
- (int)setMessageString: (NSString *)messageString;

/*!
 * @method payload
 * @abstract Retrieves the optional payload object set for the DMMessage.  Upon success, the returned 
 *	DMTransaction object’s -result method will return this message’s payload object, an object that 
 *	conforms to the NSCoding protocol.  If auto-arrival of payloads is enabled for this message’s parent 
 *	topic, then the payload object is already available locally by the time this new message is received 
 *	and the caller can call the returned DMTransaction’s -result method right away.  However, if 
 *	auto-arrival is disabled, this DMMessage’s payload is not downloaded until this -payload method is 
 *	called.  If this DMMessage’s transaction processing is set to asynchronous, then the status of the 
 *	download can be observed by setting this DMMessage’s transaction delegate or through polling the 
 *	returned DMTransaction object.  [Note: If this DMMessage’s transaction delegate is not set, then it 
 *	will inherit any transaction delegate that is explicitly set for its parent DMTopic or that has been 
 *	inherited by its parent DMTopic from the session object used to create it.]  If auto-arrival is 
 *	disabled and this DMMessage’s transaction processing is set to synchronous, then this method will not 
 *	return until the payload is completely downloaded.  In all cases, the payload is retrieved with a call 
 *	to the returned DMTransaction object’s -result method. 
 * @result A DMTransaction object whose -isSuccessful method can be used to test for success.
 */
- (DMTransaction *)payload;

/*!
 * @method payloadType
 * @abstract Returns the type corresponding to the payload for this DMMessage.  The payload type is an 
 *	NSString that represents the file extension that would be used if the payload object were serialized 
 *	to a file (‘txt’, ‘mov’, ‘plist’, ‘html’, or ‘jpg’ for instance).  Returns a type string, or nil if no 
 *	payload object is set for this DMMessage. 
 * @result An NSString payload type.
 */
- (NSString *)payloadType;

/*!
 * @method setPayload:ofType:
 * @abstract Sets the payload object and its type.  Both the payload and the type argument must be non-nil
 *	if this method is called.  The payload is an object that conforms to the NSCoding protocol. 
 *	Its size is limited to 5MB and is further limited by the space available to the .Mac member who owns the  
 *	parent topic.  The payload type is an NSString that represents the file extension that would be used 
 *	if the payload object were serialized to a file (‘txt’, ‘mov’, ‘plist’, ‘html’, or ‘jpg’ for instance). 
 *	[Note: Calling this method on an already-published message will result in kDMResourceExists being 
 *	returned; otherwise, this method returns kDMSuccess.  The -isPublished method can be used to determine 
 *	whether this DMMessage has already been published.]
 * @result A status constant. 
 */
- (int)setPayload: (id)object ofType: (NSString *)payloadType;

/*!
 * @method properties
 * @abstract Returns the optional user-defined properties dictionary set for this DMMessage.
 * @result An NSDictionary of NSString key/value pairs.
 */
- (NSDictionary *)properties;

/*!
 * @method setProperties:
 * @abstract Sets the optional properties dictionary for this DMMessage.  This dictionary cannot exceed 100K 
 *	in size.  The dictionary can contain NSString keys and values only and the 100K limit includes both keys 
 *	and values.  [Note: Calling this method on an already-published message will result in kDMResourceExists 
 *	being returned; otherwise, this method returns kDMSuccess.  The -isPublished method can be used to determine 
 *	whether this DMMessage has already been published.] 
 * @result A status constant. 
 */
- (int)setProperties: (NSDictionary *)properties;

/*!
 * @method publicationDate
 * @abstract Returns the time when this DMMessage was published to .Mac, which is an NSDate object with 
 * precision in seconds.  If this DMMessage has not yet been published, this method returns nil.
 * @result An NSDate.
 */
- (NSDate *)publicationDate;

/*!
 * @method expirationDate
 * @abstract If this DMMessage has already been published, this method returns an NSDate that marks the 
 *	time this DMMessage expires (with precision in seconds).  Otherwise, this method returns nil.  If 
 *	the expiration time returned for this DMMessage is in the past, it means that this message is awaiting 
 *	clean-up.
 * @result An NSDate.
 */
- (NSDate *)expirationDate;
                                                                                                                                                       
/*!
 * @method timeToLive
 * @abstract Returns an NSTimeInterval (with precision in seconds) representing the default time-to-live of 
 *	7 days if the default was not overridden by a DMMessagePublisher setting or by a call to this DMMessage’s 
 *	-setTimeToLive: method. 
 * @result An NSTimeInterval with precision in seconds.
 */
- (NSTimeInterval)timeToLive;

/*!
 * @method setTimeToLive:
 * @abstract This method can be used to set the time-to-live for this DMMessage prior to publication.  The 
 *	default time-to-live is 7 days, and the time-to-live cannot be made longer than 30 days.  Takes an 
 *	NSTimeInterval (precision in seconds).  [Note: Calling this method on an already-published message will 
 *	result in kDMResourceExists being returned.  If the provided ttl value not in the valid range, 
 *	kDMInvalidParameter will be returned; otherwise, this method returns kDMSuccess.  The -isPublished method 
 *	can be used to determine whether this DMMessage has already been published.]
 * @result A status constant.
 */
- (int)setTimeToLive: (NSTimeInterval)ttl;

/*!
 * @method isPublished
 * @abstract Tells whether the DMMessage has already been published to .Mac. 
 * A DMMessage can be published to .Mac only once and it becomes immutable once published. 
 * An attempt to publish a DMMessage more than once will fail.
 * @result BOOL, YES if published, NO if not.
 */
- (BOOL)isPublished;

@end

#endif
