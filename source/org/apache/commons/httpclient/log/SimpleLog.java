/*
 * $Header$
 * $Revision$
 * $Date$
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient.log;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Rod Waldhoff
 * @version $Id$
 */
public class SimpleLog implements Log {
	static protected final Properties _simplelogProps = new Properties();
	static protected boolean _showlogname = false;
	static protected boolean _showtime = false;
	static protected DateFormat _df = null;

	static {
		// add all system props that start with "httpclient."
		Enumeration enum = System.getProperties().propertyNames();
		while (enum.hasMoreElements()) {
			String name = (String) (enum.nextElement());
			if (null != name && name.startsWith("httpclient.")) {
				_simplelogProps.setProperty(name, System.getProperty(name));
			}
		}

		// add props from the resource simplelog.properties
		InputStream in = ClassLoader.getSystemResourceAsStream("simplelog.properties");
		if (null != in) {
			try {
				_simplelogProps.load(in);
				in.close();
			}
			catch (java.io.IOException e) {
				// ignored
			}
		}
		try {
		}
		catch (Throwable t) {
			// ignored
		}
		_showlogname = "true".equalsIgnoreCase(_simplelogProps.getProperty("httpclient.simplelog.showlogname", "true"));
		_showtime = "true".equalsIgnoreCase(_simplelogProps.getProperty("httpclient.simplelog.showdate", "true"));
		if (_showtime) {
			_df = new SimpleDateFormat(_simplelogProps.getProperty("httpclient.simplelog.dateformat", "yyyy/MM/dd HH:mm:ss:SSS zzz"));
		}
	}

	protected int _logLevel = Log.ERROR;

	protected String _name = null;

	public SimpleLog(String name) {
		_name = name;

		String lvl = _simplelogProps.getProperty("httpclient.simplelog.log." + _name);
		int i = String.valueOf(name).lastIndexOf(".");
		while (null == lvl && i > -1) {
			name = name.substring(0, i);
			lvl = _simplelogProps.getProperty("httpclient.simplelog.log." + name);
			i = String.valueOf(name).lastIndexOf(".");
		}
		if (null == lvl) {
			lvl = _simplelogProps.getProperty("httpclient.simplelog.defaultlog");
		}

		if ("debug".equalsIgnoreCase(lvl)) {
			_logLevel = DEBUG;
		}
		else if ("info".equalsIgnoreCase(lvl)) {
			_logLevel = INFO;
		}
		else if ("warn".equalsIgnoreCase(lvl)) {
			_logLevel = WARN;
		}
		else if ("error".equalsIgnoreCase(lvl)) {
			_logLevel = ERROR;
		}
		else if ("fatal".equalsIgnoreCase(lvl)) {
			_logLevel = FATAL;
		}
	}

	protected void log(int type, Object message, Throwable t) {
		if (_logLevel <= type) {
			StringBuffer buf = new StringBuffer();
			if (_showtime) {
				buf.append(_df.format(new Date()));
				buf.append(" ");
			}
			switch (type) {
				case DEBUG:
					buf.append("[DEBUG] ");
					break;
				case INFO:
					buf.append("[INFO] ");
					break;
				case WARN:
					buf.append("[WARN] ");
					break;
				case ERROR:
					buf.append("[ERROR] ");
					break;
				case FATAL:
					buf.append("[FATAL] ");
					break;
			}
			if (_showlogname) {
				buf.append(String.valueOf(_name)).append(" - ");
			}
			buf.append(String.valueOf(message));
			if (t != null) {
				buf.append(" <");
				buf.append(t.toString());
				buf.append(">");
				t.printStackTrace();
			}
			System.out.println(buf.toString());
		}
	}

	public final void debug(Object message) {
		log(Log.DEBUG, message, null);
	}

	public final void debug(Object message, Throwable t) {
		log(Log.DEBUG, message, t);
	}

	public final void info(Object message) {
		log(Log.INFO, message, null);
	}

	public final void info(Object message, Throwable t) {
		log(Log.INFO, message, t);
	}

	public final void warn(Object message) {
		log(Log.WARN, message, null);
	}

	public final void warn(Object message, Throwable t) {
		log(Log.WARN, message, t);
	}

	public final void error(Object message) {
		log(Log.ERROR, message, null);
	}

	public final void error(Object message, Throwable t) {
		log(Log.ERROR, message, t);
	}

	public final void fatal(Object message) {
		log(Log.FATAL, message, null);
	}

	public final void fatal(Object message, Throwable t) {
		log(Log.FATAL, message, t);
	}

	public final boolean isDebugEnabled() {
		return (_logLevel <= DEBUG);
	}

	public final boolean isInfoEnabled() {
		return (_logLevel <= INFO);
	}

	public final void setLevel(int level) {
		_logLevel = level;
	}

	public final int getLevel() {
		return _logLevel;
	}

}
