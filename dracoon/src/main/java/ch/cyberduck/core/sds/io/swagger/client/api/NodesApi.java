package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ChangeNodeCommentRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ChunkUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Comment;
import ch.cyberduck.core.sds.io.swagger.client.model.CommentList;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteS3FileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ConfigRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CopyNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateKeyPairRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateNodeCommentRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeSummaryList;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeVersionsList;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadTokenGenerateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import java.io.File;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.FileVersionList;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneratePresignedUrlsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.MissingKeysResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeParentList;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeVirusProtectionInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentList;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrlList;
import ch.cyberduck.core.sds.io.swagger.client.model.RestoreDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGuestUserAddRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomPolicies;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomPoliciesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomWebhookList;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadStatus;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagIds;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFavoritesBulkRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFilesBulkRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.io.swagger.client.model.VirusProtectionVerdictRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ZipDownloadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * ### Description:   Marks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128065; see&lt;/span&gt; the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Postcondition: A node gets marked as favorite.  ### Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node addFavorite(Long nodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
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


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add guest users to a room
   * ### Description: Add guest users to a room  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;. To add new members, the user needs the right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; non-members add&lt;/span&gt;, which is included in any role. &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Guest User Policy&lt;/span&gt; needs to be enabled.   ### Postcondition: New or existing Guest-Users now have guest-permissions for this room  ### Further Information: Batch function.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void addRoomGuestUsers(RoomGuestUserAddRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addRoomGuestUsers");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling addRoomGuestUsers");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/guest_users"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Cancel file upload
   * ### Description: Cancel a (S3) file upload and destroy the upload channel.  ### Precondition: An upload channel has been created and user has to be the creator of the upload channel.  ### Postcondition: The upload channel is removed and all temporary uploaded data is purged.  ### Further Information: It is recommended to notify the API about cancelled uploads if possible.
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Handle user-room assignments per group
   * ### Description:   Handles a list of user-room assignments by groups that have **NOT** been approved yet   **WAITING** or **DENIED** assignments can be **ACCEPTED**.  ### Precondition: None.  ### Postcondition: User-room assignment is approved and the user gets access to the group.  ### Further Information: Room administrators should **SHOULD** handle pending assignments to provide access to rooms for other users.
   * @param body  (required)
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.9.0&lt;/h3&gt;  ### Use &#x60;uploads&#x60; API  ### Description: Finishes an upload and closes the corresponding upload channel.  ### Precondition: An upload channel has been created and data has been transmitted.  ### Postcondition: The upload is finished and the temporary file is moved to the productive environment.  ### Further Information: The provided file name might be changed in accordance with the resolution strategy:   * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload.   Download share id (if exists) gets changed if: - node with the same name exists in the target container - &#x60;resolutionStrategy&#x60; is &#x60;overwrite&#x60; - &#x60;keepShareLinks&#x60; is &#x60;true&#x60;  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60;
   * @param uploadId Upload channel ID (required)
   * @param body  (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public Node completeFileUpload(String uploadId, CompleteUploadRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
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


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Complete S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: An upload channel has been created, data has been transmitted and user has to be the creator of the upload channel  ### Postcondition: Upload channel is closed. S3 multipart upload request is completed.  ### Further Information: Download share id (if exists) gets changed if: - node with the same name exists in the target container - &#x60;resolutionStrategy&#x60; is &#x60;overwrite&#x60; - &#x60;keepShareLinks&#x60; is &#x60;true&#x60;
   * @param body  (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void completeS3FileUpload(CompleteS3FileUploadRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling completeS3FileUpload");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeS3FileUpload");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}/s3"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Configure room
   * ### Description: Configure a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Room&#x27;s configuration is changed.  ### Further Information: Provided (or default) classification is taken from room when file gets uploaded without any classification.    To set &#x60;adminIds&#x60; or &#x60;adminGroupIds&#x60; the &#x60;inheritPermissions&#x60; value has to be &#x60;false&#x60;. Otherwise use: * &#x60;PUT /nodes/rooms/{room_id}/groups&#x60; * &#x60;PUT /nodes/rooms/{room_id}/users &#x60;    APIs.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node configureRoom(ConfigRoomRequest body, Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling configureRoom");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling configureRoom");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/config"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Copy node(s)
   * ### Description: Copies nodes (folder, file) to another parent.  ### Precondition: Authenticated user with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in the source parent and &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in the target parent node.  ### Postcondition: Nodes are copied to target parent.  ### Further Information: Nodes **MUST** be in same source parent.   **Rooms **CANNOT** be copied.**  Download share id (if exists) gets changed if: - node with the same name exists in the target container - &#x60;resolutionStrategy&#x60; is &#x60;overwrite&#x60; - &#x60;keepShareLinks&#x60; is &#x60;true&#x60;  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node copyNodes(CopyNodesRequest body, Long nodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling copyNodes");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling copyNodes");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/copy_to"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create key pair and preserve copy of old private key
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.24.0&lt;/h3&gt;  ### Description:   Create room rescue key pair and preserve copy of old private key.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Room rescue key pair is created.   Copy of old private key is preserved.  ### Further Information: You can submit your old private key, encrypted with your current password.   This allows migrating file keys encrypted with your old key pair to the new one.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void createAndPreserveRoomRescueKeyPair(CreateKeyPairRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createAndPreserveRoomRescueKeyPair");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling createAndPreserveRoomRescueKeyPair");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/keypairs"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create new file upload channel
   * ### Description: This endpoint creates a new upload channel which is the first step in any file upload workflow.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in the parent container (room or folder).  ### Postcondition: A new upload channel for a file is created.   Its ID and an upload token are returned.  ### Further Information: The upload ID is used for uploads with &#x60;X-Sds-Auth-Token&#x60; header, the upload token can be used for uploads without authentication header.  Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  Notes are limited to **255** characters.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CreateFileUploadResponse
   * @throws ApiException if fails to make API call
   */
  public CreateFileUploadResponse createFileUploadChannel(CreateFileUploadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFileUploadChannel");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<CreateFileUploadResponse> localVarReturnType = new GenericType<CreateFileUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create new folder
   * ### Description: Create a new folder.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in current room.  ### Postcondition: New folder is created.  ### Further Information: Folders **CANNOT** be created on top level (without parent element).   Notes are limited to **255** characters.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createFolder(CreateFolderRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
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


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create node comment
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.10.0&lt;/h3&gt;  ### Description: Create a comment for a specific node.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions on the node.  ### Postcondition: Comment is created.  ### Further Information: Maximum allowed text length: **65535** characters.
   * @param body  (required)
   * @param nodeId Node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Comment
   * @throws ApiException if fails to make API call
   */
  public Comment createNodeComment(CreateNodeCommentRequest body, Long nodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createNodeComment");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling createNodeComment");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/comments"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Comment> localVarReturnType = new GenericType<Comment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create new room
   * ### Description: Creates a new room at the provided parent node.   Creation of top level rooms provided.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt; permissions in the parent room.  ### Postcondition: A new room is created.  ### Further Information:   Rooms may only have other rooms as parent.   Rooms on top level do **NOT** have any parent.   Rooms may have rooms as children on n hierarchy levels.   If permission inheritance is disabled, there **MUST** be at least one admin user / group (with neither the group nor the user having an expiration date).  Notes are limited to **255** characters.  Provided (or default) classification is taken from room when file gets uploaded without any classification.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60;
   * @param body  (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createRoom(CreateRoomRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
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


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Download files / folders as ZIP archive
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.44.0&lt;/h3&gt;  ### Description:   Download multiple files in a ZIP archive.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in auth parent room.  ### Postcondition: Stream is returned.  ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public void downloadZipArchive(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling downloadZipArchive");
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Empty recycle bin
   * ### Description:   Empty a recycle bin.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete recycle bin&lt;/span&gt; permissions in parent room.  ### Postcondition: All files in the recycle bin are permanently removed.  ### Further Information: Actually removes the previously deleted files from the system.   **This action is irreversible.**
   * @param nodeId Room ID (required)
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Encrypt room
   * ### Description:   Activates the client-side encryption for a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Encryption of room is activated.  ### Further Information: Only empty rooms at the top level may be encrypted.   This endpoint may also be used to disable encryption of an empty room.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node encryptRoom(EncryptRoomRequest body, Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling encryptRoom");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling encryptRoom");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/encrypt"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Generate download URL
   * ### Description: Create a download URL to retrieve a file without &#x60;X-Sds-Auth-Token&#x60; Header.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in parent room.  ### Postcondition: Download token is generated and returned.  ### Further Information: The token is necessary to access &#x60;downloads&#x60; ressources.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse generateDownloadUrl(Long fileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling generateDownloadUrl");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Generate download URL for ZIP download
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.44.0&lt;/h3&gt;  ### Description:   Create a download URL to retrieve several files in one ZIP archive.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in parent room.  ### Postcondition: Download URL is generated and returned.  ### Further Information: The token is necessary to access &#x60;downloads&#x60; resources.   ZIP download is only available for files and folders.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public DownloadTokenGenerateResponse generateDownloadUrlForZipArchive(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling generateDownloadUrlForZipArchive");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Generate presigned URLs for S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Generate presigned URLs for S3 file upload.  ### Precondition: An upload channel has been created and user has to be the creator of the upload channel.  ### Postcondition: List of presigned URLs is returned.  ### Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param body  (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PresignedUrlList
   * @throws ApiException if fails to make API call
   */
  public PresignedUrlList generatePresignedUrlsFiles(GeneratePresignedUrlsRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling generatePresignedUrlsFiles");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling generatePresignedUrlsFiles");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}/s3_urls"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PresignedUrlList> localVarReturnType = new GenericType<PresignedUrlList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Generate Virus Protection Verdict Information
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.44.0&lt;/h3&gt;  ### Description: Retrieve information about the virus protection verdicts of a list of node IDs  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in that room. Room is not encrypted. Global System Policy \&quot;Virus Protection\&quot; is enabled. Room Policy \&quot;Virus Protection is enabled\&quot;.  ### Postcondition: Information returned.    
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return List&lt;NodeVirusProtectionInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public List<NodeVirusProtectionInfo> generateVirusProtectionInfo(VirusProtectionVerdictRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling generateVirusProtectionInfo");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/generate_verdict_info";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<NodeVirusProtectionInfo>> localVarReturnType = new GenericType<List<NodeVirusProtectionInfo>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Assign or unassign webhooks to room
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.19.0&lt;/h3&gt;  ### Description:   Handle room webhook assignments.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: List of webhooks is returned.  ### Further Information: None.  ### Available event types:  &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |  &lt;/details&gt;
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomWebhookList
   * @throws ApiException if fails to make API call
   */
  public RoomWebhookList handleRoomWebhookAssignments(UpdateRoomWebhookRequest body, Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling handleRoomWebhookAssignments");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling handleRoomWebhookAssignments");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/webhooks"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RoomWebhookList> localVarReturnType = new GenericType<RoomWebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Move node(s)
   * ### Description:   Moves nodes (folder, file) to another parent.  ### Precondition: Authenticated user with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; and &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete&lt;/span&gt; permissions in the source parent and &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in the target parent node.  ### Postcondition: Nodes are moved to target parent.  ### Further Information: Nodes **MUST** be in same source parent.   **Rooms **CANNOT** be moved.**  Download share id (if exists) gets changed if: - node with the same name exists in the target container - &#x60;resolutionStrategy&#x60; is &#x60;overwrite&#x60; - &#x60;keepShareLinks&#x60; is &#x60;true&#x60;  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node moveNodes(MoveNodesRequest body, Long nodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling moveNodes");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling moveNodes");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/move_to"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Remove nodes from recycle bin
   * ### Description: Permanently remove a list of nodes from the recycle bin.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete recycle bin&lt;/span&gt; permissions in parent room.  ### Postcondition: All provided nodes are removed.  ### Further Information: The removal of deleted nodes from the recycle bin is irreversible.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeDeletedNodes(DeleteDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling removeDeletedNodes");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unmark a node (room, folder or file) as favorite
   * ### Description: Unmarks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128065; see&lt;/span&gt; the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Postcondition: A node gets unmarked as favorite.  ### Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeFavorite(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling removeFavorite");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove malicious File
   * ### Description: Permanently delete a malicious file.  ### Precondition: Authenticated user with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete&lt;/span&gt; permissions on supplied nodes (for folders or files) or on superordinated node (for rooms).  ### Postcondition: Malicious file gets permanently deleted.  ### Further Information: None.
   * @param maliciousFileId Malicious file ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeMaliciousFile(Long maliciousFileId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'maliciousFileId' is set
    if (maliciousFileId == null) {
      throw new ApiException(400, "Missing the required parameter 'maliciousFileId' when calling removeMaliciousFile");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/malicious_files/{malicious_file_id}"
      .replaceAll("\\{" + "malicious_file_id" + "\\}", apiClient.escapeString(maliciousFileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove node
   * ### Description: Delete node (room, folder or file).  ### Precondition: Authenticated user with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete&lt;/span&gt; permissions on supplied nodes (for folders or files) or on superordinated node (for rooms).  ### Postcondition: Node gets deleted.  ### Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeNode(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling removeNode");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove node comment
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.10.0&lt;/h3&gt;  ### Description: Delete an existing comment for a specific node.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions on the node and is the creator of the comment **OR** &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt; in auth parent room.  ### Postcondition: Comment is deleted.  ### Further Information: None.
   * @param commentId Comment ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeNodeComment(Long commentId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'commentId' is set
    if (commentId == null) {
      throw new ApiException(400, "Missing the required parameter 'commentId' when calling removeNodeComment");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/comments/{comment_id}"
      .replaceAll("\\{" + "comment_id" + "\\}", apiClient.escapeString(commentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove nodes
   * ### Description: Delete nodes (room, folder or file).  ### Precondition: Authenticated user with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; delete&lt;/span&gt; permissions on supplied nodes (for folders or files) or on superordinated node (for rooms).  ### Postcondition: Nodes are deleted.  ### Further Information: Nodes **MUST** be in same parent.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeNodes(DeleteNodesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling removeNodes");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove rooms&#x27;s rescue key pair
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.24.0&lt;/h3&gt;  ### Description:   Delete room rescue key pair.  ### Precondition: Authenticated user.  ### Postcondition: Key pair is removed (cf. further information below).  ### Further Information: Please set a new room rescue key pair first and re-encrypt file keys with it.   If no version is set, deleted key pair with lowest preference value.   Although, &#x60;version&#x60; **SHOULD** be set. 
   * @param roomId Room ID (required)
   * @param version Version (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeRoomRescueKeyPair(Long roomId, String version, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling removeRoomRescueKeyPair");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/keypair"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request deleted node
   * ### Description:   Get metadata of a deleted node.  ### Precondition: User can access parent room and has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read recycle bin&lt;/span&gt; permissions.  ### Postcondition: Requested deleted node is returned.  ### Further Information: None.
   * @param deletedNodeId Deleted node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DeletedNode
   * @throws ApiException if fails to make API call
   */
  public DeletedNode requestDeletedNode(Long deletedNodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deletedNodeId' is set
    if (deletedNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deletedNodeId' when calling requestDeletedNode");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/deleted_nodes/{deleted_node_id}"
      .replaceAll("\\{" + "deleted_node_id" + "\\}", apiClient.escapeString(deletedNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DeletedNode> localVarReturnType = new GenericType<DeletedNode>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request deleted versions of nodes
   * ### Description:   Retrieve all deleted versions of a node.  ### Precondition: User can access parent room and has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read recycle bin&lt;/span&gt; permissions.  ### Postcondition: List of deleted versions of a node is returned.  ### Further Information: The node is identified by three parameters: * parent ID * name * type (file, folder).  Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;expireAt:desc|size:asc&#x60;   Sort by &#x60;expireAt&#x60; descending **AND** &#x60;size&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;expireAt&#x60; | Expiration date | | &#x60;accessedAt&#x60; | Last access date | | &#x60;size&#x60; | Node size | | &#x60;classification&#x60; | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | &#x60;createdAt&#x60; | Creation date | | &#x60;createdBy&#x60; | Creator first name, last name | | &#x60;updatedAt&#x60; | Last modification date | | &#x60;updatedBy&#x60; | Last modifier first name, last name | | &#x60;deletedAt&#x60; | Deleted date | | &#x60;deletedBy&#x60; | Deleter first name, last name |  &lt;/details&gt;
   * @param nodeId Parent ID (room or folder ID) (required)
   * @param type Node type (required)
   * @param name Node name (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DeletedNodeVersionsList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeVersionsList requestDeletedNodeVersions(Long nodeId, String type, String name, String xSdsDateFormat, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling requestDeletedNodeVersions");
    }
    // verify the required parameter 'type' is set
    if (type == null) {
      throw new ApiException(400, "Missing the required parameter 'type' when calling requestDeletedNodeVersions");
    }
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling requestDeletedNodeVersions");
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

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DeletedNodeVersionsList> localVarReturnType = new GenericType<DeletedNodeVersionsList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of deleted nodes
   * ### Description:   Retrieve a list of deleted nodes in a recycle bin.  ### Precondition: User can access parent room and has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read recycle bin&lt;/span&gt; permissions.  ### Postcondition: List of deleted nodes is returned.  ### Further Information: Only room IDs are accepted as parent ID since only rooms may have a recycle bin.  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;type:eq:file:folder|name:cn:searchString_1|parentPath:cn:searchString_2&#x60;   Get deleted nodes where type equals (&#x60;file&#x60; **OR** &#x60;folder&#x60;) **AND** deleted node name containing &#x60;searchString_1&#x60; **AND** deleted node parent path containing &#x60;searchString 2&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;type&#x60; | Node type filter | &#x60;eq&#x60; | Node type equals value(s).&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:folder:file&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;name&#x60; | Node name filter | &#x60;cn&#x60; | Node name contains value. | &#x60;search String&#x60; | | &#x60;parentPath&#x60; | Parent path filter | &#x60;cn&#x60; | Parent path contains value. | &#x60;search String&#x60; | | &#x60;timestampCreation&#x60; | Creation timestamp filter | &#x60;ge, le&#x60; | Creation timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampCreation:ge:2016-12-31&#x60;&amp;#124;&lt;br&gt;&#x60;timestampCreation:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;timestampModification&#x60; | Modification timestamp filter | &#x60;ge, le&#x60; | Modification timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampModification:ge:2016-12-31T23:00:00.123&#x60;&amp;#124;&lt;br&gt;&#x60;timestampModification:le:2018-01-01T11:00:00.540&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.   Nodes are sorted by type first, then by sent sort string.    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:desc|timestampCreation:asc&#x60;   Sort by &#x60;name&#x60; descending **AND** &#x60;timestampCreation&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Node name | | &#x60;cntVersions&#x60; | Number of deleted versions of this file | | &#x60;firstDeletedAt&#x60; | First deleted version | | &#x60;lastDeletedAt&#x60; | Last deleted version | | &#x60;parentPath&#x60; | Parent path of deleted node | | &#x60;timestampCreation&#x60; | Creation timestamp | | &#x60;timestampModification&#x60; | Modification timestamp |  &lt;/details&gt;
   * @param nodeId Parent ID (can only be a room ID) (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DeletedNodeSummaryList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeSummaryList requestDeletedNodesSummary(Long nodeId, String xSdsDateFormat, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling requestDeletedNodesSummary");
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

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DeletedNodeSummaryList> localVarReturnType = new GenericType<DeletedNodeSummaryList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of file versions
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.37.0&lt;/h3&gt;  ### Description:   Request a list of file versions. Both nodes and deleted nodes are included, depending on the user&#x27;s permissions.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read/read recycle bin&lt;/span&gt; permissions in parent room.  ### Postcondition: List of file versions is returned.  ### Further Information: Maximum number of file versions is 500. The list is sorted by ID DESC. 
   * @param referenceId Reference ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileVersionList
   * @throws ApiException if fails to make API call
   */
  public FileVersionList requestFileVersionList(Long referenceId, String xSdsDateFormat, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'referenceId' is set
    if (referenceId == null) {
      throw new ApiException(400, "Missing the required parameter 'referenceId' when calling requestFileVersionList");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/versions/{reference_id}"
      .replaceAll("\\{" + "reference_id" + "\\}", apiClient.escapeString(referenceId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileVersionList> localVarReturnType = new GenericType<FileVersionList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of webhooks that are assigned or can be assigned to this room
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.19.0&lt;/h3&gt;  ### Description:   Get a list of webhooks for the room scope with their assignment status.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: List of webhooks is returned.  ### Further Information:  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;isAssigned:eq:true&#x60;   Get a list of assigned webhooks to the room.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isAssigned&#x60;** | Assigned/unassigned webhooks filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  &lt;/details&gt;
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param filter Filter string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomWebhookList
   * @throws ApiException if fails to make API call
   */
  public RoomWebhookList requestListOfWebhooksForRoom(Long roomId, String xSdsDateFormat, Integer offset, Integer limit, String filter, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestListOfWebhooksForRoom");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/webhooks"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RoomWebhookList> localVarReturnType = new GenericType<RoomWebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request files without user&#x27;s file key
   * ### Description:   Requests a list of missing file keys that may be generated by the current user.    ### Precondition: User has a key pair.   Only returns users that owns one of the following permissions: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;  ### Postcondition: None.  ### Further Information: Clients **SHOULD** regularly request missing file keys to provide access to files for other users.   The returned list is ordered by priority (emergency passwords / rescue keys are returned first). There is an enforced limit of **100** items per request. A total value greater than limit signals that there are more entries but does not necessarily reflect the precise number of total items. 
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param roomId Room ID (optional)
   * @param fileId File ID (optional)
   * @param userId User ID (optional)
   * @param useKey Determines which key should be used (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return MissingKeysResponse
   * @throws ApiException if fails to make API call
   */
  public MissingKeysResponse requestMissingFileKeys(Integer offset, Integer limit, Long roomId, Long fileId, Long userId, String useKey, String xSdsAuthToken) throws ApiException {
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "use_key", useKey));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MissingKeysResponse> localVarReturnType = new GenericType<MissingKeysResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request node
   * ### Description:   Get node (room, folder or file).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in auth parent room.  ### Postcondition: Requested node is returned.  ### Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node requestNode(Long nodeId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling requestNode");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of node comments
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.10.0&lt;/h3&gt;  ### Description: Get comments for a specific node.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions on the node.  ### Postcondition: List with comments (sorted by &#x60;createdAt&#x60; timestamp) is returned.  ### Further Information: An empty list is returned if no comments were found.   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  
   * @param nodeId Node ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param hideDeleted Hide deleted comments (default: false) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CommentList
   * @throws ApiException if fails to make API call
   */
  public CommentList requestNodeComments(Long nodeId, String xSdsDateFormat, Integer offset, Integer limit, Boolean hideDeleted, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling requestNodeComments");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/comments"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "hide_deleted", hideDeleted));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<CommentList> localVarReturnType = new GenericType<CommentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of parent nodes
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.10.0&lt;/h3&gt;  ### Description:   Requests a list of node ancestors, sorted from root node to the node&#x27;s direct parent node.  ### Precondition: User is allowed to browse through the node tree until the requested node.  ### Postcondition: List of parent nodes is returned.  ### Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NodeParentList
   * @throws ApiException if fails to make API call
   */
  public NodeParentList requestNodeParents(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling requestNodeParents");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/parents"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<NodeParentList> localVarReturnType = new GenericType<NodeParentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of nodes
   * ### Description:   Provides a hierarchical list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user.  ### Postcondition: List of nodes is returned.  ### Further Information: &#x60;EncryptionInfo&#x60; is **NOT** provided.  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;type:eq:room:folder|perm:eq:read&#x60;   Get nodes where type equals (&#x60;room&#x60; **OR** &#x60;folder&#x60;) **AND** user has &#x60;read&#x60; permissions.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;type&#x60; | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;perm&#x60; | Permission filter | &#x60;eq&#x60; | Permission equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;perm:eq:read:create:delete&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;manage&#x60;&lt;/li&gt;&lt;li&gt;&#x60;read&#x60;&lt;/li&gt;&lt;li&gt;&#x60;change&#x60;&lt;/li&gt;&lt;li&gt;&#x60;create&#x60;&lt;/li&gt;&lt;li&gt;&#x60;delete&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageDownloadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageUploadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canReadRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canRestoreRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canDeleteRecycleBin&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;childPerm&#x60; | Same as &#x60;perm&#x60;, but less restrictive (applies to child nodes only).&lt;br&gt;Child nodes of the parent node which do not meet the filter condition&lt;br&gt;are **NOT** returned. | &#x60;eq&#x60; | cf. &#x60;perm&#x60; | cf. &#x60;perm&#x60; | | &#x60;name&#x60; | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | &#x60;encrypted&#x60; | Node encryption status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | &#x60;branchVersion&#x60; | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; | | &#x60;timestampCreation&#x60; | Creation timestamp filter | &#x60;ge, le&#x60; | Creation timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampCreation:ge:2016-12-31T23:00:00.123&#x60;&amp;#124;&lt;br&gt;&#x60;timestampCreation:le:2018-01-01T11:00:00.540&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;timestampModification&#x60; | Modification timestamp filter | &#x60;ge, le&#x60; | Modification timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampModification:ge:2016-12-31T23:00:00.123&#x60;&amp;#124;&lt;br&gt;&#x60;timestampModification:le:2018-01-01T11:00:00.540&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;referenceId&#x60;           | Reference ID filter               | &#x60;eq&#x60; | Reference ID equals value.   | &#x60;Integer &#x60; | &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.   Nodes are sorted by type first, then by sent sort string.    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:desc|fileType:asc&#x60;   Sort by &#x60;name&#x60; descending **AND** &#x60;fileType&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Node name | | &#x60;createdAt&#x60; | Creation date | | &#x60;createdBy&#x60; | Creator first name, last name | | &#x60;updatedAt&#x60; | Last modification date | | &#x60;updatedBy&#x60; | Last modifier first name, last name | | &#x60;fileType&#x60; | File type (extension) | | &#x60;classification&#x60; | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | &#x60;size&#x60; | Node size | | &#x60;cntDeletedVersions&#x60; | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | &#x60;timestampCreation&#x60; | Creation timestamp | | &#x60;timestampModification&#x60; | Modification timestamp |  &lt;/details&gt;  ### Deprecated sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &lt;del&gt;&#x60;cntChildren&#x60;&lt;/del&gt; | Number of direct children (**NOT** recursive; for rooms and folders only) |  &lt;/details&gt;
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel * &#x60;0&#x60; - top level nodes only  * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param parentId Parent node ID.  Only rooms and folders can be parents.  Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param roomManager Show all rooms for management perspective.  Only possible for _Rooms Managers_ / _Room Admins_.  For all other users, it will be ignored. (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList requestNodes(String xSdsDateFormat, Integer depthLevel, Long parentId, Boolean roomManager, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
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

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request user-room assignments per group
   * ### Description:   Requests a list of user-room assignments by groups that have **NOT** been approved yet   These can have the state: * **WAITING**   * **DENIED**   * **ACCEPTED**    **ACCEPTED** assignments are already removed from the list.  ### Precondition: None.  ### Postcondition: List of user-room assignments is returned.  ### Further Information: Room administrators **SHOULD** regularly request pending assingments to provide access to rooms for other users.  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;state:eq:WAITING&#x60;   Filter assignments by state &#x60;WAITING&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;userId&#x60; | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | &#x60;groupId&#x60; | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | &#x60;roomId&#x60; | Room ID filter | &#x60;eq&#x60; | Room ID equals value. | &#x60;positive Integer&#x60; | | &#x60;state&#x60; | Assignment state | &#x60;eq&#x60; | Assignment state equals value. | &#x60;WAITING or DENIED&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;userId:desc|state:asc&#x60;   Sort by &#x60;userId&#x60; descending **AND** &#x60;state&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;userId&#x60; | User ID | | &#x60;groupId&#x60; | Group ID | | &#x60;roomId&#x60; | Room ID | | &#x60;state&#x60; | State |  &lt;/details&gt;
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PendingAssignmentList
   * @throws ApiException if fails to make API call
   */
  public PendingAssignmentList requestPendingAssignments(Integer offset, Integer limit, String filter, String sort, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PendingAssignmentList> localVarReturnType = new GenericType<PendingAssignmentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request events of a room
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.3.0&lt;/h3&gt;  ### Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions on that room.  ### Postcondition: List of events is returned.  ### Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.  Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;time&#x60; | Event timestamp |  &lt;/details&gt;
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param dateStart Filter events from given date   e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateEnd Filter events until given date   e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param type Operation ID   cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @param status Operation status:  * &#x60;0&#x60; - Success  * &#x60;2&#x60; - Error (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList requestRoomActivitiesLogAsJson(Long roomId, String xSdsDateFormat, String sort, Integer offset, Integer limit, String dateStart, String dateEnd, Integer type, Long userId, Integer status, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomActivitiesLogAsJson");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/events"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<LogEventList> localVarReturnType = new GenericType<LogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request room granted group(s) or / and group(s) that can be granted
   * ### Description:   Retrieve a list of groups that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Postcondition: List of groups is returned.  ### Further Information:  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** granted to this room **AND** whose name is like &#x60;searchString&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;name&#x60; | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | &#x60;groupId&#x60; | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | &#x60;isGranted&#x60; | Filter the groups that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for groups in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | &#x60;permissionsManage&#x60; | Filter the groups that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | &#x60;effectivePerm&#x60; | Filter groups with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Group name |  &lt;/details&gt;
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomGroupList
   * @throws ApiException if fails to make API call
   */
  public RoomGroupList requestRoomGroups(Long roomId, Integer offset, Integer limit, String filter, String sort, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomGroups");
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RoomGroupList> localVarReturnType = new GenericType<RoomGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Room Policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.32.0&lt;/h3&gt;  ### Description:   Retrieve the room policies: * &#x60;defaultExpirationPeriod&#x60;  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in that room.  ### Postcondition: Room Policies returned.  ### Further Information: &#x60;defaultExpirationPeriod&#x60;: Default policy room expiration period in seconds. All existing and future files in a room will have their expiration date set to this period after their respective upload. Existing files can be set to expire earlier afterwards. &#x60;0&#x60; means no default expiration policy will be enforced.    
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomPolicies
   * @throws ApiException if fails to make API call
   */
  public RoomPolicies requestRoomPolicies(Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomPolicies");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/policies"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RoomPolicies> localVarReturnType = new GenericType<RoomPolicies>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request room rescue key
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.24.0&lt;/h3&gt;  ### Description:   Returns the file key for the room emergency password / rescue key of a certain file (if available).  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in parent room.  ### Postcondition: File key is returned.  ### Further Information: None.
   * @param fileId File ID (required)
   * @param version Version (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public FileKey requestRoomRescueKey(Long fileId, String version, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling requestRoomRescueKey");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/data_room_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request room rescue key
   * ### Description:   Retrieve the room rescue key pair.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in that room.  ### Postcondition: Key pair is returned.  ### Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param version Version (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer requestRoomRescueKeyPair(Long roomId, String xSdsDateFormat, String version, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomRescueKeyPair");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/keypair"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UserKeyPairContainer> localVarReturnType = new GenericType<UserKeyPairContainer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request all room rescue key pairs
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.24.0&lt;/h3&gt;  ### Description:   Retrieve all room rescue key pairs to allow migrating room-rescue-key-encrypted file keys.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in that room.  ### Postcondition: List of key pairs is returned.  ### Further Information: In the case of an algorithm migration to a room rescue key pair, one should create the new key pair before deleting the old one. This allows re-encrypting file keys with the new key pair, using the old one.  This API allows to retrieve both key pairs, in contrast to &#x60;GET /nodes/rooms/{room_id}/keypair&#x60;, which only delivers the preferred one. 
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return List&lt;UserKeyPairContainer&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserKeyPairContainer> requestRoomRescueKeyPairs(Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomRescueKeyPairs");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/keypairs"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<UserKeyPairContainer>> localVarReturnType = new GenericType<List<UserKeyPairContainer>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of all assigned S3 tags to the room
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.9.0&lt;/h3&gt;  ### Description:   Retrieve a list of S3 tags assigned to a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: List of assigned S3 tags is returned.  ### Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3TagList
   * @throws ApiException if fails to make API call
   */
  public S3TagList requestRoomS3Tags(Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomS3Tags");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/s3_tags"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request room granted user(s) or / and user(s) that can be granted
   * ### Description:   Retrieve a list of users that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Postcondition: None.  ### Further Information: List of users is returned.  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &gt; &#x60;permissionsManage:eq:true|user:cn:searchString&#x60;   Get all users that have &#x60;manage&#x60; permissions to this room **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;user&#x60; | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | &#x60;userId&#x60; | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | &#x60;isGranted&#x60; | Filter the users that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for users in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | &#x60;permissionsManage&#x60; | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | &#x60;effectivePerm&#x60; | Filter users with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; | | &#x60;hasRole&#x60; | User role filter&lt;br&gt;For more Roles information please call &#x60;GET /roles API&#x60; | &#x60;eq&#x60;, &#x60;neq&#x60; | User role  equals value. | &lt;ul&gt;&lt;li&gt;&#x60;CONFIG_MANAGER&#x60; - Manage global configs&lt;/li&gt;&lt;li&gt;&#x60;USER_MANAGER&#x60; - Manage Users&lt;/li&gt;&lt;li&gt;&#x60;GROUP_MANAGER&#x60; - Manage User-Groups&lt;/li&gt;&lt;li&gt;&#x60;ROOM_MANAGER&#x60; - Manage top level Data Rooms&lt;/li&gt;&lt;li&gt;&#x60;LOG_AUDITOR&#x60; - Read logs&lt;/li&gt;&lt;li&gt;&#x60;NONMEMBER_VIEWER&#x60; - View users and groups when having room manage permission&lt;/li&gt;&lt;li&gt;&#x60;USER&#x60; - Regular User role&lt;/li&gt;&lt;li&gt;&#x60;GUEST_USER&#x60; - Guest User role&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt;  ### Deprecated filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &lt;del&gt;&#x60;displayName&#x60;&lt;/del&gt; | User display name filter (use &#x60;user&#x60; filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;user:desc&#x60;   Sort by &#x60;user&#x60; descending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;user&#x60;** | User - sort by &#x60;firstName&#x60;, &#x60;lastName&#x60;, &#x60;username&#x60;, &#x60;email&#x60; (in this order) |  &lt;/details&gt;
   * @param roomId Room ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoomUserList
   * @throws ApiException if fails to make API call
   */
  public RoomUserList requestRoomUsers(Long roomId, Integer offset, Integer limit, String filter, String sort, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling requestRoomUsers");
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RoomUserList> localVarReturnType = new GenericType<RoomUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request system rescue key
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.24.0&lt;/h3&gt;  ### Description:   Returns the file key for the system emergency password / rescue key of a certain file (if available).  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions in parent room.  ### Postcondition: File key is returned.  ### Further Information: None.
   * @param fileId File ID (required)
   * @param version Version (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public FileKey requestSystemRescueKey(Long fileId, String version, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling requestSystemRescueKey");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/data_space_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request status of S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Request status of a S3 file upload.  ### Precondition: An upload channel has been created and user has to be the creator of the upload channel.  ### Postcondition: Status of S3 multipart upload request is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status | Error Code | Description | | :--- | :--- | :--- | | &#x60;400 Bad Request&#x60; | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | &#x60;400 Bad Request&#x60; | &#x60;-80001&#x60; | Invalid positive number | | &#x60;400 Bad Request&#x60; | &#x60;-80002&#x60; | Invalid number | | &#x60;400 Bad Request&#x60; | &#x60;-40001&#x60; | (Target) room is not encrypted | | &#x60;400 Bad Request&#x60; | &#x60;-40755&#x60; | Bad file name | | &#x60;400 Bad Request&#x60; | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | &#x60;400 Bad Request&#x60; | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | &#x60;403 Forbidden&#x60; |  | Access denied | | &#x60;404 Not Found&#x60; | &#x60;-20501&#x60; | Upload not found | | &#x60;404 Not Found&#x60; | &#x60;-40000&#x60; | Container not found | | &#x60;404 Not Found&#x60; | &#x60;-41000&#x60; | Node not found | | &#x60;404 Not Found&#x60; | &#x60;-70501&#x60; | User not found | | &#x60;409 Conflict&#x60; | &#x60;-40010&#x60; | Container cannot be overwritten | | &#x60;409 Conflict&#x60; |  | File cannot be overwritten | | &#x60;500 Internal Server Error&#x60; |  | System Error | | &#x60;502 Bad Gateway&#x60; |  | S3 Error | | &#x60;502 Insufficient Storage&#x60; | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | &#x60;502 Insufficient Storage&#x60; | &#x60;-40200&#x60; | Exceeds the free node quota in room | | &#x60;502 Insufficient Storage&#x60; | &#x60;-90200&#x60; | Exceeds the free customer quota | | &#x60;502 Insufficient Storage&#x60; | &#x60;-90201&#x60; | Exceeds the free customer physical disk space |  &lt;/details&gt;
   * @param uploadId Upload channel ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3FileUploadStatus
   * @throws ApiException if fails to make API call
   */
  public S3FileUploadStatus requestUploadStatusFiles(String uploadId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling requestUploadStatusFiles");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/uploads/{upload_id}"
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<S3FileUploadStatus> localVarReturnType = new GenericType<S3FileUploadStatus>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request user&#x27;s file key
   * ### Description:   Returns the file key for the current user (if available).  ### Precondition: User with one of the following permissions in parent room: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;  ### Postcondition: File key is returned.  ### Further Information: The symmetric file key is encrypted with the user&#x27;s public key.   File keys are generated with the workflow _\&quot;Generate file keys\&quot;_ that starts at &#x60;GET /nodes/missingFileKeys&#x60;.
   * @param fileId File ID (required)
   * @param version Version (NEW) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey requestUserFileKey(Long fileId, String version, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling requestUserFileKey");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}/user_file_key"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Restore deleted nodes
   * ### Description:   Restore a list of deleted nodes.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in parent room and &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; restore recycle bin&lt;/span&gt; permissions.  ### Postcondition: The selected files are moved from the recycle bin to the chosen productive container.  ### Further Information: If no parent ID is provided, the node is restored to its previous location.   The default resolution strategy is &#x60;autorename&#x60; that adds numbers to the file name until the conflict is solved.   If an existing file is overwritten, it is moved to the recycle bin instead of the restored one.  Download share id (if exists) gets changed if: - node with the same name exists in the target container - &#x60;resolutionStrategy&#x60; is &#x60;overwrite&#x60; - &#x60;keepShareLinks&#x60; is &#x60;true&#x60;
   * @param body  (required)
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke granted group(s) from room
   * ### Description:   Revoke granted groups from room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Group&#x27;s permissions are revoked.  ### Further Information: Batch function.  
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void revokeRoomGroups(RoomGroupsDeleteBatchRequest body, Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling revokeRoomGroups");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling revokeRoomGroups");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/groups"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke granted user(s) from room
   * ### Description:   Revoke granted users from room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: User&#x27;s permissions are revoked.  ### Further Information: Batch function.  
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void revokeRoomUsers(RoomUsersDeleteBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling revokeRoomUsers");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling revokeRoomUsers");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Search nodes
   * ### Description:   Provides a flat list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user is allowed to &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128065; see&lt;/span&gt; nodes (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Postcondition: List of nodes is returned.  ### Further Information:   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).   &#x60;EncryptionInfo&#x60; is **NOT** provided.   Wildcard character is the asterisk character: &#x60;*&#x60;  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;type:eq:file|createdAt:ge:2015-01-01&#x60;   Get nodes where type equals &#x60;file&#x60; **AND** file creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60;            | Filter Description                | &#x60;OPERATOR&#x60; | Operator Description                                                                                                                                                                                                                                                                | &#x60;VALUE&#x60; | |:------------------------|:----------------------------------| :--- |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| :--- | | &#x60;type&#x60;                  | Node type filter                  | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60;                                                                                                                                        | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | &#x60;fileType&#x60;              | File type filter (file extension) | &#x60;cn, eq&#x60; | File type contains / equals value.                                                                                                                                                                                                                                                  | &#x60;search String&#x60; | | &#x60;classification&#x60;        | Classification filter             | &#x60;eq&#x60; | Classification equals value.                                                                                                                                                                                                                                                        | &lt;ul&gt;&lt;li&gt;&#x60;1&#x60; - public&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - internal&lt;/li&gt;&lt;li&gt;&#x60;3&#x60; - confidential&lt;/li&gt;&lt;li&gt;&#x60;4&#x60; - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | &#x60;createdBy&#x60;             | Creator login filter              | &#x60;cn, eq&#x60; | Creator login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;).                                                                                                                                                                             | &#x60;search String&#x60; | | &#x60;createdById&#x60;           | Creator ID filter                 | &#x60;eq&#x60; | Creator ID equals value.                                                                                                                                                                                                                                                            | &#x60;positive Integer  or -1 for external user&#x60; | | &#x60;createdAt&#x60;             | Creation date filter              | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60;                                                                | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;updatedBy&#x60;             | Last modifier login filter        | &#x60;cn, eq&#x60; | Last modifier login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;).                                                                                                                                                                       | &#x60;search String&#x60; | | &#x60;updatedById&#x60;           | Last modifier ID filter           | &#x60;eq&#x60; | Modifier ID equals value.                                                                                                                                                                                                                                                           | &#x60;positive Integer or -1 for external user&#x60; | | &#x60;updatedAt&#x60;             | Last modification date filter     | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60;                                                       | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;expireAt&#x60;              | Expiration date filter            | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expireAt:ge:2016-12-31&#x60;&amp;#124;&#x60;expireAt:le:2018-01-01&#x60;                                                                | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;size&#x60;                  | Node size filter                  | &#x60;ge, le&#x60; | Node size is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;size:ge:5&#x60;&amp;#124;&#x60;size:le:10&#x60;                                                                                               | &#x60;size in bytes&#x60; | | &#x60;isFavorite&#x60;            | Favorite filter                   | &#x60;eq&#x60; |                                                                                                                                                                                                                                                                                     | &#x60;true or false&#x60; | | &#x60;branchVersion&#x60;         | Node branch version filter        | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60;                                                 | &#x60;version number&#x60; | | &#x60;parentPath&#x60;            | Parent path                       | &#x60;cn, eq&#x60; | Parent path contains / equals  value.                                                                                                                                                                                                                                               | &#x60;search String&#x60; | | &#x60;timestampCreation&#x60;     | Creation timestamp filter         | &#x60;ge, le&#x60; | Creation timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampCreation:ge:2016-12-31T23:00:00.123&#x60;&amp;#124;&lt;br&gt;&#x60;timestampCreation:le:2018-01-01T11:00:00.540&#x60;             | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;timestampModification&#x60; | Modification timestamp filter     | &#x60;ge, le&#x60; | Modification timestamp is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;timestampModification:ge:2016-12-31T23:00:00.123&#x60;&amp;#124;&lt;br&gt;&#x60;timestampModification:le:2018-01-01T11:00:00.540&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;referenceId&#x60;           | Reference ID filter               | &#x60;eq&#x60; | Reference ID equals value.                                                                                                                                                                                                                                                          | &#x60;Integer &#x60; | &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:desc|size:asc&#x60;   Sort by &#x60;name&#x60; descending **AND** &#x60;size&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Node name | | &#x60;createdAt&#x60; | Creation date | | &#x60;createdBy&#x60; | Creator first name, last name | | &#x60;updatedAt&#x60; | Last modification date | | &#x60;updatedBy&#x60; | Last modifier first name, last name | | &#x60;fileType&#x60; | File type (extension) | | &#x60;classification&#x60; | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | &#x60;size&#x60; | Node size | | &#x60;cntDeletedVersions&#x60; | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | &#x60;type&#x60; | Node type (room, folder, file) | | &#x60;parentPath&#x60; | Parent path | | &#x60;timestampCreation&#x60; | Creation timestamp | | &#x60;timestampModification&#x60; | Modification timestamp |  &lt;/details&gt;  ### Deprecated sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &lt;del&gt;&#x60;cntChildren&#x60;&lt;/del&gt; | Number of direct children (**NOT** recursive; for rooms and folders only) |  &lt;/details&gt;
   * @param searchString Search string (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel * &#x60;0&#x60; - top level nodes only (default)  * &#x60;-1&#x60; - full tree  * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param parentId Parent node ID.  Only rooms and folders can be parents.  Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList searchNodes(String searchString, String xSdsDateFormat, Integer depthLevel, Long parentId, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'searchString' is set
    if (searchString == null) {
      throw new ApiException(400, "Missing the required parameter 'searchString' when calling searchNodes");
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

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Set room policies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.32.0&lt;/h3&gt;  ### Description:   Set the room policies: * &#x60;defaultExpirationPeriod&#x60; * &#x60;virusProtectionEnabled&#x60;  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Room policy is set.  ### Further Information: &#x60;defaultExpirationPeriod&#x60;: Default policy room expiration period in seconds. All existing and future files in a room will have their expiration date set to this period after their respective upload. Existing files can be set to expire earlier afterwards. &#x60;0&#x60; means no default expiration policy will be enforced. This removes all expiration dates from existing files.  &#x60;virusProtectionEnabled&#x60;: Status of room policy for virus-protection. Can be activated for unencrypted data rooms. If enabled, the files are sent to the German IT security company G DATA CyberDefense for verification. 
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomPolicies(RoomPoliciesRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomPolicies");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomPolicies");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/policies"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set room&#x27;s rescue key pair
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.24.0&lt;/h3&gt;  ### Description:   Set room rescue key pair.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Key pair is set.  ### Further Information: Room rescue key pair can be used to upgrade algorithm.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomRescueKeyPair(UserKeyPairContainer body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomRescueKeyPair");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomRescueKeyPair");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set S3 tags for a room
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.9.0&lt;/h3&gt;  ### Description:   Set S3 tags to a room.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;.  ### Postcondition: Provided S3 tags are assigned to a room.  ### Further Information: Every request overrides current S3 tags.   Mandatory S3 tag IDs **MUST** be sent.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3TagList
   * @throws ApiException if fails to make API call
   */
  public S3TagList setRoomS3Tags(S3TagIds body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomS3Tags");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomS3Tags");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/s3_tags"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Set file keys for a list of users and files
   * ### Description:   Sets symmetric file keys for several users and files.  ### Precondition: User has file keys for the files.   Only settable by users that own one of the following permissions: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change config&lt;/span&gt;  ### Postcondition: Stores new file keys for other users.  ### Further Information: Only users with copies of the file key (encrypted with their public keys) can access a certain file.   This endpoint is used for the distribution of file keys amongst an authorized user base.   User can set file key for himself.   The users who already have a file key are ignored and keep the distributed file key 
   * @param body  (required)
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Mark or unmark a list of nodes (room, folder or file) as favorite
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.25.0&lt;/h3&gt;  ### Description:   Marks or unmarks a list of nodes (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128065; see&lt;/span&gt; the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Postcondition: Nodes gets marked as favorite.  ### Further Information: Maximum number of nodes is 200.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateFavorites(UpdateFavoritesBulkRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFavorites");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/favorites";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Updates a file’s metadata
   * ### Description: Updates a list of file’s metadata.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change&lt;/span&gt; permissions in parent room.  ### Postcondition: File&#x27;s metadata is changed.   
   * @param body  (required)
   * @param fileId File ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFile(UpdateFileRequest body, Long fileId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFile");
    }
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling updateFile");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files/{file_id}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Updates a list of  file’s metadata
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.25.0&lt;/h3&gt;  ### Description:   Updates a list of file’s metadata.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change&lt;/span&gt; permissions in parent room.  ### Postcondition: File&#x27;s metadata is changed.  ### Further Information: Maximum number of files is 200 
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateFiles(UpdateFilesBulkRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFiles");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/files";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Updates folder’s metadata
   * ### Description:   Updates folder’s metadata.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change&lt;/span&gt; permissions in parent room.  ### Postcondition: Folder&#x27;s metadata is changed.  ### Further Information: Notes are limited to **255** characters.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param folderId Folder ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFolder(UpdateFolderRequest body, Long folderId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFolder");
    }
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling updateFolder");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/folders/{folder_id}"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Edit node comment
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.10.0&lt;/h3&gt;  ### Description: Edit the text of an existing comment for a specific node.  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt; permissions on the node and is the creator of the comment.  ### Postcondition: Comments text gets changed.  ### Further Information: Maximum allowed text length: **65535** characters.
   * @param body  (required)
   * @param commentId Comment ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Comment
   * @throws ApiException if fails to make API call
   */
  public Comment updateNodeComment(ChangeNodeCommentRequest body, Long commentId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateNodeComment");
    }
    // verify the required parameter 'commentId' is set
    if (commentId == null) {
      throw new ApiException(400, "Missing the required parameter 'commentId' when calling updateNodeComment");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/comments/{comment_id}"
      .replaceAll("\\{" + "comment_id" + "\\}", apiClient.escapeString(commentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Comment> localVarReturnType = new GenericType<Comment>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Updates room’s metadata
   * ### Description:   Updates room’s metadata.  ### Precondition: User is a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt; at superordinated level.  ### Postcondition: Room&#x27;s metadata is changed.  ### Further Information: Notes are limited to **255** characters.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60;
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateRoom(UpdateRoomRequest body, Long roomId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRoom");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling updateRoom");
    }
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add or change room granted group(s)
   * ### Description: All existing group permissions will be overwritten.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;. To add new members, the user needs the right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; non-members add&lt;/span&gt;, which is included in any role.  ### Postcondition: Group&#x27;s permissions are changed.  ### Further Information: Batch function.   
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateRoomGroups(RoomGroupsAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRoomGroups");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling updateRoomGroups");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Add or change room granted user(s)
   * ### Description: All existing user permissions will be overwritten.  ### Precondition: User needs to be a &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Room Administrator&lt;/span&gt;. To add new members, the user needs the right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; non-members add&lt;/span&gt;, which is included in any role.  ### Postcondition: User&#x27;s permissions are changed.  ### Further Information: Batch function.
   * @param body  (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateRoomUsers(RoomUsersAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRoomUsers");
    }
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling updateRoomUsers");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Upload file
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128679; Deprecated since v4.9.0&lt;/h3&gt;  ### Use &#x60;uploads&#x60; API  ### Description:   Uploads a file or parts of it in an active upload channel.  ### Precondition: An upload channel has been created.  ### Postcondition: A file or parts of it are uploaded to a temporary location.  ### Further Information: This endpoints supports chunked upload.    Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;     For both file upload types set the correct &#x60;Content-Type&#x60; header and body.    ### Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id}  HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param uploadId Upload channel ID (required)
   * @param file  (optional)
   * @param contentRange Content-Range   e.g. &#x60;bytes 0-999/3980&#x60; (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   * @deprecated
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Upload file Documentation</a>
   */
  @Deprecated
  public ChunkUploadResponse uploadFileAsMultipart(String uploadId, File file, String contentRange, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadFileAsMultipart");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
