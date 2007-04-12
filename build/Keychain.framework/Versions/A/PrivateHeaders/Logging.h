/*
 *  Logging.h
 *  Keychain
 *
 *  Created by Wade Tregaskis on 26/01/05.
 *  Copyright 2005 Wade Tregaskis. All rights reserved.
 *
 */

#ifdef __OBJC__
#import <Foundation/Foundation.h>
#endif


/*! @header Logging
    @abstract Defines various macro's for logging output to various places.
    @discussion To make the Keychain framework suitable for many uses, it must be able to configure it's logging in various ways.  These included turning it on/off at compile time, and determining where debug output goes (either the console, standard out or standard error, for example).

                The default behaviours are:

                    <li>PDEBUG/PDEBUGC -> Console</li>
                    <li>PERR/PERRC -> Standard error</li>
                    <li>POUT/POUTC -> Standard output</li>
                    <li>PCONSOLE/PCONSOLEC -> Console</li>

                There are various compile-time flags which change how these macro's are defined.  They are:

                    <li>TAGGED_DEBUGGING - If defined, all output from PDEBUG/PDEBUGC will have the file name, function name and line number of the calling location.  Extremely useful for debugging as it provides an unambigious reference to the exact line that generated the error message.</li>

                    <li>NO_PCONSOLE - If defined, output from PCONSOLE/PCONSOLEC will be surpressed.</li>
                    <li>NO_PERR - If defined, output from PERR/PERRC will be surpressed.</li>
                    <li>NO_POUT - If defined, output from POUT/POUTC will be surpressed.</li>
                    <li>NDEBUG or NO_PDEBUG - If either is defined, output from PDEBUG/PDEBUGC will be surpressed.  Note: NDEBUG is a generic definition usually set by the compiler (or Xcode) based on your compile preferences.</li>

                    <li>PDEBUG_TO_STDOUT - If defined all output via PDEBUG/PDEBUGC will go to standard out.  The behaviour is undefined if DEBUG_TO_STDERR is also defined.</li>
                    <li>PDEBUG_TO_STDERR - If defined all output via PDEBUG/PDEBUGC will go to standard error.  The behaviour is undefined if DEBUG_TO_STDOUT is also defined.</li>

                    <li>PERR_TO_CONSOLE - If defined all output via PERR/PERRC will be instead routed to the console.  The result is undefined if PERR_TO_STDOUT is also defined.</li>
                    <li>PERR_TO_STDOUT - If defined all output via PERR/PERRC will be instead routed to standard out.  The result is undefined if PERR_TO_CONSOLE is also defined.</li>

                    <li>POUT_TO_CONSOLE - If defined all output via POUT/POUTC will be instead routed to the console.  The result is undefined if POUT_TO_STDERR is also defined.</li>
                    <li>POUT_TO_STDERR - If defined all output via POUT/POUTC will be instead routed to standard error.  The result is undefined if POUT_TO_CONSOLE is also defined.</li>

                    <li>PCONSOLE_TO_STDOUT - If defined all output via PCONSOLE/PCONSOLEC will be instead routed to standard out.  The result is undefined if PCONSOLE_TO_STDERR is also defined.</li>
                    <li>PCONSOLE_TO_STDERR - If defined all output via PCONSOLE/PCONSOLEC will be instead routed to standard error.  The result is undefined if PCONSOLE_TO_STDOUT is also defined.</li>

                The macro's which you can use for output are documented (as function calls) individually.  Refer to the appropriate documentation (which should be accessible via the index for this, the Logging, header file).  These macro's will only be defined if they have not already been defined.  In this way you can override them with your own custom versions by simply defining them before you import this header file.

                Note that while the details of the implementations may vary, you can consider these macros as independent of each other.  That is, if you reroute PDEBUG to stderr by defining PDEBUG_TO_STDERR, and also define PERR_TO_CONSOLE, the result is that that calls to PDEBUG go to standard error, and calls to PERR go to the console.  This is the defined behaviour that will remain unchanged regardless of the exact implementation. */


