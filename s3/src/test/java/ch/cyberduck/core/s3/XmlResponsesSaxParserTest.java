package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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


import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.io.IOUtils;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlResponsesSaxParserTest {

    @Test
    public void testParseFailure() throws Exception {
        try {
            new XmlResponsesSaxParser(new Jets3tProperties(), false).parseListBucketResponse(
                    IOUtils.toInputStream(
                            "<?xml version='1.0' encoding='UTF-8'?>\n" +
                                    "<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">\n" +
                                    "   </>" +
                                    "</ListBucketResult>"
                    )
            );
        }
        catch(ServiceException e) {
            final BackgroundException f = new S3ExceptionMappingService().map("m", e);
            assertEquals("M.", f.getMessage());
            assertEquals("The element type \"ListBucketResult\" must be terminated by the matching end-tag \"</ListBucketResult>\". Please contact your web hosting service provider for assistance.", f.getDetail());
        }
    }

    @Test
    public void testParseListBucketResponse() throws Exception {
        new XmlResponsesSaxParser(new Jets3tProperties(), false).parseListBucketResponse(
                IOUtils.toInputStream(
                        "<?xml version='1.0' encoding='UTF-8'?>\n" +
                                "<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">\n" +
                                "    <Name>bucketname</Name>\n" +
                                "    <Prefix/>\n" +
                                "    <Marker/>\n" +
                                "    <MaxKeys>1000</MaxKeys>\n" +
                                "    <Delimiter>/</Delimiter>\n" +
                                "    <IsTruncated>false</IsTruncated>\n" +
                                "    <Contents>\n" +
                                "        <Key>Screenshot 2022-04-14 at 23.50.21.png</Key>\n" +
                                "        <LastModified>2022-05-12T13:40:17.541Z</LastModified>\n" +
                                "        <ETag>\"3fcf1189802cc75006100190372048ad\"</ETag>\n" +
                                "        <Size>187976</Size>\n" +
                                "        <Owner>\n" +
                                "            <ID>testtenant:testuser</ID>\n" +
                                "            <DisplayName>testtenant:testuser</DisplayName>\n" +
                                "        </Owner>\n" +
                                "        <StorageClass>STANDARD</StorageClass>\n" +
                                "    </Contents>\n" +
                                "    <Contents>\n" +
                                "        <Key>Screenshot 2022-04-14 at 23.50.36.png</Key>\n" +
                                "        <LastModified>2022-05-12T13:40:17.541Z</LastModified>\n" +
                                "        <ETag>\"5e3fedc3bf0fd8e3aead5cecaa285fdb\"</ETag>\n" +
                                "        <Size>137389</Size>\n" +
                                "        <Owner>\n" +
                                "            <ID>testtenant:testuser</ID>\n" +
                                "            <DisplayName>testtenant:testuser</DisplayName>\n" +
                                "        </Owner>\n" +
                                "        <StorageClass>STANDARD</StorageClass>\n" +
                                "    </Contents>\n" +
                                "</ListBucketResult>"
                )
        );
    }
}
