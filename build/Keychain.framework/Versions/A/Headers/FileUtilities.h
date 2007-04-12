//
//  FileUtilities.h
//  Keychain
//
//  Created by Wade Tregaskis on Sun Jan 25 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Security/Security.h>

#import <Keychain/CSSMModule.h>


/*! @function digestOfPath
    @abstract Calculates the digest of a file.
    @discussion This method is more efficient than reading in all the file's data at once, then using the NSData extensions provided by this framework.  It reads the file in relatively small blocks (presently a quarter of a mibibyte at most, although this is an implementation detail and should not be relied upon - it has changed in past and may change again).  Note that this method may take quite some time for large files.

                You may alternatively wish to use the various NSOutputStream subclasses - e.g. HashOutputStream - instead, as these provide more fine-grained control over the reading process.
    @param path The path to the file to digest.  Should not be nil.
    @param algorithm The digest algorithm to use.
    @param CSPModule The CSP module to use to perform the digest.
    @result Returns the digest of the entire file, or nil if an error occurs. */

NSData* digestOfPath(NSString* path, CSSM_ALGORITHMS algorithm, CSSMModule *CSPModule);
