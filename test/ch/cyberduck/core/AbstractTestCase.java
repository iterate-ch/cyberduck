package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import junit.framework.TestCase;
import ch.cyberduck.core.i18n.LocaleFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import java.io.File;
import java.util.*;

/**
 * @version $Id$
 */
public class AbstractTestCase extends TestCase {

    public AbstractTestCase(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        LocaleFactory.addFactory(Factory.NATIVE_PLATFORM, new LocaleFactory() {
            @Override
            protected ch.cyberduck.core.i18n.Locale create() {
                return new ch.cyberduck.core.i18n.Locale() {
                    @Override
                    public String get(String key, String table) {
                        return key;
                    }
                };
            }
        });
        LocalFactory.addFactory(Factory.NATIVE_PLATFORM, new LocalFactory() {
            @Override
            protected Local create() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Local create(Local parent, String name) {
                return new TestLocal(parent, name);
            }

            @Override
            protected Local create(String parent, String name) {
                return new TestLocal(parent, name);
            }

            @Override
            protected Local create(String path) {
                return new TestLocal(path);
            }

            @Override
            protected Local create(File path) {
                return new TestLocal(path);
            }
        });
        PreferencesFactory.addFactory(Factory.NATIVE_PLATFORM, new PreferencesFactory() {
            @Override
            protected Preferences create() {
                return new Preferences() {
                    @Override
                    public void setProperty(String property, String value) {
                        ;
                    }

                    @Override
                    public void setProperty(String property, List<String> values) {
                        ;
                    }

                    @Override
                    public void deleteProperty(String property) {
                        ;
                    }

                    @Override
                    public List<String> getList(String property) {
                        return Collections.emptyList();
                    }

                    @Override
                    public void save() {
                        ;
                    }

                    @Override
                    protected void load() {
                        ;
                    }

                    @Override
                    public String locale() {
                        return Locale.getDefault().toString();
                    }

                    @Override
                    public List<String> applicationLocales() {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<String> systemLocales() {
                        return Collections.emptyList();
                    }
                };
            }
        });
        SerializerFactory.addFactory(Factory.NATIVE_PLATFORM, new SerializerFactory() {
            @Override
            protected Serializer create() {
                return new Serializer() {
                    private Map<String, Object> impl = new HashMap<String, Object>();

                    public void setStringForKey(String value, String key) {
                        impl.put(key, value);
                    }

                    public void setObjectForKey(Serializable value, String key) {
                        impl.put(key, value.<Map>getAsDictionary());
                    }

                    public <T extends Serializable> void setListForKey(List<T> value, String key) {
                        List<Map> list = new ArrayList<Map>();
                        for(Serializable serializable : value) {
                            list.add(serializable.<Map>getAsDictionary());
                        }
                        impl.put(key, list);
                    }

                    public Map<String, Object> getSerialized() {
                        return impl;
                    }
                };
            }
        });
        DeserializerFactory.addFactory(Factory.NATIVE_PLATFORM, new DeserializerFactory<Map<String, Object>>() {
            @Override
            protected Object create() {
                return this.create(Collections.<String, Object>emptyMap());
            }

            @Override
            protected Deserializer create(final Map<String, Object> dict) {
                return new Deserializer() {
                    public String stringForKey(String key) {
                        return (String) dict.get(key);
                    }

                    public Object objectForKey(String key) {
                        return dict.get(key);
                    }

                    public List listForKey(String key) {
                        return (List) dict.get(key);
                    }
                };
            }
        });
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static class TestLocal extends Local {
        public TestLocal(Local parent, String name) {
            super(parent, name);
        }

        public TestLocal(String parent, String name) {
            super(parent, name);
        }

        public TestLocal(String path) {
            super(path);
        }

        public TestLocal(File path) {
            super(path);
        }

        @Override
        public void setIcon(int progress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trash() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean open() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bounce() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setQuarantine(String originUrl, String dataUrl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWhereFrom(String dataUrl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writePermissions(Permission perm, boolean recursive) {
            throw new UnsupportedOperationException();
        }

        public void setStorageClass(String redundancy) {
            throw new UnsupportedOperationException();
        }

        public String getStorageClass() {
            throw new UnsupportedOperationException();
        }

        public void setVersionId(String versionId) {
            throw new UnsupportedOperationException();
        }

        public String getVersionId() {
            throw new UnsupportedOperationException();
        }
    }
}
