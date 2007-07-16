//
//  UtilitySupport.h
//  Keychain
//
//  Created by Wade Tregaskis on Fri Jan 24 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>

#import <errno.h>


/*! @function allocCSSMData
    @abstract Allocates a new CSSM_DATA structure.
    @discussion This convenience function simply allocates and initialises (to an empty state) a CSSM_DATA structure.  You could probably achieve the same effect with a simple call to calloc, but this way is more future compatible by virtue of it's abstraction.  Consequently it should play a key role in maximizing shareholder value for the succeeding quarter looking forward.

                The returned CSSM_DATA is guaranteed to be in the same state as would be returned by a call to clearCSSMData.
    @result Returns a new empty CSSM_DATA if successful, NULL otherwise (which most likely indicates a memory allocation error). */

CSSM_DATA* allocCSSMData(void);

/*! @function clearCSSMData
    @abstract Clears a CSSM_DATA structure back to it's default, empty state.
    @discussion This function releases the memory allocated to the data within the CSSM_DATA, and sets all other appropriate fields to 0 or similar.  It does not free the memory used for the CSSM_DATA structure itself.

                The CSSM_DATA after calling is guaranteed to be in the same state as it would have been when first created using allocCSSMData.
    @param data The CSSM_DATA to return to the default, empty state.  Should not be NULL. */

void clearCSSMData(CSSM_DATA *data);

/*! @function freeCSSMData
    @abstract Frees all memory associated with a CSSM_DATA structure, including that of the structure itself.
    @discussion This function is similar to the clearCSSMData function, except it goes the extra step of freeing the CSSM_DATA structure itself, in addition to it's contents.

                The passed parameter will always be invalid after a call to this function.  Note that you should always consider it invalid from the *start* of the call, if your application is multithreaded; it will most certainly pass through at least one invalid state during the function, which could create all kinds of havoc if another thread tries to use it.
    @param data The CSSM_DATA to free.  It does not have to be already cleared using clearCSSMData.  It is always invalid from the moment this function is called.  Should not be NULL. */

void freeCSSMData(CSSM_DATA *data);

/*! @function copyDataToData
    @abstract Copies a CSSM_DATA structure to another CSSM_DATA structure.
    @discussion The contents of 'source' are copied to 'destination'.  The 'Data' field of destination may be free'd and re-malloc'd if necessary (or, it may be reused).  In any case, don't rely on particular behaviour; it is undefined and indeed may vary both between versions and between parameter sets.
    @param source The source CSSM_DATA to be copied.  Should not be NULL.  If it is NULL, destination will be unmodified.
    @param destination The destination CSSM_DATA in which to copy the contents of 'source'.  Should not be NULL.
    @result Returns 0 if successful, an error code (from <errno.h>) otherwise. */

int copyDataToData(const CSSM_DATA *source, CSSM_DATA *destination);

int copyNSStringToData(NSString *source, CSSM_DATA *destination);

CSSM_DATA* dataFromNSString(NSString *string);

void copyNSDataToData(NSData *source, CSSM_DATA *destination);

// Be very careful using the following function - lots of stuff goes on inside the Keychain & Security frameworks, and the CDSA itself, even for simple requests.  If you get malloc errors or BAD_ACCESS faults, you might want to check over any code which uses this method

// P.S. Yes I know the function name contradicts itself.  I'm lazy and it's consistent.

void copyNSDataToDataNoCopy(NSData *source, CSSM_DATA *destination);

CSSM_DATA* dataFromNSData(NSData *data);

NSString* NSStringFromData(const CSSM_DATA *data);

NSString* NSStringFromNSData(NSData *data);

NSData* NSDataFromNSString(NSString *string);

NSData* NSDataFromData(const CSSM_DATA *data);

// Be very careful using the following function - lots of stuff goes on inside the Keychain & Security frameworks, and the CDSA itself, even for simple requests.  If you get malloc errors or BAD_ACCESS faults, you might want to check over any code which uses this method

NSData* NSDataFromDataNoCopy(const CSSM_DATA *data, BOOL freeWhenDone);

BOOL OIDsAreEqual(const CSSM_OID *a, const CSSM_OID *b);

/*! @function NSDataFromHumanNSString
    @abstract Converts a human-readable representation of some raw data (i.e. hex form) to the raw data form.
    @discussion This is the opposite operation to NSData's description method, and is entirely compatible and complimentary.  It ignores all newlines, carriage returns, spaces, tabs, and angle-brackets ('<' and '>').  It is, of course, not case sensitive.
    @param string The string containing the human readable hex form, e.g. "<5d2f 5aa3>" or "0x836D" etc.
    @result nil if the string is not in a valid format, the resulting NSData otherwise. */

NSData* NSDataFromHumanNSString(NSString *string);
