package ch.ethz.ssh2.sftp;

/**
 * SFTP Paket Types
 *
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class Packet {
    public static final int SSH_FXP_INIT = 1;
    public static final int SSH_FXP_VERSION = 2;
    public static final int SSH_FXP_OPEN = 3;
    public static final int SSH_FXP_CLOSE = 4;
    public static final int SSH_FXP_READ = 5;
    public static final int SSH_FXP_WRITE = 6;
    public static final int SSH_FXP_LSTAT = 7;
    public static final int SSH_FXP_FSTAT = 8;
    public static final int SSH_FXP_SETSTAT = 9;
    public static final int SSH_FXP_FSETSTAT = 10;
    public static final int SSH_FXP_OPENDIR = 11;
    public static final int SSH_FXP_READDIR = 12;
    public static final int SSH_FXP_REMOVE = 13;
    public static final int SSH_FXP_MKDIR = 14;
    public static final int SSH_FXP_RMDIR = 15;
    public static final int SSH_FXP_REALPATH = 16;
    public static final int SSH_FXP_STAT = 17;
    public static final int SSH_FXP_RENAME = 18;
    public static final int SSH_FXP_READLINK = 19;
    public static final int SSH_FXP_SYMLINK = 20;

    public static final int SSH_FXP_STATUS = 101;
    public static final int SSH_FXP_HANDLE = 102;
    public static final int SSH_FXP_DATA = 103;
    public static final int SSH_FXP_NAME = 104;
    public static final int SSH_FXP_ATTRS = 105;

    public static final int SSH_FXP_EXTENDED = 200;
    public static final int SSH_FXP_EXTENDED_REPLY = 201;

    public static String forName(int type) {
        switch(type) {
            case SSH_FXP_INIT:
                return "SSH_FXP_INIT";
            case SSH_FXP_VERSION:
                return "SSH_FXP_VERSION";
            case SSH_FXP_OPEN:
                return "SSH_FXP_OPEN";
            case SSH_FXP_CLOSE:
                return "SSH_FXP_CLOSE";
            case SSH_FXP_READ:
                return "SSH_FXP_READ";
            case SSH_FXP_WRITE:
                return "SSH_FXP_WRITE";
            case SSH_FXP_LSTAT:
                return "SSH_FXP_LSTAT";
            case SSH_FXP_FSTAT:
                return "SSH_FXP_FSTAT";
            case SSH_FXP_SETSTAT:
                return "SSH_FXP_SETSTAT";
            case SSH_FXP_FSETSTAT:
                return "SSH_FXP_FSETSTAT";
            case SSH_FXP_OPENDIR:
                return "SSH_FXP_OPENDIR";
            case SSH_FXP_READDIR:
                return "SSH_FXP_READDIR";
            case SSH_FXP_REMOVE:
                return "SSH_FXP_REMOVE";
            case SSH_FXP_MKDIR:
                return "SSH_FXP_MKDIR";
            case SSH_FXP_RMDIR:
                return "SSH_FXP_RMDIR";
            case SSH_FXP_REALPATH:
                return "SSH_FXP_REALPATH";
            case SSH_FXP_STAT:
                return "SSH_FXP_STAT";
            case SSH_FXP_RENAME:
                return "SSH_FXP_RENAME";
            case SSH_FXP_READLINK:
                return "SSH_FXP_READLINK";
            case SSH_FXP_SYMLINK:
                return "SSH_FXP_SYMLINK";
            case SSH_FXP_STATUS:
                return "SSH_FXP_STATUS";
            case SSH_FXP_HANDLE:
                return "SSH_FXP_HANDLE";
            case SSH_FXP_DATA:
                return "SSH_FXP_DATA";
            case SSH_FXP_NAME:
                return "SSH_FXP_NAME";
            case SSH_FXP_ATTRS:
                return "SSH_FXP_ATTRS";
            case SSH_FXP_EXTENDED:
                return "SSH_FXP_EXTENDED";
            case SSH_FXP_EXTENDED_REPLY:
                return "SSH_FXP_EXTENDED_REPLY";
        }
        return null;
    }
}
