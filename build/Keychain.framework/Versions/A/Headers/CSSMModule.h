//
//  CSSMModule.h
//  Keychain
//
//  Created by Wade Tregaskis on 31/7/2005.
//
//  Copyright (c) 2006, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>

#import <Keychain/NSCachedObject.h>


/*! @class CSSMModule
    @abstract Represents a particular CSSM module, such as a cryptographic module or a secure data storage system.
    @discussion This nifty little class puts a nice Cocoa interface over module management.  As the base wrapper over CSSM modules it does not actually provide an interface for loading & attaching modules - for that functionality refer to the CSSMManagedModule subclass.  Also, unlike CSSMManagedModule this class will not automatically detach or unload the wrapped module when it is deallocated, since it is not the owner of the given active module reference, just a user of it.

                <b>Very important:</b> CSSM modules wrapped by this class are <i>not</i> controlled by this class - they must be constructed before hand and pulled down afterwards as appropriate.  If you wish to manually manage the module (calling any of the relevant CSSM module functions directly), you can no longer rely on normal behaviours of this class.  For example, if you manually detach a module, this class will still think it is attached.  You will have to call the "detach" method to get this class back up to date (ignoring the return result, which will probably be NO on account of the module not really being attached to start with).  And as for unloads, etc, too.

                Note that you can have only one CSSMModule instance per unique handle - initialisers that try to initialise a new instance with a handle already wrapped by an another instance will release themselves and return the existing instance. */

@interface CSSMModule : NSCachedObject {
    // CSSM_ModuleLoad
    CSSM_GUID _GUID; // From CSSM_GetModuleGUIDFromHandle (and CSSM_GetSubserviceUIDFromHandle?)
    
    // CSSM_ModuleAttach
    CSSM_VERSION _version; // From CSSM_GetSubserviceUIDFromHandle
    uint32_t _subserviceID; // From CSSM_GetSubserviceUIDFromHandle
    CSSM_SERVICE_TYPE _subserviceType; // From CSSM_GetSubserviceUIDFromHandle
    CSSM_API_MEMORY_FUNCS _memoryFunctions; // From CSSM_GetAPIMemoryFunctions

    // The Goodness
    BOOL _loaded, _attached;
    CSSM_MODULE_HANDLE _handle;
    CSSM_RETURN _error;
}

#pragma mark Initialisers

/*! @method defaultCSPModule
    @abstract Returns the default CSP module instance (a singleton).
    @discussion Many Keychain framework classes and functions - particularly older ones not yet updated with this new CSSMModule functionality - assume a global default module for CSP operations.  To enable backward compatibility - as well as a simpler code path for those not concerned with which module they're using - this method exists, to return a default singleton instance for general CSP operations.

                Note that the returned instance is not a copy, and modifications made to it (e.g. detaching and/or unloading) will effect anything else using it, both concurrently and in future.  Generally it is a bad idea to modify the returned CSSMModule - if you wish, create a new one with your modifications and use setDefaultCSPModule: to apply it.  This avoids any race conditions about your changes.

                Note that the result may be nil, whether because of an error or because it was set this way by a call to setDefaultCSPModule:.  In any case, treat a nil result as indicating CSP functionality is not available, and fail appropriately (gracefully, if possible).
    @result Returns the default module for CSP operations, which may be nil (due either to an error or it being explicitly set to nil by a call to setDefaultCSPModule:). */

+ (CSSMModule*)defaultCSPModule;

    /*! @method setDefaultCSPModule:
    @abstract Sets the default CSP module instance.
    @discussion See the documentation for defaultCSPModule, the corresponding getter, for more information.
    @param newDefault The new default module.  Note that this is retained, not copied, so changes made to it are carried through.  Also note that it may be nil. */

+ (void)setDefaultCSPModule:(CSSMModule*)newDefault;

/*! @method defaultTPModule
    @abstract Returns the default TP module instance (a singleton).
    @discussion Many Keychain framework classes and functions - particularly older ones not yet updated with this new CSSMModule functionality - assume a global default module for TP operations.  To enable backward compatibility - as well as a simpler code path for those not concerned with which module they're using - this method exists, to return a default singleton instance for general TP operations.

                Note that the returned instance is not a copy, and modifications made to it (e.g. detaching and/or unloading) will effect anything else using it, both concurrently and in future.  Generally it is a bad idea to modify the returned CSSMModule - if you wish, create a new one with your modifications and use setDefaultTPModule: to apply it.  This avoids any race conditions about your changes.

                Note that the result may be nil, whether because of an error or because it was set this way by a call to setDefaultTPModule:.  In any case, treat a nil result as indicating TP functionality is not available, and fail appropriately (gracefully, if possible).
    @result Returns the default module for TP operations, which may be nil (due either to an error or it being explicitly set to nil by a call to setDefaultTPModule:). */

+ (CSSMModule*)defaultTPModule;

    /*! @method setDefaultTPModule:
    @abstract Sets the default TP module instance.
    @discussion See the documentation for defaultTPModule, the corresponding getter, for more information.
    @param newDefault The new default module.  Note that this is retained, not copied, so changes made to it are carried through.  Also note that it may be nil. */

