/**
 * 
 */
package com.mosso.client.cloudfiles;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.HttpException;

/**
 * @author lvaughn
 *
 */
@SuppressWarnings("serial")
public class FilesException extends HttpException {
    private Header [] httpHeaders;
    private StatusLine httpStatusLine;

    /**
     * An exception generated when a client tries to do something they aren't authorized to do.  
     * 
     * @param message        The message
     * @param httpHeaders    The returned HTTP headers
     * @param httpStatusLine The HTTP Status lined returned
     */
    public FilesException(String message, Header [] httpHeaders, StatusLine httpStatusLine)
    {
    	super (message);
    	this.httpHeaders = httpHeaders;
    	this.httpStatusLine = httpStatusLine;
    }
    
    public FilesException(String message, Throwable cause) {
    	super(message, cause);
    }

    /**
     * @return The HTTP headers returned by the server
     */
    public Header[] getHttpHeaders ()
    {
        return httpHeaders;
    }

    /**
     * @return The HTTP Headers returned by the server in a human-readable string.
     */
    public String getHttpHeadersAsString ()
    {
    	if (httpHeaders == null) return "";
    	
        StringBuffer httpHeaderString = new StringBuffer();
        for (Header h: httpHeaders)
            httpHeaderString.append(h.toExternalForm());

        return httpHeaderString.toString();
    }

    /**
     * @return The HTTP status line from the server
     */
    public StatusLine getHttpStatusLine ()
    {
        return httpStatusLine;
    }

    /**
     * @return The numeric HTTP status code from the server
     */
    public int getHttpStatusCode ()
    {
        return httpStatusLine == null? -1 : httpStatusLine.getStatusCode();
    }

    /**
     * @return The HTTP status message from the server
     */
    public String getHttpStatusMessage ()
    {
        return httpStatusLine == null ? null : httpStatusLine.getReasonPhrase();
    }

    /**
     * @return The version of HTTP used.
     */
    public String getHttpVersion ()
    {
        return httpStatusLine == null ? null : httpStatusLine.getHttpVersion();
    }

}
