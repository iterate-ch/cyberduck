//
//  MDS.h
//  Keychain
//
//  Created by Wade Tregaskis on 3/8/2005.
//
//  Copyright (c) 2006, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/mds.h>
#import <Security/mds_schema.h>


@interface MDS : NSObject {
    // MDS_Initialize
    CSSM_GUID myGUID;
    //CSSM_DATA myManifest; // Manifests (to MDS_Initialize) not supported by Apple's version of MDS
    CSSM_MEMORY_FUNCS myMemoryFunctions;
    MDS_FUNCS mdsFunctions;
    MDS_HANDLE myHandle;
    
    const void *myOpenParameters;
    const CSSM_ACCESS_CREDENTIALS *myCredentials;
    
    CSSM_RETURN lastError;
}

/*! @method defaultMDS
    @abstract Returns the default MDS singleton instance.
    @discussion The default MDS instance is created when this method is first invoked, and is initialised using the "init" method - see the description for init for details on what this means in terms of settings, et al.

                Usually you'll be happy enough to use the default MDS instance (i.e. this method).  Note that none of the initialisation methods check against existing instances, so each will always return a new instance.
    @result Returns the default MDS instance, or nil if an error occurs. */

+ (MDS*)defaultMDS;

/*! @method initWithGUID:memoryFunctions:
    @abstract Initialises the receiver to connect to the MDS module using the given GUID (representing the caller) and the given memory allocation functions.
    @discussion Pretty simple, really.  The generic "init" method calls this method with suitable generic arguments.

                This is the designated initialiser for the 
    @param GUID The GUID representing the caller.  Should not be NULL.
    @param memoryFunctions Memory management functions to be used by the MDS.  May be NULL, in which case the defaults (as obtained from CSSMModule's defaultMemoryFunctions class method) are used.
    @result Returns the receiver initialised with the given parameters, or nil if an error occurs (in which case the receiver is automatically released). */

- (MDS*)initWithGUID:(const CSSM_GUID*)GUID memoryFunctions:(const CSSM_MEMORY_FUNCS*)memoryFunctions;

/*! @method init
    @abstract Generic initialiser.
    @discussion Calls initWithGUID:memoryFunctions: using generic values, as gathered from keychainFrameworkDefaultGUID() and [CSSMModule defaultMemoryFunctions], respectively.
    @result As for initWithGUID:memoryFunctions:. */

- (MDS*)init;

- (NSArray*)query:(MDS_DB_HANDLE)handle attributes:(NSArray*)attributes forAllRecordsOfType:(CSSM_DB_RECORDTYPE)recordType withTimeLimit:(unsigned int)seconds andSizeLimit:(unsigned int)records;

- (CSSM_RETURN)lastError;

@end
