/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.session;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public interface PseudoTerminal {
    /**  */
    public final int TTY_OP_END = 0; //Indicates end of options.

    /**  */
    public final int VINTR = 1; //Interrupt character; 255 if none.  Similarly for the

    /**  */
    public final int VQUIT = 2; //The quit character (sends SIGQUIT signal on POSIX
    public final int VERASE = 3; //Erase the character to left of the cursor.

    /**  */
    public final int VKILL = 4; //Kill the current input line.

    /**  */
    public final int VEOF = 5; //End-of-file character (sends EOF from the terminal).

    /**  */
    public final int VEOL = 6; //End-of-line character in addition to carriage return
    public final int VEOL2 = 7; //Additional end-of-line character.

    /**  */
    public final int VSTART = 8; //Continues paused output (normally control-Q).

    /**  */
    public final int VSTOP = 9; //Pauses output (normally control-S).

    /**  */
    public final int VSUSP = 10; //Suspends the current program.

    /**  */
    public final int VDSUSP = 11; //Another suspend character.

    /**  */
    public final int VREPRINT = 12; //Reprints the current input line.

    /**  */
    public final int VWERASE = 13; //Erases a word left of cursor.

    /**  */
    public final int VLNEXT = 14; //Enter the next character typed literally, even if it
    public final int VFLUSH = 15; //Character to flush output.

    /**  */
    public final int VSWTCH = 16; //Switch to a different shell layer.

    /**  */
    public final int VSTATUS = 17; //Prints system status line (load, command, pid etc).

    /**  */
    public final int VDISCARD = 18; //Toggles the flushing of terminal output.

    /**  */
    public final int IGNPAR = 30; //The ignore parity flag.  The parameter SHOULD be 0 if
    public final int PARMRK = 31; //Mark parity and framing errors.

    /**  */
    public final int INPCK = 32; //Enable checking of parity errors.

    /**  */
    public final int ISTRIP = 33; //Strip 8th bit off characters.

    /**  */
    public final int INLCR = 34; //Map NL into CR on input.

    /**  */
    public final int IGNCR = 35; //Ignore CR on input.

    /**  */
    public final int ICRNL = 36; //Map CR to NL on input.

    /**  */
    public final int IUCLC = 37; //Translate uppercase characters to lowercase.

    /**  */
    public final int IXON = 38; //Enable output flow control.

    /**  */
    public final int IXANY = 39; //Any char will restart after stop.

    /**  */
    public final int IXOFF = 40; //Enable input flow control.

    /**  */
    public final int IMAXBEL = 41; //Ring bell on input queue full.

    /**  */
    public final int ISIG = 50; //Enable signals INTR, QUIT, [D]SUSP.

    /**  */
    public final int ICANON = 51; //Canonicalize input lines.

    /**  */
    public final int XCASE = 52; //Enable input and output of uppercase characters by
    public final int ECHO = 53; //Enable echoing.

    /**  */
    public final int ECHOE = 54; //Visually erase chars.

    /**  */
    public final int ECHOK = 55; //Kill character discards current line.

    /**  */
    public final int ECHONL = 56; //Echo NL even if ECHO is off.

    /**  */
    public final int NOFLSH = 57; //Don't flush after interrupt.

    /**  */
    public final int TOSTOP = 58; //Stop background jobs from output.

    /**  */
    public final int IEXTEN = 59; //Enable extensions.

    /**  */
    public final int ECHOCTL = 60; //Echo control characters as ^(Char).

    /**  */
    public final int ECHOKE = 61; //Visual erase for line kill.

    /**  */
    public final int PENDIN = 62; //Retype pending input.

    /**  */
    public final int OPOST = 70; //Enable output processing.

    /**  */
    public final int OLCUC = 71; //Convert lowercase to uppercase.

    /**  */
    public final int ONLCR = 72; //Map NL to CR-NL.

    /**  */
    public final int OCRNL = 73; //Translate carriage return to newline (output).

    /**  */
    public final int ONOCR = 74; //Translate newline to carriage return-newline
    public final int ONLRET = 75; //Newline performs a carriage return (output).

    /**  */
    public final int CS7 = 90; //7 bit mode.

    /**  */
    public final int CS8 = 91; //8 bit mode.

    /**  */
    public final int PARENB = 92; //Parity enable.

    /**  */
    public final int PARODD = 93; //Odd parity, else even.

    /**  */
    public final int TTY_OP_ISPEED = 128; //Specifies the input baud rate in bits per second.

    /**  */
    public final int TTY_OP_OSPEED = 129; //Specifies the output baud rate in bits per second.

    /**
     *
     *
     * @return
     */
    public int getColumns();

    /**
     *
     *
     * @return
     */
    public String getEncodedTerminalModes();

    /**
     *
     *
     * @return
     */
    public int getHeight();

    /**
     *
     *
     * @return
     */
    public int getRows();

    /**
     *
     *
     * @return
     */
    public String getTerm();

    /**
     *
     *
     * @return
     */
    public int getWidth();
}
