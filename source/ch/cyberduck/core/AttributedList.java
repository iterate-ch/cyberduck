package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSObject;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Facade for com.apple.cocoa.foundation.NSMutableArray
 *
 * @version $Id$
 */
public class AttributedList extends NSObject implements List {

    private NSMutableArray content = new NSMutableArray();

    //primary attributes
    protected static final String FILTER = "FILTER";
    protected static final String COMPARATOR = "COMPARATOR";

    protected static final String HIDDEN = "HIDDEN";

    /**
     * file listing has changed; the cached version should be superseded
     */
    private static final String INVALID = "INVALID";

    /**
     * file listing is not readable; permission issue
     */
    private static final String READABLE = "READABLE";

    private Attributes attributes;

    /**
     * Initialize an attributed list with default attributes
     */
    public AttributedList() {
        this.attributes = new Attributes();
    }

    /**
     * Container for file listing attributes, such as a sorting comparator and filter
     *
     * @see PathFilter
     * @see BrowserComparator
     */
    public class Attributes extends HashMap {
        /**
         * Initialize with default values
         */
        public Attributes() {
            this.put(FILTER, new NullPathFilter());
            this.put(COMPARATOR, new NullComparator());
            this.put(HIDDEN, new HashSet());
            this.put(INVALID, Boolean.FALSE);
            this.put(READABLE, Boolean.TRUE);
        }

        public Attributes(Comparator comparator, PathFilter filter) {
            this.put(COMPARATOR, comparator);
            this.put(FILTER, filter);
            this.put(HIDDEN, new java.util.HashSet());
            this.put(INVALID, Boolean.FALSE);
            this.put(READABLE, Boolean.TRUE);
        }

        public void addHidden(Path child) {
            ((Set) this.get(HIDDEN)).add(child);
        }

        public void setReadable(boolean readable) {
            this.put(READABLE, Boolean.valueOf(readable));
        }

        public boolean isReadable() {
            return this.get(READABLE).equals(Boolean.TRUE);
        }

        /**
         * Mark cached listing as superseded
         */
        public void setDirty(boolean dirty) {
            this.put(INVALID, Boolean.valueOf(dirty));
            if(dirty) {
                this.put(READABLE, Boolean.TRUE);
            }
        }

        /**
         *
         * @return true if the listing should be superseded
         */
        public boolean isDirty() {
            return this.get(INVALID).equals(Boolean.TRUE);
        }
    }

    public Attributes attributes() {
        return attributes;
    }

