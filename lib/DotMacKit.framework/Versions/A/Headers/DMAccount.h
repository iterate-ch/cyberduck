#ifndef __DMACCOUNT_H__
#define __DMACCOUNT_H__

/*
    DMAccount.h
    Copyright (C) 2004-2005 Apple Computer, Inc. All rights reserved.

    Public header file.
*/

/*! @header DMAccount 
 *  @abstract This header defines the base class used by DMMemberAccount objects, but
 *	is not meant for direct usage.
 */
 
#import <Foundation/Foundation.h>

@interface DMAccount : NSObject {
    id _account_info;
    NSString *_app_id;
    NSString *_app_name;
}

@end

#endif