/* The following are for internal use only.  Do not ever call directly any function or macro with any number of underscores prefixed. */

#ifdef __OBJC__
    #define __PCONSOLE NSLog

    static inline void __PCONSOLEC(const char *format, ...) {
        va_list vargs;
        
        va_start(vargs, format);
        NSLogv([NSString stringWithUTF8String:format], vargs);
        va_end(vargs);
    }
#else
    #define __PCONSOLE #error PCONSOLE not supported in non-Objective-C source.

    #define __PCONSOLEC(const char *format, ...) \
        do { \
            #warning "PCONSOLEC not yet supported properly for vanilla-C; output routed to stderr." \
            vfprintf(stderr, format, ## __VA_ARGS__); \
        while (0);
#endif

static inline void __PERR(NSString *format, ...) {
    va_list vargs;
    NSString *string;
    
    va_start(vargs, format);
    string = [[NSString alloc] initWithFormat:format arguments:vargs];
    fprintf(stderr, "%s", [string UTF8String]);
    [string release];
    va_end(vargs);
}

#define __PERRC(format, ...) fprintf(stderr, format, ## __VA_ARGS__)

static inline void __POUT(NSString *format, ...) {
    va_list vargs;
    NSString *string;
    
    va_start(vargs, format);
    string = [[NSString alloc] initWithFormat:format arguments:vargs];
    fprintf(stdout, "%s", [string UTF8String]);
    [string release];
    va_end(vargs);
}

#define __POUTC printf


/*! @function PCONSOLE
    @abstract Logs NSString-style formatted output to the console.
    @discussion PCONSOLE assumes the format string is NSString-style, meaning it supports all the printf-style arguments as well as %\@ for Objective-C objects.

                You may use PCONSOLE for any user-orientated messages in a GUI application, where POUT/PERR may not produce user-visible output, or may produce output that you don't wish to cloud with certain messages.

                PCONSOLE is only defined for Objective-C sources, obviously.  For other languages, you need to use PCONSOLEC.
    @param format The format string (as an NSString, not a C string).  Should not be nil.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifdef __OBJC__
    #ifndef PCONSOLE
        #ifdef NO_PCONSOLE
            #define PCONSOLE(format, ...) /* Do nothing */
        #elif defined(PCONSOLE_TO_STDOUT)
            #define PCONSOLE __POUT
        #elif defined(PCONSOLE_TO_STDERR)
            #define PCONSOLE __PERR
        #else
            #define PCONSOLE __PCONSOLE
        #endif
    #endif
#endif

/*! @function PCONSOLEC
    @abstract Logs printf-style formatted output to the console.
    @discussion PCONSOLEC is a C-style version of PCONSOLE.  That is, it takes a C string instead of an NSString as the argument, and is guaranteed only to support printf-style format strings - NSString extensions may or may not be supported, and should not be relied upon.

                You may use PCONSOLE for any user-orientated messages in a GUI application, where POUT/PERR may not produce user-visible output, or may produce output that you don't wish to cloud with certain messages.
    @param format The format string (as a C string, not an NSString).  Should not be NULL.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifndef PCONSOLEC
    #ifdef NO_PCONSOLE
        #define PCONSOLEC(format, ...) /* Do nothing */
    #elif defined(PCONSOLE_TO_STDOUT)
        #define PCONSOLEC __POUTC
    #elif defined(PCONSOLE_TO_STDERR)
        #define PCONSOLEC __PERRC
    #else
        #define PCONSOLEC __PCONSOLEC
    #endif
#endif

/*! @function PERRC
    @abstract Logs printf-style formatted output to standard error.
    @discussion PERRC assumes the format string is printf-style, meaning it is a C string (not an NSString) and supports only printf-style arguments, not the %\@ for Objective-C objects.

                Use PERRC for any user-orientated error messages.  Remember, though, that GUI applications may not have a user-visible standard error, and so the messages may be lost.  Best to only use this macro in CLI programs, or with PERR_TO_CONSOLE defined.
    @param format The format string (as a C string, not an NSString).  Should not be NULL.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifndef PERRC
    #ifdef NO_PERR
        #define PERRC(format, ...) /* Do nothing */
    #elif defined(PERR_TO_CONSOLE)
        #define PERRC __PCONSOLEC
    #elif defined(PERR_TO_STDOUT)
        #define PERRC __POUTC
    #else
        #define PERRC __PERRC
    #endif
#endif

/*! @function PERR
    @abstract Logs NSString-style formatted output to standard error.
    @discussion PERR assumes the format string is an NSString containing NSString-style formatting, meaning it supports both printf-style arguments and the %\@ for Objective-C objects.

                Use PERR for any user-orientated error messages.  Remember, though, that GUI applications may not have a user-visible standard error, and so the messages may be lost.  Best to only use this macro in CLI programs, or when the PERR_TO_CONSOLE compile-time flag is defined.

                PERR is only defined when compiling as Objective-C.  For other languages, you may need to use PERRC.
    @param format The format string (as an NSString, not a C string).  Should not be nil.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifdef __OBJC__
    #ifndef PERR
        #ifdef NO_PERR
            #define PERR(format, ...) /* Do nothing */
        #elif defined(PERR_TO_CONSOLE)
            #define PERR __PCONSOLE
        #elif defined(PERR_TO_STDOUT)
            #define PERR __POUT
        #else
            #define PERR __PERR
        #endif
    #endif
#endif

/*! @function POUTC
    @abstract Logs printf-style formatted output to standard output.
    @discussion POUTC assumes the format string is printf-style, meaning it supports only printf-style arguments, not the %\@ for Objective-C objects.

                Use POUTC for any user-orientated messages that don't explicitly relate to an error.  You might use it to print status indications, general information, or similar such purposes.  Remember that in GUI programs there may not be a user-visible standard out, and so these messages will be lost.  Best to only use this macro in CLI programs, or when the POUT_TO_CONSOLE compile-time flag is defined.
    @param format The format string (as a C string, not an NSString).  Should not be NULL.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifndef POUTC
    #ifdef NO_POUT
        #define POUTC(format, ...) /* Do nothing */
    #elif defined(POUT_TO_CONSOLE)
        #define POUTC __PCONSOLEC
    #elif defined(POUT_TO_STDERR)
        #define POUTC __PERRC
    #else
        #define POUTC __POUTC
    #endif
#endif

/*! @function POUT
    @abstract Logs NSString-style formatted output to standard output.
    @discussion POUT assumes the format string is an NSString in NSString-style, meaning it supports both printf-style arguments and the %\@ for Objective-C objects.

                Use POUT for any user-orientated messages that don't explicitly relate to an error.  You might use it to print status indications, general information, or similar such purposes.  Remember that in GUI programs there may not be a user-visible standard out, and so these messages will be lost.  Best to only use this macro in CLI programs, or when the POUT_TO_CONSOLE compile-time flag is defined.

                Note that POUT is only defined for Objective-C sources.  For other languages, you may need to use POUTC.
    @param format The format string (as an NSString, not a C string).  Should not be nil.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifdef __OBJC__
    #ifndef POUT
        #ifdef NO_STANDARD_OUTPUT
            #define POUT(format, ...) /* Do nothing */
        #elif defined(POUT_TO_CONSOLE)
            #define POUT __PCONSOLE
        #elif defined(POUT_TO_STDERR)
            #define POUT __PERR
        #else
            #define POUT __POUT
        #endif
    #endif
#endif

/*! @function PDEBUG
    @abstract Logs NSString-style formatted output to an appropriate place (e.g. the console, standard err, etc).
    @discussion PDEBUG is intended for outputing debug information that the end user need not see in normal operation.  Indeed, by default all calls to PDEBUG/PDEBUGC are stripped at compile time in release builds.

                You should use PDEBUG for any messages which meet any of the following criteria:

                    <li>They contain only programmer-centric data which will be meaningless to an end user.</li>
                    <li>They may be printed very frequently.</li>
                    <li>They indicate events or information not necessary for normal program operation.<li>

                If necessary, use a PDEBUG and PERR/POUT pair; the PERR/POUT to convey user-orientated messages (e.g. "Certificate generation failed due to invalid parameters") and the PDEBUG to list the parameters and their actual values.

                Also not that if TAGGED_DEBUGGING is defined the file, function/method and line is prepended to the message.  This is recommended as it aids debugging by eliminating ambiguities and speeding identification of the relevant source code.

                Note that PDEBUG is only supported for Objective-C sources.  For other languages, you may need to use PDEBUGC.
    @param format The format string (as an NSString, not a C string).  Should not be nil.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifdef __OBJC__
    #ifndef PDEBUG
        #if defined(NDEBUG) || defined(NO_PDEBUG)
            #define PDEBUG(format, ...) /* Do nothing */
        #elif defined(PDEBUG_TO_STDOUT)
            #ifdef TAGGED_DEBUGGING
                #define PDEBUG(format, ...) __POUT([@"%s:d - %s " stringByAppendingString:format], __FILE__, __LINE__, __func__, ## __VA_ARGS__)
            #else
                #define PDEBUG __POUT
            #endif
        #elif defined(PDEBUG_TO_STDERR)
            #ifdef TAGGED_DEBUGGING
                #define PDEBUG(format, ...) __PERR([@"%s:%d - %s " stringByAppendingString:format], __FILE__, __LINE__, __func__, ## __VA_ARGS__)
            #else
                #define PDEBUG __PERR
            #endif
        #else
            #ifdef TAGGED_DEBUGGING
                #define PDEBUG(format, ...) __PCONSOLE([@"%s:%d - %s " stringByAppendingString:format], __FILE__, __LINE__, __func__, ## __VA_ARGS__)
            #else
                #define PDEBUG __PCONSOLE
            #endif
        #endif
    #endif
#endif

/*! @function PDEBUGC
    @abstract Logs printf-style formatted output to an appropriate place (e.g. the console, standard err, etc).
    @discussion PDEBUGC is intended for outputing debug information that the end user need not see in normal operation.  Indeed, by default all calls to PDEBUG/PDEBUGC are stripped at compile time in release builds.

                You should use PDEBUGC for any messages which meet any of the following criteria:

                    <li>They contain only programmer-centric data which will be meaningless to an end user.</li>
                    <li>They may be printed very frequently.</li>
                    <li>They indicate events or information not necessary for normal program operation.<li>

                If necessary, use a PDEBUGC and PERRC/POUTC pair; the PERRC/POUTC to convey user-orientated messages (e.g. "Certificate generation failed due to invalid parameters") and the PDEBUGC to list the parameters and their actual values.

                Also not that if TAGGED_DEBUGGING is defined the file, function/method and line is prepended to the message.  This is recommended as it aids debugging by eliminating ambiguities and speeding identification of the relevant source code.
    @param format The format string (as a C string, not an NSString).  Should not be NULL.
    @param args A variable number of arguments suitable for the given 'format' string. */

#ifndef PDEBUGC
    #if defined(NDEBUG) || defined(NO_PDEBUG)
        #define PDEBUGC(format, ...) /* Do nothing */
    #elif defined(PDEBUG_TO_STDOUT)
        #ifdef TAGGED_DEBUGGING
            #define PDEBUGC(format, ...) __POUTC("%s:%d - %s " format, __FILE__, __LINE__, __func__, ## __VA_ARGS__)
        #else
            #define PDEBUGC __POUTC
        #endif
    #elif defined(PDEBUG_TO_STDERR)
        #ifdef TAGGED_DEBUGGING
            #define PDEBUGC(format, ...) __PERRC("%s:%d - %s " format, __FILE__, __LINE__, __func__, ## __VA_ARGS__)
        #else
            #define PDEBUGC __PERRC
        #endif
    #else
        #ifdef TAGGED_DEBUGGING
            #define PDEBUGC(format, ...) __PCONSOLEC("%s:%d - %s " format, __FILE__, __LINE__, __func__, ## __VA_ARGS__)
        #else
            #define PDEBUGC __PCONSOLEC
        #endif
    #endif
#endif
