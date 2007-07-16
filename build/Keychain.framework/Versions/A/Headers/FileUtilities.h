//
//  FileUtilities.h
//  Keychain
//
//  Created by Wade Tregaskis on Sun Jan 25 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Security/Security.h>


NSData* digestOfPath(NSString* path, CSSM_ALGORITHMS algorithm);
