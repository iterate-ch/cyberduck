package ch.cyberduck.core.fs.kfs;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @version $Id:$
 */
public interface KfsLibrary extends Library {
    KfsLibrary INSTANCE = (KfsLibrary) Native.loadLibrary("KFS", KfsLibrary.class);

    public class size_t extends IntegerType {
        public size_t() {
            this(0);
        }

        public size_t(long value) {
            super(Native.SIZE_T_SIZE, value);
        }
    }

    /**
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i><br>
     * enum values
     */
    public static interface kfstype_t {
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:309</i>
        public static final int KFS_REG = 0;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:310</i>
        public static final int KFS_DIR = 1;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:311</i>
        public static final int KFS_BLK = 2;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:312</i>
        public static final int KFS_CHR = 3;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:313</i>
        public static final int KFS_LNK = 4;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:314</i>
        public static final int KFS_SOCK = 5;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:315</i>
        public static final int KFS_FIFO = 6;
    }

    ;

    /**
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i><br>
     * enum values
     */
    public static interface kfsmode_t {
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:319</i>
        public static final int KFS_IRUSR = 1024;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:320</i>
        public static final int KFS_IWUSR = 512;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:321</i>
        public static final int KFS_IXUSR = 256;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:322</i>
        public static final int KFS_IRGRP = 64;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:323</i>
        public static final int KFS_IWGRP = 32;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:324</i>
        public static final int KFS_IXGRP = 16;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:325</i>
        public static final int KFS_IROTH = 4;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:326</i>
        public static final int KFS_IWOTH = 2;
        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:327</i>
        public static final int KFS_IXOTH = 1;
    }

