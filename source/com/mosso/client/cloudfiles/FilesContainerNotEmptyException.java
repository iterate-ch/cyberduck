/**
 * 
 */
package com.mosso.client.cloudfiles;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;

/**
 * @author lvaughn
 *
 */
@SuppressWarnings("serial")
public class FilesContainerNotEmptyException extends FilesException {
	/**
	 * @param message
	 * @param httpHeaders
	 * @param httpStatusLine
	 */
	public FilesContainerNotEmptyException(String message,
			Header[] httpHeaders, StatusLine httpStatusLine) {
		super(message, httpHeaders, httpStatusLine);
	}
	
}
