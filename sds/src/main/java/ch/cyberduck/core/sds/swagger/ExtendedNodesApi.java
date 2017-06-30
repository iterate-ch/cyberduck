package ch.cyberduck.core.sds.swagger;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;

import javax.ws.rs.core.GenericType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedNodesApi extends NodesApi {

    public ExtendedNodesApi(final ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Download file
     * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Download a file.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Range requests are supported (please cf. &lt;a href&#x3D;&#39;https://tools.ietf.org/html/rfc7233&#39; target&#x3D;&#39;_blank&#39;&gt;RFC 7233&lt;/a&gt; for details).&lt;/p&gt;&lt;/div&gt;
     *
     * @param xSdsAuthToken   Authentication token (required)
     * @param fileId          File Id (required)
     * @param range           Range (optional)
     * @param genericMimetype always return application/octet-stream instead of specific mimetype (optional)
     * @return Object
     * @throws ApiException if fails to make API call
     */
    public InputStream getFileData(String xSdsAuthToken, Long fileId, String range, Boolean genericMimetype) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'xSdsAuthToken' is set
        if(xSdsAuthToken == null) {
            throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFileData");
        }

        // verify the required parameter 'fileId' is set
        if(fileId == null) {
            throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFileData");
        }

        // create path and map variables
        String localVarPath = "/nodes/files/{file_id}/downloads".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "file_id" + "\\}", this.getApiClient().escapeString(fileId.toString()));

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(this.getApiClient().parameterToPairs("", "generic_mimetype", genericMimetype));

        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", this.getApiClient().parameterToString(xSdsAuthToken));
        }
        if(range != null) {
            localVarHeaderParams.put("Range", this.getApiClient().parameterToString(range));
        }


        final String[] localVarAccepts = {
                "application/octet-stream"
        };
        final String localVarAccept = this.getApiClient().selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = this.getApiClient().selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        GenericType<InputStream> localVarReturnType = new GenericType<InputStream>() {
        };
        return this.getApiClient().invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
