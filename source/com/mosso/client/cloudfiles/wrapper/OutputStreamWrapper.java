/**
 * 
 */
package com.mosso.client.cloudfiles.wrapper;

import java.io.IOException;
import java.io.OutputStream;

import com.mosso.client.cloudfiles.IFilesTransferCallback;

/**
 * @author lvaughn
 *
 */
public class OutputStreamWrapper extends OutputStream {
	private OutputStream stream;
	private IFilesTransferCallback callback = null;
	private final static int callbackInterval = 1024 * 8;
	private long lastCallback = 0;
	private long bytesTransfered = 0;
	
	public OutputStreamWrapper(OutputStream os, IFilesTransferCallback callback) {
		this.stream = os;
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		stream.write(b);
		++bytesTransfered;
		checkCallback(false);
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		// Short circuit this if we don't have a callback.
		if (callback == null) {
			stream.write(b, off, len);
			return;
		}
		
		// Otherwise, dole this out on chunks
		while(len > 0) {
			int toWrite = Math.min(len, callbackInterval);
			stream.write(b, off, toWrite);
			bytesTransfered += toWrite;
			off += toWrite;
			len -= toWrite;
			checkCallback(false);
		}
		checkCallback(true);
	}
	
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	public void close() throws IOException {
		stream.close();
		checkCallback(true);
	}
	
	public void flush() throws IOException {
		stream.flush();
		checkCallback(true);
	}
	
	private void checkCallback(boolean force) {
		if (callback != null) {
			if ((bytesTransfered - lastCallback >= callbackInterval) || 
				(force && bytesTransfered != lastCallback)) {
				callback.progress(bytesTransfered);
				lastCallback = bytesTransfered;
			}
		}
	}

}
