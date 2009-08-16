#ifndef __DMCREDENTIAL_H__
#define __DMCREDENTIAL_H__

/*
    DMCredential.h
    Copyright (C) 2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

#import <Foundation/Foundation.h>

@protocol DMCredential

/*!
 * @method name
 * @result A username string is returned. 
 */
- (NSString *)name;

/*!
 * @method password
 * @result A password string is returned.  
 */
- (NSString *)password;

/*!
 * @method owner
 * @result The name of the .Mac member who owns the credential is returned. 
 */
- (NSString *)owner;

@end

#endif
