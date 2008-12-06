/**
 * 
 */
package com.mosso.client.cloudfiles;

/**
 * @author lvaughn
 *
 */
public class FilesAccountInfo {
	private long bytesUsed;
	private int containerCount;
	
	public FilesAccountInfo(long bytes, int containers) {
		bytesUsed = bytes;
		containerCount = containers;
	}
	/**
	 * Returns the total number of bytes used by all objects in a given account.
	 * 
	 * @return the bytesUsed
	 */
	public long getBytesUsed() {
		return bytesUsed;
	}
	/**
	 * @param bytesUsed The number of bytes in the account
	 */
	public void setBytesUsed(long bytesUsed) {
		this.bytesUsed = bytesUsed;
	}
	/**
	 * The number of containers in a given account.
	 * 
	 * @return the containerCount
	 */
	public int getContainerCount() {
		return containerCount;
	}
	/**
	 * @param containerCount the containerCount to set
	 */
	public void setContainerCount(int containerCount) {
		this.containerCount = containerCount;
	}
	
	
}
