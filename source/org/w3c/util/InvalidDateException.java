// InvalidDateException.java
// $Id$
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.util;

/**
 * @author Benoît Mahé (bmahe@w3.org)
 * @version $Revision$
 */
public class InvalidDateException extends Exception {
    private static final long serialVersionUID = -9012791102239300978L;

    public InvalidDateException(String msg) {
        super(msg);
    }
}
