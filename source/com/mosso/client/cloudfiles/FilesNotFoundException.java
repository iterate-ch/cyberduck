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
public class FilesNotFoundException extends FilesException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 111718445621236026L;

	/**
	 * @param message
	 * @param httpHeaders
	 * @param httpStatusLine
	 */
	public FilesNotFoundException(String message, Header[] httpHeaders,
			StatusLine httpStatusLine) {
		super(message, httpHeaders, httpStatusLine);
	}

}