+ (void)setDefaultTPModule:(CSSMModule*)newDefault;

    /*! @method defaultCLPModule
    @abstract Returns the default CL module instance (a singleton).
    @discussion Many Keychain framework classes and functions - particularly older ones not yet updated with this new CSSMModule functionality - assume a global default module for CL operations.  To enable backward compatibility - as well as a simpler code path for those not concerned with which module they're using - this method exists, to return a default singleton instance for general CL operations.

                Note that the returned instance is not a copy, and modifications made to it (e.g. detaching and/or unloading) will effect anything else using it, both concurrently and in future.  Generally it is a bad idea to modify the returned CSSMModule - if you wish, create a new one with your modifications and use setDefaultCLModule: to apply it.  This avoids any race conditions about your changes.

                Note that the result may be nil, whether because of an error or because it was set this way by a call to setDefaultCLModule:.  In any case, treat a nil result as indicating CL functionality is not available, and fail appropriately (gracefully, if possible).
    @result Returns the default module for CL operations, which may be nil (due either to an error or it being explicitly set to nil by a call to setDefaultCLModule:). */

+ (CSSMModule*)defaultCLModule;

    /*! @method setDefaultCLModule:
    @abstract Sets the default CL module instance.
    @discussion See the documentation for defaultCLModule, the corresponding getter, for more information.
    @param newDefault The new default module.  Note that this is retained, not copied, so changes made to it are carried through.  Also note that it may be nil. */

+ (void)setDefaultCLModule:(CSSMModule*)newDefault;

/*! @method moduleWithHandle:
    @abstract Returns a CSSMModule instance for the given module handle.
    @discussion This method creates and initialises - <i>if necessary</i> - a CSSMModule instance for the given module handle.  If an instance already exists for the given handle, the existing instance is returned instead.
    @param handle The CSSM module handle of interest.
    @result Returns a CSSMModule instance for the given handle, or nil if an error occurs. */

+ (CSSMModule*)moduleWithHandle:(CSSM_MODULE_HANDLE)handle;

/*! @method initWithHandle:
    @abstract Returns a CSSMModule instance for the given module handle.
    @discussion Since there can be only one CSSMModule instance for each unique handle, this method does not guarantee to return the receiver.  If another instance already exists for the given handle, the receiver is released and the existing instance returned.  Otherwise, the receiver is appropriately initialised for the given handle and returned.
    @result Returns an CSSMModule (which may not be the receiver) initialised for the given handle. */

- (CSSMModule*)initWithHandle:(CSSM_MODULE_HANDLE)handle;


#pragma mark Getters & setters

// Yeah, I should document these, but... god... so lazy...

- (CSSM_GUID)GUID;

- (CSSM_VERSION)version;
- (uint32_t)subserviceID;
- (CSSM_SERVICE_TYPE)subserviceType;
- (const CSSM_API_MEMORY_FUNCS*)memoryFunctions;

#pragma mark Managers

/*! @method isLoaded
    @abstract Returns whether or not the receiver is loaded.
    @discussion Note that being loaded is not the same as being ready for use - the module must also be attached, using the "attach" method, and possibly have other initialisation tasks performed.  Use "isReady" to determine if the module is ready for use.

                This method does not presently guarantee not to return false positives - the module is assumed to be loaded initially, and then it's status is updated in response to errors or explicit unloads (i.e. via the unload method).  Thus, a result of YES isn't conclusive.  A result of NO, however, is.
    @result Returns YES if the receiver is loaded, NO otherwise. */

- (BOOL)isLoaded;

/*! @method detach
    @abstract Attempts to detach the receiver.
    @discussion You should detach a module once you are finished using it, or if you wish to change any of it's parameters (in which case you should probably also unload it, just to be sure, as some parameters are provided at load-time, not attachment-time).  A module may fail to detach for any number of reasons, including it still being in use.

                This method does not unload the module.  Use the "unload" method.

                The module is <b>not</b> automatically detached when the receiver is deallocated - that functionality is provided by and specific to instances of the subclass CSSMManagedModule.
    @result Returns YES if the module was detached successfully (or was already detached), NO otherwise.  If this method returns NO, you can obtain the exact error that occured using the "error" method. */

- (BOOL)detach;

/*! @method isAttached
    @abstract Returns whether or not the receiver is attached.
    @discussion Note that being attached is not the same as being ready for use - there may be other configuration that must still be done.  Use "isReady" to determine if the module is ready for use.

                This method does not presently guarantee not to return false positives - the module is assumed to be attached initially, and then it's status is updated in response to errors or explicit detaches (i.e. via the detach method).  Thus, a result of YES isn't conclusive.  A result of NO, however, is.
    @result Returns YES if the receiver is attached, NO otherwise. */

- (BOOL)isAttached;

/*! @method isReady
    @abstract Returns whether or not the receiver is loaded, attached and ready for use.
    @discussion This is a simple way to determine if the receiver has been fully loaded and initialised.  It does not attempt to perform anything to that end - just returns the status.  It is conceptually similar to checking both isLoaded and isAttached, although may also perform additional checks as necessary.

                This method does not presently guarantee not to return false positives - the module is generally assumed to be ready initially, and then it's status is updated in response to errors and so forth.  Thus, a result of YES isn't conclusive.  A result of NO, however, is.
    @result Returns YES if the module is ready for use (and ergo the result of the "handle" method will be a valid handle), NO otherwise. */

- (BOOL)isReady;

/*! @method handle
    @abstract Returns the module handle for use with CDSA functions.
    @discussion If the module has been explicitly detached or unloaded, or is otherwise invalid, 0 is returned.
    @result Returns a handle for the receiver, or 0 if the receiver no longer represents an active module. */

- (CSSM_MODULE_HANDLE)handle;

/*! @method error
    @abstract Returns the most recent error result.
    @discussion There are only two methods on the base CSSMModule which may modify this value - unload and detach.  Both set the value appropriately depending on whether they are successful or not.  A value of CSSM_OK indicates success.  Any other value is an appropriate CSSM error code, and can be translated to a human-readable string using CSSMErrorAsString().
    @result Returns the most recent error status for the receiver. */

- (CSSM_RETURN)error;

@end
