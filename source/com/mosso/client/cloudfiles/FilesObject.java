/*
 * See COPYING for license information.
 */

package com.mosso.client.cloudfiles;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class FilesObject {
    private String container;
    private String name;
    private String md5sum;
    private long size = -1;
    private String last_modified;
    private String mimeType;
    private FilesClient client = null;

    private static Logger logger = Logger.getLogger(FilesObject.class);

    /**
     * Constructs a new FilesObject (from the file system)
     *
     * @param obj       A file representing the object
     * @param mimeType  Its MIME type
     * @param container The container it lives in
     * @throws NullPointerException     A null parameter was passed in
     * @throws NoSuchAlgorithmException MD5 was not installed on the client.
     * @throws IOException              There was an I/O error talking to the server
     */
    FilesObject(File obj, String mimeType, FilesContainer container) throws NullPointerException, NoSuchAlgorithmException, IOException {
        if(obj != null) {
            if(obj.exists()) {
                if(!obj.isDirectory()) {
                    setName(obj.getName());
                    setMd5sum(FilesClient.md5Sum(obj));
                    setSize(obj.length());
                    setMimeType(mimeType);
                    setClient(container.getClient());
                    setContainer(container.getName());
                }
                else {
                    logger.fatal("Can not create Directories as FSObjects create a FilesContainer for this object");
                    throw new NullPointerException("File Object was a directory !");
                }
            }
            else {
                logger.fatal("File object must exist so we can create an MD5SUM for it !");
                throw new NullPointerException("The file object provided does not exist.");
            }
        }
        else {
            logger.fatal("Not possible to create a FilesObject from a null File.");
            throw new NullPointerException("File Object passed was null !");
        }
    }

    /**
     * Creates a new FilesObject with data from the server
     *
     * @param name      The name of the object
     * @param container The name of it's container
     * @param client    The client it can be accessed through.
     */
    FilesObject(String name, String container, FilesClient client) {
        this.name = name;
        this.container = container;
        this.client = client;
    }

    /**
     * @return The object's name on the server
     */
    public String getName() {
        return name;
    }

    /**
     * Set's the objects name (up to a maximum of 128 characters)
     *
     * @param name The new name
     */
    public void setName(String objName) {
        if(objName.length() > FilesConstants.OBJECT_NAME_LENGTH) {
            logger.warn("Object Name supplied was truncated to Max allowed of " + FilesConstants.OBJECT_NAME_LENGTH + " characters !");
            objName = objName.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
            logger.warn("Truncated Object Name is: " + objName);
        }
        this.name = objName;
    }

    /**
     * @return The MIME type of the object, pulled from the server
     * @throws HttpException
     * @throws IOException
     */
    public String getMimeType() throws HttpException, IOException {
        if(null == mimeType) {
            this.getMetaData();
        }
        return mimeType;
    }

    /**
     * @param mimeType The new MIME type for this object
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Get's the MD5 Checksum for this object
     *
     * @return The MD5 checksum, returned as a base 16 encoded string
     * @throws HttpException
     * @throws IOException
     */
    public String getMd5sum() throws HttpException, IOException {
        if(null == md5sum) {
            this.getMetaData();
        }
        return md5sum;
    }

    /**
     * @param md5sum The MD5 sum of the file (as a hex-encoded string)
     */
    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    /**
     * Returns the size of the object, in bytes
     *
     * @return The size of the object in bytes
     * @throws HttpException
     * @throws IOException
     */
    public long getSize() throws HttpException, IOException {
        if(-1 == size) {
            this.getMetaData();
        }
        return size;
    }

    /**
     * Set the size of the object
     *
     * @param size The size, in bytes
     */
    public void setSize(long size) {
        this.size = size;
    }

    public String getLastModified() {
        return last_modified;
    }

    public void setLastModified(String last_modified) {
        this.last_modified = last_modified;
    }

    /**
     * Download the object and write it to a local file
     *
     * @param localFile The file
     * @return The number of bytes written
     * @throws FileNotFoundException Could not find the local file (does the path to it exist?)
     * @throws HttpException         There was an error communicating with the server
     * @throws IOException           There was an I/O exception communicating with the server or writing the file.
     */
    public long writeObjectToFile(File localFile) throws FileNotFoundException, HttpException, IOException {
        long bytesWritten = 0;
        FileOutputStream out = new FileOutputStream(localFile);
        InputStream in = getObjectAsStream();
        byte[] data = new byte[1024];

        int ret = in.read(data, 0, data.length);
        if(ret == -1) {
            out.write(data, 0, data.length);
            return data.length;
        }

        while(ret != -1) {
            bytesWritten += ret;
            out.write(data, 0, ret);
            ret = in.read(data, 0, data.length);
        }
        out.flush();
        out.close();
        in.close();

        return bytesWritten;
    }

    /**
     * Returns an inputStream with the contents of the object
     *
     * @return An inputStream that will return the contents of the object
     * @throws HttpException There was an error communicating with the server
     * @throws IOException   There was an I/O exception communicating with the server or writing the file.
     */
    public InputStream getObjectAsStream() throws HttpException, IOException {
        return client.getObjectAsStream(container, name);
    }

    /**
     * Download the contents of the object
     *
     * @return The content of the object
     * @throws HttpException There was an error communicating with the server
     * @throws IOException   There was an I/O exception communicating with the server or writing the file.
     */
    public byte[] getObject() throws HttpException, IOException {
        return client.getObject(container, name);
    }

    /**
     * Return any metadata associated with this object
     *
     * @return The metadata
     * @throws HttpException There was an error communicating with the server
     * @throws IOException   There was an I/O exception communicating with the server or writing the file.
     */
    public FilesObjectMetaData getMetaData() throws HttpException, IOException {
        FilesObjectMetaData metaData = client.getObjectMetaData(container, name);
        if(metaData != null) {
            setMd5sum(metaData.getETag());
            setSize(Long.parseLong(metaData.getContentLength()));
            setLastModified(metaData.getLastModified());
            setMimeType(metaData.getMimeType());
        }
        return metaData;
    }

    /**
     * @param client The new client for this object
     */
    void setClient(FilesClient client) {
        this.client = client;
    }

    /**
     * @param container The new container
     */
    void setContainer(String container) {
        this.container = container;
    }

    /**
     * Returns the size as a human readable string, rounding to the nearest KB/MB/GB
     *
     * @return The size of the object as a human readable string.
     * @throws HttpException There was an error communicating with the server
     * @throws IOException   There was an I/O exception communicating with the server or writing the file.
     */
    public String getSizeString() throws HttpException, IOException {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        //Make sure the size is correct.
        if(-1 == size) {
            this.getMetaData();
        }

        //KB
        if(size > gb) {
            return (size / gb) + " GB";
        }
        else if(size > mb) {
            return (size / mb) + " MB";
        }
        else if(size > kb) {
            return (size / kb) + " KB";
        }
        else {
            return getSize() + " Bytes";
        }

    }
}
