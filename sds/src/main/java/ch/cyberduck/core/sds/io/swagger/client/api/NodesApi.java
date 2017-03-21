package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.BulkErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ChunkUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ConfigRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CopyNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeSummaryList;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeVersionsList;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadTokenGenerateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import java.io.File;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import org.joda.time.LocalDate;
import ch.cyberduck.core.sds.io.swagger.client.model.MissingKeysResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.NotRestoredNodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentList;
import ch.cyberduck.core.sds.io.swagger.client.model.PendingAssignmentsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RestoreDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomGroupsDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersDeleteBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.SyslogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.io.swagger.client.model.ZipDownloadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-03-21T19:15:03.115+01:00")
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
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Marks a node (room, folder, file) as favorite.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs read permissions on that node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A node gets marked as favorite.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Node Id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node addFavorite(String xSdsAuthToken, Long nodeId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling addFavorite");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling addFavorite");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/favorite".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Cancel file upload
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Cancel an upload and destroy the Upload Channel.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; An Upload Channel has been created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; The Upload Channel is removed and all temporary uploaded data is purged.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; It is recommended to notify the API about cancelled uploads if possible.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param uploadId Upload channel Id (required)
   * @throws ApiException if fails to make API call
   */
  public void cancelFileUpload(String xSdsAuthToken, String uploadId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling cancelFileUpload");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling cancelFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/uploads/{upload_id}".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Handle user-room assignments per group that have not been accepted yet
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Handles a list of User - Room Assignments by Groups that have not been approved yet&lt;br/&gt;&lt;strong&gt;WAITING&lt;/strong&gt; or &lt;strong&gt;DENIED&lt;/strong&gt; Assignments can be &lt;strong&gt;ACCEPTED&lt;/strong&gt;.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User - Room Assignment is approved and the user gets access to the group.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Data Room Admins should regularly handle pending Assignments to provide access to rooms for other users.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body List of Pending Assignments (required)
   * @throws ApiException if fails to make API call
   */
  public void changePendingAssignments(String xSdsAuthToken, PendingAssignmentsRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling changePendingAssignments");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changePendingAssignments");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/pending".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Finishes an upload and closes the corresponding Upload Channel.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; An Upload Channel has been created and data has been transmitted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; The upload is finished and the temporary file is moved to the productive environment.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The provided file name might be changed in accordance with the resolution strategy:&lt;br/&gt;&#39;autorename&#39; changes the file name and adds a number to avoid conflicts.&lt;br/&gt;&#39;overwrite&#39; deletes any old file with the same file name.&lt;br/&gt;&#39;fail&#39; returns an error. In this case, another PUT request with a different file name may be sent.&lt;br/&gt;&lt;br/&gt;Please ensure that all chunks have been transferred correctly before finishing the upload.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param uploadId Upload channel Id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param body  (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node completeFileUpload(String xSdsAuthToken, String uploadId, String xSdsDateFormat, CompleteUploadRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling completeFileUpload");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/uploads/{upload_id}".replaceAll("\\{format\\}","json")
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Config room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Updates a room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Room&#39;s configuration is changed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node configRoom(String xSdsAuthToken, Long roomId, ConfigRoomRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling configRoom");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling configRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling configRoom");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/config".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Copy file system nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Copies nodes (folder,file) to another parent.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user with read permissions in the source parent and write permissions in the target parent node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Nodes are copied to target parent.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Nodes must be in same source parent. Rooms cannot be copied.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Target Parent Node ID (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node copyNodes(String xSdsAuthToken, Long nodeId, CopyNodesRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling copyNodes");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling copyNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling copyNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/copy_to".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download token
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Create a download token to retrieve a file without X-Sds-Auth-Token Header.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Download token is generated and returned.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The token is necessary to access /downloads ressources.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse createFileDownloadToken(String xSdsAuthToken, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createFileDownloadToken");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling createFileDownloadToken");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/downloads".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new file upload channel
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; This endpoint creates a new upload channel which is the first step in any file upload workflow.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has create permission in the parent container (room or folder).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A new upload channel for a file is created. Its ID and an upload token are returned.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The uploadID is used for uploads with X-Sds-Auth-Token header, the upload token can be used for uploads without authentication header.&lt;/p&gt;&lt;p&gt;Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Room/Folder/File name convention&lt;/h4&gt;&lt;h5&gt;Room/Folder/File names are limited to 150 characters.&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;Not allowed Room/Folder/File names&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;Not allowed characters in Room/Folder/File name&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;../&#39;, &#39;\\&#39;, &#39;&amp;lt;&#39;,&#39;&amp;gt;&#39;, &#39;:&#39;, &#39;\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @return CreateFileUploadResponse
   * @throws ApiException if fails to make API call
   */
  public CreateFileUploadResponse createFileUpload(String xSdsAuthToken, CreateFileUploadRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createFileUpload");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFileUpload");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/uploads".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<CreateFileUploadResponse> localVarReturnType = new GenericType<CreateFileUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new folder
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Creates a new folder.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has create permission in current Data Room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A new folder is created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Folders cannot be created on top level (without parent element).&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Room/Folder/File name convention&lt;/h4&gt;&lt;h5&gt;Room/Folder/File names are limited to 150 characters.&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;Not allowed Room/Folder/File names&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;Not allowed characters in Room/Folder/File name&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;../&#39;, &#39;\\&#39;, &#39;&amp;lt;&#39;,&#39;&amp;gt;&#39;, &#39;:&#39;, &#39;\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createFolder(String xSdsAuthToken, CreateFolderRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createFolder");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFolder");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/folders".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt; Creates a new Data Room at the provided parent node. Creation of top level rooms provided.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has create permissions in the parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A new room is created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt;&lt;br/&gt;Rooms may only have other rooms as parent.&lt;br/&gt;Rooms on top level do not have any parent.&lt;br/&gt;Rooms may have rooms as children on &lt;b&gt;n&lt;/b&gt; hierarchy levels.&lt;br/&gt;If permission inheritance is disabled, there must be at least one admin or admin group (with neither the group nor the user having an expiration date).&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param body  (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node createRoom(String xSdsAuthToken, String xSdsDateFormat, CreateRoomRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createRoom");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete nodes from Recycle Bin
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Permanently remove a list of nodes from the recycle bin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has \&quot;Delete Recycle Bin\&quot; permissions in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; All provided nodes are removed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The removal of deleted nodes from the recycle bin is irreversible.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteDeletedNodes(String xSdsAuthToken, DeleteDeletedNodesRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteDeletedNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteDeletedNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/deleted_nodes".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete file system node
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Delete node (room, folder, file).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user with delete permissions on supplied nodes.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Node gets deleted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Node id (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteNode(String xSdsAuthToken, Long nodeId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteNode");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling deleteNode");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete file system nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Delete nodes (room, folder, file).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user with delete permissions on supplied nodes.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Nodes are deleted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Nodes must be in same parent.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteNodes(String xSdsAuthToken, DeleteNodesRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke group permissions from room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Batch function. Revoke groups from room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Group&#39;s permissions are revoked.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomGroupsBatch(String xSdsAuthToken, Long roomId, RoomGroupsDeleteBatchRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteRoomGroupsBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomGroupsBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomGroupsBatch");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/groups".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Revoke user permissions from room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Batch function. Revoke users from room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User&#39;s permissions are revoked.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteRoomUsersBatch(String xSdsAuthToken, Long roomId, RoomUsersDeleteBatchRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteRoomUsersBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling deleteRoomUsersBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoomUsersBatch");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/users".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Empty Recycle Bin
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Empty a recycle bin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has \&quot;Delete Recycle Bin \&quot; permissions in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; All files in the recycle bin are permanently removed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Actually removes the previously deleted files from the system. This action is irreversible.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Parent node ID (required)
   * @throws ApiException if fails to make API call
   */
  public void emptyDeletedNodes(String xSdsAuthToken, Long nodeId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling emptyDeletedNodes");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling emptyDeletedNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/deleted_nodes".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Encrypt room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Activates the client-side encryption for a Data Room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Encryption of Data Room is activated.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Only empty rooms at the top level may be encrypted. This endpoint may also be used to disable encryption of an empty room.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node encryptRoom(String xSdsAuthToken, Long roomId, EncryptRoomRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling encryptRoom");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling encryptRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling encryptRoom");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/encrypt".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Data Room Rescue file key
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Returns the fileKey for the Data Room Rescue Key of a certain file (if available).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataRoomFileKey(String xSdsAuthToken, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getDataRoomFileKey");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getDataRoomFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/data_room_file_key".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get DataSpace Rescue file key
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Returns the fileKey for the Data Space Rescue Key of a certain file (if available).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getDataSpaceFileKey(String xSdsAuthToken, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getDataSpaceFileKey");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getDataSpaceFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/data_space_file_key".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download file
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Download a file.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Range requests are supported (please cf. &lt;a href&#x3D;&#39;https://tools.ietf.org/html/rfc7233&#39; target&#x3D;&#39;_blank&#39;&gt;RFC 7233&lt;/a&gt; for details).&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @param range Range (optional)
   * @param genericMimetype always return application/octet-stream instead of specific mimetype (optional)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object getFileData(String xSdsAuthToken, Long fileId, String range, Boolean genericMimetype) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFileData");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFileData");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/downloads".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (range != null)
      localVarHeaderParams.put("Range", apiClient.parameterToString(range));

    
    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get file headers
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Retrieve the header of a file transmission. Please cf. &lt;a href&#x3D;&#39;https://tools.ietf.org/html/rfc7233&#39; target&#x3D;&#39;_blank&#39;&gt;RFC 7233&lt;/a&gt; for details.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Equivalent of &lt;code&gt;HEAD /downloads/{token}&lt;/code&gt;, when a client can set &lt;em&gt;X-Sds-Auth-Token&lt;/em&gt; header.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @param genericMimetype always return application/octet-stream instead of specific mimetype (optional)
   * @throws ApiException if fails to make API call
   */
  public void getFileDataHead(String xSdsAuthToken, Long fileId, Boolean genericMimetype) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFileDataHead");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFileDataHead");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/downloads".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get deleted node
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Get the meta data of one deleted node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User can access parent room and has \&quot;Read Recycle Bin\&quot; permissions.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param deletedNodeId Deleted Node ID (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return DeletedNode
   * @throws ApiException if fails to make API call
   */
  public DeletedNode getFsDeletedNode(String xSdsAuthToken, Long deletedNodeId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFsDeletedNode");
    }
    
    // verify the required parameter 'deletedNodeId' is set
    if (deletedNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deletedNodeId' when calling getFsDeletedNode");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/deleted_nodes/{deleted_node_id}".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<DeletedNode> localVarReturnType = new GenericType<DeletedNode>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get deleted versions
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Retrieve all deleted versions of a node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User can access parent room and has \&quot;Read Recycle Bin\&quot; permissions.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The node is identified by three parameters: parent ID, name and type (file|folder).&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;expireAt&lt;/dt&gt;&lt;dd&gt;Expiration date&lt;/dd&gt;&lt;dt&gt;accessedAt&lt;/dt&gt;&lt;dd&gt;Last access date&lt;/dd&gt;&lt;dt&gt;size&lt;/dt&gt;&lt;dd&gt;Node size&lt;/dd&gt;&lt;dt&gt;classification&lt;/dt&gt;&lt;dd&gt;Classification ID. File only&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Node created by user&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Modification date &lt;/dd&gt;&lt;dt&gt;updatedBy&lt;/dt&gt;&lt;dd&gt;Node modified by user&lt;/dd&gt;&lt;dt&gt;deletedAt&lt;/dt&gt;&lt;dd&gt;Deleted date&lt;/dd&gt;&lt;dt&gt;deletedBy&lt;/dt&gt;&lt;dd&gt;Node deleted by user&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;expireAt:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by expireAt descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Parent Node ID (required)
   * @param type Node type (required)
   * @param name Node name (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param sort Sorting string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @return DeletedNodeVersionsList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeVersionsList getFsDeletedNodeVersions(String xSdsAuthToken, Long nodeId, String type, String name, String xSdsDateFormat, String sort, Integer offset, Integer limit) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFsDeletedNodeVersions");
    }
    
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
    String localVarPath = "/nodes/{node_id}/deleted_nodes/versions".replaceAll("\\{format\\}","json")
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<DeletedNodeVersionsList> localVarReturnType = new GenericType<DeletedNodeVersionsList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get deleted nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Retrieve a list of deleted nodes in a recycle bin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User can access parent room and has \&quot;Read Recycle Bin\&quot; permissions.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Only room IDs are accepted as node ID since only rooms have recycle bins.&lt;/p&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;[:&amp;lt;VALUE&amp;gt;...]&lt;/dfn&gt;&lt;/p&gt;&lt;h5&gt;Fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;type&lt;/dt&gt;&lt;dd&gt;Node type filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values allowed)&lt;br/&gt;VALUE: &lt;code&gt;folder|file&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Node name filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Node name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;parentPath&lt;/dt&gt;&lt;dd&gt;Parent path filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Parent path contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;type:eq:file:folder|name:cn:searchString|parentPath:cn:searchString1&lt;/samp&gt;&lt;br/&gt;&amp;rarr; Get deleted nodes where type equals &#39;file&#39; or &#39;folder&#39; AND deleted node name contains &#39;searchString&#39; AND deleted node parent path contains &#39;searchString1&#39;.&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Node name&lt;/dd&gt;&lt;dt&gt;cntVersions&lt;/dt&gt;&lt;dd&gt;Number of deleted versions of this file&lt;/dd&gt;&lt;dt&gt;firstDeletedAt&lt;/dt&gt;&lt;dd&gt;First deleted version&lt;/dd&gt;&lt;dt&gt;lastDeletedAt&lt;/dt&gt;&lt;dd&gt;Last deleted version&lt;/dd&gt;&lt;dt&gt;parentPath&lt;/dt&gt;&lt;dd&gt;Parent path of deleted node&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by name descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Auth parent node ID (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param filter Filter string (optional)
   * @param sort Sorting string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @return DeletedNodeSummaryList
   * @throws ApiException if fails to make API call
   */
  public DeletedNodeSummaryList getFsDeletedNodesSummary(String xSdsAuthToken, Long nodeId, String xSdsDateFormat, String filter, String sort, Integer offset, Integer limit) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFsDeletedNodesSummary");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsDeletedNodesSummary");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/deleted_nodes".replaceAll("\\{format\\}","json")
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<DeletedNodeSummaryList> localVarReturnType = new GenericType<DeletedNodeSummaryList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get node
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Get all file system node (Room, Folder, File)&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has read permissions in auth parent room/subroom.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Node id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node getFsNode(String xSdsAuthToken, Long nodeId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFsNode");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getFsNode");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get file system nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Provides a hierarchical list of file system nodes (Rooms, Folders, Files) of a given parent that are accessible by the current user.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; EncryptionInfo is not provided.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;[:&amp;lt;VALUE&amp;gt;...]&lt;/dfn&gt;&lt;/p&gt;&lt;h5&gt;Fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;type&lt;/dt&gt;&lt;dd&gt;Node type filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values allowed)&lt;br/&gt;VALUE: &lt;code&gt;room|folder|file&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;perm&lt;/dt&gt;&lt;dd&gt;Permissions filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values allowed)&lt;br/&gt;VALUE: &lt;code&gt;manage|read|change|create|delete|manageDownloadShare|manageUploadShare|&lt;br/&gt;canReadRecycleBin|canRestoreRecycleBin|canDeleteRecycleBin&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;childPerm&lt;/dt&gt;&lt;dd&gt;The same as perm, but less restrictive (applied to child nodes only)&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values allowed)&lt;br/&gt;VALUE: &lt;code&gt;manage|read|change|create|delete|manageDownloadShare|manageUploadShare|&lt;br/&gt;canReadRecycleBin|canRestoreRecycleBin|canDeleteRecycleBin&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Node name filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;encrypted&lt;/dt&gt;&lt;dd&gt;Node encryption status filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (Node is encrypted, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;true|false&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;branchVersion&lt;/dt&gt;&lt;dd&gt;Node branch version&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Version Number&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;type:eq:room:folder|perm:eq:read&lt;/samp&gt;&lt;br/&gt;&amp;rarr; Get nodes where type equals &#39;room&#39; or &#39;folder&#39; AND user has read permissions.&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Node name&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator user name&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;updatedBy&lt;/dt&gt;&lt;dd&gt;Modifier user name&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Modification date&lt;/dd&gt;&lt;dt&gt;fileType&lt;/dt&gt;&lt;dd&gt;File type (extension)&lt;/dd&gt;&lt;dt&gt;classification&lt;/dt&gt;&lt;dd&gt;Classification&lt;/dd&gt;&lt;dt&gt;size&lt;/dt&gt;&lt;dd&gt;Node size&lt;/dd&gt;&lt;dt&gt;cntAdmins&lt;/dt&gt;&lt;dd&gt;For rooms only: Number of admins&lt;/dd&gt;&lt;dt&gt;cntUsers&lt;/dt&gt;&lt;dd&gt;For rooms only: Number of users&lt;/dd&gt;&lt;dt&gt;cntChildren&lt;/dt&gt;&lt;dd&gt;For rooms/subrooms/folder only: Number of direct children (not recursive)&lt;/dd&gt;&lt;dt&gt;cntDeletedVersions&lt;/dt&gt;&lt;dd&gt;For files/folder only: Number of deleted versions of this file/folder (not recursive)&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by name descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param depthLevel &lt;ul&gt;&lt;li&gt;0 - top-level nodes only;&lt;/li&gt;&lt;li&gt;-1 - full tree;&lt;/li&gt;&lt;li&gt;any positive N - include N levels starting from the current node&lt;/li&gt;&lt;/ul&gt; (optional, default to 0)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID 0 or empty is the root node. (optional)
   * @param roomManager Show all data rooms for management perspective. Only posible for Room Managers. For all other users, it will be ignored. (optional)
   * @param filter Filter string (optional)
   * @param sort Sorting string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList getFsNodes(String xSdsAuthToken, String xSdsDateFormat, Integer depthLevel, Long parentId, Boolean roomManager, String filter, String sort, Integer offset, Integer limit) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getFsNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes".replaceAll("\\{format\\}","json");

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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download token for zip download
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Create a download token to retrieve several files in one ZIP archive.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has read permission in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Download token is generated and returned.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The token is necessary to access /downloads resources.&lt;br/&gt;ZIP download is only available for files and folders.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @return DownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public DownloadTokenGenerateResponse getNodesAsZip(String xSdsAuthToken, ZipDownloadRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getNodesAsZip");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling getNodesAsZip");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/zip".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<DownloadTokenGenerateResponse> localVarReturnType = new GenericType<DownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download files/folders as ZIP
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Download multiple files in a ZIP archive.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object getNodesAsZipDownload(String xSdsAuthToken, ZipDownloadRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getNodesAsZipDownload");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling getNodesAsZipDownload");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/zip/download".replaceAll("\\{format\\}","json");

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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user-room assignments per group that have not been accepted yet
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Requests a list of User - Room Assignments by Groups that have not been approved yet&lt;br/&gt;These can have the state &lt;strong&gt;WAITING&lt;/strong&gt; or &lt;strong&gt;DENIED&lt;/strong&gt;. &lt;strong&gt;ACCEPTED&lt;/strong&gt; Assignments are already removed from the list.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Data Room Admins should regularly request pending Assingments to provide access to rooms for other users.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;[:&amp;lt;VALUE&amp;gt;...]&lt;/dfn&gt;&lt;/p&gt;&lt;h5&gt;Fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;User ID filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;groupId&lt;/dt&gt;&lt;dd&gt;Group ID filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;roomId&lt;/dt&gt;&lt;dd&gt;Room ID filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;state&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Assignment state&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;WAITING|DENIED&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;User ID&lt;/dd&gt;&lt;dt&gt;groupId&lt;/dt&gt;&lt;dd&gt;Group ID&lt;/dd&gt;&lt;dt&gt;roomId&lt;/dt&gt;&lt;dd&gt;Room ID&lt;/dd&gt;&lt;dt&gt;state&lt;/dt&gt;&lt;dd&gt;State&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;userId:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by User ID descending&lt;/p&gt;&lt;/div&gt; 
   * @param xSdsAuthToken Authentication token (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return PendingAssignmentList
   * @throws ApiException if fails to make API call
   */
  public PendingAssignmentList getPendingAssignments(String xSdsAuthToken, Integer offset, Integer limit, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getPendingAssignments");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/pending".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<PendingAssignmentList> localVarReturnType = new GenericType<PendingAssignmentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get events of a room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve syslog (&#x3D; audit log) events related to a Room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Requires read permission on that room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Output may be limited to a certain number of entries. Please use filter criteria and paging.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param dateStart Start date (2015-12-31T23:59:00) (optional)
   * @param dateEnd End date (2015-12-31T23:59:00) (optional)
   * @param type Operation ID type (see table LOG_OPERATIONS) (optional)
   * @param userId User ID (optional)
   * @param status Operation status: 0 &#x3D; SUCCESS, 2 &#x3D; ERROR (optional)
   * @return SyslogEventList
   * @throws ApiException if fails to make API call
   */
  public SyslogEventList getRoomActivitiesLog(String xSdsAuthToken, Long roomId, String xSdsDateFormat, Integer offset, Integer limit, LocalDate dateStart, LocalDate dateEnd, List<Integer> type, List<Integer> userId, List<Integer> status) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getRoomActivitiesLog");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomActivitiesLog");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/activities_log".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "room_id" + "\\}", apiClient.escapeString(roomId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "status", status));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<SyslogEventList> localVarReturnType = new GenericType<SyslogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room granted groups or/and groups that can be granted
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Retrieve a list of groups that are and/or can be granted to the room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Right \&quot;Groups Read\&quot; required.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Group name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;isGranted&lt;/dt&gt;&lt;dd&gt;Filter the groups that have access/no access to this room&lt;br/&gt;&lt;b&gt;Attention! This filter is only available for data room administrators.&lt;br/&gt; Other users can only look for users in their rooms, so this filter is TRUE and cannot be overridden.&lt;/b&gt;&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false|any]. Default value is &lt;code&gt;true&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;permissionsManage&lt;/dt&gt;&lt;dd&gt;Filter the groups that have/don&#39;t have manage right in this room&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false].&lt;/dd&gt;&lt;dt&gt;effectivePerm&lt;/dt&gt;&lt;dd&gt;Filter groups with permissions or effective permissions&lt;br/&gt;FALSE: Direct permissions TRUE: Effective permissions.&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false]. Default value is &lt;code&gt;true&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;groupId&lt;/dt&gt;&lt;dd&gt;Filter the groups by id&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: Positive Integer&lt;/dd&gt;&lt;/dl&gt;Example: &lt;samp&gt;isGranted:eq:false|name:cn:searchstring&lt;/samp&gt;&lt;br/&gt;- get all groups that have no rights to this room of and whose name is like searchstring&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Group filter (optional)
   * @return RoomGroupList
   * @throws ApiException if fails to make API call
   */
  public RoomGroupList getRoomGroups(String xSdsAuthToken, Long roomId, Integer offset, Integer limit, String filter) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getRoomGroups");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomGroups");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/groups".replaceAll("\\{format\\}","json")
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RoomGroupList> localVarReturnType = new GenericType<RoomGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get room granted users or/and users that can be granted
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Retrieve a list of groups that are and/or can be granted to the room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Right \&quot;Users Read\&quot; required.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;displayName&lt;/dt&gt;&lt;dd&gt;User display name (firstName, lastName, login)&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;isGranted&lt;/dt&gt;&lt;dd&gt;Filter the users that have access/no access to this room.&lt;br/&gt;&lt;b&gt;Attention! This filter is only available for data room administrators.&lt;br/&gt; Other users can only look for users in their rooms, so this filter is TRUE and cannot be overridden.&lt;/b&gt;&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false|any]. Default value is &lt;code&gt;true&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;permissionsManage&lt;/dt&gt;&lt;dd&gt;Filter the users that have/don&#39;t have manage right in this room&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false].&lt;/dd&gt;&lt;dt&gt;effectivePerm&lt;/dt&gt;&lt;dd&gt;Filter users with permissions, effective permissions or direct effective permissions&lt;br/&gt;FALSE: Direct permissions TRUE: Effective permissions. ANY: Direct effective.&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false|any]. Default value is &lt;code&gt;false&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;Filter the users by id&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: Positive Integer&lt;/dd&gt;&lt;/dl&gt;Example: &lt;samp&gt;isGranted:eq:true|displayName:cn:searchstring|permissions_manage:eq:true&lt;/samp&gt;&lt;br/&gt;- get all users that have manage rights to this room of and whose name is like searchstring&lt;br/&gt;&lt;br/&gt;&lt;b&gt;The filters are connected by AND&lt;/b&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter User filter (optional)
   * @return RoomUserList
   * @throws ApiException if fails to make API call
   */
  public RoomUserList getRoomUsers(String xSdsAuthToken, Long roomId, Integer offset, Integer limit, String filter) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getRoomUsers");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling getRoomUsers");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/users".replaceAll("\\{format\\}","json")
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RoomUserList> localVarReturnType = new GenericType<RoomUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user&#39;s file key
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Returns the FileKey for the current user (if available).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User with read, create or manage download share permissions in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; The symmetric FileKey is encrypted with the user&#39;s PublicKey. FileKeys are generated with the Workflow \&quot;Generate FileKeys\&quot; that starts at &lt;b&gt;GET /nodes/missingFileKeys&lt;/b&gt;.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File Id (required)
   * @return FileKey
   * @throws ApiException if fails to make API call
   */
  public FileKey getUserFileKey(String xSdsAuthToken, Long fileId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getUserFileKey");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getUserFileKey");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}/user_file_key".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileKey> localVarReturnType = new GenericType<FileKey>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get files where the user has no filekey
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Requests a list of missing FileKeys that may be generated by the current user.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has a KeyPair.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Clients should regularly request missing FileKeys to provide access to files for other users. The returned list is ordered by priority (Rescue Keys are returned first).&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param roomId Room Id (optional)
   * @param fileId File Id (optional)
   * @param userId User Id (optional)
   * @return MissingKeysResponse
   * @throws ApiException if fails to make API call
   */
  public MissingKeysResponse missingFileKeys(String xSdsAuthToken, Integer offset, Integer limit, Long roomId, Long fileId, Long userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling missingFileKeys");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/missingFileKeys".replaceAll("\\{format\\}","json");

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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<MissingKeysResponse> localVarReturnType = new GenericType<MissingKeysResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Move file system nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt;Moves nodes (folder,file) to another parent.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user with read and delete permissions in the source parent and write permissions in the target parent node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Nodes are moved to target parent.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Nodes must be in same source parent. Rooms cannot be moved.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Target Parent Node ID (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node moveNodes(String xSdsAuthToken, Long nodeId, MoveNodesRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling moveNodes");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling moveNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling moveNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/move_to".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Restore deleted nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Restore a list of deleted nodes.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has write permissions in parent room and \&quot;Restore Recycle Bin\&quot; permissions.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; The selected files are moved from the recycle bin to the chosen productive container.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; If no parent ID is provided, the node is restored to its previous location.&lt;br/&gt;The default resolution strategy is &lt;code&gt;autorename&lt;/code&gt; that adds numbers to the file name until the conflict is solved. If an existing file is overwritten, it is moved to the recycle bin instead of the restored one.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void restoreNodes(String xSdsAuthToken, RestoreDeletedNodesRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling restoreNodes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling restoreNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/deleted_nodes/actions/restore".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get Data Room Rescue KeyPair
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Retrieve the Data Room Rescue Key Pair.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has read permission in that room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer roomRescueKey(String xSdsAuthToken, Long roomId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling roomRescueKey");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling roomRescueKey");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/keypair".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<UserKeyPairContainer> localVarReturnType = new GenericType<UserKeyPairContainer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Search file system nodes
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br/&gt;Provides a flat list of file system nodes (Rooms, Folders, Files) of a given parent that are accessible by the current user.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Authenticated user with read permission on parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt;&lt;br/&gt; A maximum of 500 results is returned. For more results please use paging (offset + limit).&lt;br/&gt; EncryptionInfo is not provided.&lt;br/&gt; Wildcard character is the asterisk (*).&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;[:&amp;lt;VALUE&amp;gt;...]&lt;/dfn&gt;&lt;/p&gt;&lt;h5&gt;Fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;type&lt;/dt&gt;&lt;dd&gt;Node type filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;room|folder|file&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;fileType&lt;/dt&gt;&lt;dd&gt;File type filter (file extension)&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;classification&lt;/dt&gt;&lt;dd&gt;File classification  filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;1|2|3|4&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creation username filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation data filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Date (yyyy-HH-mm)&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;updatedBy&lt;/dt&gt;&lt;dd&gt;Last change username filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Last change date filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Date (yyyy-HH-mm)&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;expireAt&lt;/dt&gt;&lt;dd&gt;Expire date filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Date (yyyy-HH-mm)&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;size&lt;/dt&gt;&lt;dd&gt;Size filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Size in bytes&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;isFavorite&lt;/dt&gt;&lt;dd&gt;Favorite filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;true|false&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;branchVersion&lt;/dt&gt;&lt;dd&gt;Node branch version&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Version Number&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;type:eq:file|createdAt:ge:2015-01-01&lt;/samp&gt;&lt;br/&gt;&amp;rarr; Get nodes where type equals file AND file was created at or after 2015-01-01&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Node name&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator user name&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;updatedBy&lt;/dt&gt;&lt;dd&gt;Modifier user name&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Modification date&lt;/dd&gt;&lt;dt&gt;fileType&lt;/dt&gt;&lt;dd&gt;File type (extension)&lt;/dd&gt;&lt;dt&gt;classification&lt;/dt&gt;&lt;dd&gt;Classification&lt;/dd&gt;&lt;dt&gt;size&lt;/dt&gt;&lt;dd&gt;Node size&lt;/dd&gt;&lt;dt&gt;cntAdmins&lt;/dt&gt;&lt;dd&gt;For rooms only: Number of admins&lt;/dd&gt;&lt;dt&gt;cntUsers&lt;/dt&gt;&lt;dd&gt;For rooms only: Number of users&lt;/dd&gt;&lt;dt&gt;cntChildren&lt;/dt&gt;&lt;dd&gt;For rooms/subrooms/folder only: Number of direct children (not recursive)&lt;/dd&gt;&lt;dt&gt;cntDeletedVersions&lt;/dt&gt;&lt;dd&gt;For files/folder only: Number of deleted versions of this file/folder (not recursive)&lt;/dd&gt;&lt;dt&gt;type&lt;/dt&gt;&lt;dd&gt;Node Type (Room - Folder - File)&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by name descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param searchString Search string (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param depthLevel &lt;ul&gt;&lt;li&gt;0 - top-level nodes only;&lt;/li&gt;&lt;li&gt;-1 - full tree;&lt;/li&gt;&lt;li&gt;any positive N - include N levels starting from the current node&lt;/li&gt;&lt;/ul&gt; (optional, default to 0)
   * @param parentId Parent node ID. Only rooms and folders can be parents. Parent ID 0 or empty is the root node. (optional)
   * @param filter Filter string (optional)
   * @param sort Sorting string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @return NodeList
   * @throws ApiException if fails to make API call
   */
  public NodeList searchFsNodes(String xSdsAuthToken, String searchString, String xSdsDateFormat, Integer depthLevel, Long parentId, String filter, String sort, Integer offset, Integer limit) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling searchFsNodes");
    }
    
    // verify the required parameter 'searchString' is set
    if (searchString == null) {
      throw new ApiException(400, "Missing the required parameter 'searchString' when calling searchFsNodes");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/search".replaceAll("\\{format\\}","json");

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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<NodeList> localVarReturnType = new GenericType<NodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or change room granted groups
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Batch function. All existing group permissions will be overwritten.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Group&#39;s permissions are changed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void setRoomGroupsBatch(String xSdsAuthToken, Long roomId, RoomGroupsAddBatchRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling setRoomGroupsBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomGroupsBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomGroupsBatch");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/groups".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Add or change room granted users
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Batch function. All existing user permissions will be overwritten.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs to be Data Room Admin.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; User&#39;s permissions are changed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void setRoomUsersBatch(String xSdsAuthToken, Long roomId, RoomUsersAddBatchRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling setRoomUsersBatch");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling setRoomUsersBatch");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setRoomUsersBatch");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}/users".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Set FileKeys for a list of users and files
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Sets symmetric FileKeys for several users and files.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has FileKeys for the files.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Stores new FileKeys for other users.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Only users with copies of the FileKey (encrypted with their public keys) can access a certain file. This endpoint is used for the distribution of FileKeys amongst an authorized user base.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void setUserFileKeys(String xSdsAuthToken, UserFileKeySetBatchRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling setUserFileKeys");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setUserFileKeys");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/keys".replaceAll("\\{format\\}","json");

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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unmark a node (room, folder or file) as favorite
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Unmarks a node (room, folder, file) as favorite.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User needs read permissions on that node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A node gets unmarked as favorite.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param nodeId Node Id (required)
   * @throws ApiException if fails to make API call
   */
  public void unmarkFavorite(String xSdsAuthToken, Long nodeId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling unmarkFavorite");
    }
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling unmarkFavorite");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/{node_id}/favorite".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update file meta data
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Updates a file&#39;s meta data.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has change permissions in parent room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Meta data changed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Room/Folder/File name convention&lt;/h4&gt;&lt;h5&gt;Room/Folder/File names are limited to 150 characters.&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;Not allowed Room/Folder/File names&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;Not allowed characters in Room/Folder/File name&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;../&#39;, &#39;\\&#39;, &#39;&amp;lt;&#39;,&#39;&amp;gt;&#39;, &#39;:&#39;, &#39;\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param fileId File ID (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFile(String xSdsAuthToken, Integer fileId, UpdateFileRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling updateFile");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling updateFile");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFile");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/{file_id}".replaceAll("\\{format\\}","json")
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update folder
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Renames an existing folder.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User has change permissions in current Data Room.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; The folder is renamed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Room/Folder/File name convention&lt;/h4&gt;&lt;h5&gt;Room/Folder/File names are limited to 150 characters.&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;Not allowed Room/Folder/File names&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;Not allowed characters in Room/Folder/File name&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;../&#39;, &#39;\\&#39;, &#39;&amp;lt;&#39;,&#39;&amp;gt;&#39;, &#39;:&#39;, &#39;\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;/div&gt;&lt;/p&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param folderId Folder Id (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateFolder(String xSdsAuthToken, Long folderId, UpdateFolderRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling updateFolder");
    }
    
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling updateFolder");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateFolder");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/folders/{folder_id}".replaceAll("\\{format\\}","json")
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update room
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Update a room&#39;s meta data.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; User is admin in superordinated level.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Room&#39;s meta data is changed.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Room/Folder/File name convention&lt;/h4&gt;&lt;h5&gt;Room/Folder/File names are limited to 150 characters.&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;Not allowed Room/Folder/File names&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;,&#39;.&#39;,&#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;Not allowed characters in Room/Folder/File name&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;&lt;code&gt;&#39;../&#39;, &#39;\\&#39;, &#39;&amp;lt;&#39;,&#39;&amp;gt;&#39;, &#39;:&#39;, &#39;\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param roomId Room Id (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node updateRoom(String xSdsAuthToken, Long roomId, UpdateRoomRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling updateRoom");
    }
    
    // verify the required parameter 'roomId' is set
    if (roomId == null) {
      throw new ApiException(400, "Missing the required parameter 'roomId' when calling updateRoom");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRoom");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/rooms/{room_id}".replaceAll("\\{format\\}","json")
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

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload file
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt;&lt;br /&gt; Uploads a file or parts of it in an active Upload Channel.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; An Upload Channel has been created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A file or parts of it are uploaded to a temporary location.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; This endpoints supports chunked upload. Please cf. &lt;a href&#x3D;&#39;https://tools.ietf.org/html/rfc7233&#39; target&#x3D;&#39;_blank&#39;&gt;RFC 7233&lt;/a&gt; for further information.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param uploadId Upload channel Id (required)
   * @param file  (required)
   * @param contentRange Content Range (format: \&quot;bytes 0-999/3980\&quot;) (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   */
  public ChunkUploadResponse uploadFile(String xSdsAuthToken, String uploadId, File file, String contentRange) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling uploadFile");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadFile");
    }
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling uploadFile");
    }
    
    // create path and map variables
    String localVarPath = "/nodes/files/uploads/{upload_id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (contentRange != null)
      localVarHeaderParams.put("Content-Range", apiClient.parameterToString(contentRange));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
