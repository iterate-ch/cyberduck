package ch.cyberduck.core.sds.io.swagger.client.api;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.*;

import javax.ws.rs.core.GenericType;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
public class NodesApi {
  private ApiClient apiClient;

  public NodesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public NodesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Mark a node (room, folder or file) as favorite
   * ### Functional Description:   Marks a node (room, folder, file) as favorite.  ### Precondition: User needs _\&quot;read\&quot;_ permissions on that node.  ### Effects: A node gets marked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node addFavorite(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling addFavorite");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/favorite"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Cancel file upload
   * ### Functional Description: Cancel an upload and destroy the Upload Channel.  ### Precondition: An Upload Channel has been created.  ### Effects: The Upload Channel is removed and all temporary uploaded data is purged.  ### &amp;#9432; Further Information: It is recommended to notify the API about cancelled uploads if possible.
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void cancelFileUpload(String uploadId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling cancelFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Handle user-room assignments per group that have not been accepted yet
   * ### Functional Description:   Handles a list of user-room assignments by groups that have not been approved yet   **WAITING** or **DENIED** assignments can be **ACCEPTED**.  ### Precondition: None.  ### Effects: User-room assignment is approved and the user gets access to the group.  ### &amp;#9432; Further Information: Room administrators should regularly handle pending assignments to provide access to rooms for other users.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void changePendingAssignments(PendingAssignmentsRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changePendingAssignments");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/pending";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * ### Functional Description: Finishes an upload and closes the corresponding Upload Channel.  ### Precondition: An Upload Channel has been created and data has been transmitted.  ### Effects: The upload is finished and the temporary file is moved to the productive environment.  ### &amp;#9432; Further Information: The provided file name might be changed in accordance with the resolution strategy:   * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload.  ### 200 OK is not used by this API
   * @param uploadId Upload channel ID (required)
   * @param body body (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node completeFileUpload(String uploadId, CompleteUploadRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Config room
   * ### Functional Description: Updates a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Room&#39;s configuration is changed.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node configRoom(Long roomId, ConfigRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling configRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling configRoom");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/config"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Copy file system nodes
   * ### Functional Description: Copies nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are copied to target parent.  ### &amp;#9432; Further Information: Nodes must be in same source parent.   **Rooms cannot be copied.**
   * @param nodeId Target parent node ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node copyNodes(Long nodeId, CopyNodesRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling copyNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling copyNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/copy_to"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download token
   * ### Functional Description: Create a download token to retrieve a file without &#x60;X-Sds-Auth-Token&#x60; Header.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download token is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; ressources.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse createFileDownloadToken(Long fileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling createFileDownloadToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/downloads"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new file upload channel
   * ### Functional Description: This endpoint creates a new upload channel which is the first step in any file upload workflow.  ### Precondition: User has _\&quot;create\&quot;_ permissions in the parent container (room or folder).  ### Effects: A new upload channel for a file is created.   Its ID and an upload token are returned.  ### &amp;#9432; Further Information: The upload ID is used for uploads with &#x60;X-Sds-Auth-Token&#x60; header, the upload token can be used for uploads without authentication header.  Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&#x60;  * Not allowed characters in names:   &#x60;&#39;../&#39;, &#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CreateFileUploadResponse
   * @throws ApiException if fails to make API call
   */
  public CreateFileUploadResponse createFileUpload(CreateFileUploadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<CreateFileUploadResponse> localVarReturnType = new GenericType<CreateFileUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new folder
   * ### Functional Description: Creates a new folder.  ### Precondition: User has _\&quot;create\&quot;_ permissions in current room.  ### Effects: A new folder is created.  ### &amp;#9432; Further Information: Folders cannot be created on top level (without parent element).  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&#x60;  * Not allowed characters in names:   &#x60;&#39;../&#39;, &#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createFolder(CreateFolderRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFolder");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/folders";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new room
   * ### Functional Description: Creates a new room at the provided parent node.   Creation of top level rooms provided.  ### Precondition: User has _\&quot;create\&quot;_ permissions in the parent room.  ### Effects: A new room is created.  ### &amp;#9432; Further Information:   Rooms may only have other rooms as parent.   Rooms on top level do not have any parent.   Rooms may have rooms as children on n hierarchy levels.   If permission inheritance is disabled, there must be at least one admin user / group (with neither the group nor the user having an expiration date).  Notes are limited to **255** characters.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createRoom(CreateRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createRoom");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete nodes from Recycle Bin
   * ### Functional Description: Permanently remove a list of nodes from the Recycle Bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All provided nodes are removed.  ### &amp;#9432; Further Information: The removal of deleted nodes from the Recycle Bin is irreversible.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteDeletedNodes(DeleteDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteDeletedNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/deleted_nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete file system node
   * ### Functional Description: Delete node (room, folder, file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on supplied nodes.  ### Effects: Node gets deleted.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteNode(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling deleteNode");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete file system nodes
   * ### Functional Description: Delete nodes (room, folder, file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on supplied nodes.  ### Effects: Nodes are deleted.  ### &amp;#9432; Further Information: Nodes must be in same parent.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteNodes(DeleteNodesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke group permissions from room
   * ### Functional Description:   Batch function.   Revoke groups from room.  ### Precondition: User needs to be a room administrator.  ### Effects: Group&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomGroupsBatch(Long roomId, RoomGroupsDeleteBatchRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomGroupsBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomGroupsBatch");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/groups"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke user permissions from room
   * ### Functional Description:   Batch function.   Revoke users from room.  ### Precondition: User needs to be a room administrator.  ### Effects: User&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomUsersBatch(Long roomId, RoomUsersDeleteBatchRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomUsersBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomUsersBatch");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/users"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Empty Recycle Bin
   * ### Functional Description:   Empty a Recycle Bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All files in the Recycle Bin are permanently removed.  ### &amp;#9432; Further Information: Actually removes the previously deleted files from the system.   This action is irreversible.
   * @param nodeId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void emptyDeletedNodes(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling emptyDeletedNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/deleted_nodes"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Encrypt room
   * ### Functional Description:   Activates the client-side encryption for a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Encryption of room is activated.  ### &amp;#9432; Further Information: Only empty rooms at the top level may be encrypted.   This endpoint may also be used to disable encryption of an empty room.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node encryptRoom(Long roomId, EncryptRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling encryptRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling encryptRoom");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/encrypt"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room emergency password
   * ### Functional Description:   Returns the file key for the room emergency password of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataRoomFileKey(Long fileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getDataRoomFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/data_room_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get system emergency password
   * ### Functional Description:   Returns the file key for the system emergency password of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataSpaceFileKey(Long fileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getDataSpaceFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/data_space_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download file
   * ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getFileData(Long fileId, String range, Boolean genericMimetype, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFileData");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/downloads"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));

    if (range != null)
      localVarHeaderParams.put("Range", apiClient.parameterToString(range));
if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download file
   * ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getFileData1(Long fileId, String range, Boolean genericMimetype, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFileData1");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/downloads"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));

    if (range != null)
      localVarHeaderParams.put("Range", apiClient.parameterToString(range));
if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get deleted node
   * ### Functional Description:   Get the meta data of one deleted node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param deletedNodeId Deleted node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DeletedNode
   * @throws ApiException if fails to make API call
   */
  public DeletedNode getFsDeletedNode(Long deletedNodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'deletedNodeId' is set
    if (deletedNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deletedNodeId' when calling getFsDeletedNode");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/deleted_nodes/{deleted_node_id}"
      .replaceAll("\\{" + "deleted_node_id" + "\\}", apiClient.escapeString(deletedNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DeletedNode> localVarReturnType = new GenericType<DeletedNode>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get deleted versions
   * ### Functional Description:   Retrieve all deleted versions of a node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: The node is identified by three parameters: * parent ID * name * type (file, folder).  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;expireAt:desc&#x60;   Sort by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;accessedAt&#x60;** | Last access date | | **&#x60;size&#x60;** | Node size | | **&#x60;classification&#x60;** | Classification ID (for files only):&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - for internal use only&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator user name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier user name | | **&#x60;deletedAt&#x60;** | Deleted date | | **&#x60;deletedBy&#x60;** | Node deleted by user |
   * @param nodeId Auth parent node ID (required)
   * @param type Node type (required)
   * @param name Node name (required)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DeletedNodeVersionsList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeVersionsList getFsDeletedNodeVersions(Long nodeId, String type, String name, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsDeletedNodeVersions");
    }
    
    // verify the required parameter 'type' is set
    if (type == null) {
      throw new ApiException(400, "Missing the required parameter 'type' when calling getFsDeletedNodeVersions");
    }
    
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling getFsDeletedNodeVersions");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/deleted_nodes/versions"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "name", name));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DeletedNodeVersionsList> localVarReturnType = new GenericType<DeletedNodeVersionsList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get deleted nodes
   * ### Functional Description:   Retrieve a list of deleted nodes in a Recycle Bin.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: Only room IDs are accepted as node ID since only rooms have Recycle Bins.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file:folder|name:cn:searchString_1|parentPath:cn:searchString_2&#x60;   Get deleted nodes where type equals (&#x60;file&#x60; **OR** &#x60;folder&#x60;) **AND** deleted node name containing &#x60;searchString_1&#x60; **AND** deleted node parent path containing &#x60;searchString 2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value(s).&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:folder:file&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;name&#x60;** | Node name filter | &#x60;cn&#x60; | Node name contains value. | &#x60;search String&#x60; | | **&#x60;parentPath&#x60;** | Parent path filter | &#x60;cn&#x60; | Parent path contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;cntVersions&#x60;** | Number of deleted versions of this file | | **&#x60;firstDeletedAt&#x60;** | First deleted version | | **&#x60;lastDeletedAt&#x60;** | Last deleted version | | **&#x60;parentPath&#x60;** | Parent path of deleted node |
   * @param nodeId Auth parent node ID (required)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DeletedNodeSummaryList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeSummaryList getFsDeletedNodesSummary(Long nodeId, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsDeletedNodesSummary");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/deleted_nodes"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DeletedNodeSummaryList> localVarReturnType = new GenericType<DeletedNodeSummaryList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get node
   * ### Functional Description:   Get node (room, folder, file).  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node getFsNode(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsNode");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get file system nodes
   * ### Functional Description:   Provides a hierarchical list of file system nodes (rooms, folders, files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: &#x60;EncryptionInfo&#x60; is not provided.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:room:folder|perm:eq:read&#x60;   Get nodes where type equals (&#x60;room&#x60; **OR** &#x60;folder&#x60;) **AND** user has &#x60;read&#x60; permissions.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;perm&#x60;** | Permission filter | &#x60;eq&#x60; | Permission equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;perm:eq:read:create:delete&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;manage&#x60;&lt;/li&gt;&lt;li&gt;&#x60;read&#x60;&lt;/li&gt;&lt;li&gt;&#x60;change&#x60;&lt;/li&gt;&lt;li&gt;&#x60;create&#x60;&lt;/li&gt;&lt;li&gt;&#x60;delete&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageDownloadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageUploadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canReadRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canRestoreRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canDeleteRecycleBin&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;childPerm&#x60;** | Same as **&#x60;perm&#x60;**, but less restrictive (applies to child nodes only) | &#x60;eq&#x60; | cf. **&#x60;perm&#x60;** | cf. **&#x60;perm&#x60;** | | **&#x60;name&#x60;** | Node name filter | &#x60;cn&#x60; | Node name contains value. | &#x60;search String&#x60; | | **&#x60;encrypted&#x60;** | Node encryption status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator user name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier user name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID (for files only):&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - for internal use only&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;nodeCntChildren&#x60;** | Number of direct children (not recursive; for rooms and folders only) | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (not recursive; for files and folders only) |
   * @param depthLevel * &#x60;0&#x60; - top level nodes only * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node * &#x60;-1&#x60; - full tree (**DEPRECATED**: will be removed) (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param roomManager Show all rooms for management perspective. Only possible for _Rooms Managers_. For all other users, it will be ignored. (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList getFsNodes(Integer depthLevel, Long parentId, Boolean roomManager, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "depth_level", depthLevel));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "parent_id", parentId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_manager", roomManager));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download token for zip download
   * ### Functional Description:   Create a download token to retrieve several files in one ZIP archive.  ### Precondition: User has _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download token is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; resources.   ZIP download is only available for files and folders.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse getNodesAsZip(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling getNodesAsZip");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/zip";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download files / folders as ZIP
   * ### Functional Description:   Download multiple files in a ZIP archive.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getNodesAsZipDownload(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling getNodesAsZipDownload");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/zip/download";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user-room assignments per group that have not been accepted yet
   * ### Functional Description:   Requests a list of user-room assignments by groups that have not been approved yet   These can have the state: * **WAITING**   * **DENIED**   * **ACCEPTED**    **ACCEPTED** assignments are already removed from the list.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: Room administrators should regularly request pending assingments to provide access to rooms for other users.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;state:eq:WAITING&#x60;   Filter assignments by state &#x60;WAITING&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;roomId&#x60;** | Room ID filter | &#x60;eq&#x60; | Room ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;state&#x60;** | Assignment state | &#x60;eq&#x60; | Assignment state equals value. | &#x60;WAITING or DENIED&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;userId:desc&#x60;   Sort by &#x60;userId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;userId&#x60;** | User ID | | **&#x60;groupId&#x60;** | Group ID | | **&#x60;roomId&#x60;** | Room ID | | **&#x60;state&#x60;** | State |
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PendingAssignmentList
   * @throws ApiException if fails to make API call
   */
  public PendingAssignmentList getPendingAssignments(Integer offset, Integer limit, String filter, String sort, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/pending";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<PendingAssignmentList> localVarReturnType = new GenericType<PendingAssignmentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get events of a room
   * ### Use &#x60;nodes/rooms/{room_id}/events&#x60; API  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param dateStart Start date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateEnd End date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList getRoomActivitiesLog(Long roomId, Integer offset, Integer limit, String dateStart, String dateEnd, Integer type, Long userId, Integer status, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomActivitiesLog");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/events"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8", "text/csv"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<LogEventList> localVarReturnType = new GenericType<LogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get events of a room
   * ### Use &#x60;nodes/rooms/{room_id}/events&#x60; API  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param dateStart Start date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateEnd End date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return SyslogEventList
   * @throws ApiException if fails to make API call
   */
  public SyslogEventList getRoomActivitiesLog1(Long roomId, Integer offset, Integer limit, String dateStart, String dateEnd, Integer type, Long userId, Integer status, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomActivitiesLog1");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/activities_log"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8", "text/csv"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<SyslogEventList> localVarReturnType = new GenericType<SyslogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room granted groups or / and groups that can be granted
   * ### Functional Description:   Retrieve a list of groups that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** granted to this room **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the groups that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for groups in their rooms, so this filter is &#x60;true&#x60; and cannot be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the groups that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter groups with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;true&#x60; |
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomGroupList
   * @throws ApiException if fails to make API call
   */
  public RoomGroupList getRoomGroups(Long roomId, Integer offset, Integer limit, String filter, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/groups"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RoomGroupList> localVarReturnType = new GenericType<RoomGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room granted users or / and users that can be granted
   * ### Functional Description:   Retrieve a list of users that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;permissionsManage:eq:true|displayName:cn:searchString&#x60;   Get all users that have &#x60;manage&#x60; permissions to this room **AND** whose (firstname **OR** lastname **OR** login) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;displayName&#x60;** | User display name filter | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;login&#x60;). | &#x60;search String&#x60; | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the users that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for users in their rooms, so this filter is &#x60;true&#x60; and cannot be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; |
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomUserList
   * @throws ApiException if fails to make API call
   */
  public RoomUserList getRoomUsers(Long roomId, Integer offset, Integer limit, String filter, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/users"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RoomUserList> localVarReturnType = new GenericType<RoomUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user&#39;s file key
   * ### Functional Description:   Returns the file key for the current user (if available).  ### Precondition: User with _\&quot;read\&quot;_, _\&quot;create\&quot;_ or _\&quot;manage download share\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: The symmetric file key is encrypted with the user&#39;s public key.   File keys are generated with the workflow _\&quot;Generate file keys\&quot;_ that starts at &#x60;GET /nodes/missingFileKeys&#x60;.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getUserFileKey(Long fileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getUserFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/user_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get files where the user has no file key
   * ### Functional Description:   Requests a list of missing file keys that may be generated by the current user.  ### Precondition: User has a keypair.  ### Effects: None.  ### &amp;#9432; Further Information: Clients should regularly request missing file keys to provide access to files for other users.   The returned list is ordered by priority (emergency passwords are returned first).    ### Please note:  This API returns **1024** entries at maximum.   There might be more entries even if a total of 1024 is returned.
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param roomId Room ID (optional)
   * @param fileId File ID (optional)
   * @param userId User ID (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return MissingKeysResponse
   * @throws ApiException if fails to make API call
   */
  public MissingKeysResponse missingFileKeys(Integer offset, Integer limit, Long roomId, Long fileId, Long userId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes/missingFileKeys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_id", roomId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "file_id", fileId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<MissingKeysResponse> localVarReturnType = new GenericType<MissingKeysResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Move file system nodes
   * ### Functional Description:   Moves nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ and _\&quot;delete\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are moved to target parent.  ### &amp;#9432; Further Information: Nodes must be in same source parent.   **Rooms cannot be moved.**
   * @param nodeId Target parent node ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node moveNodes(Long nodeId, MoveNodesRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling moveNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling moveNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/move_to"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Restore deleted nodes
   * ### Functional Description:   Restore a list of deleted nodes.  ### Precondition: User has _\&quot;create\&quot;_ permissions in parent room and _\&quot;restore recycle bin\&quot;_ permissions.  ### Effects: The selected files are moved from the Recycle Bin to the chosen productive container.  ### &amp;#9432; Further Information: If no parent ID is provided, the node is restored to its previous location.   The default resolution strategy is &#x60;autorename&#x60; that adds numbers to the file name until the conflict is solved.   If an existing file is overwritten, it is moved to the Recycle Bin instead of the restored one.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void restoreNodes(RestoreDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling restoreNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/deleted_nodes/actions/restore";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get room emergency password
   * ### Functional Description:   Retrieve the room emergency password.  ### Precondition: User has _\&quot;read\&quot;_ permissions in that room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer roomRescueKey(Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling roomRescueKey");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/keypair"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UserKeyPairContainer> localVarReturnType = new GenericType<UserKeyPairContainer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Search file system nodes
   * ### Functional Description:   Provides a flat list of file system nodes (rooms, folders, files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ permissions on parent room.  ### Effects: None.  ### &amp;#9432; Further Information:   A maximum of **500** results is returned.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;).   &#x60;EncryptionInfo&#x60; is not provided.   Wildcard character is the asterisk character: **&#x60;*&#x60;**  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file|createdAt:ge:2015-01-01&#x60;   Get nodes where type equals &#x60;file&#x60; **AND** file creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;fileType&#x60;** | File type filter (file extension) | &#x60;cn, eq&#x60; | File type contains / equals value. | &#x60;search String&#x60; | | **&#x60;classification&#x60;** | File classification filter | &#x60;eq&#x60; | Classification equals value. | &lt;ul&gt;&lt;li&gt;&#x60;1&#x60; - public&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - for internal use only&lt;/li&gt;&lt;li&gt;&#x60;3&#x60; - confidential&lt;/li&gt;&lt;li&gt;&#x60;4&#x60; - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdBy&#x60;** | Creator login filter | &#x60;cn, eq&#x60; | Creator login contains / equals value. | &#x60;search String&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedBy&#x60;** | Last modifier login filter | &#x60;cn, eq&#x60; | Last modifier login contains / equals value. | &#x60;search String&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expireAt&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expireAt:ge:2016-12-31&#x60;&amp;#124;&#x60;expireAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;size&#x60;** | Node size filter | &#x60;ge, le&#x60; | Node size is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;size:ge:5&#x60;&amp;#124;&#x60;size:le:10&#x60; | &#x60;size in bytes&#x60; | | **&#x60;isFavorite&#x60;** | Favorite filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; | | **&#x60;parentPath&#x60;** | Parent path | &#x60;cn, eq&#x60; | Parent path contains / equals  value. | &#x60;search String&#x60; |   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator user name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier user name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID (for files only):&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - for internal use only&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;nodeCntChildren&#x60;** | Number of direct children (not recursive; for rooms and folders only) | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (not recursive; for files and folders only) | | **&#x60;type&#x60;** | Node type (room, folder, file) | | **&#x60;parentPath&#x60;** | Parent path | 
   * @param searchString Search string (required)
   * @param depthLevel * &#x60;0&#x60; - top level nodes only (default) * &#x60;-1&#x60; - full tree * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList searchFsNodes(String searchString, Integer depthLevel, Long parentId, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'searchString' is set
    if (searchString == null) {
      throw new ApiException(400, "Missing the required parameter 'searchString' when calling searchFsNodes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/search";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "search_string", searchString));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "depth_level", depthLevel));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "parent_id", parentId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or change room granted groups
   * ### Functional Description: Batch function.   All existing group permissions will be overwritten.  ### Precondition: User needs to be a room administrator.  ### Effects: Group&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomGroupsBatch(Long roomId, RoomGroupsAddBatchRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomGroupsBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomGroupsBatch");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/groups"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Add or change room granted users
   * ### Functional Description: Batch function.   All existing user permissions will be overwritten.  ### Precondition: User needs to be a room administrator.  ### Effects: User&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomUsersBatch(Long roomId, RoomUsersAddBatchRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomUsersBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomUsersBatch");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/users"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set file keys for a list of users and files
   * ### Functional Description:   Sets symmetric file keys for several users and files.  ### Precondition: User has file keys for the files.  ### Effects: Stores new file keys for other users.  ### &amp;#9432; Further Information: Only users with copies of the file key (encrypted with their public keys) can access a certain file.   This endpoint is used for the distribution of file keys amongst an authorized user base.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setUserFileKeys(UserFileKeySetBatchRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setUserFileKeys");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unmark a node (room, folder or file) as favorite
   * ### Functional Description: Unmarks a node (room, folder, file) as favorite.  ### Precondition: User needs _\&quot;read\&quot;_ permissions on that node.  ### Effects: A node gets unmarked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void unmarkFavorite(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling unmarkFavorite");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/favorite"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update file meta data
   * ### Functional Description:   Updates a file&#39;s meta data.  ### Precondition: User has _\&quot;change\&quot;_ permissions in parent room.  ### Effects: Meta data changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&#x60;  * Not allowed characters in names:   &#x60;&#39;../&#39;, &#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&#x60; 
   * @param fileId File ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFile(Long fileId, UpdateFileRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling updateFile");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update folder
   * ### Functional Description:   Renames an existing folder.  ### Precondition: User has _\&quot;change\&quot;_ permissions in current room.  ### Effects: The folder is renamed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&#x60;  * Not allowed characters in names:   &#x60;&#39;../&#39;, &#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&#x60; 
   * @param folderId Folder ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFolder(Long folderId, UpdateFolderRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling updateFolder");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFolder");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/folders/{folder_id}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update room
   * ### Functional Description:   Update a room&#39;s meta data.  ### Precondition: User is a room administrator at superordinated level.  ### Effects: Room&#39;s meta data is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&#x60;  * Not allowed characters in names:   &#x60;&#39;../&#39;, &#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&#x60;
   * @param roomId Room ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateRoom(Long roomId, UpdateRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling updateRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRoom");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload file
   * ### Functional Description:   Uploads a file or parts of it in an active Upload Channel.  ### Precondition: An Upload Channel has been created.  ### Effects: A file or parts of it are uploaded to a temporary location.  ### &amp;#9432; Further Information: This endpoints supports chunked upload.   Please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for further information.
   * @param uploadId Upload channel ID (required)
   * @param file File (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   */
  public ChunkUploadResponse uploadFile(String uploadId, File file, String contentRange, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadFile");
    }
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling uploadFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (contentRange != null)
      localVarHeaderParams.put("Content-Range", apiClient.parameterToString(contentRange));
if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
