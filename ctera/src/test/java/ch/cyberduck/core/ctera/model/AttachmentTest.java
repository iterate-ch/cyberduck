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

        final String expected =
            "<obj>\n" +
                "  <att id=\"type\">\n" +
                "    <val>user-defined</val>\n" +
                "  </att>\n" +
                "  <att id=\"name\">\n" +
                "    <val>attachMobileDevice</val>\n" +
                "  </att>\n" +
                "  <att id=\"param\">\n" +
                "    <obj class=\"AttachedMobileDeviceParams\">\n" +
                "      <att id=\"deviceType\">\n" +
                "        <val>Mobile</val>\n" +
                "      </att>\n" +
                "      <att id=\"deviceMac\">\n" +
                "        <val>myMacAddress</val>\n" +
                "      </att>\n" +
                "      <att id=\"ssoActivationCode\">\n" +
                "        <val>mySsoActivationCode</val>\n" +
                "      </att>\n" +
                "      <att id=\"password\"/>\n" +
                "      <att id=\"hostname\">\n" +
                "        <val>myHostname</val>\n" +
                "      </att>\n" +
                "    </obj>\n" +
                "  </att>\n" +
                "</obj>\n";

        final Attachment attachment = new Attachment();
        final ArrayList<Attachment.Attribute> attributes = new ArrayList<>();

        final Attachment.Attribute type = new Attachment.Attribute();
        type.setId("type");
        type.setVal("user-defined");
        attributes.add(type);
        final Attachment.Attribute name = new Attachment.Attribute();
        name.setId("name");
        name.setVal("attachMobileDevice");
        attributes.add(name);
        attachment.setAttributes(attributes);

        final Attachment.AttachedMobileDeviceParams attachedMobileDeviceParams = new Attachment.AttachedMobileDeviceParams();
        final ArrayList<Attachment.Attribute> paramAttributes = new ArrayList<>();
        attachedMobileDeviceParams.setAtt(paramAttributes);
        final Attachment.Attribute methodParams = new Attachment.Attribute();
        methodParams.setId("param");
        methodParams.setParams(attachedMobileDeviceParams);
        attributes.add(methodParams);
        final Attachment.Attribute deviceType = new Attachment.Attribute();
        deviceType.setId("deviceType");
        deviceType.setVal("Mobile");
        paramAttributes.add(deviceType);
        final Attachment.Attribute deviceMac = new Attachment.Attribute();
        deviceMac.setId("deviceMac");
        deviceMac.setVal("myMacAddress");
        paramAttributes.add(deviceMac);
        final Attachment.Attribute ssoActivationCode = new Attachment.Attribute();
        ssoActivationCode.setId("ssoActivationCode");
        ssoActivationCode.setVal("mySsoActivationCode");
        paramAttributes.add(ssoActivationCode);
        final Attachment.Attribute password = new Attachment.Attribute();
        password.setId("password");
        paramAttributes.add(password);
        final Attachment.Attribute hostname = new Attachment.Attribute();
        hostname.setId("hostname");
        hostname.setVal("myHostname");
        paramAttributes.add(hostname);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String xml = xmlMapper.writeValueAsString(attachment);
        assertEquals(expected, xml);
    }
}
