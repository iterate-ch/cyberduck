/**
 * 
 */
package com.mosso.client.cloudfiles;

/**
 * @author lvaughn
 *
 */
public class FilesCDNContainer {
	private boolean enabled;
//	private String userAgentACL;
//	private String referrerACL;
	private int ttl;
	private String cdnURL;
	private String name;
	
	public FilesCDNContainer() {
	}

	public FilesCDNContainer(String cdnURL) {
		this.cdnURL = cdnURL;
	}
	
	public FilesCDNContainer(String name, boolean enabled, int ttl) {
        this.name = name;
		this.enabled = enabled;
		this.ttl = ttl;
		//this(enabled, ttl, null, null);
	}
	
//	public FilesCDNContainer(boolean enabled, int ttl, String userAgentACL, String referrerACL) {
//		this.enabled = enabled;
//		this.ttl = ttl;
//		this.userAgentACL = userAgentACL;
//		this.referrerACL = referrerACL;
//	}
	
	/**
	 * @return Is this container CDN enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
//	/**
//	 * @return the userAgentACL
//	 */
//	public String getUserAgentACL() {
//		return userAgentACL;
//	}
//	/**
//	 * @param userAgentACL the userAgentACL to set
//	 */
//	public void setUserAgentACL(String userAgentACL) {
//		this.userAgentACL = userAgentACL;
//	}
//	/**
//	 * @return the refererACL
//	 */
//	public String getReferrerACL() {
//		return referrerACL;
//	}
//	/**
//	 * @param refererACL the refererACL to set
//	 */
//	public void setReferrerACL(String referrerACL) {
//		this.referrerACL = referrerACL;
//	}
	/**
	 * @return the ttl
	 */
	public int getTtl() {
		return ttl;
	}
	/**
	 * @param ttl the ttl to set
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * @return the cdnURL
	 */
	public String getCdnURL() {
		return cdnURL;
	}

	/**
	 * @param cdnURL the cdnURL to set
	 */
	public void setCdnURL(String cdnURL) {
		this.cdnURL = cdnURL;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
