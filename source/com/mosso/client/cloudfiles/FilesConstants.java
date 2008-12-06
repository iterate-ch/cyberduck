/*
 * See COPYING for license information.
 */ 

package com.mosso.client.cloudfiles;

import java.util.Properties;
import java.io.IOException;
import org.apache.log4j.Logger;

public class FilesConstants
{
    private static Logger logger = Logger.getLogger(FilesConstants.class);
    
    public static final String USER_AGENT = "java-cloudfiles/1.2.0";
    
    /** HTTP Header token that identifies the username to Cloud Files **/
    public static final String X_STORAGE_USER   = "x-auth-user";
    /** HTTP header token that identifies the password to Cloud Files **/
    public static final String X_STORAGE_PASS   = "x-auth-key";
    /** HTTP header token that identifies the Storage URL after a successful user login to Cloud Files **/
    public static final String X_STORAGE_URL    = "X-Storage-Url";
    /** HTTP header that identifies the CDN Management URL after a successful login to Cloud Files **/
    public static final String X_CDN_MANAGEMENT_URL = "X-CDN-Management-URL";
    /** HTTP header token that identifies the Storage Token after a successful user login to Cloud Files **/
    public static final String X_AUTH_TOKEN  = "X-Auth-Token";
    /** HTTP header token that is returned on a HEAD request against a Container.  The value of this header is the number of Objects in the Container **/
    public static final String X_CONTAINER_OBJECT_COUNT = "X-Container-Object-Count";
    /** HTTP header token that is returned on a HEAD request against a Container.  The value of this header is the number of Objects in the Container **/
    public static final String X_CONTAINER_BYTES_USED = "X-Container-Bytes-Used";
    /** HTTP header token that is returned on a HEAD request against an Account.  The value of this header is the number of Containers in the Account **/
    public static final String X_ACCOUNT_CONTAINER_COUNT = "X-Account-Container-Count";
    /** HTTP header token that is returned on a HEAD request against an Account.  The value of this header is the total size of the Objects in the Account **/
    public static final String X_ACCOUNT_BYTES_USED = "X-Account-Bytes-Used";
    /** HTTP header token that is returned by calls to the CDN Management API **/
    public static final String X_CDN_URI = "X-CDN-URI";
    /** HTTP header token that is returned by calls to the CDN Management API **/
    public static final String X_CDN_TTL = "X-TTL";
    /** HTTP header token that is returned by calls to the CDN Management API **/
    public static final String X_CDN_ENABLED = "X-CDN-Enabled";
    /** HTTP header token that is returned by calls to the CDN Management API **/
    public static final String X_CDN_USER_AGENT_ACL = "X-User-Agent-ACL";
    /** HTTP header token that is returned by calls to the CDN Management API **/
    public static final String X_CDN_REFERRER_ACL = "X-Referrer-ACL ";
    /** HTTP Header used by Cloud Files for the MD5Sum of the object being created in a Container **/
    public static final String E_TAG = "ETag";
    /** These constants are used for performing queries on the content of a container **/
    public static final String LIST_CONTAINER_NAME_QUERY = "prefix";
    public static final String LIST_CONTAINER_LIMIT_OBJ_COUNT_QUERY = "limit";
    public static final String LIST_CONTAINER_START_OFFSET_QUERY = "offset";
    
    /** Prefix Cloud Files expects on all Meta data headers on Objects **/
    public static final String X_OBJECT_META = "X-Object-Meta-";

    /** This code represents the HTTP code returned by Cloud Files that represents Container created **/
    public static final int CONTAINER_CREATED = 201;
    /** This code represents the HTTP code returned by Cloud Files that represents Container existed **/
    public static final int CONTAINER_EXISTED = 202;
    /** This code represents the HTTP code returned by Cloud Files to indicate a Container was Deleted **/
    public static final int CONTAINER_DELETED = 204;
    /** This code represents the HTTP code returned by Cloud Files to indicate a Container was not found  **/
    public static final int CONTAINER_NOT_FOUND = 404;
    /** This code represents the HTTP code returned by Cloud Files to indicate a Container was not Empty  **/
    public static final int CONTAINER_NOT_EMPTY = 409;
    /** This code represents an object being created in Cloud Files **/
    public static final int OBJECT_CREATED = 201;

    /** This code represents an object being created in Cloud Files **/
    public static final int OBJECT_DELETED = 204;
    
    /** This code represents an object not being found in Cloud Files **/
    public static final int OBJECT_NOT_FOUND = 404;

    public static Properties MIMETYPES = new Properties ();

    static {
    	try
        {
    		MIMETYPES.load (FilesConstants.class.getResourceAsStream("MIME.types"));
        }
        catch (IOException err)
        {
            logger.warn ("Could not load MIME.types all refrences to FilesConstants.MIMETYPES will return null.", err);
        }
    }

    /**
     * Convenience method to get a MIME Type.  If none is found it will return "application/octet-stream"
     * 
     * @param fileExt
     * @return The suggested MIME type for the file extention.  
     */
    public static String getMimetype (String fileExt)
    {
    	return FilesConstants.MIMETYPES.getProperty(fileExt, "application/octet-stream");
    }
}
