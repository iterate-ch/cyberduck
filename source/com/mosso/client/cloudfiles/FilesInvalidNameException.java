/**
 * 
 */
package com.mosso.client.cloudfiles;

/**
 * @author lvaughn
 *
 */
public class FilesInvalidNameException extends FilesException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9043382616400647532L;

	public FilesInvalidNameException(String name) {
		super("Invalid name: " + name, null, null);
	}
	
}
