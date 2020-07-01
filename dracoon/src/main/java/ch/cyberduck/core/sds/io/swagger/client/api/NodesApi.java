package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
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
import ch.cyberduck.core.sds.io.swagger.client.model.GeneratePresignedUrlsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.MissingKeysResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeBatchOperationErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeParentList;
import ch.cyberduck.core.sds.io.swagger.client.model.NotRestoredNodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentList;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrlList;
import ch.cyberduck.core.sds.io.swagger.client.model.RestoreDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomWebhookList;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadStatus;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagIds;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagList;
import ch.cyberduck.core.sds.io.swagger.client.model.SyslogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.io.swagger.client.model.ZipDownloadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
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
   * ### Functional Description:   Marks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: A node gets marked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node addFavorite(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return addFavoriteWithHttpInfo(nodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Mark a node (room, folder or file) as favorite
   * ### Functional Description:   Marks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: A node gets marked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> addFavoriteWithHttpInfo(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * ### Functional Description: Cancel an (S3) file upload and destroy the upload channel.  ### Precondition: * Valid upload ID * An upload channel has been created * User has to be the creator of the upload channel  ### Effects: The upload channel is removed and all temporary uploaded data is purged.  ### &amp;#9432; Further Information: It is recommended to notify the API about cancelled uploads if possible.
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void cancelFileUpload(String uploadId, String xSdsAuthToken) throws ApiException {

    cancelFileUploadWithHttpInfo(uploadId, xSdsAuthToken);
  }

  /**
   * Cancel file upload
   * ### Functional Description: Cancel an (S3) file upload and destroy the upload channel.  ### Precondition: * Valid upload ID * An upload channel has been created * User has to be the creator of the upload channel  ### Effects: The upload channel is removed and all temporary uploaded data is purged.  ### &amp;#9432; Further Information: It is recommended to notify the API about cancelled uploads if possible.
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> cancelFileUploadWithHttpInfo(String uploadId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Edit node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Edit the text of an existing comment for a specific node.  ### Precondition: * User has _\&quot;read\&quot;_ permissions on the node. * User has to be the creator of the comment.  ### Effects: Comments text gets changed.  ### &amp;#9432; Further Information: Maximum allowed text length: **65535** characters.
   * @param body body (required)
   * @param commentId Comment ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Comment
   * @throws ApiException if fails to make API call
   */
  public Comment changeNodeComment(ChangeNodeCommentRequest body, Long commentId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return changeNodeCommentWithHttpInfo(body, commentId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Edit node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Edit the text of an existing comment for a specific node.  ### Precondition: * User has _\&quot;read\&quot;_ permissions on the node. * User has to be the creator of the comment.  ### Effects: Comments text gets changed.  ### &amp;#9432; Further Information: Maximum allowed text length: **65535** characters.
   * @param body body (required)
   * @param commentId Comment ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Comment&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Comment> changeNodeCommentWithHttpInfo(ChangeNodeCommentRequest body, Long commentId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeNodeComment");
    }
    
    // verify the required parameter 'commentId' is set
    if (commentId == null) {
      throw new ApiException(400, "Missing the required parameter 'commentId' when calling changeNodeComment");
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
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Comment> localVarReturnType = new GenericType<Comment>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Handle user-room assignments per group
   * ### Functional Description:   Handles a list of user-room assignments by groups that have **NOT** been approved yet   **WAITING** or **DENIED** assignments can be **ACCEPTED**.  ### Precondition: None.  ### Effects: User-room assignment is approved and the user gets access to the group.  ### &amp;#9432; Further Information: Room administrators should **SHOULD** handle pending assignments to provide access to rooms for other users.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void changePendingAssignments(PendingAssignmentsRequest body, String xSdsAuthToken) throws ApiException {

    changePendingAssignmentsWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Handle user-room assignments per group
   * ### Functional Description:   Handles a list of user-room assignments by groups that have **NOT** been approved yet   **WAITING** or **DENIED** assignments can be **ACCEPTED**.  ### Precondition: None.  ### Effects: User-room assignment is approved and the user gets access to the group.  ### &amp;#9432; Further Information: Room administrators should **SHOULD** handle pending assignments to provide access to rooms for other users.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> changePendingAssignmentsWithHttpInfo(PendingAssignmentsRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * ## &amp;#9888; Deprecated since version 4.9.0  ### Use &#x60;uploads&#x60; API  ### Functional Description: Finishes an upload and closes the corresponding upload channel.  ### Precondition: An upload channel has been created and data has been transmitted.  ### Effects: The upload is finished and the temporary file is moved to the productive environment.  ### &amp;#9432; Further Information: The provided file name might be changed in accordance with the resolution strategy:   * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;  ### 200 OK is **NOT** used by this API 
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param body body (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public Node completeFileUpload(String uploadId, String xSdsAuthToken, String xSdsDateFormat, CompleteUploadRequest body) throws ApiException {
    return completeFileUploadWithHttpInfo(uploadId, xSdsAuthToken, xSdsDateFormat, body).getData();
      }

  /**
   * Complete file upload
   * ## &amp;#9888; Deprecated since version 4.9.0  ### Use &#x60;uploads&#x60; API  ### Functional Description: Finishes an upload and closes the corresponding upload channel.  ### Precondition: An upload channel has been created and data has been transmitted.  ### Effects: The upload is finished and the temporary file is moved to the productive environment.  ### &amp;#9432; Further Information: The provided file name might be changed in accordance with the resolution strategy:   * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;  ### 200 OK is **NOT** used by this API 
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param body body (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<Node> completeFileUploadWithHttpInfo(String uploadId, String xSdsAuthToken, String xSdsDateFormat, CompleteUploadRequest body) throws ApiException {
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
      "application/json"
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
   * Complete S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: * Valid upload ID * An upload channel has been created and data has been transmitted * User has to be the creator of the upload channel  ### Effects: Upload channel is closed. S3 multipart upload request is completed.  ### &amp;#9432; Further Information: #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**   
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void completeS3FileUpload(CompleteS3FileUploadRequest body, String uploadId, String xSdsAuthToken) throws ApiException {

    completeS3FileUploadWithHttpInfo(body, uploadId, xSdsAuthToken);
  }

  /**
   * Complete S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: * Valid upload ID * An upload channel has been created and data has been transmitted * User has to be the creator of the upload channel  ### Effects: Upload channel is closed. S3 multipart upload request is completed.  ### &amp;#9432; Further Information: #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**   
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> completeS3FileUploadWithHttpInfo(CompleteS3FileUploadRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Configure room
   * ### Functional Description: Configure a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Room&#39;s configuration is changed.  ### &amp;#9432; Further Information: Provided (or default) classification is taken from room when file gets uploaded without any classification.    To set &#x60;adminIds&#x60; or &#x60;adminGroupIds&#x60; the &#x60;inheritPermissions&#x60; value has to be &#x60;false&#x60;. Otherwise use: * &#x60;PUT /nodes/rooms/{room_id}/groups&#x60; * &#x60;PUT /nodes/rooms/{room_id}/users &#x60;    APIs.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node configRoom(ConfigRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return configRoomWithHttpInfo(body, roomId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Configure room
   * ### Functional Description: Configure a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Room&#39;s configuration is changed.  ### &amp;#9432; Further Information: Provided (or default) classification is taken from room when file gets uploaded without any classification.    To set &#x60;adminIds&#x60; or &#x60;adminGroupIds&#x60; the &#x60;inheritPermissions&#x60; value has to be &#x60;false&#x60;. Otherwise use: * &#x60;PUT /nodes/rooms/{room_id}/groups&#x60; * &#x60;PUT /nodes/rooms/{room_id}/users &#x60;    APIs.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> configRoomWithHttpInfo(ConfigRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling configRoom");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling configRoom");
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
      "application/json"
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
   * Copy node(s)
   * ### Functional Description: Copies nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are copied to target parent.  ### &amp;#9432; Further Information: Nodes **MUST** be in same source parent.   &amp;#9888; **Rooms **CANNOT** be copied.** ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node copyNodes(CopyNodesRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return copyNodesWithHttpInfo(body, nodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Copy node(s)
   * ### Functional Description: Copies nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are copied to target parent.  ### &amp;#9432; Further Information: Nodes **MUST** be in same source parent.   &amp;#9888; **Rooms **CANNOT** be copied.** ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> copyNodesWithHttpInfo(CopyNodesRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Generate download URL
   * ### Functional Description: Create a download URL to retrieve a file without &#x60;X-Sds-Auth-Token&#x60; Header.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download token is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; ressources.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse createFileDownloadToken(Long fileId, String xSdsAuthToken) throws ApiException {
    return createFileDownloadTokenWithHttpInfo(fileId, xSdsAuthToken).getData();
      }

  /**
   * Generate download URL
   * ### Functional Description: Create a download URL to retrieve a file without &#x60;X-Sds-Auth-Token&#x60; Header.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download token is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; ressources.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;DownloadTokenGenerateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DownloadTokenGenerateResponse> createFileDownloadTokenWithHttpInfo(Long fileId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * ### Functional Description: This endpoint creates a new upload channel which is the first step in any file upload workflow.  ### Precondition: User has _\&quot;create\&quot;_ permissions in the parent container (room or folder).  ### Effects: A new upload channel for a file is created.   Its ID and an upload token are returned.  ### &amp;#9432; Further Information: The upload ID is used for uploads with &#x60;X-Sds-Auth-Token&#x60; header, the upload token can be used for uploads without authentication header.  Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CreateFileUploadResponse
   * @throws ApiException if fails to make API call
   */
  public CreateFileUploadResponse createFileUpload(CreateFileUploadRequest body, String xSdsAuthToken) throws ApiException {
    return createFileUploadWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create new file upload channel
   * ### Functional Description: This endpoint creates a new upload channel which is the first step in any file upload workflow.  ### Precondition: User has _\&quot;create\&quot;_ permissions in the parent container (room or folder).  ### Effects: A new upload channel for a file is created.   Its ID and an upload token are returned.  ### &amp;#9432; Further Information: The upload ID is used for uploads with &#x60;X-Sds-Auth-Token&#x60; header, the upload token can be used for uploads without authentication header.  Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;CreateFileUploadResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CreateFileUploadResponse> createFileUploadWithHttpInfo(CreateFileUploadRequest body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<CreateFileUploadResponse> localVarReturnType = new GenericType<CreateFileUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new folder
   * ### Functional Description: Create a new folder.  ### Precondition: User has _\&quot;create\&quot;_ permissions in current room.  ### Effects: New folder is created.  ### &amp;#9432; Further Information: Folders **CANNOT** be created on top level (without parent element).  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createFolder(CreateFolderRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return createFolderWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Create new folder
   * ### Functional Description: Create a new folder.  ### Precondition: User has _\&quot;create\&quot;_ permissions in current room.  ### Effects: New folder is created.  ### &amp;#9432; Further Information: Folders **CANNOT** be created on top level (without parent element).  Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> createFolderWithHttpInfo(CreateFolderRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * Create node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Create a comment for a specific node.  ### Precondition: User has _\&quot;read\&quot;_ permissions on the node.  ### Effects: Comment is created.  ### &amp;#9432; Further Information: Maximum allowed text length: **65535** characters.
   * @param body body (required)
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Comment
   * @throws ApiException if fails to make API call
   */
  public Comment createNodeComment(CreateNodeCommentRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return createNodeCommentWithHttpInfo(body, nodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Create node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Create a comment for a specific node.  ### Precondition: User has _\&quot;read\&quot;_ permissions on the node.  ### Effects: Comment is created.  ### &amp;#9432; Further Information: Maximum allowed text length: **65535** characters.
   * @param body body (required)
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Comment&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Comment> createNodeCommentWithHttpInfo(CreateNodeCommentRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Comment> localVarReturnType = new GenericType<Comment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new room
   * ### Functional Description: Creates a new room at the provided parent node.   Creation of top level rooms provided.  ### Precondition: User has _\&quot;manage\&quot;_ permissions in the parent room.  ### Effects: A new room is created.  ### &amp;#9432; Further Information:   Rooms may only have other rooms as parent.   Rooms on top level do **NOT** have any parent.   Rooms may have rooms as children on n hierarchy levels.   If permission inheritance is disabled, there **MUST** be at least one admin user / group (with neither the group nor the user having an expiration date).  Notes are limited to **255** characters.  Provided (or default) classification is taken from room when file gets uploaded without any classification.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createRoom(CreateRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return createRoomWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Create new room
   * ### Functional Description: Creates a new room at the provided parent node.   Creation of top level rooms provided.  ### Precondition: User has _\&quot;manage\&quot;_ permissions in the parent room.  ### Effects: A new room is created.  ### &amp;#9432; Further Information:   Rooms may only have other rooms as parent.   Rooms on top level do **NOT** have any parent.   Rooms may have rooms as children on n hierarchy levels.   If permission inheritance is disabled, there **MUST** be at least one admin user / group (with neither the group nor the user having an expiration date).  Notes are limited to **255** characters.  Provided (or default) classification is taken from room when file gets uploaded without any classification.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> createRoomWithHttpInfo(CreateRoomRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * Delete nodes from recycle bin
   * ### Functional Description: Permanently remove a list of nodes from the recycle bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All provided nodes are removed.  ### &amp;#9432; Further Information: The removal of deleted nodes from the recycle bin is irreversible.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteDeletedNodes(DeleteDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {

    deleteDeletedNodesWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Delete nodes from recycle bin
   * ### Functional Description: Permanently remove a list of nodes from the recycle bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All provided nodes are removed.  ### &amp;#9432; Further Information: The removal of deleted nodes from the recycle bin is irreversible.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteDeletedNodesWithHttpInfo(DeleteDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete node
   * ### Functional Description: Delete node (room, folder or file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on: * supplied nodes (for folders or files) * superordinated node (for rooms)  ### Effects: Node gets deleted.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteNode(Long nodeId, String xSdsAuthToken) throws ApiException {

    deleteNodeWithHttpInfo(nodeId, xSdsAuthToken);
  }

  /**
   * Delete node
   * ### Functional Description: Delete node (room, folder or file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on: * supplied nodes (for folders or files) * superordinated node (for rooms)  ### Effects: Node gets deleted.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteNodeWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Delete an existing comment for a specific node.  ### Precondition: * User has _\&quot;read\&quot;_ permissions on the node. * User has to be:     * Creator of the comment **OR**     * Room administrator in auth parent room.  ### Effects: Comment is deleted.  ### &amp;#9432; Further Information: None.
   * @param commentId Comment ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteNodeComment(Long commentId, String xSdsAuthToken) throws ApiException {

    deleteNodeCommentWithHttpInfo(commentId, xSdsAuthToken);
  }

  /**
   * Delete node comment
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Delete an existing comment for a specific node.  ### Precondition: * User has _\&quot;read\&quot;_ permissions on the node. * User has to be:     * Creator of the comment **OR**     * Room administrator in auth parent room.  ### Effects: Comment is deleted.  ### &amp;#9432; Further Information: None.
   * @param commentId Comment ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteNodeCommentWithHttpInfo(Long commentId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'commentId' is set
    if (commentId == null) {
      throw new ApiException(400, "Missing the required parameter 'commentId' when calling deleteNodeComment");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete nodes
   * ### Functional Description: Delete nodes (room, folder or file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on: * supplied nodes (for folders or files) * superordinated node (for rooms)  ### Effects: Nodes are deleted.  ### &amp;#9432; Further Information: Nodes **MUST** be in same parent.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteNodes(DeleteNodesRequest body, String xSdsAuthToken) throws ApiException {

    deleteNodesWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Delete nodes
   * ### Functional Description: Delete nodes (room, folder or file).  ### Precondition: Authenticated user with _\&quot;delete\&quot;_ permissions on: * supplied nodes (for folders or files) * superordinated node (for rooms)  ### Effects: Nodes are deleted.  ### &amp;#9432; Further Information: Nodes **MUST** be in same parent.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteNodesWithHttpInfo(DeleteNodesRequest body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke granted group(s) from room
   * ### Functional Description:   Batch function.   Revoke granted groups from room.  ### Precondition: User needs to be a room administrator.  ### Effects: Group&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomGroupsBatch(RoomGroupsDeleteBatchRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {

    deleteRoomGroupsBatchWithHttpInfo(body, roomId, xSdsAuthToken, xSdsDateFormat);
  }

  /**
   * Revoke granted group(s) from room
   * ### Functional Description:   Batch function.   Revoke granted groups from room.  ### Precondition: User needs to be a room administrator.  ### Effects: Group&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteRoomGroupsBatchWithHttpInfo(RoomGroupsDeleteBatchRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomGroupsBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomGroupsBatch");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke granted user(s) from room
   * ### Functional Description:   Batch function.   Revoke granted users from room.  ### Precondition: User needs to be a room administrator.  ### Effects: User&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomUsersBatch(RoomUsersDeleteBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {

    deleteRoomUsersBatchWithHttpInfo(body, roomId, xSdsAuthToken);
  }

  /**
   * Revoke granted user(s) from room
   * ### Functional Description:   Batch function.   Revoke granted users from room.  ### Precondition: User needs to be a room administrator.  ### Effects: User&#39;s permissions are revoked.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteRoomUsersBatchWithHttpInfo(RoomUsersDeleteBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomUsersBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomUsersBatch");
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Empty recycle bin
   * ### Functional Description:   Empty a recycle bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All files in the recycle bin are permanently removed.  ### &amp;#9432; Further Information: Actually removes the previously deleted files from the system.   &amp;#9888; **This action is irreversible.**
   * @param nodeId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void emptyDeletedNodes(Long nodeId, String xSdsAuthToken) throws ApiException {

    emptyDeletedNodesWithHttpInfo(nodeId, xSdsAuthToken);
  }

  /**
   * Empty recycle bin
   * ### Functional Description:   Empty a recycle bin.  ### Precondition: User has _\&quot;delete recycle bin\&quot;_ permissions in parent room.  ### Effects: All files in the recycle bin are permanently removed.  ### &amp;#9432; Further Information: Actually removes the previously deleted files from the system.   &amp;#9888; **This action is irreversible.**
   * @param nodeId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> emptyDeletedNodesWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Encrypt room
   * ### Functional Description:   Activates the client-side encryption for a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Encryption of room is activated.  ### &amp;#9432; Further Information: Only empty rooms at the top level may be encrypted.   This endpoint may also be used to disable encryption of an empty room.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node encryptRoom(EncryptRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return encryptRoomWithHttpInfo(body, roomId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Encrypt room
   * ### Functional Description:   Activates the client-side encryption for a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Encryption of room is activated.  ### &amp;#9432; Further Information: Only empty rooms at the top level may be encrypted.   This endpoint may also be used to disable encryption of an empty room.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> encryptRoomWithHttpInfo(EncryptRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Generate presigned URLs for S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Generate presigned URLs for S3 file upload.  ### Precondition: * Valid upload ID * User has to be the creator of the upload channel  ### Effects: List of presigned URLs is returned.  ### &amp;#9432; Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PresignedUrlList
   * @throws ApiException if fails to make API call
   */
  public PresignedUrlList generatePresignedUrlsFiles(GeneratePresignedUrlsRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
    return generatePresignedUrlsFilesWithHttpInfo(body, uploadId, xSdsAuthToken).getData();
      }

  /**
   * Generate presigned URLs for S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Generate presigned URLs for S3 file upload.  ### Precondition: * Valid upload ID * User has to be the creator of the upload channel  ### Effects: List of presigned URLs is returned.  ### &amp;#9432; Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;PresignedUrlList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PresignedUrlList> generatePresignedUrlsFilesWithHttpInfo(GeneratePresignedUrlsRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<PresignedUrlList> localVarReturnType = new GenericType<PresignedUrlList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room emergency password (rescue key)
   * ### Functional Description:   Returns the file key for the room emergency password / rescue key of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataRoomFileKey(Long fileId, String xSdsAuthToken) throws ApiException {
    return getDataRoomFileKeyWithHttpInfo(fileId, xSdsAuthToken).getData();
      }

  /**
   * Get room emergency password (rescue key)
   * ### Functional Description:   Returns the file key for the room emergency password / rescue key of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;FileKey&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileKey> getDataRoomFileKeyWithHttpInfo(Long fileId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * Get system emergency password (rescue key)
   * ### Functional Description:   Returns the file key for the system emergency password / rescue key of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataSpaceFileKey(Long fileId, String xSdsAuthToken) throws ApiException {
    return getDataSpaceFileKeyWithHttpInfo(fileId, xSdsAuthToken).getData();
      }

  /**
   * Get system emergency password (rescue key)
   * ### Functional Description:   Returns the file key for the system emergency password / rescue key of a certain file (if available).  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;FileKey&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileKey> getDataSpaceFileKeyWithHttpInfo(Long fileId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range  e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public Integer getFileData(Long fileId, String range, String xSdsAuthToken, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getFileDataWithHttpInfo(fileId, range, xSdsAuthToken, genericMimetype, inline).getData();
      }

  /**
   * Download file
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range  e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<Integer> getFileDataWithHttpInfo(Long fileId, String range, String xSdsAuthToken, Boolean genericMimetype, Boolean inline) throws ApiException {
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "inline", inline));

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
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range  e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public Integer getFileData1(Long fileId, String range, String xSdsAuthToken, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getFileData1WithHttpInfo(fileId, range, xSdsAuthToken, genericMimetype, inline).getData();
      }

  /**
   * Download file
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;downloads&#x60; API  ### Functional Description: Download a file.  ### Precondition: User with _\&quot;read\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param fileId File ID (required)
   * @param range Range  e.g. &#x60;bytes&#x3D;0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<Integer> getFileData1WithHttpInfo(Long fileId, String range, String xSdsAuthToken, Boolean genericMimetype, Boolean inline) throws ApiException {
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "inline", inline));

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
   * ### Functional Description:   Get metadata of a deleted node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param deletedNodeId Deleted node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return DeletedNode
   * @throws ApiException if fails to make API call
   */
  public DeletedNode getFsDeletedNode(Long deletedNodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return getFsDeletedNodeWithHttpInfo(deletedNodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Get deleted node
   * ### Functional Description:   Get metadata of a deleted node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param deletedNodeId Deleted node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;DeletedNode&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DeletedNode> getFsDeletedNodeWithHttpInfo(Long deletedNodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * Get deleted versions of nodes
   * ### Functional Description:   Retrieve all deleted versions of a node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: The node is identified by three parameters: * parent ID (only room IDs are accepted as parent ID since only rooms may have a recycle bin.) * name * type (file, folder).  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;expireAt:desc&#x60;   Sort by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;accessedAt&#x60;** | Last access date | | **&#x60;size&#x60;** | Node size | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;deletedAt&#x60;** | Deleted date | | **&#x60;deletedBy&#x60;** | Deleter first name, last name |
   * @param name Node name (required)
   * @param nodeId Parent ID (room or folder ID) (required)
   * @param type Node type (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return DeletedNodeVersionsList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeVersionsList getFsDeletedNodeVersions(String name, Long nodeId, String type, String xSdsAuthToken, String xSdsDateFormat, Integer limit, Integer offset, String sort) throws ApiException {
    return getFsDeletedNodeVersionsWithHttpInfo(name, nodeId, type, xSdsAuthToken, xSdsDateFormat, limit, offset, sort).getData();
      }

  /**
   * Get deleted versions of nodes
   * ### Functional Description:   Retrieve all deleted versions of a node.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: The node is identified by three parameters: * parent ID (only room IDs are accepted as parent ID since only rooms may have a recycle bin.) * name * type (file, folder).  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;expireAt:desc&#x60;   Sort by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;accessedAt&#x60;** | Last access date | | **&#x60;size&#x60;** | Node size | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;deletedAt&#x60;** | Deleted date | | **&#x60;deletedBy&#x60;** | Deleter first name, last name |
   * @param name Node name (required)
   * @param nodeId Parent ID (room or folder ID) (required)
   * @param type Node type (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;DeletedNodeVersionsList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DeletedNodeVersionsList> getFsDeletedNodeVersionsWithHttpInfo(String name, Long nodeId, String type, String xSdsAuthToken, String xSdsDateFormat, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling getFsDeletedNodeVersions");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsDeletedNodeVersions");
    }
    
    // verify the required parameter 'type' is set
    if (type == null) {
      throw new ApiException(400, "Missing the required parameter 'type' when calling getFsDeletedNodeVersions");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/deleted_nodes/versions"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "name", name));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Get list of deleted nodes
   * ### Functional Description:   Retrieve a list of deleted nodes in a recycle bin.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: Only room IDs are accepted as parent ID since only rooms may have a recycle bin.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file:folder|name:cn:searchString_1|parentPath:cn:searchString_2&#x60;   Get deleted nodes where type equals (&#x60;file&#x60; **OR** &#x60;folder&#x60;) **AND** deleted node name containing &#x60;searchString_1&#x60; **AND** deleted node parent path containing &#x60;searchString 2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value(s).&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:folder:file&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;name&#x60;** | Node name filter | &#x60;cn&#x60; | Node name contains value. | &#x60;search String&#x60; | | **&#x60;parentPath&#x60;** | Parent path filter | &#x60;cn&#x60; | Parent path contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   **Nodes are sorted by type first, then by sent sort string.**   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;cntVersions&#x60;** | Number of deleted versions of this file | | **&#x60;firstDeletedAt&#x60;** | First deleted version | | **&#x60;lastDeletedAt&#x60;** | Last deleted version | | **&#x60;parentPath&#x60;** | Parent path of deleted node |
   * @param nodeId Parent ID (can only be a room ID) (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return DeletedNodeSummaryList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeSummaryList getFsDeletedNodesSummary(Long nodeId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getFsDeletedNodesSummaryWithHttpInfo(nodeId, xSdsAuthToken, xSdsDateFormat, filter, limit, offset, sort).getData();
      }

  /**
   * Get list of deleted nodes
   * ### Functional Description:   Retrieve a list of deleted nodes in a recycle bin.  ### Precondition: User can access parent room and has _\&quot;read recycle bin\&quot;_ permissions.  ### Effects: None.  ### &amp;#9432; Further Information: Only room IDs are accepted as parent ID since only rooms may have a recycle bin.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file:folder|name:cn:searchString_1|parentPath:cn:searchString_2&#x60;   Get deleted nodes where type equals (&#x60;file&#x60; **OR** &#x60;folder&#x60;) **AND** deleted node name containing &#x60;searchString_1&#x60; **AND** deleted node parent path containing &#x60;searchString 2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value(s).&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:folder:file&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;name&#x60;** | Node name filter | &#x60;cn&#x60; | Node name contains value. | &#x60;search String&#x60; | | **&#x60;parentPath&#x60;** | Parent path filter | &#x60;cn&#x60; | Parent path contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   **Nodes are sorted by type first, then by sent sort string.**   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;cntVersions&#x60;** | Number of deleted versions of this file | | **&#x60;firstDeletedAt&#x60;** | First deleted version | | **&#x60;lastDeletedAt&#x60;** | Last deleted version | | **&#x60;parentPath&#x60;** | Parent path of deleted node |
   * @param nodeId Parent ID (can only be a room ID) (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;DeletedNodeSummaryList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DeletedNodeSummaryList> getFsDeletedNodesSummaryWithHttpInfo(Long nodeId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * ### Functional Description:   Get node (room, folder or file).  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node getFsNode(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return getFsNodeWithHttpInfo(nodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Get node
   * ### Functional Description:   Get node (room, folder or file).  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> getFsNodeWithHttpInfo(Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * Get list of nodes
   * ### Functional Description:   Provides a hierarchical list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: &#x60;EncryptionInfo&#x60; is **NOT** provided.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:room:folder|perm:eq:read&#x60;   Get nodes where type equals (&#x60;room&#x60; **OR** &#x60;folder&#x60;) **AND** user has &#x60;read&#x60; permissions.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;perm&#x60;** | Permission filter | &#x60;eq&#x60; | Permission equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;perm:eq:read:create:delete&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;manage&#x60;&lt;/li&gt;&lt;li&gt;&#x60;read&#x60;&lt;/li&gt;&lt;li&gt;&#x60;change&#x60;&lt;/li&gt;&lt;li&gt;&#x60;create&#x60;&lt;/li&gt;&lt;li&gt;&#x60;delete&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageDownloadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageUploadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canReadRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canRestoreRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canDeleteRecycleBin&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;childPerm&#x60;** | Same as **&#x60;perm&#x60;**, but less restrictive (applies to child nodes only).&lt;br&gt;Child nodes of the parent node which do not meet the filter condition&lt;br&gt;are **NOT** returned. | &#x60;eq&#x60; | cf. **&#x60;perm&#x60;** | cf. **&#x60;perm&#x60;** | | **&#x60;name&#x60;** | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | **&#x60;encrypted&#x60;** | Node encryption status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   **Nodes are sorted by type first, then by sent sort string.**   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;cntChildren&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of direct children (**NOT** recursive; for rooms and folders only) |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel (**DEPRECATED**: will be removed) * &#x60;0&#x60; - top level nodes only * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param roomManager Show all rooms for management perspective. Only possible for _Rooms Managers_. For all other users, it will be ignored. (optional)
   * @param sort Sort string (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList getFsNodes(String xSdsAuthToken, String xSdsDateFormat, Integer depthLevel, String filter, Integer limit, Integer offset, Long parentId, Boolean roomManager, String sort) throws ApiException {
    return getFsNodesWithHttpInfo(xSdsAuthToken, xSdsDateFormat, depthLevel, filter, limit, offset, parentId, roomManager, sort).getData();
      }

  /**
   * Get list of nodes
   * ### Functional Description:   Provides a hierarchical list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: &#x60;EncryptionInfo&#x60; is **NOT** provided.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:room:folder|perm:eq:read&#x60;   Get nodes where type equals (&#x60;room&#x60; **OR** &#x60;folder&#x60;) **AND** user has &#x60;read&#x60; permissions.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;perm&#x60;** | Permission filter | &#x60;eq&#x60; | Permission equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;perm:eq:read:create:delete&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;manage&#x60;&lt;/li&gt;&lt;li&gt;&#x60;read&#x60;&lt;/li&gt;&lt;li&gt;&#x60;change&#x60;&lt;/li&gt;&lt;li&gt;&#x60;create&#x60;&lt;/li&gt;&lt;li&gt;&#x60;delete&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageDownloadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;manageUploadShare&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canReadRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canRestoreRecycleBin&#x60;&lt;/li&gt;&lt;li&gt;&#x60;canDeleteRecycleBin&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;childPerm&#x60;** | Same as **&#x60;perm&#x60;**, but less restrictive (applies to child nodes only).&lt;br&gt;Child nodes of the parent node which do not meet the filter condition&lt;br&gt;are **NOT** returned. | &#x60;eq&#x60; | cf. **&#x60;perm&#x60;** | cf. **&#x60;perm&#x60;** | | **&#x60;name&#x60;** | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | **&#x60;encrypted&#x60;** | Node encryption status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   **Nodes are sorted by type first, then by sent sort string.**   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;cntChildren&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of direct children (**NOT** recursive; for rooms and folders only) |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel (**DEPRECATED**: will be removed) * &#x60;0&#x60; - top level nodes only * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param roomManager Show all rooms for management perspective. Only possible for _Rooms Managers_. For all other users, it will be ignored. (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;NodeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NodeList> getFsNodesWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, Integer depthLevel, String filter, Integer limit, Integer offset, Long parentId, Boolean roomManager, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "depth_level", depthLevel));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "parent_id", parentId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_manager", roomManager));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Get list of webhooks that are assigned or can be assigned to this room 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the room scope with their assignment status.  ### Precondition: User needs to be a room administrator.  ### Effects: List of webhooks is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isAssigned:eq:true&#x60;   Get a list of assigned webhooks to the room.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isAssigned&#x60;** | Assigned/unassigned webhooks filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | 
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoomWebhookList
   * @throws ApiException if fails to make API call
   */
  public RoomWebhookList getListOfWebhooksForRoom(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
    return getListOfWebhooksForRoomWithHttpInfo(roomId, xSdsAuthToken, xSdsDateFormat, filter, limit, offset).getData();
      }

  /**
   * Get list of webhooks that are assigned or can be assigned to this room 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the room scope with their assignment status.  ### Precondition: User needs to be a room administrator.  ### Effects: List of webhooks is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isAssigned:eq:true&#x60;   Get a list of assigned webhooks to the room.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isAssigned&#x60;** | Assigned/unassigned webhooks filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | 
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoomWebhookList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoomWebhookList> getListOfWebhooksForRoomWithHttpInfo(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getListOfWebhooksForRoom");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/{room_id}/webhooks"
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RoomWebhookList> localVarReturnType = new GenericType<RoomWebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of node comments
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Get comments for a specific node.  ### Precondition: User has _\&quot;read\&quot;_ permissions on the node.  ### Effects: List with comments (sorted by &#x60;createdAt&#x60; timestamp) is returned.  ### &amp;#9432; Further Information: An empty list is returned if no comments were found.   Output is limited to **500** entries. For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param hideDeleted Hide deleted comments (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return CommentList
   * @throws ApiException if fails to make API call
   */
  public CommentList getNodeComments(Long nodeId, String xSdsAuthToken, String xSdsDateFormat, Boolean hideDeleted, Integer limit, Integer offset) throws ApiException {
    return getNodeCommentsWithHttpInfo(nodeId, xSdsAuthToken, xSdsDateFormat, hideDeleted, limit, offset).getData();
      }

  /**
   * Get list of node comments
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description: Get comments for a specific node.  ### Precondition: User has _\&quot;read\&quot;_ permissions on the node.  ### Effects: List with comments (sorted by &#x60;createdAt&#x60; timestamp) is returned.  ### &amp;#9432; Further Information: An empty list is returned if no comments were found.   Output is limited to **500** entries. For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param hideDeleted Hide deleted comments (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;CommentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CommentList> getNodeCommentsWithHttpInfo(Long nodeId, String xSdsAuthToken, String xSdsDateFormat, Boolean hideDeleted, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getNodeComments");
    }
    
    // create path and map variables
    String localVarPath = "/v4/nodes/{node_id}/comments"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "hide_deleted", hideDeleted));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<CommentList> localVarReturnType = new GenericType<CommentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of parent nodes
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description:   Requests a list of node ancestors, sorted from root node to the node&#39;s direct parent node.  ### Precondition: User is allowed to browse through the node tree until the requested node.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NodeParentList
   * @throws ApiException if fails to make API call
   */
  public NodeParentList getNodeParents(Long nodeId, String xSdsAuthToken) throws ApiException {
    return getNodeParentsWithHttpInfo(nodeId, xSdsAuthToken).getData();
      }

  /**
   * Get list of parent nodes
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description:   Requests a list of node ancestors, sorted from root node to the node&#39;s direct parent node.  ### Precondition: User is allowed to browse through the node tree until the requested node.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NodeParentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NodeParentList> getNodeParentsWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getNodeParents");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NodeParentList> localVarReturnType = new GenericType<NodeParentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download URL for ZIP download
   * ### Functional Description:   Create a download URL to retrieve several files in one ZIP archive.  ### Precondition: User has _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download URL is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; resources.   ZIP download is only available for files and folders.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse getNodesAsZip(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    return getNodesAsZipWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Generate download URL for ZIP download
   * ### Functional Description:   Create a download URL to retrieve several files in one ZIP archive.  ### Precondition: User has _\&quot;read\&quot;_ permissions in parent room.  ### Effects: Download URL is generated and returned.  ### &amp;#9432; Further Information: The token is necessary to access &#x60;downloads&#x60; resources.   ZIP download is only available for files and folders.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;DownloadTokenGenerateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DownloadTokenGenerateResponse> getNodesAsZipWithHttpInfo(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * Download files / folders as ZIP archive
   * ### Functional Description:   Download multiple files in a ZIP archive.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getNodesAsZipDownload(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
    return getNodesAsZipDownloadWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Download files / folders as ZIP archive
   * ### Functional Description:   Download multiple files in a ZIP archive.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getNodesAsZipDownloadWithHttpInfo(ZipDownloadRequest body, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user-room assignments per group
   * ### Functional Description:   Requests a list of user-room assignments by groups that have **NOT** been approved yet   These can have the state: * **WAITING**   * **DENIED**   * **ACCEPTED**    **ACCEPTED** assignments are already removed from the list.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: Room administrators **SHOULD** regularly request pending assingments to provide access to rooms for other users.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;state:eq:WAITING&#x60;   Filter assignments by state &#x60;WAITING&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;roomId&#x60;** | Room ID filter | &#x60;eq&#x60; | Room ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;state&#x60;** | Assignment state | &#x60;eq&#x60; | Assignment state equals value. | &#x60;WAITING or DENIED&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;userId:desc&#x60;   Sort by &#x60;userId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;userId&#x60;** | User ID | | **&#x60;groupId&#x60;** | Group ID | | **&#x60;roomId&#x60;** | Room ID | | **&#x60;state&#x60;** | State |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return PendingAssignmentList
   * @throws ApiException if fails to make API call
   */
  public PendingAssignmentList getPendingAssignments(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getPendingAssignmentsWithHttpInfo(xSdsAuthToken, filter, limit, offset, sort).getData();
      }

  /**
   * Get user-room assignments per group
   * ### Functional Description:   Requests a list of user-room assignments by groups that have **NOT** been approved yet   These can have the state: * **WAITING**   * **DENIED**   * **ACCEPTED**    **ACCEPTED** assignments are already removed from the list.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: Room administrators **SHOULD** regularly request pending assingments to provide access to rooms for other users.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;state:eq:WAITING&#x60;   Filter assignments by state &#x60;WAITING&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;roomId&#x60;** | Room ID filter | &#x60;eq&#x60; | Room ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;state&#x60;** | Assignment state | &#x60;eq&#x60; | Assignment state equals value. | &#x60;WAITING or DENIED&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;userId:desc&#x60;   Sort by &#x60;userId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;userId&#x60;** | User ID | | **&#x60;groupId&#x60;** | Group ID | | **&#x60;roomId&#x60;** | Room ID | | **&#x60;state&#x60;** | State |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;PendingAssignmentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PendingAssignmentList> getPendingAssignmentsWithHttpInfo(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes/rooms/pending";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<PendingAssignmentList> localVarReturnType = new GenericType<PendingAssignmentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get events of a room
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output is limited to **500** entries. For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList getRoomActivitiesLog(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, Long userId) throws ApiException {
    return getRoomActivitiesLogWithHttpInfo(roomId, xSdsAuthToken, xSdsDateFormat, dateEnd, dateStart, limit, offset, sort, status, type, userId).getData();
      }

  /**
   * Get events of a room
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output is limited to **500** entries. For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @return ApiResponse&lt;LogEventList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<LogEventList> getRoomActivitiesLogWithHttpInfo(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, Long userId) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json", "text/csv"
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
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;nodes/rooms/{room_id}/events&#x60; API  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @return SyslogEventList
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public SyslogEventList getRoomActivitiesLog1(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, Long userId) throws ApiException {
    return getRoomActivitiesLog1WithHttpInfo(roomId, xSdsAuthToken, xSdsDateFormat, dateEnd, dateStart, limit, offset, sort, status, type, userId).getData();
      }

  /**
   * Get events of a room
   * ## &amp;#9888; Deprecated since version 4.3.0  ### Use &#x60;nodes/rooms/{room_id}/events&#x60; API  ### Functional Description: Retrieve syslog (audit log) events related to a room.  ### Precondition: Requires _\&quot;read\&quot;_ permissions on that room.  ### Effects: None.  ### &amp;#9432; Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @return ApiResponse&lt;SyslogEventList&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<SyslogEventList> getRoomActivitiesLog1WithHttpInfo(Long roomId, String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, Long userId) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json", "text/csv"
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
   * Get room granted group(s) or / and group(s) that can be granted
   * ### Functional Description:   Retrieve a list of groups that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** granted to this room **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the groups that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for groups in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the groups that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter groups with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoomGroupList
   * @throws ApiException if fails to make API call
   */
  public RoomGroupList getRoomGroups(Long roomId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    return getRoomGroupsWithHttpInfo(roomId, xSdsAuthToken, filter, limit, offset).getData();
      }

  /**
   * Get room granted group(s) or / and group(s) that can be granted
   * ### Functional Description:   Retrieve a list of groups that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** granted to this room **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the groups that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for groups in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the groups that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter groups with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoomGroupList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoomGroupList> getRoomGroupsWithHttpInfo(Long roomId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Get list of all assigned S3 tags to the room
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve a list of S3 tags assigned to a room.  ### Precondition: User needs to be a room administrator.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3TagList
   * @throws ApiException if fails to make API call
   */
  public S3TagList getRoomS3Tags(Long roomId, String xSdsAuthToken) throws ApiException {
    return getRoomS3TagsWithHttpInfo(roomId, xSdsAuthToken).getData();
      }

  /**
   * Get list of all assigned S3 tags to the room
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve a list of S3 tags assigned to a room.  ### Precondition: User needs to be a room administrator.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3TagList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3TagList> getRoomS3TagsWithHttpInfo(Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomS3Tags");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room granted user(s) or / and user(s) that can be granted
   * ### Functional Description:   Retrieve a list of users that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;permissionsManage:eq:true|user:cn:searchString&#x60;   Get all users that have &#x60;manage&#x60; permissions to this room **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the users that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for users in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoomUserList
   * @throws ApiException if fails to make API call
   */
  public RoomUserList getRoomUsers(Long roomId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    return getRoomUsersWithHttpInfo(roomId, xSdsAuthToken, filter, limit, offset).getData();
      }

  /**
   * Get room granted user(s) or / and user(s) that can be granted
   * ### Functional Description:   Retrieve a list of users that are and / or can be granted to the room.  ### Precondition: Any permissions on target room.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;permissionsManage:eq:true|user:cn:searchString&#x60;   Get all users that have &#x60;manage&#x60; permissions to this room **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;isGranted&#x60;** | Filter the users that have (no) access to this room.&lt;br&gt;**This filter is only available for room administrators.**&lt;br&gt;**Other users can only look for users in their rooms, so this filter is &#x60;true&#x60; and **CANNOT** be overridden.** | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoomUserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoomUserList> getRoomUsersWithHttpInfo(Long roomId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Request status of S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Request status of a S3 file upload.  ### Precondition: * Valid upload ID * An upload channel has been created * User has to be the creator of the upload channel  ### Effects: None.  ### &amp;#9432; Further Information:  ### Possible errors:  | Http Status | Error Code | Description | | :--- | :--- | :--- | | **&#x60;400 Bad Request&#x60;** | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | **&#x60;400 Bad Request&#x60;** | &#x60;-80001&#x60; | Invalid positive number | | **&#x60;400 Bad Request&#x60;** | &#x60;-80002&#x60; | Invalid number | | **&#x60;400 Bad Request&#x60;** | &#x60;-40001&#x60; | (Target) room is not encrypted | | **&#x60;400 Bad Request&#x60;** | &#x60;-40755&#x60; | Bad file name | | **&#x60;400 Bad Request&#x60;** | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | **&#x60;400 Bad Request&#x60;** | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | **&#x60;403 Forbidden&#x60;** |  | Access denied | | **&#x60;404 Not Found&#x60;** | &#x60;-20501&#x60; | Upload not found | | **&#x60;404 Not Found&#x60;** | &#x60;-40000&#x60; | Container not found | | **&#x60;404 Not Found&#x60;** | &#x60;-41000&#x60; | Node not found | | **&#x60;404 Not Found&#x60;** | &#x60;-70501&#x60; | User not found | | **&#x60;409 Conflict&#x60;** | &#x60;-40010&#x60; | Container cannot be overwritten | | **&#x60;409 Conflict&#x60;** |  | File cannot be overwritten | | **&#x60;500 Internal Server Error&#x60;** |  | System Error | | **&#x60;502 Bad Gateway&#x60;** |  | S3 Error | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-40200&#x60; | Exceeds the free node quota in room | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90200&#x60; | Exceeds the free customer quota | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90201&#x60; | Exceeds the free customer physical disk space | 
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3FileUploadStatus
   * @throws ApiException if fails to make API call
   */
  public S3FileUploadStatus getUploadStatus(String uploadId, String xSdsAuthToken) throws ApiException {
    return getUploadStatusWithHttpInfo(uploadId, xSdsAuthToken).getData();
      }

  /**
   * Request status of S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Request status of a S3 file upload.  ### Precondition: * Valid upload ID * An upload channel has been created * User has to be the creator of the upload channel  ### Effects: None.  ### &amp;#9432; Further Information:  ### Possible errors:  | Http Status | Error Code | Description | | :--- | :--- | :--- | | **&#x60;400 Bad Request&#x60;** | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | **&#x60;400 Bad Request&#x60;** | &#x60;-80001&#x60; | Invalid positive number | | **&#x60;400 Bad Request&#x60;** | &#x60;-80002&#x60; | Invalid number | | **&#x60;400 Bad Request&#x60;** | &#x60;-40001&#x60; | (Target) room is not encrypted | | **&#x60;400 Bad Request&#x60;** | &#x60;-40755&#x60; | Bad file name | | **&#x60;400 Bad Request&#x60;** | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | **&#x60;400 Bad Request&#x60;** | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | **&#x60;403 Forbidden&#x60;** |  | Access denied | | **&#x60;404 Not Found&#x60;** | &#x60;-20501&#x60; | Upload not found | | **&#x60;404 Not Found&#x60;** | &#x60;-40000&#x60; | Container not found | | **&#x60;404 Not Found&#x60;** | &#x60;-41000&#x60; | Node not found | | **&#x60;404 Not Found&#x60;** | &#x60;-70501&#x60; | User not found | | **&#x60;409 Conflict&#x60;** | &#x60;-40010&#x60; | Container cannot be overwritten | | **&#x60;409 Conflict&#x60;** |  | File cannot be overwritten | | **&#x60;500 Internal Server Error&#x60;** |  | System Error | | **&#x60;502 Bad Gateway&#x60;** |  | S3 Error | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-40200&#x60; | Exceeds the free node quota in room | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90200&#x60; | Exceeds the free customer quota | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90201&#x60; | Exceeds the free customer physical disk space | 
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3FileUploadStatus&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3FileUploadStatus> getUploadStatusWithHttpInfo(String uploadId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling getUploadStatus");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3FileUploadStatus> localVarReturnType = new GenericType<S3FileUploadStatus>() {};
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
    return getUserFileKeyWithHttpInfo(fileId, xSdsAuthToken).getData();
      }

  /**
   * Get user&#39;s file key
   * ### Functional Description:   Returns the file key for the current user (if available).  ### Precondition: User with _\&quot;read\&quot;_, _\&quot;create\&quot;_ or _\&quot;manage download share\&quot;_ permissions in parent room.  ### Effects: None.  ### &amp;#9432; Further Information: The symmetric file key is encrypted with the user&#39;s public key.   File keys are generated with the workflow _\&quot;Generate file keys\&quot;_ that starts at &#x60;GET /nodes/missingFileKeys&#x60;.
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;FileKey&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileKey> getUserFileKeyWithHttpInfo(Long fileId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * Assign or unassign webhooks to room 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Handle room webhook assignments.  ### Precondition: User needs to be a room administrator.  ### Effects: List of webhooks is returned.  ### &amp;#9432; Further Information: None.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return RoomWebhookList
   * @throws ApiException if fails to make API call
   */
  public RoomWebhookList handleRoomWebhookAssignments(UpdateRoomWebhookRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return handleRoomWebhookAssignmentsWithHttpInfo(body, roomId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Assign or unassign webhooks to room 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Handle room webhook assignments.  ### Precondition: User needs to be a room administrator.  ### Effects: List of webhooks is returned.  ### &amp;#9432; Further Information: None.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;RoomWebhookList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoomWebhookList> handleRoomWebhookAssignmentsWithHttpInfo(UpdateRoomWebhookRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RoomWebhookList> localVarReturnType = new GenericType<RoomWebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get files without user&#39;s file key
   * ### Functional Description:   Requests a list of missing file keys that may be generated by the current user.   Only returns users that owns one of the following permissions  * *&#x60;manage&#x60;*  * *&#x60;read&#x60;*  * *&#x60;manageDownloadShare&#x60;*  ### Precondition: User has a keypair.  ### Effects: None.  ### &amp;#9432; Further Information: Clients **SHOULD** regularly request missing file keys to provide access to files for other users.   The returned list is ordered by priority (emergency passwords / rescue keys are returned first).    ### Please note:  This API returns **1024** entries at maximum.   There might be more entries even if a total of 1024 is returned. 
   * @param xSdsAuthToken Authentication token (optional)
   * @param fileId File ID (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param roomId Room ID (optional)
   * @param userId User ID (optional)
   * @return MissingKeysResponse
   * @throws ApiException if fails to make API call
   */
  public MissingKeysResponse missingFileKeys(String xSdsAuthToken, Long fileId, Integer limit, Integer offset, Long roomId, Long userId) throws ApiException {
    return missingFileKeysWithHttpInfo(xSdsAuthToken, fileId, limit, offset, roomId, userId).getData();
      }

  /**
   * Get files without user&#39;s file key
   * ### Functional Description:   Requests a list of missing file keys that may be generated by the current user.   Only returns users that owns one of the following permissions  * *&#x60;manage&#x60;*  * *&#x60;read&#x60;*  * *&#x60;manageDownloadShare&#x60;*  ### Precondition: User has a keypair.  ### Effects: None.  ### &amp;#9432; Further Information: Clients **SHOULD** regularly request missing file keys to provide access to files for other users.   The returned list is ordered by priority (emergency passwords / rescue keys are returned first).    ### Please note:  This API returns **1024** entries at maximum.   There might be more entries even if a total of 1024 is returned. 
   * @param xSdsAuthToken Authentication token (optional)
   * @param fileId File ID (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param roomId Room ID (optional)
   * @param userId User ID (optional)
   * @return ApiResponse&lt;MissingKeysResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MissingKeysResponse> missingFileKeysWithHttpInfo(String xSdsAuthToken, Long fileId, Integer limit, Integer offset, Long roomId, Long userId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/nodes/missingFileKeys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "file_id", fileId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "room_id", roomId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Move node(s)
   * ### Functional Description:   Moves nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ and _\&quot;delete\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are moved to target parent.  ### &amp;#9432; Further Information: Nodes **MUST** be in same source parent.   &amp;#9888; **Rooms **CANNOT** be moved.** ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node moveNodes(MoveNodesRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return moveNodesWithHttpInfo(body, nodeId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Move node(s)
   * ### Functional Description:   Moves nodes (folder, file) to another parent.  ### Precondition: Authenticated user with _\&quot;read\&quot;_ and _\&quot;delete\&quot;_ permissions in the source parent and _\&quot;create\&quot;_ permissions in the target parent node.  ### Effects: Nodes are moved to target parent.  ### &amp;#9432; Further Information: Nodes **MUST** be in same source parent.   &amp;#9888; **Rooms **CANNOT** be moved.** ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param nodeId Target parent node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> moveNodesWithHttpInfo(MoveNodesRequest body, Long nodeId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Restore deleted nodes
   * ### Functional Description:   Restore a list of deleted nodes.  ### Precondition: User has _\&quot;create\&quot;_ permissions in parent room and _\&quot;restore recycle bin\&quot;_ permissions.  ### Effects: The selected files are moved from the recycle bin to the chosen productive container.  ### &amp;#9432; Further Information: If no parent ID is provided, the node is restored to its previous location.   The default resolution strategy is &#x60;autorename&#x60; that adds numbers to the file name until the conflict is solved.   If an existing file is overwritten, it is moved to the recycle bin instead of the restored one. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void restoreNodes(RestoreDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {

    restoreNodesWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Restore deleted nodes
   * ### Functional Description:   Restore a list of deleted nodes.  ### Precondition: User has _\&quot;create\&quot;_ permissions in parent room and _\&quot;restore recycle bin\&quot;_ permissions.  ### Effects: The selected files are moved from the recycle bin to the chosen productive container.  ### &amp;#9432; Further Information: If no parent ID is provided, the node is restored to its previous location.   The default resolution strategy is &#x60;autorename&#x60; that adds numbers to the file name until the conflict is solved.   If an existing file is overwritten, it is moved to the recycle bin instead of the restored one. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> restoreNodesWithHttpInfo(RestoreDeletedNodesRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get room emergency password (rescue key)
   * ### Functional Description:   Retrieve the room emergency password (rescue key).  ### Precondition: User has _\&quot;read\&quot;_ permissions in that room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer roomRescueKey(Long roomId, String xSdsAuthToken) throws ApiException {
    return roomRescueKeyWithHttpInfo(roomId, xSdsAuthToken).getData();
      }

  /**
   * Get room emergency password (rescue key)
   * ### Functional Description:   Retrieve the room emergency password (rescue key).  ### Precondition: User has _\&quot;read\&quot;_ permissions in that room.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;UserKeyPairContainer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserKeyPairContainer> roomRescueKeyWithHttpInfo(Long roomId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * Search nodes
   * ### Functional Description:   Provides a flat list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ nodes (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: None.  ### &amp;#9432; Further Information:   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  &#x60;EncryptionInfo&#x60; is **NOT** provided.   Wildcard character is the asterisk character: **&#x60;*&#x60;**  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file|createdAt:ge:2015-01-01&#x60;   Get nodes where type equals &#x60;file&#x60; **AND** file creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;fileType&#x60;** | File type filter (file extension) | &#x60;cn, eq&#x60; | File type contains / equals value. | &#x60;search String&#x60; | | **&#x60;classification&#x60;** | Classification filter | &#x60;eq&#x60; | Classification equals value. | &lt;ul&gt;&lt;li&gt;&#x60;1&#x60; - public&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - internal&lt;/li&gt;&lt;li&gt;&#x60;3&#x60; - confidential&lt;/li&gt;&lt;li&gt;&#x60;4&#x60; - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdBy&#x60;** | Creator login filter | &#x60;cn, eq&#x60; | Creator login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;createdById&#x60;** | (**&#x60;NEW&#x60;**) Creator ID filter | &#x60;eq&#x60; | Creator ID equals value. | &#x60;search String&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedBy&#x60;** | Last modifier login filter | &#x60;cn, eq&#x60; | Last modifier login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;updatedById&#x60;** | (**&#x60;NEW&#x60;**) Last modifier ID filter | &#x60;eq&#x60; | Modifier ID equals value. | &#x60;search String&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expireAt&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expireAt:ge:2016-12-31&#x60;&amp;#124;&#x60;expireAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;size&#x60;** | Node size filter | &#x60;ge, le&#x60; | Node size is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;size:ge:5&#x60;&amp;#124;&#x60;size:le:10&#x60; | &#x60;size in bytes&#x60; | | **&#x60;isFavorite&#x60;** | Favorite filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; | | **&#x60;parentPath&#x60;** | Parent path | &#x60;cn, eq&#x60; | Parent path contains / equals  value. | &#x60;search String&#x60; |   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | **&#x60;type&#x60;** | Node type (room, folder, file) | | **&#x60;parentPath&#x60;** | Parent path | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;cntChildren&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of direct children (**NOT** recursive; for rooms and folders only) | 
   * @param searchString Search string (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel * &#x60;0&#x60; - top level nodes only (default) * &#x60;-1&#x60; - full tree * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param sort Sort string (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList searchFsNodes(String searchString, String xSdsAuthToken, String xSdsDateFormat, Integer depthLevel, String filter, Integer limit, Integer offset, Long parentId, String sort) throws ApiException {
    return searchFsNodesWithHttpInfo(searchString, xSdsAuthToken, xSdsDateFormat, depthLevel, filter, limit, offset, parentId, sort).getData();
      }

  /**
   * Search nodes
   * ### Functional Description:   Provides a flat list of file system nodes (rooms, folders or files) of a given parent that are accessible by the current user.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ nodes (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: None.  ### &amp;#9432; Further Information:   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  &#x60;EncryptionInfo&#x60; is **NOT** provided.   Wildcard character is the asterisk character: **&#x60;*&#x60;**  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;type:eq:file|createdAt:ge:2015-01-01&#x60;   Get nodes where type equals &#x60;file&#x60; **AND** file creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;type&#x60;** | Node type filter | &#x60;eq&#x60; | Node type equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**).&lt;br&gt;e.g. &#x60;type:eq:room:folder&#x60; | &lt;ul&gt;&lt;li&gt;&#x60;room&#x60;&lt;/li&gt;&lt;li&gt;&#x60;folder&#x60;&lt;/li&gt;&lt;li&gt;&#x60;file&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;fileType&#x60;** | File type filter (file extension) | &#x60;cn, eq&#x60; | File type contains / equals value. | &#x60;search String&#x60; | | **&#x60;classification&#x60;** | Classification filter | &#x60;eq&#x60; | Classification equals value. | &lt;ul&gt;&lt;li&gt;&#x60;1&#x60; - public&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - internal&lt;/li&gt;&lt;li&gt;&#x60;3&#x60; - confidential&lt;/li&gt;&lt;li&gt;&#x60;4&#x60; - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;createdBy&#x60;** | Creator login filter | &#x60;cn, eq&#x60; | Creator login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;createdById&#x60;** | (**&#x60;NEW&#x60;**) Creator ID filter | &#x60;eq&#x60; | Creator ID equals value. | &#x60;search String&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedBy&#x60;** | Last modifier login filter | &#x60;cn, eq&#x60; | Last modifier login contains / equals value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;updatedById&#x60;** | (**&#x60;NEW&#x60;**) Last modifier ID filter | &#x60;eq&#x60; | Modifier ID equals value. | &#x60;search String&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expireAt&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expireAt:ge:2016-12-31&#x60;&amp;#124;&#x60;expireAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;size&#x60;** | Node size filter | &#x60;ge, le&#x60; | Node size is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;size:ge:5&#x60;&amp;#124;&#x60;size:le:10&#x60; | &#x60;size in bytes&#x60; | | **&#x60;isFavorite&#x60;** | Favorite filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;branchVersion&#x60;** | Node branch version filter | &#x60;ge, le&#x60; | Branch version is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;branchVersion:ge:1423280937404&#x60;&amp;#124;&#x60;branchVersion:le:1523280937404&#x60; | &#x60;version number&#x60; | | **&#x60;parentPath&#x60;** | Parent path | &#x60;cn, eq&#x60; | Parent path contains / equals  value. | &#x60;search String&#x60; |   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Node name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator first name, last name | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;updatedBy&#x60;** | Last modifier first name, last name | | **&#x60;fileType&#x60;** | File type (extension) | | **&#x60;classification&#x60;** | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;size&#x60;** | Node size | | **&#x60;cntDeletedVersions&#x60;** | Number of deleted versions of this file / folder (**NOT** recursive; for files and folders only) | | **&#x60;type&#x60;** | Node type (room, folder, file) | | **&#x60;parentPath&#x60;** | Parent path | | **&#x60;cntAdmins&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of admins (for rooms only)| | **&#x60;cntUsers&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of users (for rooms only) | | **&#x60;cntChildren&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Number of direct children (**NOT** recursive; for rooms and folders only) | 
   * @param searchString Search string (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param depthLevel * &#x60;0&#x60; - top level nodes only (default) * &#x60;-1&#x60; - full tree * &#x60;n&#x60; (any positive number) - include &#x60;n&#x60; levels starting from the current node (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID &#x60;0&#x60; or empty is the root node. (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;NodeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NodeList> searchFsNodesWithHttpInfo(String searchString, String xSdsAuthToken, String xSdsDateFormat, Integer depthLevel, String filter, Integer limit, Integer offset, Long parentId, String sort) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "depth_level", depthLevel));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "parent_id", parentId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "search_string", searchString));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Add or change room granted group(s)
   * ### Functional Description: Batch function.   All existing group permissions will be overwritten.  ### Precondition: User needs to be a room administrator. To add new members, the user needs the right NONMEMBERS_ADD, which is included in any role.  ### Effects: Group&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomGroupsBatch(RoomGroupsAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {

    setRoomGroupsBatchWithHttpInfo(body, roomId, xSdsAuthToken);
  }

  /**
   * Add or change room granted group(s)
   * ### Functional Description: Batch function.   All existing group permissions will be overwritten.  ### Precondition: User needs to be a room administrator. To add new members, the user needs the right NONMEMBERS_ADD, which is included in any role.  ### Effects: Group&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> setRoomGroupsBatchWithHttpInfo(RoomGroupsAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomGroupsBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomGroupsBatch");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set S3 tags for a room
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Set S3 tags to a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Provided S3 tags are assigned to a room.  ### &amp;#9432; Further Information: Every request overrides current S3 tags.   Mandatory S3 tag IDs **MUST** be sent.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3TagList
   * @throws ApiException if fails to make API call
   */
  public S3TagList setRoomS3Tags(S3TagIds body, Long roomId, String xSdsAuthToken) throws ApiException {
    return setRoomS3TagsWithHttpInfo(body, roomId, xSdsAuthToken).getData();
      }

  /**
   * Set S3 tags for a room
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Set S3 tags to a room.  ### Precondition: User needs to be a room administrator.  ### Effects: Provided S3 tags are assigned to a room.  ### &amp;#9432; Further Information: Every request overrides current S3 tags.   Mandatory S3 tag IDs **MUST** be sent.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3TagList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3TagList> setRoomS3TagsWithHttpInfo(S3TagIds body, Long roomId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or change room granted user(s)
   * ### Functional Description: Batch function.   All existing user permissions will be overwritten.  ### Precondition: User needs to be a room administrator. To add new members, the user needs the right NONMEMBERS_ADD, which is included in any role.  ### Effects: User&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setRoomUsersBatch(RoomUsersAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {

    setRoomUsersBatchWithHttpInfo(body, roomId, xSdsAuthToken);
  }

  /**
   * Add or change room granted user(s)
   * ### Functional Description: Batch function.   All existing user permissions will be overwritten.  ### Precondition: User needs to be a room administrator. To add new members, the user needs the right NONMEMBERS_ADD, which is included in any role.  ### Effects: User&#39;s permissions are changed.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> setRoomUsersBatchWithHttpInfo(RoomUsersAddBatchRequest body, Long roomId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomUsersBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomUsersBatch");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set file keys for a list of users and files
   * ### Functional Description:   Sets symmetric file keys for several users and files.  ### Precondition: User has file keys for the files.   FileKeys can only be set for users that own permission \&quot;manage\&quot; or \&quot;read\&quot; or \&quot;manageDownloadShare\&quot; in the container.  ### Effects: Stores new file keys for other users.  ### &amp;#9432; Further Information: Only users with copies of the file key (encrypted with their public keys) can access a certain file.   This endpoint is used for the distribution of file keys amongst an authorized user base.   User can set fileKey for himself.   The users who already have a fileKey are ignored and keep the distributed fileKey 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setUserFileKeys(UserFileKeySetBatchRequest body, String xSdsAuthToken) throws ApiException {

    setUserFileKeysWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Set file keys for a list of users and files
   * ### Functional Description:   Sets symmetric file keys for several users and files.  ### Precondition: User has file keys for the files.   FileKeys can only be set for users that own permission \&quot;manage\&quot; or \&quot;read\&quot; or \&quot;manageDownloadShare\&quot; in the container.  ### Effects: Stores new file keys for other users.  ### &amp;#9432; Further Information: Only users with copies of the file key (encrypted with their public keys) can access a certain file.   This endpoint is used for the distribution of file keys amongst an authorized user base.   User can set fileKey for himself.   The users who already have a fileKey are ignored and keep the distributed fileKey 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> setUserFileKeysWithHttpInfo(UserFileKeySetBatchRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unmark a node (room, folder or file) as favorite
   * ### Functional Description: Unmarks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: A node gets unmarked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void unmarkFavorite(Long nodeId, String xSdsAuthToken) throws ApiException {

    unmarkFavoriteWithHttpInfo(nodeId, xSdsAuthToken);
  }

  /**
   * Unmark a node (room, folder or file) as favorite
   * ### Functional Description: Unmarks a node (room, folder or file) as favorite.  ### Precondition: Authenticated user is allowed to _\&quot;see\&quot;_ the node (i.e. &#x60;isBrowsable &#x3D; true&#x60;).  ### Effects: A node gets unmarked as favorite.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> unmarkFavoriteWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Updates a file’s metadata
   * ### Functional Description:   Updates file’s metadata.  ### Precondition: User has _\&quot;change\&quot;_ permissions in parent room.  ### Effects: File&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFile(UpdateFileRequest body, Long fileId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateFileWithHttpInfo(body, fileId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Updates a file’s metadata
   * ### Functional Description:   Updates file’s metadata.  ### Precondition: User has _\&quot;change\&quot;_ permissions in parent room.  ### Effects: File&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param fileId File ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> updateFileWithHttpInfo(UpdateFileRequest body, Long fileId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Updates folder’s metadata
   * ### Functional Description:   Updates folder’s metadata.  ### Precondition: User has _\&quot;change\&quot;_ permissions in parent room.  ### Effects: Folder&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param folderId Folder ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFolder(UpdateFolderRequest body, Long folderId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateFolderWithHttpInfo(body, folderId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Updates folder’s metadata
   * ### Functional Description:   Updates folder’s metadata.  ### Precondition: User has _\&quot;change\&quot;_ permissions in parent room.  ### Effects: Folder&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param folderId Folder ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> updateFolderWithHttpInfo(UpdateFolderRequest body, Long folderId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Updates room’s metadata
   * ### Functional Description:   Updates room’s metadata.  ### Precondition: User is a room administrator at superordinated level.  ### Effects: Room&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateRoom(UpdateRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateRoomWithHttpInfo(body, roomId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Updates room’s metadata
   * ### Functional Description:   Updates room’s metadata.  ### Precondition: User is a room administrator at superordinated level.  ### Effects: Room&#39;s metadata is changed.  ### &amp;#9432; Further Information: Notes are limited to **255** characters.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
   * @param body body (required)
   * @param roomId Room ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> updateRoomWithHttpInfo(UpdateRoomRequest body, Long roomId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
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
   * Upload file
   * ## &amp;#9888; Deprecated since version 4.9.0  ### Use &#x60;uploads&#x60; API  ### Functional Description:   Uploads a file or parts of it in an active upload channel.  ### Precondition: An upload channel has been created.  ### Effects: A file or parts of it are uploaded to a temporary location.  ### &amp;#9432; Further Information: This endpoints supports chunked upload.   Please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for further information.  Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;     For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id}  HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param file File (required)
   * @param uploadId Upload channel ID (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ChunkUploadResponse uploadFile(File file, String uploadId, String contentRange, String xSdsAuthToken) throws ApiException {
    return uploadFileWithHttpInfo(file, uploadId, contentRange, xSdsAuthToken).getData();
      }

  /**
   * Upload file
   * ## &amp;#9888; Deprecated since version 4.9.0  ### Use &#x60;uploads&#x60; API  ### Functional Description:   Uploads a file or parts of it in an active upload channel.  ### Precondition: An upload channel has been created.  ### Effects: A file or parts of it are uploaded to a temporary location.  ### &amp;#9432; Further Information: This endpoints supports chunked upload.   Please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for further information.  Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;     For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/nodes/files/uploads/{upload_id}  HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param file File (required)
   * @param uploadId Upload channel ID (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ChunkUploadResponse&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<ChunkUploadResponse> uploadFileWithHttpInfo(File file, String uploadId, String contentRange, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling uploadFile");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadFile");
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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
