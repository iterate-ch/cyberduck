/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport;

import java.awt.Component;

import javax.swing.JOptionPane;

import javax.swing.SwingUtilities;

import java.lang.reflect.InvocationTargetException;

/**
 * Prompts the user to allow or deny a host access though Swing Dialogs.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class DialogHostKeyVerification
    extends HostKeyVerification {
    Component parent;

    /**
     * Creates a new DialogHostKeyVerification object.
     *
     * @param parent the parent component for modal operation
     *
     * @throws InvalidHostFileException if the host file does not exist or is
     *         invalid
     */
    public DialogHostKeyVerification(Component parent)
                              throws InvalidHostFileException {
        this.parent = parent;
    }

    /**
     * Creates a new DialogHostKeyVerification object.
     *
     * @param parent the parent component for modal operation
     * @param hostFileName the path to the host file
     *
     * @throws InvalidHostFileException if the host file does not exist or is
     *         invalid
     */
    public DialogHostKeyVerification(Component parent, String hostFileName)
                              throws InvalidHostFileException {
        super(hostFileName);
        this.parent = parent;
    }

    /**
     * Displays a message box informing the user that the host is denied
     *
     * @param host The name of the host
     */
    public void onDeniedHost(final String host)
                                            throws TransportProtocolException {
        // Show a message to the user to inform them that the host
        // is denied
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              JOptionPane.showMessageDialog(parent,
                           "Access to '" + host + "' is denied.\n"
                           + "Verify the access granted/denied in the allowed hosts file.",
                           "Remote Host Authentication",
                           JOptionPane.OK_OPTION);
            }
          });
        }
        catch(InvocationTargetException ite) {
          throw new TransportProtocolException("Invocation Exception: " + ite.getMessage());
        }
        catch(InterruptedException ie) {
          throw new TransportProtocolException("SwingUtilities thread interrupted!");
        }
    }

    /**
     * Requests that the user confirm a changed host key
     *
     * @param host The name of the host
     * @param recordedFingerprint The fingerprint recorded in allowed hosts
     *        file
     * @param actualFingerprint The fingerprint the host supplied
     */
    public void onHostKeyMismatch(final String host, final String recordedFingerprint,
                                  final String actualFingerprint)
                                            throws TransportProtocolException {

        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {

                Object options[] = getOptions();

                int res =
                    JOptionPane.showOptionDialog(parent,
                                                 "The host '" + host
                                                 + "' has provided a different host key.\nThe host key"
                                                 + " fingerprint provided is '"
                                                 + actualFingerprint + "'.\n"
                                                 + "The allowed host key fingerprint is "
                                                 + recordedFingerprint
                                                 + ".\nDo you want to allow this host?",
                                                 "Remote host authentication",
                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE, null,
                                                 options, options[0]);

                try {
                    // Handle the reply
                    if ((options.length==3) && (res==0)) {
                        // Always allow the host with the new fingerprint
                        allowHost(host, actualFingerprint, true);
                    } else if (((options.length==2) && (res==0))
                                   || ((options.length==3) && (res==1))) {
                        // Only allow the host this once
                        allowHost(host, actualFingerprint, false);
                    }
                } catch (InvalidHostFileException e) {
                    showExceptionMessage(e);
                }
            }
          });
        }
        catch(InvocationTargetException ite) {
          throw new TransportProtocolException("Invocation Exception: " + ite.getMessage());
        }
        catch(InterruptedException ie) {
          throw new TransportProtocolException("SwingUtilities thread interrupted!");
        }

    }

    /**
     * Requests that the user allow or deny access from the host
     *
     * @param host The host name of the server
     * @param fingerprint The fingerprint of the host key it supplied
     */
    public void onUnknownHost(final String host, final String fingerprint)
                                            throws TransportProtocolException {
        // Set up the users options. Only allow always if we can
        // write to the hosts file
         try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              Object options[] = getOptions();

              int res =
                  JOptionPane.showOptionDialog(parent,
                                               "The host '" + host
                                               + "' is unknown. The host key"
                                               + " fingerprint is\n'" + fingerprint
                                               + "'.\nDo you want to allow this host?",
                                               "Remote host authentication",
                                               JOptionPane.YES_NO_CANCEL_OPTION,
                                               JOptionPane.QUESTION_MESSAGE, null,
                                               options, options[0]);

              try {
                  // Handle the reply
                  if ((options.length==3) && (res==0)) {
                      // Always allow the host with the new fingerprint
                      allowHost(host, fingerprint, true);
                  } else if (((options.length==2) && (res==0))
                                 || ((options.length==3) && (res==1))) {
                      // Only allow the host this once
                      allowHost(host, fingerprint, false);
                  }
              } catch (InvalidHostFileException e) {
                  showExceptionMessage(e);
              }
            }
          });
        }
        catch(InvocationTargetException ite) {
          throw new TransportProtocolException("Invocation Exception: " + ite.getMessage());
        }
        catch(InterruptedException ie) {
          throw new TransportProtocolException("SwingUtilities thread interrupted!");
        }


    }

    /**
     * Return options appropriate for the security
     *
     * @return an array of options
     */
    private String[] getOptions() {
        return isHostFileWriteable() ? new String[] {"Always", "Yes", "No"}
                                     : new String[] {"Yes", "No"};
    }

    /**
     * Displays an error message
     *
     * @param e the excpetion
     */
    private void showExceptionMessage(Exception e) {
        JOptionPane.showMessageDialog(parent,
                                      "An unexpected error occured!\n\n"
                                      + e.getMessage(), "Host Verification",
                                      JOptionPane.ERROR_MESSAGE);
    }
}
