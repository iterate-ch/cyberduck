package ch.cyberduck.core.ctera.model;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("obj")
public class Attachment {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "att")
    private List<Attribute> attributes;

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public Attachment setAttributes(final List<Attribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attribute {
        @JacksonXmlProperty(isAttribute = true)
        private String id;

        private String val;

        @JacksonXmlProperty(localName = "obj")
        private AttachedMobileDeviceParams params;

        public String getId() {
            return id;
        }

        public Attribute setId(final String id) {
            this.id = id;
            return this;
        }

        public String getVal() {
            return val;
        }

        public Attribute setVal(final String val) {
            this.val = val;
            return this;
        }

        public AttachedMobileDeviceParams getParams() {
            return params;
        }

        public Attribute setParams(final AttachedMobileDeviceParams params) {
            this.params = params;
            return this;
        }
    }

    @JsonRootName("obj")
    public static class AttachedMobileDeviceParams {

        @JacksonXmlProperty(isAttribute = true, localName = "class")
        private String clazz = this.getClass().getSimpleName();

        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Attribute> att;

        public List<Attribute> getAtt() {
            return att;
        }

        public AttachedMobileDeviceParams setAtt(final List<Attribute> att) {
            this.att = att;
            return this;
        }

        public String getClazz() {
            return clazz;
        }
    }
}