    public int size() {
        return this.content.count();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean contains(Object object) {
        return this.content.containsObject(object);
    }

    public Iterator iterator() {
        return new Iterator() {
            private int pos = 0;
            private int size = AttributedList.this.size();
            private int last = -1;

            public boolean hasNext() {
                return pos < size;
            }

            public Object next() {
                if(pos == size) {
                    throw new NoSuchElementException();
                }
                last = pos;
                return AttributedList.this.get(pos++);
            }

            public void remove() {
                if(last < 0) {
                    throw new IllegalStateException();
                }
                AttributedList.this.remove(last);
                pos--;
                size--;
                last = -1;
            }
        };
    }

    /**
     * @return an array containing all of the elements in this collection
     */
    public Object[] toArray() {
        Object[] array = new Object[this.size()];
        int i = 0;
        for(Iterator iter = this.iterator(); iter.hasNext(); i++) {
            array[i] = iter.next();
        }
        return array;
    }

    public Object[] toArray(Object[] objects) {
        int size = this.size();
        if(objects.length < size) {
            objects = (Object[]) Array.newInstance(objects.getClass().getComponentType(), size);
        }
        else if(objects.length > size) {
            objects[size] = null;
        }
        Iterator iter = iterator();
        for(int pos = 0; pos < size; pos++) {
            objects[pos] = iter.next();
        }
        return objects;
    }

    /**
     * @param object Path
     * @return true if this collection changed as a result of the call
     */
    public boolean add(Object object) {
        this.content.addObject(object);
        return true;
    }

    /**
     * @param object Path
     * @return true if this collection changed as a result of the call
     */
    public boolean remove(Object object) {
        this.content.removeObject(object);
        return true;
    }

    /**
     * @param collection
     * @return true if this collection contains all of the elements in the specified collection
     */
    public boolean containsAll(java.util.Collection collection) {
        for(Iterator iter = collection.iterator(); iter.hasNext();) {
            if(!this.contains(iter.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param collection
     * @return true if this collection changed as a result of the call
     */
    public boolean addAll(java.util.Collection collection) {
        this.content.addObjectsFromArray(new NSArray(collection.toArray()));
        return true;
    }

    public boolean addAll(int i, java.util.Collection collection) {
        for(Iterator iter = collection.iterator(); iter.hasNext();) {
            this.content.insertObjectAtIndex(iter.next(), i);
            i++;
        }
        return true;
    }

    /**
     * @param collection
     * @return true if this collection changed as a result of the call
     */
    public boolean removeAll(java.util.Collection collection) {
        this.content.removeObjectsInArray(new NSArray(collection.toArray()));
        return true;
    }

    /**
     * @param collection
     * @return true if this collection changed as a result of the call
     */
    public boolean retainAll(java.util.Collection collection) {
        boolean changed = false;
        for(Iterator iter = this.iterator(); iter.hasNext();) {
            if(!collection.contains(iter.next())) {
                iter.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Removes all of the elements from this collection
     */
    public void clear() {
        this.content.removeAllObjects();
    }

    public boolean equals(Object object) {
        return this.content.equals(object);
    }

    public int hashCode() {
        return this.content.hashCode();
    }

    public Object get(int i) {
        if(i >= this.size()) {
            return null;
        }
        return this.content.objectAtIndex(i);
    }

    /**
     * @param i      position
     * @param object
     * @return the element previously at the specified position.
     */
    public Object set(int i, Object object) {
        Object previous = this.get(i);
        this.content.replaceObjectAtIndex(i, object);
        return previous;
    }

    public void add(int i, Object object) {
        this.content.insertObjectAtIndex(object, i);
    }

    /**
     * @param i
     * @return the element previously at the specified position.
     */
    public Object remove(int i) {
        if(i >= this.size()) {
            return null;
        }
        Object previous = this.get(i);
        this.content.removeObjectAtIndex(i);
        return previous;
    }

    public int indexOf(Object object) {
        int i = this.content.indexOfObject(object);
        if(i == NSArray.NotFound) {
            return -1;
        }
        return i;
    }

    public int lastIndexOf(Object o) {
        int pos = size();
        ListIterator itr = listIterator(pos);
        while(--pos >= 0) {
            if(o.equals(itr.previous())) {
                return pos;
            }
        }
        return -1;
    }

    /**
     * @return a list iterator of the elements in this list (in proper sequence), starting at the specified
     *         position in this list.
     */
    public ListIterator listIterator() {
        return this.listIterator(0);
    }

    /**
     * @param index index of first element to be returned from the list iterator (by a call to the next method).
     * @return a list iterator of the elements in this list (in proper sequence), starting at the specified
     * position in this list.
     */
    public ListIterator listIterator(final int index) {
        return new ListIterator() {
            private int position = index;
            private int lastReturned = -1;
            private int size = AttributedList.this.size();

            public boolean hasNext() {
                return position < size;
            }

            public boolean hasPrevious() {
                return position > 0;
            }

            public Object next() {
                if(position == size) {
                    throw new NoSuchElementException();
                }
                lastReturned = position;
                return AttributedList.this.get(position++);
            }

            public Object previous() {
                if(position == 0) {
                    throw new NoSuchElementException();
                }
                lastReturned = --position;
                return AttributedList.this.get(lastReturned);
            }

            public int nextIndex() {
                return position;
            }

            public int previousIndex() {
                return position - 1;
            }

            public void remove() {
                if(lastReturned < 0) {
                    throw new IllegalStateException();
                }
                AttributedList.this.remove(lastReturned);
                size--;
                position = lastReturned;
                lastReturned = -1;
            }

            public void set(Object o) {
                if(lastReturned < 0) {
                    throw new IllegalStateException();
                }
                AttributedList.this.set(lastReturned, o);
            }

            public void add(Object o) {
                AttributedList.this.add(position++, o);
                size++;
                lastReturned = -1;
            }
        };
    }

    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }
}


