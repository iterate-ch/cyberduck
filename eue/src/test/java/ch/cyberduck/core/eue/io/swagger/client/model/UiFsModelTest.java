package ch.cyberduck.core.eue.io.swagger.client.model;

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

import ch.cyberduck.core.eue.io.swagger.client.JSON;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class UiFsModelTest {

    @Test
    public void testParseReadModel() throws Exception {
        final String response = "{\n" +
                "    \"ui:fs\": {\n" +
                "        \"children\": [],\n" +
                "        \"childrenCount\": 0,\n" +
                "        \"childrenSize\": 0,\n" +
                "        \"creationMillis\": 1634542620210,\n" +
                "        \"lastResourceOperation\": \"CREATE\",\n" +
                "        \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "        \"metaETag\": \"AAABfJJVDjIAAAF8klUOMg\",\n" +
                "        \"modificationMillis\": 1634542620210,\n" +
                "        \"name\": \"k5cqpXoz\",\n" +
                "        \"parents\": [\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"resourceURI\": \"../resourceAlias/ROOT\"\n" +
                "                }\n" +
                "            }\n" +
                "        ],\n" +
                "        \"resourceType\": \"container\",\n" +
                "        \"resourceURI\": \"1031830638715470263\",\n" +
                "        \"size\": 0,\n" +
                "        \"version\": 307564843\n" +
                "    },\n" +
                "    \"ui:win32\": {\n" +
                "        \"creationMillis\": 1634542620210,\n" +
                "        \"hidden\": false,\n" +
                "        \"lastAccessMillis\": 1634542620210,\n" +
                "        \"lastModificationMillis\": 1634542620210,\n" +
                "        \"readOnly\": false,\n" +
                "        \"system\": false\n" +
                "    }\n" +
                "}\n";
        final UiFsModel model = new JSON().getContext(UiFsModel.class).readValue(response, UiFsModel.class);
        assertNotNull(model);
    }

    @Test
    public void testParseReadTrashResponse() throws Exception {
        final String response = "{\n" +
                "    \"ui:fs\": {\n" +
                "        \"children\": [\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634539705187,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJIol9gAAAF8kiiWTQ\",\n" +
                "                    \"modificationMillis\": 1634539706328,\n" +
                "                    \"name\": \"AJbbJ3hs\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031808491536253398\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 304650566\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634539705187,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634539706328,\n" +
                "                    \"lastModificationMillis\": 1634539706328,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634537534938,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"DISTRIBUTOR\",\n" +
                "                    \"metaETag\": \"AAABfJIm_D0AAAF8kgd4Pw\",\n" +
                "                    \"modificationMillis\": 1634539600957,\n" +
                "                    \"name\": \"lavx8zq7\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031804473040705690\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 302480184\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634537534938,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634539600957,\n" +
                "                    \"lastModificationMillis\": 1634539600957,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634539785401,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJIp12YAAAF8kinO7A\",\n" +
                "                    \"modificationMillis\": 1634539788134,\n" +
                "                    \"name\": \"NakYt9SI\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031810530152551768\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 304730597\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634539785401,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634539788134,\n" +
                "                    \"lastModificationMillis\": 1634539788134,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634541326824,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJJBVTgAAAF8kkFUFA\",\n" +
                "                    \"modificationMillis\": 1634541327672,\n" +
                "                    \"name\": \"pRlKTQcm\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031825506376357690\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 306272013\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634541326824,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634541327672,\n" +
                "                    \"lastModificationMillis\": 1634541327672,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634541356667,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJJByY0AAAF8kkHIbQ\",\n" +
                "                    \"modificationMillis\": 1634541357453,\n" +
                "                    \"name\": \"qLXnhQ3J\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031825506376357744\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 306301798\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634541356667,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634541357453,\n" +
                "                    \"lastModificationMillis\": 1634541357453,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"childrenCount\": 1,\n" +
                "                    \"childrenSize\": 0,\n" +
                "                    \"creationMillis\": 1634537116640,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJIBIXsAAAF8kgET4A\",\n" +
                "                    \"modificationMillis\": 1634537120123,\n" +
                "                    \"name\": \"UUJNwdsX\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"container\",\n" +
                "                    \"resourceURI\": \"../resource/1031800718991824353\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 302061273\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634537116640,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634537120123,\n" +
                "                    \"lastModificationMillis\": 1634537120123,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"contentETag\": \"07_pjo8c6JFhSFRWmiD19FCNarQCW4crezqCgu91sSo\",\n" +
                "                    \"creationMillis\": 1634539736340,\n" +
                "                    \"lastResourceOperation\": \"MOVE\",\n" +
                "                    \"lastResourceOperationClient\": \"MAMCLOUDSYNCMAC\",\n" +
                "                    \"metaETag\": \"AAABfJIpKzsAAAF8kikPOQ\",\n" +
                "                    \"modificationMillis\": 1634539744059,\n" +
                "                    \"name\": \"XDpcRBNP\",\n" +
                "                    \"originalParents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"ROOT\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"parents\": [\n" +
                "                        {\n" +
                "                            \"ui:fs\": {\n" +
                "                                \"resourceURI\": \"TRASH\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"resourceType\": \"file\",\n" +
                "                    \"resourceURI\": \"../resource/1031808491536253428\",\n" +
                "                    \"size\": 0,\n" +
                "                    \"version\": 304681522\n" +
                "                },\n" +
                "                \"ui:win32\": {\n" +
                "                    \"creationMillis\": 1634539736340,\n" +
                "                    \"hidden\": false,\n" +
                "                    \"lastAccessMillis\": 1634539744059,\n" +
                "                    \"lastModificationMillis\": 1634539744059,\n" +
                "                    \"readOnly\": false,\n" +
                "                    \"system\": false\n" +
                "                }\n" +
                "            }\n" +
                "        ],\n" +
                "        \"childrenCount\": 7,\n" +
                "        \"childrenSize\": 0,\n" +
                "        \"creationMillis\": 1630570853325,\n" +
                "        \"lastResourceOperation\": \"CREATE\",\n" +
                "        \"lastResourceOperationClient\": \"ONEREGISTRATION\",\n" +
                "        \"metaETag\": \"AAABfJJFS2oAAAF7pZizzQ\",\n" +
                "        \"modificationMillis\": 1634541587306,\n" +
                "        \"name\": \"Gel\\u00f6schte Dateien\",\n" +
                "        \"parents\": [\n" +
                "            {\n" +
                "                \"ui:fs\": {\n" +
                "                    \"resourceURI\": \"ROOT\"\n" +
                "                }\n" +
                "            }\n" +
                "        ],\n" +
                "        \"resourceType\": \"aliascontainer\",\n" +
                "        \"resourceURI\": \"TRASH\",\n" +
                "        \"size\": 0,\n" +
                "        \"version\": 630765252\n" +
                "    },\n" +
                "    \"ui:win32\": {\n" +
                "        \"creationMillis\": 1630570853325,\n" +
                "        \"hidden\": false,\n" +
                "        \"lastAccessMillis\": 1634541587306,\n" +
                "        \"lastModificationMillis\": 1634541587306,\n" +
                "        \"readOnly\": false,\n" +
                "        \"system\": false\n" +
                "    }\n" +
                "}\n";
        final UiFsModel model = new JSON().getContext(UiFsModel.class).readValue(response, UiFsModel.class);
        assertNotNull(model);
    }
}