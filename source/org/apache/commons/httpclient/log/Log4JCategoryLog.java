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

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * @author Rod Waldhoff
 * @version $Id$
 */
public class Log4JCategoryLog  implements Log {
    Category _category = null;

    public Log4JCategoryLog(String name) {
        _category = Category.getInstance(name);
    }

    public final void debug(Object message) {
        _category.debug(message);
    }

    public final void debug(Object message, Throwable t) {
        _category.debug(message,t);
    }

    public final void info(Object message) {
        _category.info(message);
    }

    public final void info(Object message, Throwable t) {
        _category.info(message,t);
    }

    public final void warn(Object message) {
        _category.warn(message);
    }
    public final void warn(Object message, Throwable t) {
        _category.warn(message,t);
    }

    public final void error(Object message) {
        _category.error(message);
    }

    public final void error(Object message, Throwable t) {
        _category.error(message,t);
    }

    public final void fatal(Object message) {
        _category.fatal(message);
    }

    public final void fatal(Object message, Throwable t) {
        _category.fatal(message,t);
    }

    public final boolean isDebugEnabled() {
        return _category.isDebugEnabled();
    }

    public final boolean isInfoEnabled() {
        return _category.isInfoEnabled();
    }

    public final boolean isEnabledFor(Priority p) {
        return _category.isEnabledFor(p);
    }

    public final void setLevel(int level) {
        switch(level) {
            case Log.DEBUG:
                _category.setPriority(Priority.DEBUG);
                break;
            case Log.INFO:
                _category.setPriority(Priority.INFO);
                break;
            case Log.WARN:
                _category.setPriority(Priority.WARN);
                break;
            case Log.ERROR:
                _category.setPriority(Priority.ERROR);
                break;
            case Log.FATAL:
                _category.setPriority(Priority.FATAL);
                break;
            default:
                _category.setPriority(Priority.toPriority(level));
                break;
        }
    }

    public final int getLevel() {
        return _category.getPriority().toInt();
    }

}
