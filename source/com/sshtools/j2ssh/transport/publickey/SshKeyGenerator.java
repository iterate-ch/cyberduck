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
package com.sshtools.j2ssh.transport.publickey;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.Method;

import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;


/**
 * Generates private/public keys of any of the configured public key formats.
 *

 * @author Lee David Painter <A HREF="mailto:lee@sshtools.com">lee@sshtools.com</A>
 * @version $Id$
 */
public class SshKeyGenerator {
    private static String filename = null;
    private static String type = "dsa";
    private static int bits = 1024;
    private static boolean useGUI;
    private static boolean guiAvailable;
    private static boolean toOpenSSH = false;
    private static boolean toSECSH = false;
    private static boolean changePass = false;

    // Test if the GUI is available
    static {
        try {
            Class.forName("com.sshtools.j2ssh.keygen.Main");
            guiAvailable = true;
        } catch (ClassNotFoundException cnfe) {
        }
    }

    /**
     * Constructs the key generator
     */
    public SshKeyGenerator() {
    }

    /**
     * Generates a public/private key pair for the given public key type
     *
     * @param type The public key type (e.g ssh-dss)
     * @param bits The number of bits
     * @param filename The filename to output to (will be appended with
     *        .pub/.prv)
     * @param username the users name who created the file
     * @param passphrase the passphrase to encrypt (null or "" force empty
     *        passphrase)
     *
     * @throws AlgorithmNotSupportedException if the public key algorithm is
     *         not supported
     */
    public void generateKeyPair(String type, int bits, String filename,
                                String username, String passphrase)
                         throws IOException {

            System.out.println("****Sshtools.com SSH Key Pair Generator****");

            String keyType = type;

            if (keyType.equalsIgnoreCase("DSA")) {
                keyType = "ssh-dss";
            }

            if (keyType.equalsIgnoreCase("RSA")) {
                keyType = "ssh-rsa";
            }

            final SshKeyPair pair = SshKeyPairFactory.newInstance(keyType);

            System.out.println("Generating " + String.valueOf(bits) + " bit "
                               + keyType + " key pair");

            Thread thread =
                new Thread(new Runnable() {
                        public void run() {
                            pair.generate(SshKeyGenerator.this.bits);
                        }
                    });
            thread.setDaemon(true);
            thread.start();

            while (thread.isAlive()) {
                System.out.print(".");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            System.out.println();

            System.out.println("Creating Public Key file " + filename + ".pub");

            // Now save the files
            SshPublicKeyFile pub =
                SshPublicKeyFile.create(pair.getPublicKey(),
                                        new SECSHPublicKeyFormat(username,
                                                                 String.valueOf(bits)
                                                                 + "-bit "
                                                                 + type));

            FileOutputStream out = new FileOutputStream(filename + ".pub");
            out.write(pub.getBytes());
            out.close();

            System.out.println("Generating Private Key file " + filename);

            if (passphrase==null) {
                passphrase = promptForPassphrase(true);
            }

            SshPrivateKeyFile prv =
                SshPrivateKeyFile.create(pair.getPrivateKey(), passphrase,
                                         new SshtoolsPrivateKeyFormat(username,
                                                                      String
                                                                      .valueOf(bits)
                                                                      + "-bit "
                                                                      + type));

            out = new FileOutputStream(filename);
            out.write(prv.getBytes());
            out.close();

    }

    /**
     * Main method to create keys from the command line
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            processCommandLine(args);

            /** Setup logging into generate.log */
            RollingFileAppender log =
                new org.apache.log4j.RollingFileAppender(new PatternLayout(),
                                                         "generate.log", true);
            log.setMaxFileSize("10KB");
            BasicConfigurator.configure(log);

            /**
             * Now perform the necersary commands
             */
            if (useGUI) {
                Class c = Class.forName("com.sshtools.j2ssh.keygen.Main");
                Method m = c.getMethod("main", new Class[] {args.getClass()});
                m.invoke(null, new Object[] {new String[] {}});
            } else {
                File f = new File(filename);

                if (filename==null) {
                    System.err.print("You must supply a valid file to convert!");
                    System.exit(1);
                }


                if (toOpenSSH || toSECSH) {
                    /**
                     * We are converting an existing file
                     */

                    if (!f.exists()) {
                        System.err.print("The file " + f.getAbsolutePath()
                                         + " does not exist!");
                        System.exit(1);
                    }

                    try {
                        if (toOpenSSH) {
                            System.out.print(convertPublicKeyFile(f, new SECSHPublicKeyFormat(),
                                                 new OpenSSHPublicKeyFormat()));
                        } else {
                            System.out.print(convertPublicKeyFile(f, new OpenSSHPublicKeyFormat(),
                                                 new SECSHPublicKeyFormat()));
                        }
                    } catch (InvalidSshKeyException e) {
                        System.err.println("The key format is invalid!");
                    } catch (IOException ioe) {
                        System.err.println("An error occurs whilst reading the file "
                                           + f.getAbsolutePath());
                    }

                    System.exit(0);
                }

                if (changePass) {

                    if (!f.exists()) {
                        System.err.print("The file " + f.getAbsolutePath()
                                         + " does not exist!");
                        System.exit(1);
                    }

                    changePassphrase(f);


                } else {
                    SshKeyGenerator generator = new SshKeyGenerator();

                    String username = System.getProperty("user.name");
                    generator.generateKeyPair(type, bits, filename, username,
                                              null);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Process the command line options
     *
     * @param args the command line arguments
     */
    public static void processCommandLine(String args[]) {
        if (args.length>0) {
            for (int i = 0;i<args.length;i++) {
                if (args[i].equalsIgnoreCase("-b")) {
                    bits = Integer.parseInt(args[++i]);
                } else if (args[i].equalsIgnoreCase("-t")) {
                    type = args[++i];
                } else if (args[i].equalsIgnoreCase("-p")) {
                    changePass = true;
                } else if (args[i].equalsIgnoreCase("-g") && guiAvailable) {
                    useGUI = true;
                } else if (args[i].equalsIgnoreCase("-i") && guiAvailable) {
                    toSECSH = true;
                } else if (args[i].equalsIgnoreCase("-e") && guiAvailable) {
                    toOpenSSH = true;
                } else if (!args[i].startsWith("-")) {
                    if (filename!=null) {
                        printUsage();
                        System.exit(1);
                    }

                    filename = args[i];
                }
            }
        }

        if (!useGUI && (filename==null)) {
            printUsage();
            System.exit(0);
        }
    }

    /**
     * Changes the passphrase on the file by prompting the user to supply an
     * new passphrase.
     *
     * @param f the file to change passphrase on
     */
    private static void changePassphrase(File f) {
        System.out.println("Opening Private Key file "
                               + f.getAbsolutePath());
        try {
            System.out.println("Opening Private Key file "
                               + f.getAbsolutePath());
            String oldPassphrase = promptForPassphrase(false);
            String newPassphrase = promptForPassphrase(true);
            changePassphrase(f, oldPassphrase, newPassphrase);
        }
        catch (InvalidSshKeyException e) {
            System.err.println("The key format is invalid!");
        }
        catch (IOException ioe) {
            System.err.println("An error occurs whilst reading the file "
                               + f.getAbsolutePath());
        }
    }

    /**
     * Changes the passphrase on the file.
     *
     * @param f the file to change passphrase on
     * @param oldPassphrase the old passphrase
     * @param newPassphrase the new passphrase
     */
    public static void changePassphrase(File f, String oldPassphrase, String newPassphrase)
                    throws IOException, InvalidSshKeyException  {
        // Open up the file with its current format
        SshPrivateKeyFile file =
            SshPrivateKeyFile.parse(f, new SshtoolsPrivateKeyFormat());

        System.out.println("Saving Private Key file with new passphrase");

        file.changePassphrase(oldPassphrase, newPassphrase);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            out.write(file.getBytes());
        }
        finally {
            if(out != null)
                out.close();
        }
    }

    /**
     * Converts a public key file from one format to another and prints the new
     * format to stdout. To write to a new file use the following command:<br>
     * <br>
     * ssh-keygen -e mykey.pub > mynewkey.pub
     *
     * @param f the file to convert
     * @param current the current format for parsing
     * @param convert the new format to convert to
     * @throws InvalidSshKeyException if key is invalid
     * @throws IOException on I/O error
     */
    public static String convertPublicKeyFile(File f,
                                             SshPublicKeyFormat current,
                                             SshPublicKeyFormat convert)
                        throws InvalidSshKeyException, IOException {
        // Open up the file with its current format
        SshPublicKeyFile file = SshPublicKeyFile.parse(f, current);

        // Set the new format
        file.setFormat(convert);

        // Output to stdout
        return file.toString();
    }

    /**
     * Prints out the command line options to the console
     */
    private static void printUsage() {
        System.out.println("Usage: SshKeyGenerator [options] filename");
        System.out.println("Options:");
        System.out.println("-b bits        Number of bits in the key to create.");
        System.out.println("-e             Convert OpenSSH to IETF SECSH key file.");
        System.out.println("-i             Convert IETF SECSH to OpenSSH key file.");
        System.out.println("-t type        The type of key to create.");
        System.out.println("-p             Change the passphrase of the private key file.");

        if (guiAvailable) {
            System.out.println("-g \t\tUse GUI to create key");
        }
    }

    /**
     * Prompts the user for a passphrase with option to confirm
     *
     * @param confirm <tt>true</tt> if the user should confirm the passphrase
     *        otherwise <tt>false</tt>
     *
     * @return the entered passphrase
     *
     * @throws IOException if an IO error occurs reading the users input
     */
    private static String promptForPassphrase(boolean confirm)
                                       throws IOException {
        // Confirm the passphrase
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

        String pass1 = "";
        String pass2 = "";

        while (true) {
            System.out.print("Enter passphrase: ");
            pass1 = reader.readLine();

            if (!confirm) {
                break;
            }

            System.out.print("Confirm passphrase: ");
            pass2 = reader.readLine();

            if (pass1.equals(pass2)) {
                if (pass1.trim().length()==0) {
                    System.out.print("You supplied an empty passphrase, are you sure? [Yes|No]: ");
                    pass2 = reader.readLine();

                    if (pass2.equalsIgnoreCase("YES")) {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                System.out.println("The passphrases supplied were not indentical! Try again");
            }
        }

        return pass1;
    }
}
