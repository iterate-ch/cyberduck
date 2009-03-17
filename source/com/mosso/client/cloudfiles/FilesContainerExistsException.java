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
public class FilesContainerExistsException extends FilesException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7282149064519490145L;

	/**
	 * @param message
	 * @param httpHeaders
	 * @param httpStatusLine
	 */
	public FilesContainerExistsException(String message, Header[] httpHeaders,
			StatusLine httpStatusLine) {
		super(message, httpHeaders, httpStatusLine);
	}

}
