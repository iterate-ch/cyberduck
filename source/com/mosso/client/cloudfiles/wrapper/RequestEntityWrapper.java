/**
 * 
 */
package com.mosso.client.cloudfiles.wrapper;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

import com.mosso.client.cloudfiles.IFilesTransferCallback;

/**
 * @author lvaughn
 *
 */
public class RequestEntityWrapper implements RequestEntity {
	private RequestEntity entity;
	private IFilesTransferCallback callback = null;
	
	public RequestEntityWrapper(RequestEntity entity, IFilesTransferCallback callback) {
		this.entity = entity;
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#getContentLength()
	 */
	public long getContentLength() {
		return entity.getContentLength();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#getContentType()
	 */
	public String getContentType() {
		return entity.getContentType();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#isRepeatable()
	 */
	public boolean isRepeatable() {
		return entity.isRepeatable();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.methods.RequestEntity#writeRequest(java.io.OutputStream)
	 */
	public void writeRequest(OutputStream stream) throws IOException {
		entity.writeRequest(new OutputStreamWrapper(stream, callback));
		
	}
	
}
