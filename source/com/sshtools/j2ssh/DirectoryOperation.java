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
package com.sshtools.j2ssh;

import com.sshtools.j2ssh.sftp.SftpFile;

import java.io.*;

import java.util.*;


public class DirectoryOperation {
    Vector unchangedFiles = new Vector();
    Vector newFiles = new Vector();
    Vector updatedFiles = new Vector();
    Vector deletedFiles = new Vector();
    Vector recursedDirectories = new Vector();

    public DirectoryOperation() {
    }

    protected void addNewFile(File f) {
        newFiles.add(f);
    }

    protected void addUpdatedFile(File f) {
        updatedFiles.add(f);
    }

    protected void addDeletedFile(File f) {
        deletedFiles.add(f);
    }

    protected void addUnchangedFile(File f) {
        unchangedFiles.add(f);
    }

    protected void addNewFile(SftpFile f) {
        newFiles.add(f);
    }

    protected void addUpdatedFile(SftpFile f) {
        updatedFiles.add(f);
    }

    protected void addDeletedFile(SftpFile f) {
        deletedFiles.add(f);
    }

    protected void addUnchangedFile(SftpFile f) {
        unchangedFiles.add(f);
    }

    public List getNewFiles() {
        return newFiles;
    }

    public List getUpdatedFiles() {
        return updatedFiles;
    }

    public List getUnchangedFiles() {
        return unchangedFiles;
    }

    public List getDeletedFiles() {
        return deletedFiles;
    }

    public boolean containsFile(File f) {
        return unchangedFiles.contains(f) || newFiles.contains(f) ||
        updatedFiles.contains(f) || deletedFiles.contains(f) ||
        recursedDirectories.contains(f);
    }

    public boolean containsFile(SftpFile f) {
        return unchangedFiles.contains(f) || newFiles.contains(f) ||
        updatedFiles.contains(f) || deletedFiles.contains(f) ||
        recursedDirectories.contains(f.getAbsolutePath());
    }

    public void addDirectoryOperation(DirectoryOperation op, File f) {
        updatedFiles.addAll(op.getUpdatedFiles());
        newFiles.addAll(op.getNewFiles());
        unchangedFiles.addAll(op.getUnchangedFiles());
        deletedFiles.addAll(op.getDeletedFiles());
        recursedDirectories.add(f);
    }

    public int getFileCount() {
        return newFiles.size() + updatedFiles.size();
    }

    public void addDirectoryOperation(DirectoryOperation op, String file) {
        updatedFiles.addAll(op.getUpdatedFiles());
        newFiles.addAll(op.getNewFiles());
        unchangedFiles.addAll(op.getUnchangedFiles());
        deletedFiles.addAll(op.getDeletedFiles());
        recursedDirectories.add(file);
    }

    public long getTransferSize() {
        Object obj;
        long size = 0;
        SftpFile sftpfile;
        File file;

        for (Iterator i = newFiles.iterator(); i.hasNext();) {
            obj = i.next();

            if (obj instanceof File) {
                file = (File) obj;

                if (file.isFile()) {
                    size += file.length();
                }
            } else if (obj instanceof SftpFile) {
                sftpfile = (SftpFile) obj;

                if (sftpfile.isFile()) {
                    size += sftpfile.getAttributes().getSize().longValue();
                }
            }
        }

        for (Iterator i = updatedFiles.iterator(); i.hasNext();) {
            obj = i.next();

            if (obj instanceof File) {
                file = (File) obj;

                if (file.isFile()) {
                    size += file.length();
                }
            } else if (obj instanceof SftpFile) {
                sftpfile = (SftpFile) obj;

                if (sftpfile.isFile()) {
                    size += sftpfile.getAttributes().getSize().longValue();
                }
            }
        }

        // Add a value for deleted files??
        return size;
    }
}
