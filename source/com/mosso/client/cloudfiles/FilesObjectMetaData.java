/*
 * See COPYING for license information.
 */ 

package com.mosso.client.cloudfiles;

import java.util.HashMap;
import java.util.Map;

public class FilesObjectMetaData
{
    private String mimeType;
    private String contentLength;
    private String eTag;
    private String lastModified;
    private Map<String, String> metaData = new HashMap<String, String>();

    /**
     * An object storing the metadata for an FS Cloud object
     * 
     * @param mimeType      The MIME type for the object
     * @param contentLength The content-length (e.g., size) of the object
     * @param eTag          The MD5 check-sum of the object's contents
     * @param lastModified  The last time the object was modified.
     */
    public FilesObjectMetaData(String mimeType, String contentLength, String eTag, String lastModified)
    {
        this.mimeType = mimeType;
        this.contentLength = contentLength;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * An object storing the metadata for an FS Cloud object
     * 
     * @param mimeType      The MIME type for the object
     * @param contentLength The content-length (e.g., size) of the object
     * @param lastModified  The last time the object was modified.
     */
    public FilesObjectMetaData(String mimeType, String contentLength, String lastModified)
    {
        this.mimeType = mimeType;
        this.contentLength = contentLength;
        this.lastModified = lastModified;
    }

    /**
     * The last time the object was modified
     * 
     * @return The last modification date
     */
    public String getLastModified()
    {
        return lastModified;
    }

    /**
     * Set the last time the object was modified 
     */
   void setLastModified(String lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * The MIME type of the object
     * 
     * @return The MIME type of the object
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Set's the MIME type of the object
     * 
     * @param mimeType
     */
    void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * The size of the object, in bytes
     * 
     * @return The size of the object
     */
    public String getContentLength()
    {
        return contentLength;
    }

    /**
     * Set the size of the object
     * 
     * @param contentLength The new content length
     */
    void setContentLength(String contentLength)
    {
        this.contentLength = contentLength;
    }

    /**
     * The MD5 checksum represented in a hex-encoded string
     * 
     * @return The eTag
     */
    public String getETag()
    {
        return eTag;
    }

    /**
     * Set the MD5 checksum for this object
     * 
     * @param eTag The new eTag
     */
    void setETag(String eTag)
    {
        this.eTag = eTag;
    }

    /**
     * The metadata associated with this object.
     * 
     * @return The object's metadata
     */
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    /**
     * Set new metatdata for this object.  Warning, this metadata clears out all old metadata.  To add new fields, use 
     * <code>setMetaData</code> instead.
     * 
     * @param metaData The new metadata
     */
    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    /**
     * Add a new meta-data entry (or overwrite an old one)
     * 
     * @param key   The key for this key-value pair
     * @param value The value for this key-value pair
     */
    public void addMetaData (String key, String value)
    {
        metaData.put(FilesConstants.X_OBJECT_META.concat(key), value);
    }

    /** Constructs a new header for a given metadata key
     * @param key The key
     * @return The name of a header for this key
     */
    public String getMetaKey (String key)
    {
        return metaData.get(FilesConstants.X_OBJECT_META.concat(key));
    }

    /**
     * Tests to see if a given metadata key is present for this object
     * 
     * @param key The key to check for
     * @return True if it's present, false otherwise.
     */
    public boolean containesKey (String key)
    {
        return metaData.containsKey(FilesConstants.X_OBJECT_META.concat(key));
    }
}
