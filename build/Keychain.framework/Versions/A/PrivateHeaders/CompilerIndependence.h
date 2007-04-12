//
//  CompilerIndependence.h
//  Keychain
//
//  Created by Wade Tregaskis on Tues Dec 6 2005.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


#import <sys/types.h>


#if __GNUC__ >= 3
    //#define inline          inline __attribute__ ((always_inline))

    #ifndef __pure
        #define __pure          __attribute__ ((pure))
    #endif

    #ifndef __const
        #define __const         __attribute__ ((const))
    #endif

    #ifndef __noreturn
        #define __noreturn      __attribute__ ((noreturn))
    #endif

    #ifndef __malloc
        #define __malloc        __attribute__ ((malloc))
    #endif

    #ifndef __must_check
        #define __must_check	__attribute__ ((warn_unused_result))
    #endif

    #ifndef __deprecated
        #define __deprecated	__attribute__ ((deprecated))
    #endif

    #ifndef __used
        #define __used          __attribute__ ((used))
    #endif

    #ifndef __unused
        #define __unused        __attribute__ ((unused))
    #endif

    #ifndef __packed
        #define __packed        __attribute__ ((packed))
    #endif

    #ifndef likely
        #define likely(x)       __builtin_expect (!!(x), 1)
    #endif

    #ifndef unlikely
        #define unlikely(x)     __builtin_expect (!!(x), 0)
    #endif
#else
    //#define inline          /* no inline */

    #ifndef __pure
        #define __pure          /* no pure */
    #endif

    #ifndef __const
        #define __const         /* no const */
    #endif

    #ifndef __noreturn
        #define __noreturn      /* no noreturn */
    #endif

    #ifndef __malloc
        #define __malloc        /* no malloc */
    #endif

    #ifndef __must_check
        #define __must_check	/* no warn_unused_result */
    #endif

    #ifndef __deprecated
        #define __deprecated	/* no deprecated */
    #endif

    #ifndef __used
        #define __used          /* no used */
    #endif

    #ifndef __unused
        #define __unused        /* no unused */
    #endif

    #ifndef __packed
        #define __packed        /* no packed */
    #endif

    #ifndef likely
        #define likely(x)       (x)
    #endif

    #ifndef unlikely
        #define unlikely(x)     (x)
    #endif
#endif
