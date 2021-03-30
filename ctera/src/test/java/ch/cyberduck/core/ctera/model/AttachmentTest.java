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

import org.junit.Test;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static org.junit.Assert.assertEquals;

public class AttachmentTest {

    @Test
    public void testSerialize() throws JsonProcessingException {

        final String expected = "<obj>\n" +
            "  <attr id=\"type\">user-defined</attr>\n" +
            "  <attr id=\"name\">attachMobileDevice</attr>\n" +
            "  <obj class=\"AttachedMobileDeviceParams\">\n" +
            "    <attr id=\"deviceType\">Mobile</attr>\n" +
            "    <attr id=\"serverName\">mountainduck.na.ctera.me</attr>\n" +
            "    <attr id=\"deviceMac\">myMacAddress</attr>\n" +
            "    <attr id=\"ssoActivationCode\">mySsoActivationCode</attr>\n" +
            "    <attr id=\"password\"/>\n" +
            "    <attr id=\"hostname\">myHostname</attr>\n" +
            "  </obj>\n" +
            "</obj>\n";

        final Attachment attachment = new Attachment();
        final ArrayList<Attachment.Attribute> attributes = new ArrayList<>();

        final Attachment.Attribute type = new Attachment.Attribute();
        type.setId("type");
        type.setValue("user-defined");
        attributes.add(type);
        final Attachment.Attribute name = new Attachment.Attribute();
        name.setId("name");
        name.setValue("attachMobileDevice");
        attributes.add(name);
        attachment.setAttr(attributes);

        final Attachment.AttachedMobileDeviceParams params = new Attachment.AttachedMobileDeviceParams();
        attachment.setMobileParams(params);
        final ArrayList<Attachment.Attribute> paramAttributes = new ArrayList<>();
        params.setAttr(paramAttributes);
        final Attachment.Attribute deviceType = new Attachment.Attribute();
        deviceType.setId("deviceType");
        deviceType.setValue("Mobile");
        paramAttributes.add(deviceType);
        final Attachment.Attribute serverName = new Attachment.Attribute();
        serverName.setId("serverName");
        serverName.setValue("mountainduck.na.ctera.me");
        paramAttributes.add(serverName);
        final Attachment.Attribute deviceMac = new Attachment.Attribute();
        deviceMac.setId("deviceMac");
        deviceMac.setValue("myMacAddress");
        paramAttributes.add(deviceMac);
        final Attachment.Attribute ssoActivationCode = new Attachment.Attribute();
        ssoActivationCode.setId("ssoActivationCode");
        ssoActivationCode.setValue("mySsoActivationCode");
        paramAttributes.add(ssoActivationCode);
        final Attachment.Attribute password = new Attachment.Attribute();
        password.setId("password");
        paramAttributes.add(password);
        final Attachment.Attribute hostname = new Attachment.Attribute();
        hostname.setId("hostname");
        hostname.setValue("myHostname");
        paramAttributes.add(hostname);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String xml = xmlMapper.writeValueAsString(attachment);
        assertEquals(expected, xml);
    }
}