    ;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IROTH = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_CPUTIME = 68;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFCHR = 8192;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SS_REPL_MAX = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TIMEOUTS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TIMER_MAX = 52;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_RAW_SOCKETS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IXOTH = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_CHARCLASS_NAME_MAX = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int NAME_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_EVENT_FILTER = 98;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int F_LOCK = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LP64_OFF64_LDFLAGS = 29;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_NMAX = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LP64_OFF64_LIBS = 30;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_CHOWN_RESTRICTED = 7;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MONOTONIC_CLOCK = 74;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int F_TLOCK = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SEM_NSEMS_MAX = 49;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFLNK = 40960;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SYNCHRONIZED_IO = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int F_TEST = 3;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __DARWIN_NULL = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_LINK_MAX = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_NPROCESSORS_ONLN = 58;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_64_BIT_INO_T = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_IMMUTABLE = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int SEEK_END = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE = 97;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int SCHAR_MAX = 127;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_RTSIG_MAX = 48;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int SEEK_SET = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SPORADIC_SERVER = 81;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SS_REPL_MAX = 126;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_MAX_INPUT = 3;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_OPEN_MAX = 5;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_VERS_1050 = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFFBIG_LDFLAGS = 25;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MEMLOCK = 30;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_PRIORITY_SCHEDULING = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SPIN_LOCKS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SPAWN = 79;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_FSYNC = 38;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_GETPW_R_SIZE_MAX = 71;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/_limits.h</i>
    public static final int __DARWIN_CLK_TCK = 100;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFMT = 61440;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_COLL_WEIGHTS_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_V6_LP64_OFF64 = 105;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_GETGR_R_SIZE_MAX = 70;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int ARG_MAX = (256 * 1024);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int GID_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IRGRP = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_MAX_CANON = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _WPERM_OK = (1 << 20);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TIMEOUTS = 95;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFDIR = 16384;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_BC_DIM_MAX = 10;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int CHAR_MAX = 127;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _RMFILE_OK = (1 << 14);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS_ACCOUNTING = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFXATTR = 65536;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int R_OK = (1 << 2);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_NODUMP = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LPBIG_OFFBIG_CFLAGS = 11;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_RWLOCKATTR_SIZE__ = 12;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_PASS_MAX = 131;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MEMLOCK_RANGE = 31;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int PTHREAD_STACK_MIN = 8192;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_RAW_SOCKETS = 119;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _V6_ILP32_OFF32 = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT16_MAX = 65535;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_REALTIME = 111;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int ULLONG_MAX = -1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int EXPR_NEST_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_ENH_I18N = (1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int LINK_MAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SYMLOOP_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_EXPR_NEST_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT8_MIN = -128;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _CHOWN_OK = (1 << 21);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SAVED_IDS = 7;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_MAX_INPUT = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int __DARWIN_BIG_ENDIAN = 4321;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h</i>
    public static final int __DARWIN_NBBY = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LP64_OFF64_LDFLAGS = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int ACCESSX_MAX_DESCRIPTORS = 100;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int F_OK = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_LEGACY = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT64_MAX = -1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int NGROUPS_MAX = 16;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS_LOCATE = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int SF_IMMUTABLE = 131072;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _RATTR_OK = (1 << 15);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_SUF_UNIX03_SET = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int MAX_INPUT = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_IOV_MAX = 56;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_2_SYMLINKS = 15;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int WORD_BIT = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long INT64_MAX = 9223372036854775807L;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_BC_BASE_MAX = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFF32_CFLAGS = 20;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_REALTIME = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XBS5_LPBIG_OFFBIG = 125;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_REC_MAX_XFER_SIZE = 21;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS_TRACK = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LPBIG_OFFBIG_CFLAGS = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_LANGMAX = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_SAFE_FUNCTIONS = 91;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_UPE = 25;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_TEXTMAX = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_CASE_PRESERVING = 12;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IRWXG = 56;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IRWXO = 7;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_REALTIME_THREADS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SHELL = 78;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int UINT_MAX = -1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int LINE_MAX = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IRWXU = 448;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_STREAM_MAX = 26;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFF32_LIBS = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_BARRIERS = 66;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int SCHAR_MIN = (-128);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_NGROUPS_MAX = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_WIDTH_RESTRICTED_ENVS = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_RTSIG_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XBS5_ILP32_OFF32 = 122;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_V6_ILP32_OFFBIG = 104;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int UID_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_FORT_RUN = 22;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFFBIG_CFLAGS = 24;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int LONG_MIN = (-2147483647 - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_ASYNCHRONOUS_IO = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TRACE_INHERIT = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_BC_STRING_MAX = 12;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_COND_SIZE__ = 24;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _WATTR_OK = (1 << 16);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_CHILD_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_NAME_MAX = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_LINK_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS_CHECKPOINT = 61;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IXGRP = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SYNCHRONIZED_IO = 40;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long ULONG_MAX = 4294967295L;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdbool.h</i>
    public static final int __bool_true_false_are_defined = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SIGQUEUE_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_ASYNCHRONOUS_IO = 28;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_ISVTX = 512;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int USHRT_MAX = 65535;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long LLONG_MAX = 9223372036854775807L;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TRACE_EVENT_FILTER = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_EQUIV_CLASS_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_AIO_LISTIO_MAX = 42;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFWHT = 57344;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_DELAYTIMER_MAX = 45;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_DARWIN_USER_CACHE_DIR = 65538;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SYMLOOP_MAX = 120;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_BC_SCALE_MAX = 11;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_READER_WRITER_LOCKS = 76;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_SETMAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TTY_NAME_MAX = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_ENH_I18N = 109;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_HIDDEN = 32768;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IWGRP = 16;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/_types.h</i>
    public static final int __DARWIN_WCHAR_MIN = (-2147483647 - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IWOTH = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IWUSR = 128;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TTY_NAME_MAX = 101;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h</i>
    public static final int __DARWIN_FD_SETSIZE = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MESSAGE_PASSING = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int CHAR_MIN = (-128);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SHARED_MEMORY_OBJECTS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_MQ_OPEN_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XBS5_LP64_OFF64 = 124;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_ATEXIT_MAX = 107;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_THREAD_DESTRUCTOR_ITERATIONS = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _APPEND_OK = (1 << 13);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _RPERM_OK = (1 << 19);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SHELL = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_SYS_MAX = 129;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LPBIG_OFFBIG_LIBS = 13;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_AIO_MAX = 43;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SEM_NSEMS_MAX = 256;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TZNAME_MAX = 6;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_IPV6 = 118;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_VERSION = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_PRIORITIZED_IO = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final String __DARWIN_SUF_UNIX03 = "$UNIX2003";
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _V6_LP64_OFF64 = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SEM_VALUE_MAX = 50;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int __DARWIN_PDP_ENDIAN = 3412;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_XCU_VERSION = 121;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFFBIG_LIBS = 26;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_HOST_NAME_MAX = 72;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_CHOWN_RESTRICTED = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS = 59;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_XCU_VERSION = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_LINE_MAX = 15;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int STDERR_FILENO = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_SPORADIC_SERVER = 92;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int X_OK = (1 << 0);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_CLOCK_SELECTION = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_STREAM_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_LOCALEDEF = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_NAME_MAX = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_NO_TRUNC = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TIMERS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_ATTR_STACKSIZE = 83;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_STACK_MIN = 93;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_IPV6 = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int LONG_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_AIO_PRIO_DELTA_MAX = 44;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_C_DEV = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT8_MAX = 127;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_CPUTIME = 84;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_SIZE__ = 596;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_THREAD_KEYS_MAX = 128;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_LINE_MAX = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int PATH_MAX = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_RWLOCK_SIZE__ = 124;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_REGEXP = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_CLOCK_SELECTION = 67;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TYPED_MEMORY_OBJECTS = 102;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int _DARWIN_FEATURE_UNIX_CONFORMANCE = 3;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_PROCESS_SHARED = 90;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_MSGMAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_SW_DEV = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _EXECUTE_OK = (1 << 11);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_CPUTIME = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int SEEK_CUR = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_AIO_LISTIO_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_DARWIN_USER_DIR = 65536;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _REXT_OK = (1 << 17);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _WEXT_OK = (1 << 18);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int STDOUT_FILENO = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_LOCALEDEF = 23;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_SETTABLE = 65535;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _XOPEN_NAME_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_EVENT_NAME_MAX = 127;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/_types.h</i>
    public static final int __DARWIN_WCHAR_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_ADVISORY_INFO = 65;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_ISUID = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _XOPEN_PATH_MAX = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long LLONG_MIN = (9223372036854775807L - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MESSAGE_PASSING = 33;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_RE_DUP_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_BC_SCALE_MAX = 99;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_FILESIZEBITS = 18;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _DELETE_OK = (1 << 12);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_PRIORITY_SCHEDULING = 35;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TIMERS = 41;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_ALLOC_SIZE_MIN = 16;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_JOB_CONTROL = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_BLKSIZE = 512;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_PRIO_INHERIT = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT16_MIN = -32768;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int F_ULOCK = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_CHILD_MAX = 25;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_PAGESIZE = 29;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_DESTRUCTOR_ITERATIONS = 85;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int SF_SETTABLE = -65536;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS_MESSAGE = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_LEGACY = 110;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFF32_LDFLAGS = 3;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int COLL_WEIGHTS_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LPBIG_OFFBIG_LDFLAGS = 33;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int W_OK = (1 << 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_UNIX03 = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SIGQUEUE_MAX = 51;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TRACE_LOG = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_MAX_CANON = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_JOB_CONTROL = 6;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_ATTR_STACKADDR = 82;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_USER_EVENT_MAX = 130;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int IOV_MAX = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int BC_DIM_MAX = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _XOPEN_IOV_MAX = 16;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int SHRT_MIN = (-32768);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LP64_OFF64_CFLAGS = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int BC_SCALE_MAX = 99;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_STREAMS = 114;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_C_BIND = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int NL_ARGMAX = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int PIPE_BUF = 512;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_LONG_DOUBLE_IS_DOUBLE = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int __WORDSIZE = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_PATH_MAX = 256;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_APPEND = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT8_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SYMLINK_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final int __DARWIN_NON_CANCELABLE = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LP64_OFF64_CFLAGS = 28;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_REC_INCR_XFER_SIZE = 20;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_REALTIME_THREADS = 112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_PRIO_IO = 19;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int CHAR_BIT = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_FILE_LOCKING = 69;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_MUTEX_SIZE__ = 40;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int MB_LEN_MAX = 6;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_UNIX = 115;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_DELAYTIMER_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _POSIX2_VERSION = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int _QUAD_LOWWORD = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_FSYNC = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_SYMLINK_MAX = 24;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int EQUIV_CLASS_MAX = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_CHAR_TERM = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_CHAR_TERM = 20;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_BC_STRING_MAX = 1000;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_CRYPT = (1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _V6_ILP32_OFFBIG = (1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int LONG_BIT = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_NPROCESSORS_CONF = 57;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int OPEN_MAX = 10240;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_C_BIND = 18;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_FORT_DEV = 21;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_HOST_NAME_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int INT_MIN = (-2147483647 - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_REC_MIN_XFER_SIZE = 22;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_CRYPT = 108;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MEMORY_PROTECTION = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_VDISABLE = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_PRIO_PROTECT = 88;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_C_DEV = 19;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_SW_DEV = 24;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_EXTENDED_SECURITY_NP = 13;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_SHM = (1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_MUTEXATTR_SIZE__ = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS_LOCATE = 62;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SPAWN = (-1);
    /**
     * define<br>
     * Conversion Error : [2]<br>
     * SKIPPED:<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i><br>
     * [2]
     */
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _READ_OK = (1 << 9);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _POSIX_VERSION = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TRACE_USER_EVENT_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TZNAME_MAX = 27;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_V6_ILP32_OFF32 = 103;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_SHM = 113;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFFBIG_LDFLAGS = 6;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SPORADIC_SERVER = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TIMER_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SEM_VALUE_MAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_LOGIN_NAME_MAX = 73;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_FORT_DEV = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_KEYS_MAX = 86;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SEMAPHORES = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_NAME_MAX = 128;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREADS = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_NAME_CHARS_MAX = 10;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_NGROUPS_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_UNIX = (1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int PTHREAD_DESTRUCTOR_ITERATIONS = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREADS = 96;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_PRIO_INHERIT = 87;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFFBIG_CFLAGS = 5;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MQ_OPEN_MAX = 46;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XOPEN_VERSION = 116;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int SF_APPEND = 262144;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_SAFE_FUNCTIONS = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_ATTR_STACKSIZE = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SEMAPHORES = 37;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MAPPED_FILES = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_PIPE_BUF = 512;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_PATH_MAX = 5;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFFBIG_LIBS = 7;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int CHARCLASS_NAME_MAX = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_REALTIME_SIGNALS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_SPORADIC_SERVER = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_DARWIN_USER_TEMP_DIR = 65537;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_CPUTIME = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MQ_PRIO_MAX = 75;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_OPEN_MAX = 20;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int CHILD_MAX = 266;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFF32_LDFLAGS = 21;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _V6_LPBIG_OFFBIG = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int UF_OPAQUE = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT16_MAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFFBIG_LINTFLAGS = 27;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IXUSR = 64;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int WCHAR_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_PRIO_PROTECT = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TRACE_EVENT_NAME_MAX = 30;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int RE_DUP_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int PASS_MAX = 128;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_THREADS_MAX = 94;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_REGEXP = 77;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_BARRIERS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int _QUAD_HIGHWORD = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_STREAMS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MEMLOCK_RANGE = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFBLK = 24576;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int SHRT_MAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MEMLOCK = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MEMORY_PROTECTION = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFIFO = 4096;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LP64_OFF64_LINTFLAGS = 31;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_ADVISORY_INFO = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_LOGIN_NAME_MAX = 9;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_SAVED_IDS = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int SF_ARCHIVED = 65536;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFREG = 32768;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_VERSION = 17;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_REC_XFER_ALIGN = 23;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_BC_BASE_MAX = 99;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_CASE_SENSITIVE = 11;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_MAPPED_FILES = 47;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_MONOTONIC_CLOCK = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IRUSR = 256;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LPBIG_OFFBIG_LIBS = 34;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_PROCESS_SHARED = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_COLL_WEIGHTS_MAX = 13;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_PRIORITY_SCHEDULING = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TRACE = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_ISGID = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT32_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_RE_DUP_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_PIPE_BUF = 6;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IFSOCK = 49152;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TRACE_NAME_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_TRACE_SYS_MAX = 8;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_LPBIG_OFFBIG_LINTFLAGS = 35;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int PTHREAD_KEYS_MAX = 512;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_ARG_MAX = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS_TRACK = 64;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_ARG_MAX = 4096;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_ONCE_SIZE__ = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long UINT32_MAX = 4294967295L;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_ASYNC_IO = 17;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_V6_LPBIG_OFFBIG = 106;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h</i>
    public static final String __DARWIN_SUF_EXTSN = "$DARWIN_EXTSN";
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int __DARWIN_LITTLE_ENDIAN = 1234;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int STDIN_FILENO = 0;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_CLK_TCK = 3;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_ATTR_SIZE__ = 36;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int BC_STRING_MAX = 1000;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_NO_TRUNC = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int UCHAR_MAX = 255;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int INT_MAX = 2147483647;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _CS_PATH = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_TYPED_MEMORY_OBJECTS = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS_MESSAGE = 63;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_READER_WRITER_LOCKS = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LPBIG_OFFBIG_LDFLAGS = 12;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_FORT_RUN = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_2_PBS_ACCOUNTING = 60;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_LP64_OFF64_LIBS = 10;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_REALTIME_SIGNALS = 36;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_SYNC_IO = 25;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_INHERIT = 99;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_XBS5_ILP32_OFFBIG = 123;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
    public static final int __PTHREAD_CONDATTR_SIZE__ = 4;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_POSIX_V6_ILP32_OFF32_CFLAGS = 2;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SHARED_MEMORY_OBJECTS = 39;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_THREAD_PRIORITY_SCHEDULING = 89;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _PC_AUTH_OPAQUE_NP = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int MAX_CANON = 1024;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XOPEN_VERSION = 600;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_UPE = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_EXPR_NEST_MAX = 14;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_MQ_PRIO_MAX = 32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int NZERO = 20;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/syslimits.h</i>
    public static final int BC_BASE_MAX = 99;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_THREAD_THREADS_MAX = 64;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_AIO_MAX = 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_PRIORITIZED_IO = 34;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFF32_LIBS = 22;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX_THREAD_ATTR_STACKADDR = 200112;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_TRACE_LOG = 100;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _WRITE_OK = (1 << 10);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX_SSIZE_MAX = 32767;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/limits.h</i>
    public static final int _POSIX2_BC_DIM_MAX = 2048;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int ACCESSX_MAX_TABLESIZE = (16 * 1024);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_RE_DUP_MAX = 16;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _POSIX2_PBS_CHECKPOINT = (-1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _CS_XBS5_ILP32_OFF32_LINTFLAGS = 23;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_SPIN_LOCKS = 80;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_ISTXT = S_ISVTX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int PDP_ENDIAN = __DARWIN_PDP_ENDIAN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int UQUAD_MAX = ULLONG_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long INTMAX_MAX = INT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_FAST64_MAX = UINT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IREAD = S_IRUSR;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_FAST16_MIN = INT16_MIN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int PTRDIFF_MAX = INT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int SSIZE_MAX = LONG_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/select.h</i>
    public static final int FD_SETSIZE = __DARWIN_FD_SETSIZE;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long QUAD_MIN = 9223372036854775807L - 1;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT32_MIN = (INT32_MAX - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int WINT_MAX = INT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_FAST32_MAX = INT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_LEAST16_MIN = INT16_MIN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_FAST8_MAX = INT8_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_LEAST8_MIN = INT8_MIN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XBS5_ILP32_OFF32 = _V6_ILP32_OFF32;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINTMAX_MAX = UINT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IWRITE = S_IWUSR;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int L_SET = SEEK_SET;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_FAST16_MAX = INT16_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_LEAST16_MAX = INT16_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_LEAST8_MAX = UINT8_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int WCHAR_MIN = (WCHAR_MAX - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_LEAST16_MAX = UINT16_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long SIZE_MAX = UINT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INTPTR_MAX = INT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int _ACCESS_EXTENDED_MASK = (1 << 9 | 1 << 10 | 1 << 11 | 1 << 12 | 1 << 13 | 1 << 14 | 1 << 17 | 1 << 18 | 1 << 15 | 1 << 16 | 1 << 19 | 1 << 20 | 1 << 21);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_FAST8_MAX = UINT8_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XBS5_LPBIG_OFFBIG = _V6_LPBIG_OFFBIG;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int L_INCR = SEEK_CUR;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long UINT_FAST32_MAX = UINT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_FAST8_MIN = INT8_MIN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long UINT_LEAST32_MAX = UINT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long SIZE_T_MAX = ULONG_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_LEAST8_MAX = INT8_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int S_IEXEC = S_IXUSR;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int DEFFILEMODE = (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int NULL = __DARWIN_NULL;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int INT_LEAST32_MAX = INT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_FAST16_MAX = UINT16_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
    public static final int NBBY = __DARWIN_NBBY;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h</i>
    public static final int L_XTND = SEEK_END;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long INT64_MIN = (INT64_MAX - 1);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int LITTLE_ENDIAN = __DARWIN_LITTLE_ENDIAN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final int CLK_TCK = __DARWIN_CLK_TCK;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long INT_FAST64_MAX = INT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h</i>
    public static final int ACCESSPERMS = (S_IRWXU | S_IRWXG | S_IRWXO);
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XBS5_ILP32_OFFBIG = _V6_ILP32_OFFBIG;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long INT_LEAST64_MAX = INT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _XBS5_LP64_OFF64 = _V6_LP64_OFF64;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int BIG_ENDIAN = __DARWIN_BIG_ENDIAN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h</i>
    public static final int __DARWIN_BYTE_ORDER = __DARWIN_LITTLE_ENDIAN;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h</i>
    public static final int _SC_PAGE_SIZE = _SC_PAGESIZE;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/limits.h</i>
    public static final long QUAD_MAX = LLONG_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final long UINTPTR_MAX = UINT32_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int UINT_LEAST64_MAX = UINT64_MAX;
    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h</i>
    public static final int SIG_ATOMIC_MAX = INT32_MAX;

    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/_types.h</i>
    public static class __mbstate_t extends Union {
        /// C type : char[128]
        public byte[] __mbstate8 = new byte[(128)];
        /// for alignment
        public long _mbstateL;

        public __mbstate_t() {
            super();
        }

        /// @param _mbstateL for alignment
        public __mbstate_t(long _mbstateL) {
            super();
            this._mbstateL = _mbstateL;
            setType(java.lang.Long.TYPE);
        }

        /// @param __mbstate8 C type : char[128]
        public __mbstate_t(byte __mbstate8[]) {
            super();
            if(__mbstate8.length != this.__mbstate8.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__mbstate8 = __mbstate8;
            setType(byte[].class);
        }

        public static class ByReference extends __mbstate_t implements Structure.ByReference {

        }


        public static class ByValue extends __mbstate_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:57</i>
    public static class __darwin_pthread_handler_rec extends Structure {
        /**
         * Routine to call<br>
         * C type : __routine_callback
         */
        public __darwin_pthread_handler_rec.__routine_callback __routine;
        /**
         * Argument to pass<br>
         * C type : void*
         */
        public Pointer __arg;
        /// C type : __darwin_pthread_handler_rec*
        public __darwin_pthread_handler_rec.ByReference __next;

        /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h</i>
        public interface __routine_callback extends Callback {
            /// @param voidPtr1 Routine to call
            void apply(Pointer voidPtr1);
        }


        public __darwin_pthread_handler_rec() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__routine", "__arg", "__next"});
        }

        /**
         * @param __routine Routine to call<br>
         *                  C type : __routine_callback<br>
         * @param __arg     Argument to pass<br>
         *                  C type : void*<br>
         * @param __next    C type : __darwin_pthread_handler_rec*
         */
        public __darwin_pthread_handler_rec(__darwin_pthread_handler_rec.__routine_callback __routine, Pointer __arg, __darwin_pthread_handler_rec.ByReference __next) {
            super();
            this.__routine = __routine;
            this.__arg = __arg;
            this.__next = __next;
            initFieldOrder();
        }

        public static class ByReference extends __darwin_pthread_handler_rec implements Structure.ByReference {

        }


        public static class ByValue extends __darwin_pthread_handler_rec implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:63</i>
    public static class _opaque_pthread_attr_t extends Structure {
        public NativeLong __sig;
        /// C type : char[36]
        public byte[] __opaque = new byte[(36)];

        public _opaque_pthread_attr_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[36]
        public _opaque_pthread_attr_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_attr_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_attr_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:64</i>
    public static class _opaque_pthread_cond_t extends Structure {
        public NativeLong __sig;
        /// C type : char[24]
        public byte[] __opaque = new byte[(24)];

        public _opaque_pthread_cond_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[24]
        public _opaque_pthread_cond_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_cond_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_cond_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:65</i>
    public static class _opaque_pthread_condattr_t extends Structure {
        public NativeLong __sig;
        /// C type : char[4]
        public byte[] __opaque = new byte[(4)];

        public _opaque_pthread_condattr_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[4]
        public _opaque_pthread_condattr_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_condattr_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_condattr_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:66</i>
    public static class _opaque_pthread_mutex_t extends Structure {
        public NativeLong __sig;
        /// C type : char[40]
        public byte[] __opaque = new byte[(40)];

        public _opaque_pthread_mutex_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[40]
        public _opaque_pthread_mutex_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_mutex_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_mutex_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:67</i>
    public static class _opaque_pthread_mutexattr_t extends Structure {
        public NativeLong __sig;
        /// C type : char[8]
        public byte[] __opaque = new byte[(8)];

        public _opaque_pthread_mutexattr_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[8]
        public _opaque_pthread_mutexattr_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_mutexattr_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_mutexattr_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:68</i>
    public static class _opaque_pthread_once_t extends Structure {
        public NativeLong __sig;
        /// C type : char[4]
        public byte[] __opaque = new byte[(4)];

        public _opaque_pthread_once_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[4]
        public _opaque_pthread_once_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_once_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_once_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:69</i>
    public static class _opaque_pthread_rwlock_t extends Structure {
        public NativeLong __sig;
        /// C type : char[124]
        public byte[] __opaque = new byte[(124)];

        public _opaque_pthread_rwlock_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[124]
        public _opaque_pthread_rwlock_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_rwlock_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_rwlock_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:70</i>
    public static class _opaque_pthread_rwlockattr_t extends Structure {
        public NativeLong __sig;
        /// C type : char[12]
        public byte[] __opaque = new byte[(12)];

        public _opaque_pthread_rwlockattr_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__opaque"});
        }

        /// @param __opaque C type : char[12]
        public _opaque_pthread_rwlockattr_t(NativeLong __sig, byte __opaque[]) {
            super();
            this.__sig = __sig;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_rwlockattr_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_rwlockattr_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h:71</i>
    public static class _opaque_pthread_t extends Structure {
        public NativeLong __sig;
        /// C type : __darwin_pthread_handler_rec*
        public __darwin_pthread_handler_rec.ByReference __cleanup_stack;
        /// C type : char[596]
        public byte[] __opaque = new byte[(596)];

        public _opaque_pthread_t() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"__sig", "__cleanup_stack", "__opaque"});
        }

        /**
         * @param __cleanup_stack C type : __darwin_pthread_handler_rec*<br>
         * @param __opaque        C type : char[596]
         */
        public _opaque_pthread_t(NativeLong __sig, __darwin_pthread_handler_rec.ByReference __cleanup_stack, byte __opaque[]) {
            super();
            this.__sig = __sig;
            this.__cleanup_stack = __cleanup_stack;
            if(__opaque.length != this.__opaque.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.__opaque = __opaque;
            initFieldOrder();
        }

        public static class ByReference extends _opaque_pthread_t implements Structure.ByReference {

        }


        public static class ByValue extends _opaque_pthread_t implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/unistd.h:137</i>
    public static class accessx_descriptor extends Structure {
        public int ad_name_offset;
        public int ad_flags;
        /// C type : int[2]
        public int[] ad_pad = new int[(2)];

        public accessx_descriptor() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"ad_name_offset", "ad_flags", "ad_pad"});
        }

        /// @param ad_pad C type : int[2]
        public accessx_descriptor(int ad_name_offset, int ad_flags, int ad_pad[]) {
            super();
            this.ad_name_offset = ad_name_offset;
            this.ad_flags = ad_flags;
            if(ad_pad.length != this.ad_pad.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.ad_pad = ad_pad;
            initFieldOrder();
        }

        public static class ByReference extends accessx_descriptor implements Structure.ByReference {

        }


        public static class ByValue extends accessx_descriptor implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h:89</i>
    public static class timespec extends Structure {
        /// C type : __darwin_time_t
        public NativeLong tv_sec;
        public NativeLong tv_nsec;

        public timespec() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"tv_sec", "tv_nsec"});
        }

        /// @param tv_sec C type : __darwin_time_t
        public timespec(NativeLong tv_sec, NativeLong tv_nsec) {
            super();
            this.tv_sec = tv_sec;
            this.tv_nsec = tv_nsec;
            initFieldOrder();
        }

        public static class ByReference extends timespec implements Structure.ByReference {

        }


        public static class ByValue extends timespec implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h:101</i>
    public static class timeval extends Structure {
        /**
         * seconds<br>
         * C type : __darwin_time_t
         */
        public NativeLong tv_sec;
        /**
         * and microseconds<br>
         * C type : __darwin_suseconds_t
         */
        public int tv_usec;

        public timeval() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"tv_sec", "tv_usec"});
        }

        /**
         * @param tv_sec  seconds<br>
         *                C type : __darwin_time_t<br>
         * @param tv_usec and microseconds<br>
         *                C type : __darwin_suseconds_t
         */
        public timeval(NativeLong tv_sec, int tv_usec) {
            super();
            this.tv_sec = tv_sec;
            this.tv_usec = tv_usec;
            initFieldOrder();
        }

        public static class ByReference extends timeval implements Structure.ByReference {

        }


        public static class ByValue extends timeval implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h</i>
    public static class fd_set extends Structure {
        /// C type : __int32_t[(((1024) + ((sizeof(__int32_t) * 8) - 1)) / (sizeof(__int32_t) * 8))]
        public int[] fds_bits = new int[(((1024) + ((4 * 8) - 1)) / (4 * 8))];

        public fd_set() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"fds_bits"});
        }

        /// @param fds_bits C type : __int32_t[(((1024) + ((sizeof(__int32_t) * 8) - 1)) / (sizeof(__int32_t) * 8))]
        public fd_set(int fds_bits[]) {
            super();
            if(fds_bits.length != this.fds_bits.length) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.fds_bits = fds_bits;
            initFieldOrder();
        }

        public static class ByReference extends fd_set implements Structure.ByReference {

        }


        public static class ByValue extends fd_set implements Structure.ByValue {

        }


    }


    /**
     * XXX So deprecated, it would make your head spin<br>
     * * The old stat structure.  In fact, this is not used by the kernel at all,<br>
     * and should not be used by user space, and should be removed from this<br>
     * header file entirely (along with the unused cvtstat() prototype in<br>
     * vnode_internal.h).<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:150</i>
     */
    public static class ostat extends Structure {
        /// inode's device
        public short st_dev;
        /**
         * inode's number<br>
         * C type : ino_t
         */
        public int st_ino;
        /**
         * inode protection mode<br>
         * C type : mode_t
         */
        public short st_mode;
        /**
         * number of hard links<br>
         * C type : nlink_t
         */
        public short st_nlink;
        /// user ID of the file's owner
        public short st_uid;
        /// group ID of the file's group
        public short st_gid;
        /// device type
        public short st_rdev;
        /// file size, in bytes
        public int st_size;
        /**
         * time of last access<br>
         * C type : timespec
         */
        public timespec st_atimespec;
        /**
         * time of last data modification<br>
         * C type : timespec
         */
        public timespec st_mtimespec;
        /**
         * time of last file status change<br>
         * C type : timespec
         */
        public timespec st_ctimespec;
        /// optimal blocksize for I/O
        public int st_blksize;
        /// blocks allocated for file
        public int st_blocks;
        /// user defined flags for file
        public int st_flags;
        /// file generation number
        public int st_gen;

        public ostat() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"st_dev", "st_ino", "st_mode", "st_nlink", "st_uid", "st_gid", "st_rdev", "st_size", "st_atimespec", "st_mtimespec", "st_ctimespec", "st_blksize", "st_blocks", "st_flags", "st_gen"});
        }

        public static class ByReference extends ostat implements Structure.ByReference {

        }


        public static class ByValue extends ostat implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:226</i>
    public static class stat extends Structure {
        /**
         * [XSI] ID of device containing file<br>
         * C type : dev_t
         */
        public int st_dev;
        /**
         * [XSI] File serial number<br>
         * C type : ino_t
         */
        public int st_ino;
        /**
         * [XSI] Mode of file (see below)<br>
         * C type : mode_t
         */
        public short st_mode;
        /**
         * [XSI] Number of hard links<br>
         * C type : nlink_t
         */
        public short st_nlink;
        /**
         * [XSI] User ID of the file<br>
         * C type : uid_t
         */
        public int st_uid;
        /**
         * [XSI] Group ID of the file<br>
         * C type : gid_t
         */
        public int st_gid;
        /**
         * [XSI] Device ID<br>
         * C type : dev_t
         */
        public int st_rdev;
        /**
         * time of last access<br>
         * C type : timespec
         */
        public timespec st_atimespec;
        /**
         * time of last data modification<br>
         * C type : timespec
         */
        public timespec st_mtimespec;
        /**
         * time of last status change<br>
         * C type : timespec
         */
        public timespec st_ctimespec;
        /**
         * [XSI] file size, in bytes<br>
         * C type : off_t
         */
        public long st_size;
        /**
         * [XSI] blocks allocated for file<br>
         * C type : blkcnt_t
         */
        public long st_blocks;
        /**
         * [XSI] optimal blocksize for I/O<br>
         * C type : blksize_t
         */
        public int st_blksize;
        /// user defined flags for file
        public int st_flags;
        /// file generation number
        public int st_gen;
        /// RESERVED: DO NOT USE!
        public int st_lspare;
        /**
         * RESERVED: DO NOT USE!<br>
         * C type : __int64_t[2]
         */
        public long[] st_qspare = new long[(2)];

        public stat() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"st_dev", "st_ino", "st_mode", "st_nlink", "st_uid", "st_gid", "st_rdev", "st_atimespec", "st_mtimespec", "st_ctimespec", "st_size", "st_blocks", "st_blksize", "st_flags", "st_gen", "st_lspare", "st_qspare"});
        }

        public static class ByReference extends stat implements Structure.ByReference {

        }


        public static class ByValue extends stat implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:259</i>
    public static class stat64 extends Structure {
        /// C type : dev_t
        public int st_dev;
        /// C type : mode_t
        public short st_mode;
        /// C type : nlink_t
        public short st_nlink;
        /// C type : __darwin_ino64_t
        public long st_ino;
        /// C type : uid_t
        public int st_uid;
        /// C type : gid_t
        public int st_gid;
        /// C type : dev_t
        public int st_rdev;
        /// C type : timespec
        public timespec st_atimespec;
        /// C type : timespec
        public timespec st_mtimespec;
        /// C type : timespec
        public timespec st_ctimespec;
        /// C type : timespec
        public timespec st_birthtimespec;
        /// C type : off_t
        public long st_size;
        /// C type : blkcnt_t
        public long st_blocks;
        /// C type : blksize_t
        public int st_blksize;
        public int st_flags;
        public int st_gen;
        public int st_lspare;
        /// C type : __int64_t[2]
        public long[] st_qspare = new long[(2)];

        public stat64() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"st_dev", "st_mode", "st_nlink", "st_ino", "st_uid", "st_gid", "st_rdev", "st_atimespec", "st_mtimespec", "st_ctimespec", "st_birthtimespec", "st_size", "st_blocks", "st_blksize", "st_flags", "st_gen", "st_lspare", "st_qspare"});
        }

        public static class ByReference extends stat64 implements Structure.ByReference {

        }


        public static class ByValue extends stat64 implements Structure.ByValue {

        }


    }


    /**
     * \brief		<br>
     * \details<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:438</i>
     */
    public static class kfsoptions extends Structure {
        /// C type : const char*
        public String mountpoint;

        public kfsoptions() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"mountpoint"});
        }

        /// @param mountpoint C type : const char*
        public kfsoptions(String mountpoint) {
            super();
            this.mountpoint = mountpoint;
            initFieldOrder();
        }

        public static class ByReference extends kfsoptions implements Structure.ByReference {

        }


        public static class ByValue extends kfsoptions implements Structure.ByValue {

        }


    }


    /**
     * \brief		<br>
     * \details	<br>
     * Currently unsupported filesystem features:<br>
     * - No support for users/groups on files<br>
     * - No support for creating special file types<br>
     * - No support for hard links<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:450</i>
     */
    public static class kfsfilesystem extends Structure {
        /// C type : kfsstatfs_f
        public kfsstatfs_f statfs;
        /// C type : kfsstat_f
        public kfsstat_f stat;
        /// C type : kfsread_f
        public kfsread_f read;
        /// C type : kfswrite_f
        public kfswrite_f write;
        /// C type : kfssymlink_f
        public kfssymlink_f symlink;
        /// C type : kfsreadlink_f
        public kfsreadlink_f readlink;
        /// C type : kfscreate_f
        public kfscreate_f create;
        /// C type : kfsremove_f
        public kfsremove_f remove;
        /// C type : kfsrename_f
        public kfsrename_f rename;
        /// C type : kfstruncate_f
        public kfstruncate_f truncate;
        /// C type : kfschmod_f
        public kfschmod_f chmod;
        /// C type : kfsutimes_f
        public kfsutimes_f utimes;
        /// C type : kfsmkdir_f
        public kfsmkdir_f mkdir;
        /// C type : kfsrmdir_f
        public kfsrmdir_f rmdir;
        /// C type : kfsreaddir_f
        public kfsreaddir_f readdir;
        /// C type : kfsoptions_t
        public kfsoptions options;
        /// C type : void*
        public Pointer context;

        public kfsfilesystem() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"statfs", "stat", "read", "write", "symlink", "readlink", "create", "remove", "rename", "truncate", "chmod", "utimes", "mkdir", "rmdir", "readdir", "options", "context"});
        }

        public static class ByReference extends kfsfilesystem implements Structure.ByReference {

        }


        public static class ByValue extends kfsfilesystem implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:503</i>
    public static class kfstime extends Structure {
        public long sec;
        public long nsec;

        public kfstime() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"sec", "nsec"});
        }

        public kfstime(long sec, long nsec) {
            super();
            this.sec = sec;
            this.nsec = nsec;
            initFieldOrder();
        }

        public static class ByReference extends kfstime implements Structure.ByReference {

        }


        public static class ByValue extends kfstime implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:508</i>
    public static class kfsstat extends Structure {
        /// C type : kfstype_t
        public int type;
        /// C type : kfsmode_t
        public int mode;
        public long size;
        public long used;
        /// C type : kfstime_t
        public kfstime atime;
        /// C type : kfstime_t
        public kfstime mtime;
        /// C type : kfstime_t
        public kfstime ctime;

        public kfsstat() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"type", "mode", "size", "used", "atime", "mtime", "ctime"});
        }

        /**
         * @param type  C type : kfstype_t<br>
         * @param mode  C type : kfsmode_t<br>
         * @param atime C type : kfstime_t<br>
         * @param mtime C type : kfstime_t<br>
         * @param ctime C type : kfstime_t
         */
        public kfsstat(int type, int mode, long size, long used, kfstime atime, kfstime mtime, kfstime ctime) {
            super();
            this.type = type;
            this.mode = mode;
            this.size = size;
            this.used = used;
            this.atime = atime;
            this.mtime = mtime;
            this.ctime = ctime;
            initFieldOrder();
        }

        public static class ByReference extends kfsstat implements Structure.ByReference {

        }


        public static class ByValue extends kfsstat implements Structure.ByValue {

        }


    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:518</i>
    public static class kfsstatfs extends Structure {
        public long free;
        public long size;

        public kfsstatfs() {
            super();
            initFieldOrder();
        }

        protected void initFieldOrder() {
            setFieldOrder(new java.lang.String[]{"free", "size"});
        }

        public kfsstatfs(long free, long size) {
            super();
            this.free = free;
            this.size = size;
            initFieldOrder();
        }

        public static class ByReference extends kfsstatfs implements Structure.ByReference {

        }


        public static class ByValue extends kfsstatfs implements Structure.ByValue {

        }


    }


    /**
     * \brief		Stat a filesystem<br>
     * \details	Get statistics from the filesystem located at path. Set all attributes you<br>
     * support in the stat structure.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsstatfs_f extends Callback {
        boolean apply(String path, kfsstatfs stat, Pointer context);
    }


    /**
     * \brief		Stat a file<br>
     * \details	Get statistics from the file located at path. Set all attributes you<br>
     * support in the stat structure. This should not follow symbolic links.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsstat_f extends Callback {
        boolean apply(String path, kfsstat stat, Pointer context);
    }


    /**
     * \brief		Write to a file<br>
     * \details	Write length bytes to the file at path starting at offset. Write the data from the buffer which<br>
     * is guarenteed to be large enough to hold length bytes. Return -1 on error.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfswrite_f extends Callback {
        size_t apply(String path, Pointer buf, size_t offset, size_t length, Pointer context);
    }

    /**
     * \brief		Read from a file<br>
     * \details	Read length bytes from the file at path starting at offset. Read the data into the buffer which<br>
     * is guarenteed to be large enough to hold length bytes. Return -1 on error.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsread_f extends Callback {
        size_t apply(String path, Pointer buf, size_t offset, size_t length, Pointer context);
    }

    /**
     * \brief		Create a symbolic link<br>
     * \details	Create a link at path with the given value.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfssymlink_f extends Callback {
        boolean apply(String path, String value, Pointer context);
    }

    /**
     * \brief		Read the contents of a link<br>
     * \details	Get the contents of the link at path, and return by reference a newly created<br>
     * string in value (you should allocate memeory for this). The caller will free<br>
     * the memory you allocate. This is not your responsibility.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsreadlink_f extends Callback {
        boolean apply(String path, PointerByReference value, Pointer context);
    }

    /**
     * \brief		Create a file<br>
     * \details	Create a file at the given path.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfscreate_f extends Callback {
        boolean apply(String path, Pointer context);
    }


    /**
     * \brief		Remove a file<br>
     * \details	Remove a file at the given path.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsremove_f extends Callback {
        boolean apply(String path, Pointer context);
    }


    /**
     * \brief		Move a file<br>
     * \details	Move a file at the given path to the new path.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsrename_f extends Callback {
        boolean apply(String path, String new_path, Pointer context);
    }


    /**
     * \brief		Resize a file<br>
     * \details	Resize the file to the given size.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfstruncate_f extends Callback {
        boolean apply(String path, long size, Pointer context);
    }


    /**
     * \brief		Change mode for a file<br>
     * \details	Change to the specified mode.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfschmod_f extends Callback {
        boolean apply(String path, int mode, Pointer context);
    }


    /**
     * \brief		Change times for a file<br>
     * \details	Change the access and modification times of a file. If a time should be set,<br>
     * it will be non-null.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsutimes_f extends Callback {
        boolean apply(String path, kfstime atime, kfstime mtime, Pointer context);
    }


    /**
     * \brief		Create a directory<br>
     * \details	Create a directory at the given path.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsmkdir_f extends Callback {
        boolean apply(String path, Pointer context);
    }


    /**
     * \brief		Remove a directory<br>
     * \details	Remove a directory at the given path.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsrmdir_f extends Callback {
        boolean apply(String path, Pointer context);
    }


    /**
     * \brief		Get a directory's contents<br>
     * \details	Get the contents of the directory at path. Add file entries to the contents by calling<br>
     * kfscontents_append. Values are copied so you do not need to keep them in memory.<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h</i>
     */
    public interface kfsreaddir_f extends Callback {
        boolean apply(String path, Pointer contents, Pointer context);
    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:566</i>
    public interface kfs_set_thread_begin_callback_arg1_callback extends Callback {
        void apply();
    }


    /// <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:567</i>
    public interface kfs_set_thread_end_callback_arg1_callback extends Callback {
        void apply();
    }


    /**
     * Original signature : <code>void _exit(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:415</i>
     */
    void _exit(int int1);

    /**
     * Original signature : <code>int access(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:416</i><br>
     *
     * @deprecated use the safer methods {@link #access(java.lang.String, int)} and {@link #access(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int access(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int access(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:416</i>
     */
    int access(String charPtr1, int int1);

    /**
     * Original signature : <code>int alarm(unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:417</i>
     */
    int alarm(int int1);

    /**
     * Original signature : <code>int chdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:419</i><br>
     *
     * @deprecated use the safer methods {@link #chdir(java.lang.String)} and {@link #chdir(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int chdir(Pointer charPtr1);

    /**
     * Original signature : <code>int chdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:419</i>
     */
    int chdir(String charPtr1);

    /**
     * Original signature : <code>int chown(const char*, uid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:420</i><br>
     *
     * @deprecated use the safer methods {@link #chown(java.lang.String, int, int)} and {@link #chown(com.sun.jna.Pointer, int, int)} instead
     */
    @Deprecated
    int chown(Pointer charPtr1, int uid_t1, int gid_t1);

    /**
     * Original signature : <code>int chown(const char*, uid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:420</i>
     */
    int chown(String charPtr1, int uid_t1, int gid_t1);

    /**
     * Original signature : <code>int close(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:421</i>
     */
    int close(int int1);

    /**
     * Original signature : <code>size_t confstr(int, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:422</i><br>
     *
     * @deprecated use the safer methods {@link #confstr(int, java.nio.ByteBuffer, size_t)} and {@link #confstr(int, com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    size_t confstr(int int1, Pointer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>size_t confstr(int, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:422</i>
     */
    size_t confstr(int int1, ByteBuffer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>char* crypt(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:423</i><br>
     *
     * @deprecated use the safer methods {@link #crypt(java.lang.String, java.lang.String)} and {@link #crypt(com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer crypt(Pointer charPtr1, Pointer charPtr2);

    /**
     * Original signature : <code>char* crypt(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:423</i>
     */
    Pointer crypt(String charPtr1, String charPtr2);

    /**
     * Original signature : <code>char* ctermid(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:424</i><br>
     *
     * @deprecated use the safer methods {@link #ctermid(java.nio.ByteBuffer)} and {@link #ctermid(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer ctermid(Pointer charPtr1);

    /**
     * Original signature : <code>char* ctermid(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:424</i>
     */
    Pointer ctermid(ByteBuffer charPtr1);

    /**
     * Original signature : <code>int dup(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:425</i>
     */
    int dup(int int1);

    /**
     * Original signature : <code>int dup2(int, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:426</i>
     */
    int dup2(int int1, int int2);

    /**
     * Original signature : <code>void encrypt(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:428</i><br>
     *
     * @deprecated use the safer methods {@link #encrypt(java.nio.ByteBuffer, int)} and {@link #encrypt(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    void encrypt(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>void encrypt(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:428</i>
     */
    void encrypt(ByteBuffer charPtr1, int int1);

    /**
     * Original signature : <code>int execl(const char*, const char*, null)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:432</i>
     */
    int execl(String charPtr1, String charPtr2, Object... varargs);

    /**
     * Original signature : <code>int execle(const char*, const char*, null)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:433</i>
     */
    int execle(String charPtr1, String charPtr2, Object... varargs);

    /**
     * Original signature : <code>int execlp(const char*, const char*, null)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:434</i>
     */
    int execlp(String charPtr1, String charPtr2, Object... varargs);

    /**
     * Original signature : <code>int execv(const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:435</i><br>
     *
     * @deprecated use the safer methods {@link #execv(java.lang.String, java.lang.String[])} and {@link #execv(com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference)} instead
     */
    @Deprecated
    int execv(Pointer charPtr1, PointerByReference charPtrPtr1);

    /**
     * Original signature : <code>int execv(const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:435</i>
     */
    int execv(String charPtr1, String charPtrPtr1[]);

    /**
     * Original signature : <code>int execve(const char*, const char**, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:436</i><br>
     *
     * @deprecated use the safer methods {@link #execve(java.lang.String, java.lang.String[], java.lang.String[])} and {@link #execve(com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference, com.sun.jna.ptr.PointerByReference)} instead
     */
    @Deprecated
    int execve(Pointer charPtr1, PointerByReference charPtrPtr1, PointerByReference charPtrPtr2);

    /**
     * Original signature : <code>int execve(const char*, const char**, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:436</i>
     */
    int execve(String charPtr1, String charPtrPtr1[], String charPtrPtr2[]);

    /**
     * Original signature : <code>int execvp(const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:437</i><br>
     *
     * @deprecated use the safer methods {@link #execvp(java.lang.String, java.lang.String[])} and {@link #execvp(com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference)} instead
     */
    @Deprecated
    int execvp(Pointer charPtr1, PointerByReference charPtrPtr1);

    /**
     * Original signature : <code>int execvp(const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:437</i>
     */
    int execvp(String charPtr1, String charPtrPtr1[]);

    /**
     * Original signature : <code>int fchown(int, uid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:438</i>
     */
    int fchown(int int1, int uid_t1, int gid_t1);

    /**
     * Original signature : <code>int fchdir(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:439</i>
     */
    int fchdir(int int1);

    /**
     * Original signature : <code>pid_t fork()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:440</i>
     */
    int fork();

    /**
     * Original signature : <code>fpathconf(int, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:441</i>
     */
    int fpathconf(int int1, int int2);

    /**
     * Original signature : <code>int fsync(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:442</i>
     */
    int fsync(int int1);

    /**
     * Original signature : <code>int ftruncate(int, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:443</i>
     */
    int ftruncate(int int1, long off_t1);

    /**
     * Original signature : <code>char* getcwd(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:444</i><br>
     *
     * @deprecated use the safer methods {@link #getcwd(java.nio.ByteBuffer, size_t)} and {@link #getcwd(com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    Pointer getcwd(Pointer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>char* getcwd(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:444</i>
     */
    Pointer getcwd(ByteBuffer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>gid_t getegid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:445</i>
     */
    int getegid();

    /**
     * Original signature : <code>uid_t geteuid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:446</i>
     */
    int geteuid();

    /**
     * Original signature : <code>gid_t getgid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:447</i>
     */
    int getgid();

    /**
     * Original signature : <code>int getgroups(int, gid_t[])</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:448</i><br>
     *
     * @deprecated use the safer methods {@link #getgroups(int, java.nio.IntBuffer)} and {@link #getgroups(int, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int getgroups(int int1, IntByReference gid_tArr1);

    /**
     * Original signature : <code>int getgroups(int, gid_t[])</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:448</i>
     */
    int getgroups(int int1, IntBuffer gid_tArr1);

    /**
     * Original signature : <code>gethostid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:449</i>
     */
    int gethostid();

    /**
     * Original signature : <code>int gethostname(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:450</i><br>
     *
     * @deprecated use the safer methods {@link #gethostname(java.nio.ByteBuffer, size_t)} and {@link #gethostname(com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    int gethostname(Pointer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>int gethostname(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:450</i>
     */
    int gethostname(ByteBuffer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>char* getlogin()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:451</i>
     */
    Pointer getlogin();

    /**
     * Original signature : <code>int getlogin_r(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:452</i><br>
     *
     * @deprecated use the safer methods {@link #getlogin_r(java.nio.ByteBuffer, size_t)} and {@link #getlogin_r(com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    int getlogin_r(Pointer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>int getlogin_r(char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:452</i>
     */
    int getlogin_r(ByteBuffer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>pid_t getpgid(pid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:454</i>
     */
    int getpgid(int pid_t1);

    /**
     * Original signature : <code>pid_t getpgrp()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:455</i>
     */
    int getpgrp();

    /**
     * Original signature : <code>pid_t getpid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:456</i>
     */
    int getpid();

    /**
     * Original signature : <code>pid_t getppid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:457</i>
     */
    int getppid();

    /**
     * Original signature : <code>pid_t getsid(pid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:458</i>
     */
    int getsid(int pid_t1);

    /**
     * Original signature : <code>uid_t getuid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:459</i>
     */
    int getuid();

    /**
     * obsoleted by getcwd()<br>
     * Original signature : <code>char* getwd(char*)</code><br>
     *
     * @param charPtr1 obsoleted by getcwd()<br>
     *                 <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:460</i><br>
     * @deprecated use the safer methods {@link #getwd(java.nio.ByteBuffer)} and {@link #getwd(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer getwd(Pointer charPtr1);

    /**
     * obsoleted by getcwd()<br>
     * Original signature : <code>char* getwd(char*)</code><br>
     *
     * @param charPtr1 obsoleted by getcwd()<br>
     *                 <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:460</i>
     */
    Pointer getwd(ByteBuffer charPtr1);

    /**
     * Original signature : <code>int isatty(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:461</i>
     */
    int isatty(int int1);

    /**
     * Original signature : <code>int lchown(const char*, uid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:462</i><br>
     *
     * @deprecated use the safer methods {@link #lchown(java.lang.String, int, int)} and {@link #lchown(com.sun.jna.Pointer, int, int)} instead
     */
    @Deprecated
    int lchown(Pointer charPtr1, int uid_t1, int gid_t1);

    /**
     * Original signature : <code>int lchown(const char*, uid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:462</i>
     */
    int lchown(String charPtr1, int uid_t1, int gid_t1);

    /**
     * Original signature : <code>int link(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:463</i><br>
     *
     * @deprecated use the safer methods {@link #link(java.lang.String, java.lang.String)} and {@link #link(com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int link(Pointer charPtr1, Pointer charPtr2);

    /**
     * Original signature : <code>int link(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:463</i>
     */
    int link(String charPtr1, String charPtr2);

    /**
     * Original signature : <code>int lockf(int, int, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:464</i>
     */
    int lockf(int int1, int int2, long off_t1);

    /**
     * Original signature : <code>off_t lseek(int, off_t, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:465</i>
     */
    long lseek(int int1, long off_t1, int int2);

    /**
     * Original signature : <code>int nice(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:466</i>
     */
    int nice(int int1);

    /**
     * Original signature : <code>pathconf(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:467</i><br>
     *
     * @deprecated use the safer methods {@link #pathconf(java.lang.String, int)} and {@link #pathconf(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int pathconf(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>pathconf(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:467</i>
     */
    int pathconf(String charPtr1, int int1);

    /**
     * Original signature : <code>int pause()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:468</i>
     */
    int pause();

    /**
     * Original signature : <code>ssize_t pread(int, void*, size_t, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:470</i>
     */
    size_t pread(int int1, Pointer voidPtr1, size_t size_t1, long off_t1);

    /**
     * Original signature : <code>ssize_t pwrite(int, const void*, size_t, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:471</i>
     */
    size_t pwrite(int int1, Pointer voidPtr1, size_t size_t1, long off_t1);

    /**
     * Original signature : <code>ssize_t read(int, void*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:472</i>
     */
    size_t read(int int1, Pointer voidPtr1, size_t size_t1);

    /**
     * Original signature : <code>ssize_t readlink(const char*, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:473</i><br>
     *
     * @deprecated use the safer methods {@link #readlink(java.lang.String, java.nio.ByteBuffer, size_t)} and {@link #readlink(com.sun.jna.Pointer, com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    size_t readlink(Pointer charPtr1, Pointer charPtr2, size_t size_t1);

    /**
     * Original signature : <code>ssize_t readlink(const char*, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:473</i>
     */
    size_t readlink(String charPtr1, ByteBuffer charPtr2, size_t size_t1);

    /**
     * Original signature : <code>int rmdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:474</i><br>
     *
     * @deprecated use the safer methods {@link #rmdir(java.lang.String)} and {@link #rmdir(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int rmdir(Pointer charPtr1);

    /**
     * Original signature : <code>int rmdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:474</i>
     */
    int rmdir(String charPtr1);

    /**
     * Original signature : <code>int setegid(gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:475</i>
     */
    int setegid(int gid_t1);

    /**
     * Original signature : <code>int seteuid(uid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:476</i>
     */
    int seteuid(int uid_t1);

    /**
     * Original signature : <code>int setgid(gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:477</i>
     */
    int setgid(int gid_t1);

    /**
     * Original signature : <code>int setpgid(pid_t, pid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:478</i>
     */
    int setpgid(int pid_t1, int pid_t2);

    /**
     * Original signature : <code>pid_t setpgrp()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:480</i>
     */
    int setpgrp();

    /**
     * Original signature : <code>int setregid(gid_t, gid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:484</i>
     */
    int setregid(int gid_t1, int gid_t2);

    /**
     * Original signature : <code>int setreuid(uid_t, uid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:485</i>
     */
    int setreuid(int uid_t1, int uid_t2);

    /**
     * Original signature : <code>pid_t setsid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:486</i>
     */
    int setsid();

    /**
     * Original signature : <code>int setuid(uid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:487</i>
     */
    int setuid(int uid_t1);

    /**
     * Original signature : <code>int sleep(unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:488</i>
     */
    int sleep(int int1);

    /**
     * Original signature : <code>void swab(const void*, void*, ssize_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:490</i>
     */
    void swab(Pointer voidPtr1, Pointer voidPtr2, size_t ssize_t1);

    /**
     * Original signature : <code>int symlink(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:491</i><br>
     *
     * @deprecated use the safer methods {@link #symlink(java.lang.String, java.lang.String)} and {@link #symlink(com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int symlink(Pointer charPtr1, Pointer charPtr2);

    /**
     * Original signature : <code>int symlink(const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:491</i>
     */
    int symlink(String charPtr1, String charPtr2);

    /**
     * Original signature : <code>void sync()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:492</i>
     */
    void sync();

    /**
     * Original signature : <code>sysconf(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:493</i>
     */
    int sysconf(int int1);

    /**
     * Original signature : <code>pid_t tcgetpgrp(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:494</i>
     */
    int tcgetpgrp(int int1);

    /**
     * Original signature : <code>int tcsetpgrp(int, pid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:495</i>
     */
    int tcsetpgrp(int int1, int pid_t1);

    /**
     * Original signature : <code>int truncate(const char*, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:496</i><br>
     *
     * @deprecated use the safer methods {@link #truncate(java.lang.String, long)} and {@link #truncate(com.sun.jna.Pointer, long)} instead
     */
    @Deprecated
    int truncate(Pointer charPtr1, long off_t1);

    /**
     * Original signature : <code>int truncate(const char*, off_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:496</i>
     */
    int truncate(String charPtr1, long off_t1);

    /**
     * Original signature : <code>char* ttyname(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:497</i>
     */
    Pointer ttyname(int int1);

    /**
     * Original signature : <code>int ttyname_r(int, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:499</i><br>
     *
     * @deprecated use the safer methods {@link #ttyname_r(int, java.nio.ByteBuffer, size_t)} and {@link #ttyname_r(int, com.sun.jna.Pointer, size_t)} instead
     */
    @Deprecated
    int ttyname_r(int int1, Pointer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>int ttyname_r(int, char*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:499</i>
     */
    int ttyname_r(int int1, ByteBuffer charPtr1, size_t size_t1);

    /**
     * Original signature : <code>useconds_t ualarm(useconds_t, useconds_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:503</i>
     */
    int ualarm(int useconds_t1, int useconds_t2);

    /**
     * Original signature : <code>int unlink(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:505</i><br>
     *
     * @deprecated use the safer methods {@link #unlink(java.lang.String)} and {@link #unlink(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int unlink(Pointer charPtr1);

    /**
     * Original signature : <code>int unlink(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:505</i>
     */
    int unlink(String charPtr1);

    /**
     * Original signature : <code>int usleep(useconds_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:506</i>
     */
    int usleep(int useconds_t1);

    /**
     * Original signature : <code>pid_t vfork()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:507</i>
     */
    int vfork();

    /**
     * Original signature : <code>ssize_t write(int, const void*, size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:508</i>
     */
    size_t write(int int1, Pointer voidPtr1, size_t size_t1);

    /**
     * Original signature : <code>int pselect(int, fd_set*, fd_set*, fd_set*, timespec*, const sigset_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/select.h:133</i><br>
     *
     * @deprecated use the safer methods {@link #pselect(int, KfsLibrary.fd_set, KfsLibrary.fd_set, KfsLibrary.fd_set, KfsLibrary.timespec, java.nio.IntBuffer)} and {@link #pselect(int, KfsLibrary.fd_set, KfsLibrary.fd_set, KfsLibrary.fd_set, KfsLibrary.timespec, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int pselect(int int1, fd_set fd_setPtr1, fd_set fd_setPtr2, fd_set fd_setPtr3, timespec timespecPtr1, IntByReference sigset_tPtr1);

    /**
     * Original signature : <code>int pselect(int, fd_set*, fd_set*, fd_set*, timespec*, const sigset_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/select.h:133</i>
     */
    int pselect(int int1, fd_set fd_setPtr1, fd_set fd_setPtr2, fd_set fd_setPtr3, timespec timespecPtr1, IntBuffer sigset_tPtr1);

    /**
     * Original signature : <code>int select(int, fd_set*, fd_set*, fd_set*, timeval*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_select.h:39</i>
     */
    int select(int int1, fd_set fd_setPtr1, fd_set fd_setPtr2, fd_set fd_setPtr3, timeval timevalPtr1);

    /**
     * Original signature : <code>void _Exit(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:303</i>
     */
    void _Exit(int int1);

    /**
     * Original signature : <code>int accessx_np(accessx_descriptor*, size_t, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:304</i><br>
     *
     * @deprecated use the safer methods {@link #accessx_np(KfsLibrary.accessx_descriptor, size_t, java.nio.IntBuffer)} and {@link #accessx_np(KfsLibrary.accessx_descriptor, size_t, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int accessx_np(accessx_descriptor accessx_descriptorPtr1, size_t size_t1, IntByReference intPtr1);

    /**
     * Original signature : <code>int accessx_np(accessx_descriptor*, size_t, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:304</i>
     */
    int accessx_np(accessx_descriptor accessx_descriptorPtr1, size_t size_t1, IntBuffer intPtr1);

    /**
     * Original signature : <code>int acct(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:305</i><br>
     *
     * @deprecated use the safer methods {@link #acct(java.lang.String)} and {@link #acct(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int acct(Pointer charPtr1);

    /**
     * Original signature : <code>int acct(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:305</i>
     */
    int acct(String charPtr1);

    /**
     * Original signature : <code>int add_profil(char*, size_t, unsigned long, unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:306</i><br>
     *
     * @deprecated use the safer methods {@link #add_profil(java.nio.ByteBuffer, size_t, com.sun.jna.NativeLong, int)} and {@link #add_profil(com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong, int)} instead
     */
    @Deprecated
    int add_profil(Pointer charPtr1, size_t size_t1, NativeLong u1, int int1);

    /**
     * Original signature : <code>int add_profil(char*, size_t, unsigned long, unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:306</i>
     */
    int add_profil(ByteBuffer charPtr1, size_t size_t1, NativeLong u1, int int1);

    /**
     * Original signature : <code>void* brk(const void*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:307</i>
     */
    Pointer brk(Pointer voidPtr1);

    /**
     * Original signature : <code>int chroot(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:308</i><br>
     *
     * @deprecated use the safer methods {@link #chroot(java.lang.String)} and {@link #chroot(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int chroot(Pointer charPtr1);

    /**
     * Original signature : <code>int chroot(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:308</i>
     */
    int chroot(String charPtr1);

    /**
     * Original signature : <code>void endusershell()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:309</i>
     */
    void endusershell();

    /**
     * Original signature : <code>int execvP(const char*, const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:310</i><br>
     *
     * @deprecated use the safer methods {@link #execvP(java.lang.String, java.lang.String, java.lang.String[])} and {@link #execvP(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.ptr.PointerByReference)} instead
     */
    @Deprecated
    int execvP(Pointer charPtr1, Pointer charPtr2, PointerByReference charPtrPtr1);

    /**
     * Original signature : <code>int execvP(const char*, const char*, const char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:310</i>
     */
    int execvP(String charPtr1, String charPtr2, String charPtrPtr1[]);

    /**
     * Original signature : <code>char* fflagstostr(unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:311</i>
     */
    Pointer fflagstostr(NativeLong u1);

    /**
     * Original signature : <code>int getdtablesize()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:312</i>
     */
    int getdtablesize();

    /**
     * Original signature : <code>int getdomainname(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:313</i><br>
     *
     * @deprecated use the safer methods {@link #getdomainname(java.nio.ByteBuffer, int)} and {@link #getdomainname(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int getdomainname(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int getdomainname(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:313</i>
     */
    int getdomainname(ByteBuffer charPtr1, int int1);

    /**
     * Original signature : <code>int getgrouplist(const char*, int, int*, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:314</i><br>
     *
     * @deprecated use the safer methods {@link #getgrouplist(java.lang.String, int, java.nio.IntBuffer, java.nio.IntBuffer)} and {@link #getgrouplist(com.sun.jna.Pointer, int, com.sun.jna.ptr.IntByReference, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int getgrouplist(Pointer charPtr1, int int1, IntByReference intPtr1, IntByReference intPtr2);

    /**
     * Original signature : <code>int getgrouplist(const char*, int, int*, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:314</i>
     */
    int getgrouplist(String charPtr1, int int1, IntBuffer intPtr1, IntBuffer intPtr2);

    /**
     * Original signature : <code>mode_t getmode(const void*, mode_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:315</i>
     */
    short getmode(Pointer voidPtr1, short mode_t1);

    /**
     * Original signature : <code>int getpagesize()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:316</i>
     */
    int getpagesize();

    /**
     * Original signature : <code>char* getpass(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:317</i><br>
     *
     * @deprecated use the safer methods {@link #getpass(java.lang.String)} and {@link #getpass(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer getpass(Pointer charPtr1);

    /**
     * Original signature : <code>char* getpass(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:317</i>
     */
    Pointer getpass(String charPtr1);

    /**
     * Original signature : <code>int getpeereid(int, uid_t*, gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:318</i><br>
     *
     * @deprecated use the safer methods {@link #getpeereid(int, java.nio.IntBuffer, java.nio.IntBuffer)} and {@link #getpeereid(int, com.sun.jna.ptr.IntByReference, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int getpeereid(int int1, IntByReference uid_tPtr1, IntByReference gid_tPtr1);

    /**
     * Original signature : <code>int getpeereid(int, uid_t*, gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:318</i>
     */
    int getpeereid(int int1, IntBuffer uid_tPtr1, IntBuffer gid_tPtr1);

    /**
     * Original signature : <code>int getsgroups_np(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:320</i><br>
     *
     * @deprecated use the safer methods {@link #getsgroups_np(java.nio.IntBuffer)} and {@link #getsgroups_np(com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int getsgroups_np(IntByReference intPtr1);

    /**
     * Original signature : <code>int getsgroups_np(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:320</i>
     */
    int getsgroups_np(IntBuffer intPtr1);

    /**
     * Original signature : <code>char* getusershell()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:322</i>
     */
    Pointer getusershell();

    /**
     * Original signature : <code>int getwgroups_np(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:323</i><br>
     *
     * @deprecated use the safer methods {@link #getwgroups_np(java.nio.IntBuffer)} and {@link #getwgroups_np(com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int getwgroups_np(IntByReference intPtr1);

    /**
     * Original signature : <code>int getwgroups_np(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:323</i>
     */
    int getwgroups_np(IntBuffer intPtr1);

    /**
     * Original signature : <code>int initgroups(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:324</i><br>
     *
     * @deprecated use the safer methods {@link #initgroups(java.lang.String, int)} and {@link #initgroups(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int initgroups(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int initgroups(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:324</i>
     */
    int initgroups(String charPtr1, int int1);

    /**
     * Original signature : <code>int iruserok(unsigned long, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:325</i><br>
     *
     * @deprecated use the safer methods {@link #iruserok(com.sun.jna.NativeLong, int, java.lang.String, java.lang.String)} and {@link #iruserok(com.sun.jna.NativeLong, int, com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int iruserok(NativeLong u1, int int1, Pointer charPtr1, Pointer charPtr2);

    /**
     * Original signature : <code>int iruserok(unsigned long, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:325</i>
     */
    int iruserok(NativeLong u1, int int1, String charPtr1, String charPtr2);

    /**
     * Original signature : <code>int iruserok_sa(const void*, int, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:326</i><br>
     *
     * @deprecated use the safer methods {@link #iruserok_sa(com.sun.jna.Pointer, int, int, java.lang.String, java.lang.String)} and {@link #iruserok_sa(com.sun.jna.Pointer, int, int, com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int iruserok_sa(Pointer voidPtr1, int int1, int int2, Pointer charPtr1, Pointer charPtr2);

    /**
     * Original signature : <code>int iruserok_sa(const void*, int, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:326</i>
     */
    int iruserok_sa(Pointer voidPtr1, int int1, int int2, String charPtr1, String charPtr2);

    /**
     * Original signature : <code>int issetugid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:327</i>
     */
    int issetugid();

    /**
     * Original signature : <code>char* mkdtemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:328</i><br>
     *
     * @deprecated use the safer methods {@link #mkdtemp(java.nio.ByteBuffer)} and {@link #mkdtemp(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer mkdtemp(Pointer charPtr1);

    /**
     * Original signature : <code>char* mkdtemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:328</i>
     */
    Pointer mkdtemp(ByteBuffer charPtr1);

    /**
     * Original signature : <code>int mknod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:329</i><br>
     *
     * @deprecated use the safer methods {@link #mknod(java.lang.String)} and {@link #mknod(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int mknod(Pointer charPtr1);

    /**
     * Original signature : <code>int mknod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:329</i>
     */
    int mknod(String charPtr1);

    /**
     * Original signature : <code>int mkstemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:330</i><br>
     *
     * @deprecated use the safer methods {@link #mkstemp(java.nio.ByteBuffer)} and {@link #mkstemp(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int mkstemp(Pointer charPtr1);

    /**
     * Original signature : <code>int mkstemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:330</i>
     */
    int mkstemp(ByteBuffer charPtr1);

    /**
     * Original signature : <code>int mkstemps(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:331</i><br>
     *
     * @deprecated use the safer methods {@link #mkstemps(java.nio.ByteBuffer, int)} and {@link #mkstemps(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int mkstemps(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int mkstemps(char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:331</i>
     */
    int mkstemps(ByteBuffer charPtr1, int int1);

    /**
     * Original signature : <code>char* mktemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:332</i><br>
     *
     * @deprecated use the safer methods {@link #mktemp(java.nio.ByteBuffer)} and {@link #mktemp(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer mktemp(Pointer charPtr1);

    /**
     * Original signature : <code>char* mktemp(char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:332</i>
     */
    Pointer mktemp(ByteBuffer charPtr1);

    /**
     * Original signature : <code>int nfssvc(int, void*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:333</i>
     */
    int nfssvc(int int1, Pointer voidPtr1);

    /**
     * Original signature : <code>int profil(char*, size_t, unsigned long, unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:334</i><br>
     *
     * @deprecated use the safer methods {@link #profil(java.nio.ByteBuffer, size_t, com.sun.jna.NativeLong, int)} and {@link #profil(com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong, int)} instead
     */
    @Deprecated
    int profil(Pointer charPtr1, size_t size_t1, NativeLong u1, int int1);

    /**
     * Original signature : <code>int profil(char*, size_t, unsigned long, unsigned int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:334</i>
     */
    int profil(ByteBuffer charPtr1, size_t size_t1, NativeLong u1, int int1);

    /**
     * Original signature : <code>int pthread_setugid_np()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:335</i>
     */
    int pthread_setugid_np();

    /**
     * Original signature : <code>int pthread_getugid_np(uid_t*, gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:336</i><br>
     *
     * @deprecated use the safer methods {@link #pthread_getugid_np(java.nio.IntBuffer, java.nio.IntBuffer)} and {@link #pthread_getugid_np(com.sun.jna.ptr.IntByReference, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int pthread_getugid_np(IntByReference uid_tPtr1, IntByReference gid_tPtr1);

    /**
     * Original signature : <code>int pthread_getugid_np(uid_t*, gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:336</i>
     */
    int pthread_getugid_np(IntBuffer uid_tPtr1, IntBuffer gid_tPtr1);

    /**
     * Original signature : <code>int rcmd(char**, int, const char*, const char*, const char*, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:337</i><br>
     *
     * @deprecated use the safer methods {@link #rcmd(com.sun.jna.ptr.PointerByReference, int, java.lang.String, java.lang.String, java.lang.String, java.nio.IntBuffer)} and {@link #rcmd(com.sun.jna.ptr.PointerByReference, int, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int rcmd(PointerByReference charPtrPtr1, int int1, Pointer charPtr1, Pointer charPtr2, Pointer charPtr3, IntByReference intPtr1);

    /**
     * Original signature : <code>int rcmd(char**, int, const char*, const char*, const char*, int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:337</i>
     */
    int rcmd(PointerByReference charPtrPtr1, int int1, String charPtr1, String charPtr2, String charPtr3, IntBuffer intPtr1);

    /**
     * Original signature : <code>int rcmd_af(char**, int, const char*, const char*, const char*, int*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:338</i><br>
     *
     * @deprecated use the safer methods {@link #rcmd_af(com.sun.jna.ptr.PointerByReference, int, java.lang.String, java.lang.String, java.lang.String, java.nio.IntBuffer, int)} and {@link #rcmd_af(com.sun.jna.ptr.PointerByReference, int, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.ptr.IntByReference, int)} instead
     */
    @Deprecated
    int rcmd_af(PointerByReference charPtrPtr1, int int1, Pointer charPtr1, Pointer charPtr2, Pointer charPtr3, IntByReference intPtr1, int int2);

    /**
     * Original signature : <code>int rcmd_af(char**, int, const char*, const char*, const char*, int*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:338</i>
     */
    int rcmd_af(PointerByReference charPtrPtr1, int int1, String charPtr1, String charPtr2, String charPtr3, IntBuffer intPtr1, int int2);

    /**
     * Original signature : <code>int reboot(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:340</i>
     */
    int reboot(int int1);

    /**
     * Original signature : <code>int revoke(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:341</i><br>
     *
     * @deprecated use the safer methods {@link #revoke(java.lang.String)} and {@link #revoke(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int revoke(Pointer charPtr1);

    /**
     * Original signature : <code>int revoke(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:341</i>
     */
    int revoke(String charPtr1);

    /**
     * Original signature : <code>int rresvport(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:342</i><br>
     *
     * @deprecated use the safer methods {@link #rresvport(java.nio.IntBuffer)} and {@link #rresvport(com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int rresvport(IntByReference intPtr1);

    /**
     * Original signature : <code>int rresvport(int*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:342</i>
     */
    int rresvport(IntBuffer intPtr1);

    /**
     * Original signature : <code>int rresvport_af(int*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:343</i><br>
     *
     * @deprecated use the safer methods {@link #rresvport_af(java.nio.IntBuffer, int)} and {@link #rresvport_af(com.sun.jna.ptr.IntByReference, int)} instead
     */
    @Deprecated
    int rresvport_af(IntByReference intPtr1, int int1);

    /**
     * Original signature : <code>int rresvport_af(int*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:343</i>
     */
    int rresvport_af(IntBuffer intPtr1, int int1);

    /**
     * Original signature : <code>int ruserok(const char*, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:344</i><br>
     *
     * @deprecated use the safer methods {@link #ruserok(java.lang.String, int, java.lang.String, java.lang.String)} and {@link #ruserok(com.sun.jna.Pointer, int, com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int ruserok(Pointer charPtr1, int int1, Pointer charPtr2, Pointer charPtr3);

    /**
     * Original signature : <code>int ruserok(const char*, int, const char*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:344</i>
     */
    int ruserok(String charPtr1, int int1, String charPtr2, String charPtr3);

    /**
     * Original signature : <code>void* sbrk(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:345</i>
     */
    Pointer sbrk(int int1);

    /**
     * Original signature : <code>int setdomainname(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:346</i><br>
     *
     * @deprecated use the safer methods {@link #setdomainname(java.lang.String, int)} and {@link #setdomainname(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int setdomainname(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int setdomainname(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:346</i>
     */
    int setdomainname(String charPtr1, int int1);

    /**
     * Original signature : <code>int setgroups(int, const gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:347</i><br>
     *
     * @deprecated use the safer methods {@link #setgroups(int, java.nio.IntBuffer)} and {@link #setgroups(int, com.sun.jna.ptr.IntByReference)} instead
     */
    @Deprecated
    int setgroups(int int1, IntByReference gid_tPtr1);

    /**
     * Original signature : <code>int setgroups(int, const gid_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:347</i>
     */
    int setgroups(int int1, IntBuffer gid_tPtr1);

    /**
     * Original signature : <code>void sethostid(long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:348</i>
     */
    void sethostid(NativeLong l1);

    /**
     * Original signature : <code>int sethostname(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:349</i><br>
     *
     * @deprecated use the safer methods {@link #sethostname(java.lang.String, int)} and {@link #sethostname(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int sethostname(Pointer charPtr1, int int1);

    /**
     * Original signature : <code>int sethostname(const char*, int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:349</i>
     */
    int sethostname(String charPtr1, int int1);

    /**
     * Original signature : <code>void setkey(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:351</i><br>
     *
     * @deprecated use the safer methods {@link #setkey(java.lang.String)} and {@link #setkey(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    void setkey(Pointer charPtr1);

    /**
     * Original signature : <code>void setkey(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:351</i>
     */
    void setkey(String charPtr1);

    /**
     * Original signature : <code>int setlogin(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:355</i><br>
     *
     * @deprecated use the safer methods {@link #setlogin(java.lang.String)} and {@link #setlogin(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int setlogin(Pointer charPtr1);

    /**
     * Original signature : <code>int setlogin(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:355</i>
     */
    int setlogin(String charPtr1);

    /**
     * Original signature : <code>void* setmode(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:356</i><br>
     *
     * @deprecated use the safer methods {@link #setmode(java.lang.String)} and {@link #setmode(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    Pointer setmode(Pointer charPtr1);

    /**
     * Original signature : <code>void* setmode(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:356</i>
     */
    Pointer setmode(String charPtr1);

    /**
     * Original signature : <code>int setrgid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:357</i>
     */
    int setrgid();

    /**
     * Original signature : <code>int setruid()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:358</i>
     */
    int setruid();

    /**
     * Original signature : <code>int setsgroups_np(int, const)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:359</i>
     */
    int setsgroups_np(int int1, int uuid_t);

    /**
     * Original signature : <code>void setusershell()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:360</i>
     */
    void setusershell();

    /**
     * Original signature : <code>int setwgroups_np(int, const)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:361</i>
     */
    int setwgroups_np(int int1, int uuid_t);

    /**
     * Original signature : <code>int strtofflags(char**, unsigned long*, unsigned long*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:362</i>
     */
    int strtofflags(PointerByReference charPtrPtr1, NativeLongByReference uPtr1, NativeLongByReference uPtr2);

    /**
     * Original signature : <code>int swapon(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:363</i><br>
     *
     * @deprecated use the safer methods {@link #swapon(java.lang.String)} and {@link #swapon(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int swapon(Pointer charPtr1);

    /**
     * Original signature : <code>int swapon(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:363</i>
     */
    int swapon(String charPtr1);

    /**
     * Original signature : <code>int syscall(int, null)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:364</i>
     */
    int syscall(int int1, Object... varargs);

    /**
     * Original signature : <code>int ttyslot()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:365</i>
     */
    int ttyslot();

    /**
     * Original signature : <code>int undelete(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:366</i><br>
     *
     * @deprecated use the safer methods {@link #undelete(java.lang.String)} and {@link #undelete(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int undelete(Pointer charPtr1);

    /**
     * Original signature : <code>int undelete(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:366</i>
     */
    int undelete(String charPtr1);

    /**
     * Original signature : <code>int unwhiteout(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:367</i><br>
     *
     * @deprecated use the safer methods {@link #unwhiteout(java.lang.String)} and {@link #unwhiteout(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int unwhiteout(Pointer charPtr1);

    /**
     * Original signature : <code>int unwhiteout(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:367</i>
     */
    int unwhiteout(String charPtr1);

    /**
     * Original signature : <code>void* valloc(size_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:368</i>
     */
    Pointer valloc(size_t size_t1);

    /**
     * Original signature : <code>int getsubopt(char**, const char**, char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:371</i><br>
     *
     * @deprecated use the safer methods {@link #getsubopt(com.sun.jna.ptr.PointerByReference, java.lang.String[], com.sun.jna.ptr.PointerByReference)} and {@link #getsubopt(com.sun.jna.ptr.PointerByReference, com.sun.jna.ptr.PointerByReference, com.sun.jna.ptr.PointerByReference)} instead
     */
    @Deprecated
    int getsubopt(PointerByReference charPtrPtr1, PointerByReference charPtrPtr2, PointerByReference charPtrPtr3);

    /**
     * Original signature : <code>int getsubopt(char**, const char**, char**)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:371</i>
     */
    int getsubopt(PointerByReference charPtrPtr1, String charPtrPtr2[], PointerByReference charPtrPtr3);

    /**
     * Original signature : <code>int getattrlist(const char*, void*, void*, size_t, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:383</i><br>
     *
     * @deprecated use the safer methods {@link #getattrlist(java.lang.String, com.sun.jna.Pointer, com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong)} and {@link #getattrlist(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong)} instead
     */
    @Deprecated
    int getattrlist(Pointer charPtr1, Pointer voidPtr1, Pointer voidPtr2, size_t size_t1, NativeLong u1);

    /**
     * Original signature : <code>int getattrlist(const char*, void*, void*, size_t, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:383</i>
     */
    int getattrlist(String charPtr1, Pointer voidPtr1, Pointer voidPtr2, size_t size_t1, NativeLong u1);

    /**
     * Original signature : <code>int setattrlist(const char*, void*, void*, size_t, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:384</i><br>
     *
     * @deprecated use the safer methods {@link #setattrlist(java.lang.String, com.sun.jna.Pointer, com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong)} and {@link #setattrlist(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, size_t, com.sun.jna.NativeLong)} instead
     */
    @Deprecated
    int setattrlist(Pointer charPtr1, Pointer voidPtr1, Pointer voidPtr2, size_t size_t1, NativeLong u1);

    /**
     * Original signature : <code>int setattrlist(const char*, void*, void*, size_t, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:384</i>
     */
    int setattrlist(String charPtr1, Pointer voidPtr1, Pointer voidPtr2, size_t size_t1, NativeLong u1);

    /**
     * Original signature : <code>int exchangedata(const char*, const char*, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:385</i><br>
     *
     * @deprecated use the safer methods {@link #exchangedata(java.lang.String, java.lang.String, com.sun.jna.NativeLong)} and {@link #exchangedata(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.NativeLong)} instead
     */
    @Deprecated
    int exchangedata(Pointer charPtr1, Pointer charPtr2, NativeLong u1);

    /**
     * Original signature : <code>int exchangedata(const char*, const char*, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:385</i>
     */
    int exchangedata(String charPtr1, String charPtr2, NativeLong u1);

    /**
     * Original signature : <code>int getdirentriesattr(int, void*, void*, size_t, unsigned long*, unsigned long*, unsigned long*, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:386</i>
     */
    int getdirentriesattr(int int1, Pointer voidPtr1, Pointer voidPtr2, size_t size_t1, NativeLongByReference uPtr1, NativeLongByReference uPtr2, NativeLongByReference uPtr3, NativeLong u1);

    /**
     * Original signature : <code>int searchfs(const char*, void*, void*, unsigned long, unsigned long, void*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:387</i><br>
     *
     * @deprecated use the safer methods {@link #searchfs(java.lang.String, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.NativeLong, com.sun.jna.NativeLong, com.sun.jna.Pointer)} and {@link #searchfs(com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.NativeLong, com.sun.jna.NativeLong, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int searchfs(Pointer charPtr1, Pointer voidPtr1, Pointer voidPtr2, NativeLong u1, NativeLong u2, Pointer voidPtr3);

    /**
     * Original signature : <code>int searchfs(const char*, void*, void*, unsigned long, unsigned long, void*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:387</i>
     */
    int searchfs(String charPtr1, Pointer voidPtr1, Pointer voidPtr2, NativeLong u1, NativeLong u2, Pointer voidPtr3);

    /**
     * Original signature : <code>int fsctl(const char*, unsigned long, void*, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:389</i><br>
     *
     * @deprecated use the safer methods {@link #fsctl(java.lang.String, com.sun.jna.NativeLong, com.sun.jna.Pointer, com.sun.jna.NativeLong)} and {@link #fsctl(com.sun.jna.Pointer, com.sun.jna.NativeLong, com.sun.jna.Pointer, com.sun.jna.NativeLong)} instead
     */
    @Deprecated
    int fsctl(Pointer charPtr1, NativeLong u1, Pointer voidPtr1, NativeLong u2);

    /**
     * Original signature : <code>int fsctl(const char*, unsigned long, void*, unsigned long)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/unistd.h:389</i>
     */
    int fsctl(String charPtr1, NativeLong u1, Pointer voidPtr1, NativeLong u2);

    /**
     * [XSI]<br>
     * Original signature : <code>int chmod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:422</i><br>
     *
     * @deprecated use the safer methods {@link #chmod(java.lang.String)} and {@link #chmod(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int chmod(Pointer charPtr1);

    /**
     * [XSI]<br>
     * Original signature : <code>int chmod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:422</i>
     */
    int chmod(String charPtr1);

    /**
     * Original signature : <code>int fchmod(int)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:423</i>
     */
    int fchmod(int int1);

    /**
     * Original signature : <code>int fstat(int, stat*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:424</i>
     */
    int fstat(int int1, stat statPtr1);

    /**
     * Original signature : <code>int lstat(const char*, stat*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:425</i><br>
     *
     * @deprecated use the safer methods {@link #lstat(java.lang.String, KfsLibrary.stat)} and {@link #lstat(com.sun.jna.Pointer, KfsLibrary.stat)} instead
     */
    @Deprecated
    int lstat(Pointer charPtr1, stat statPtr1);

    /**
     * Original signature : <code>int lstat(const char*, stat*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:425</i>
     */
    int lstat(String charPtr1, stat statPtr1);

    /**
     * Original signature : <code>int mkdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:426</i><br>
     *
     * @deprecated use the safer methods {@link #mkdir(java.lang.String)} and {@link #mkdir(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int mkdir(Pointer charPtr1);

    /**
     * Original signature : <code>int mkdir(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:426</i>
     */
    int mkdir(String charPtr1);

    /**
     * Original signature : <code>int mkfifo(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:427</i><br>
     *
     * @deprecated use the safer methods {@link #mkfifo(java.lang.String)} and {@link #mkfifo(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int mkfifo(Pointer charPtr1);

    /**
     * Original signature : <code>int mkfifo(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:427</i>
     */
    int mkfifo(String charPtr1);

    /**
     * Original signature : <code>int stat(const char*, stat*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:428</i><br>
     *
     * @deprecated use the safer methods {@link #stat(java.lang.String, KfsLibrary.stat)} and {@link #stat(com.sun.jna.Pointer, KfsLibrary.stat)} instead
     */
    @Deprecated
    int stat(Pointer charPtr1, stat statPtr1);

    /**
     * Original signature : <code>int stat(const char*, stat*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:428</i>
     */
    int stat(String charPtr1, stat statPtr1);

    /**
     * Original signature : <code>mode_t umask(mode_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:430</i>
     */
    short umask(short mode_t1);

    /**
     * Original signature : <code>int chflags(const char*, __uint32_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:438</i><br>
     *
     * @deprecated use the safer methods {@link #chflags(java.lang.String, int)} and {@link #chflags(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int chflags(Pointer charPtr1, int __uint32_t1);

    /**
     * Original signature : <code>int chflags(const char*, __uint32_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:438</i>
     */
    int chflags(String charPtr1, int __uint32_t1);

    /**
     * Original signature : <code>int chmodx_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:439</i><br>
     *
     * @deprecated use the safer methods {@link #chmodx_np(java.lang.String, KfsLibrary.filesec_t)} and {@link #chmodx_np(com.sun.jna.Pointer, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int chmodx_np(Pointer charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int chmodx_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:439</i>
     */
    int chmodx_np(String charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int fchflags(int, __uint32_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:440</i>
     */
    int fchflags(int int1, int __uint32_t1);

    /**
     * Original signature : <code>int fchmodx_np(int, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:441</i>
     */
    int fchmodx_np(int int1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int fstatx_np(int, stat*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:442</i>
     */
    int fstatx_np(int int1, stat statPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int lchflags(const char*, __uint32_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:443</i><br>
     *
     * @deprecated use the safer methods {@link #lchflags(java.lang.String, int)} and {@link #lchflags(com.sun.jna.Pointer, int)} instead
     */
    @Deprecated
    int lchflags(Pointer charPtr1, int __uint32_t1);

    /**
     * Original signature : <code>int lchflags(const char*, __uint32_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:443</i>
     */
    int lchflags(String charPtr1, int __uint32_t1);

    /**
     * Original signature : <code>int lchmod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:444</i><br>
     *
     * @deprecated use the safer methods {@link #lchmod(java.lang.String)} and {@link #lchmod(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    int lchmod(Pointer charPtr1);

    /**
     * Original signature : <code>int lchmod(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:444</i>
     */
    int lchmod(String charPtr1);

    /**
     * Original signature : <code>int lstatx_np(const char*, stat*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:445</i><br>
     *
     * @deprecated use the safer methods {@link #lstatx_np(java.lang.String, KfsLibrary.stat, KfsLibrary.filesec_t)} and {@link #lstatx_np(com.sun.jna.Pointer, KfsLibrary.stat, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int lstatx_np(Pointer charPtr1, stat statPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int lstatx_np(const char*, stat*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:445</i>
     */
    int lstatx_np(String charPtr1, stat statPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int mkdirx_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:446</i><br>
     *
     * @deprecated use the safer methods {@link #mkdirx_np(java.lang.String, KfsLibrary.filesec_t)} and {@link #mkdirx_np(com.sun.jna.Pointer, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int mkdirx_np(Pointer charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int mkdirx_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:446</i>
     */
    int mkdirx_np(String charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int mkfifox_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:447</i><br>
     *
     * @deprecated use the safer methods {@link #mkfifox_np(java.lang.String, KfsLibrary.filesec_t)} and {@link #mkfifox_np(com.sun.jna.Pointer, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int mkfifox_np(Pointer charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int mkfifox_np(const char*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:447</i>
     */
    int mkfifox_np(String charPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int statx_np(const char*, stat*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:448</i><br>
     *
     * @deprecated use the safer methods {@link #statx_np(java.lang.String, KfsLibrary.stat, KfsLibrary.filesec_t)} and {@link #statx_np(com.sun.jna.Pointer, KfsLibrary.stat, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int statx_np(Pointer charPtr1, stat statPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int statx_np(const char*, stat*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:448</i>
     */
    int statx_np(String charPtr1, stat statPtr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int umaskx_np(filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:449</i>
     */
    int umaskx_np(filesec_t filesec_t1);

    /**
     * The following are simillar  to stat and friends except provide struct stat64 instead of struct stat<br>
     * Original signature : <code>int fstatx64_np(int, stat64*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:451</i>
     */
    int fstatx64_np(int int1, stat64 stat64Ptr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int lstatx64_np(const char*, stat64*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:452</i><br>
     *
     * @deprecated use the safer methods {@link #lstatx64_np(java.lang.String, KfsLibrary.stat64, KfsLibrary.filesec_t)} and {@link #lstatx64_np(com.sun.jna.Pointer, KfsLibrary.stat64, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int lstatx64_np(Pointer charPtr1, stat64 stat64Ptr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int lstatx64_np(const char*, stat64*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:452</i>
     */
    int lstatx64_np(String charPtr1, stat64 stat64Ptr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int statx64_np(const char*, stat64*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:453</i><br>
     *
     * @deprecated use the safer methods {@link #statx64_np(java.lang.String, KfsLibrary.stat64, KfsLibrary.filesec_t)} and {@link #statx64_np(com.sun.jna.Pointer, KfsLibrary.stat64, KfsLibrary.filesec_t)} instead
     */
    @Deprecated
    int statx64_np(Pointer charPtr1, stat64 stat64Ptr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int statx64_np(const char*, stat64*, filesec_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:453</i>
     */
    int statx64_np(String charPtr1, stat64 stat64Ptr1, filesec_t filesec_t1);

    /**
     * Original signature : <code>int fstat64(int, stat64*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:454</i>
     */
    int fstat64(int int1, stat64 stat64Ptr1);

    /**
     * Original signature : <code>int lstat64(const char*, stat64*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:455</i><br>
     *
     * @deprecated use the safer methods {@link #lstat64(java.lang.String, KfsLibrary.stat64)} and {@link #lstat64(com.sun.jna.Pointer, KfsLibrary.stat64)} instead
     */
    @Deprecated
    int lstat64(Pointer charPtr1, stat64 stat64Ptr1);

    /**
     * Original signature : <code>int lstat64(const char*, stat64*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:455</i>
     */
    int lstat64(String charPtr1, stat64 stat64Ptr1);

    /**
     * Original signature : <code>int stat64(const char*, stat64*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:456</i><br>
     *
     * @deprecated use the safer methods {@link #stat64(java.lang.String, KfsLibrary.stat64)} and {@link #stat64(com.sun.jna.Pointer, KfsLibrary.stat64)} instead
     */
    @Deprecated
    int stat64(Pointer charPtr1, stat64 stat64Ptr1);

    /**
     * Original signature : <code>int stat64(const char*, stat64*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/stat.h:456</i>
     */
    int stat64(String charPtr1, stat64 stat64Ptr1);

    /**
     * \brief		Mout a filesystem<br>
     * \details	Mounts a filesystem and returns an identifier that you will need in order to unmount it.<br>
     * On error, this will return -1. The kfs library will not be able to tell when a filesystem<br>
     * is unmounted. You should make your best effort to call kfs_unmount explicitly (even if<br>
     * the system has already unmounted the filesystem) to reclaim identifiers and free memory<br>
     * used by the kfs library. This mount command will create the directory at the mountpoint<br>
     * if needed (but will not create intermediate directories).<br>
     * Original signature : <code>kfsid_t kfs_mount(const kfsfilesystem_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:479</i>
     */
    long kfs_mount(kfsfilesystem filesystem);

    /**
     * \brief		Unmount a filesystem<br>
     * \details	Give the identifier you received when mounting the filesystem.<br>
     * Original signature : <code>void kfs_unmount(kfsid_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:485</i>
     */
    void kfs_unmount(long identifier);

    /**
     * \brief		Create a content listing<br>
     * \details	You must call destory unless you relinquish ownership at some point.<br>
     * Original signature : <code>kfscontents_t* kfscontents_create()</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:527</i>
     */
    Pointer kfscontents_create();

    /**
     * \brief		Destory a content listing<br>
     * \details	Destory a content listing.<br>
     * Original signature : <code>void kfscontents_destroy(kfscontents_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:533</i>
     */
    void kfscontents_destroy(Pointer contents);

    /**
     * \brief		Append an entry<br>
     * \details	Append an entry to this listing of contents. Once you append the entry,<br>
     * you relinquish ownership of it, so you do not need to destory it. This<br>
     * will be done for you.<br>
     * Original signature : <code>void kfscontents_append(kfscontents_t*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:541</i><br>
     *
     * @deprecated use the safer methods {@link #kfscontents_append(com.sun.jna.Pointer, java.lang.String)} and {@link #kfscontents_append(com.sun.jna.Pointer, com.sun.jna.Pointer)} instead
     */
    @Deprecated
    void kfscontents_append(Pointer contents, Pointer entry);

    /**
     * \brief		Append an entry<br>
     * \details	Append an entry to this listing of contents. Once you append the entry,<br>
     * you relinquish ownership of it, so you do not need to destory it. This<br>
     * will be done for you.<br>
     * Original signature : <code>void kfscontents_append(kfscontents_t*, const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:541</i>
     */
    void kfscontents_append(Pointer contents, String entry);

    /**
     * \brief		Get the count of a content listing<br>
     * \details	Get the count of a content listing.<br>
     * Original signature : <code>uint64_t kfscontents_count(kfscontents_t*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:547</i>
     */
    long kfscontents_count(Pointer contents);

    /**
     * \brief		Get an entry in a content listing<br>
     * \details	Get an entry in a content listing. This method returns NULL if the<br>
     * index is out of range.<br>
     * Original signature : <code>char* kfscontents_at(kfscontents_t*, uint64_t)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:554</i>
     */
    Pointer kfscontents_at(Pointer contents, long index);

    /**
     * Original signature : <code>void kfs_set_thread_begin_callback(kfs_set_thread_begin_callback_arg1_callback)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:566</i>
     */
    void kfs_set_thread_begin_callback(kfs_set_thread_begin_callback_arg1_callback arg1);

    /**
     * Original signature : <code>void kfs_set_thread_end_callback(kfs_set_thread_end_callback_arg1_callback)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:567</i>
     */
    void kfs_set_thread_end_callback(kfs_set_thread_end_callback_arg1_callback arg1);

    /**
     * \brief		Write an error message<br>
     * \details	Like perror. Uses perror for unknown errors.<br>
     * Original signature : <code>void kfs_perror(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:585</i><br>
     *
     * @deprecated use the safer methods {@link #kfs_perror(java.lang.String)} and {@link #kfs_perror(com.sun.jna.Pointer)} instead
     */
    @Deprecated
    void kfs_perror(Pointer s);

    /**
     * \brief		Write an error message<br>
     * \details	Like perror. Uses perror for unknown errors.<br>
     * Original signature : <code>void kfs_perror(const char*)</code><br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h:585</i>
     */
    void kfs_perror(String s);

    /// Pointer to unknown (opaque) type
    public static class filesec_t extends PointerType {
        public filesec_t(Pointer address) {
            super(address);
        }

        public filesec_t() {
            super();
        }
    }


}

