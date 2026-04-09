package ch.cyberduck.core.idgard.io.swagger.client.api;

import ch.cyberduck.core.idgard.io.swagger.client.ApiException;
import ch.cyberduck.core.idgard.io.swagger.client.ApiClient;
import ch.cyberduck.core.idgard.io.swagger.client.Configuration;
import ch.cyberduck.core.idgard.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.idgard.io.swagger.client.model.APIError;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxCreation;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxEdition;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxMember;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxMemberEdition;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxMetaData;
import ch.cyberduck.core.idgard.io.swagger.client.model.BoxesListWithInfos;
import ch.cyberduck.core.idgard.io.swagger.client.model.CTAResponse;
import ch.cyberduck.core.idgard.io.swagger.client.model.ChildrenDeletion;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMBoxGroups;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMGroup;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMGroupAssignment;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMGroupsUsers;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMNodeAssignment;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMNodePermission;
import ch.cyberduck.core.idgard.io.swagger.client.model.ERMUsersGroup;
import ch.cyberduck.core.idgard.io.swagger.client.model.Entry;
import ch.cyberduck.core.idgard.io.swagger.client.model.EntryEdition;
import ch.cyberduck.core.idgard.io.swagger.client.model.EntryList;
import ch.cyberduck.core.idgard.io.swagger.client.model.EntryLock;
import ch.cyberduck.core.idgard.io.swagger.client.model.FileOptionEditions;
import ch.cyberduck.core.idgard.io.swagger.client.model.FolderCreation;
import ch.cyberduck.core.idgard.io.swagger.client.model.IdName;
import ch.cyberduck.core.idgard.io.swagger.client.model.IdgardBox;
import ch.cyberduck.core.idgard.io.swagger.client.model.InlineResponse4001;
import ch.cyberduck.core.idgard.io.swagger.client.model.JournalEntry;
import ch.cyberduck.core.idgard.io.swagger.client.model.JournalFilter;
import ch.cyberduck.core.idgard.io.swagger.client.model.JournalUserId;
import ch.cyberduck.core.idgard.io.swagger.client.model.MailToMembers;
import ch.cyberduck.core.idgard.io.swagger.client.model.NewERMGroup;
import ch.cyberduck.core.idgard.io.swagger.client.model.Note;
import ch.cyberduck.core.idgard.io.swagger.client.model.NoteCreation;
import ch.cyberduck.core.idgard.io.swagger.client.model.OpenClose;
import ch.cyberduck.core.idgard.io.swagger.client.model.Quarantine;
import ch.cyberduck.core.idgard.io.swagger.client.model.SSMInvitation;
import ch.cyberduck.core.idgard.io.swagger.client.model.SharingInfo;
import ch.cyberduck.core.idgard.io.swagger.client.model.Sms;
import ch.cyberduck.core.idgard.io.swagger.client.model.UserTagDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BoxApiApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

  public BoxApiApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BoxApiApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public void setHeadersOverrides(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * 
   * Accept or deny Cta. Deprecated due to concurrency issues. Use &#x60;/uiapi/BoxAPI/v1/rest/cta/{boxId}&#x60; instead.
   * @param boxId  (required)
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public void agreeCta(String boxId, Boolean body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling agreeCta");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/cta/{boxId}/agree"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * assign a box-guest into a group, all guests are always in ALL group, so there&#x27;s no need to add anyone into this group.
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void assignUserToGroup(ERMGroupAssignment body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling assignUserToGroup");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling assignUserToGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/assignments"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Add box reporters to a dataroom specified by the id. The result of this action is the addition of all box reporters configured in the enterprise account to the specified dataroom if the auditor feature is booked.
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void auditDataroom(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling auditDataroom");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/members/audit/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Creates a new box. Providing data is optional, every empty field will be filled with default values. If the User can only create private boxes (old private users), given data will be discarded.
   * @param body  (required)
   * @return IdgardBox
   * @throws ApiException if fails to make API call
   */
  public IdgardBox createBox(BoxCreation body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createBox");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<IdgardBox> localVarReturnType = new GenericType<IdgardBox>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Creates a new directory in given parent with given name
   * @param body  (required)
   * @param boxId  (required)
   * @param parentId  (required)
   * @return Entry
   * @throws ApiException if fails to make API call
   */
  public Entry createFolder(FolderCreation body, String boxId, String parentId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createFolder");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling createFolder");
    }
    // verify the required parameter 'parentId' is set
    if (parentId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentId' when calling createFolder");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}/{parentId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "parentId" + "\\}", apiClient.escapeString(parentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Entry> localVarReturnType = new GenericType<Entry>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * create a new group, name must not be empty. When the group is created from UserLabel, requires id.id is the labelId and id.type is TAG, otherwise id.type should be GROUP.
   * @param body  (required)
   * @param boxId  (required)
   * @return ERMGroup
   * @throws ApiException if fails to make API call
   */
  public ERMGroup createGroup(NewERMGroup body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createGroup");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling createGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/groups"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMGroup> localVarReturnType = new GenericType<ERMGroup>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Creates a new note with given information in the provided parent (box/folder)
   * @param body  (required)
   * @param boxId  (required)
   * @param parentId  (required)
   * @return Note
   * @throws ApiException if fails to make API call
   */
  public Note createNote(NoteCreation body, String boxId, String parentId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createNote");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling createNote");
    }
    // verify the required parameter 'parentId' is set
    if (parentId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentId' when calling createNote");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/notes/{boxId}/{parentId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "parentId" + "\\}", apiClient.escapeString(parentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Note> localVarReturnType = new GenericType<Note>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Deletes the box entirely with all it&#x27;s content
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBox(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling deleteBox");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Deletes the given entries and returns a list of all deleted ones
   * @param body  (required)
   * @param boxId  (required)
   * @return List&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public List<String> deleteChildren(ChildrenDeletion body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteChildren");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling deleteChildren");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * delete user-created group.
   * @param boxId  (required)
   * @param groupId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteGroup(String boxId, String groupId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling deleteGroup");
    }
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling deleteGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/groups/{groupId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "groupId" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Downloads Journal with generated id. The format may be set to csv (default) or xlsx
   * @param downloadId  (required)
   * @throws ApiException if fails to make API call
   */
  public void downloadJournal(String downloadId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'downloadId' is set
    if (downloadId == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadId' when calling downloadJournal");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/journal/download";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "downloadId", downloadId));


    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * duplicate a directory &amp; its sub-directly recursively in the same level with given name,preserving name,descriptions and erm-specific attributes
   * @param body  (required)
   * @param boxId  (required)
   * @return Entry
   * @throws ApiException if fails to make API call
   */
  public Entry duplicateFolder(IdName body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling duplicateFolder");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling duplicateFolder");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/folder-duplication"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Entry> localVarReturnType = new GenericType<Entry>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Update the Box&#x27;s settings, ignore tags, use dedicated tagAPI for handling tags
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void editBox(BoxEdition body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling editBox");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling editBox");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Moves and/or renames the child. If newName is specified, the entry will be renamed. If newParentId is  specified, the entry will be moved. Both actions can be combined in one request.
   * @param body  (required)
   * @param boxId  (required)
   * @param childId  (required)
   * @throws ApiException if fails to make API call
   */
  public void editChild(EntryEdition body, String boxId, String childId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling editChild");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling editChild");
    }
    // verify the required parameter 'childId' is set
    if (childId == null) {
      throw new ApiException(400, "Missing the required parameter 'childId' when calling editChild");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}/{childId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "childId" + "\\}", apiClient.escapeString(childId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Inside Datarooms changes a file&#x27;s option (View-Only, Watermark, None)
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void editFileOption(FileOptionEditions body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling editFileOption");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling editFileOption");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * reset membership of all members of the box to status specified in request, could be slow if box member list is big. Current members not in the list will be removed from box.
   * @param boxId  (required)
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   */
  public void editMembers(String boxId, List<BoxMemberEdition> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling editMembers");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/members/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * request exporting box structure, complete option( which include folders,files and notes) is applicable only for box owner and box admin, default export contains only folders. valid format is pdf or xml (default is pdf). response is the uri where this pdf is allocated. the uri can only used once.
   * @param boxId  (required)
   * @param complete  (optional)
   * @param format  (optional)
   * @param language  (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String exportBoxIndex(String boxId, Boolean complete, String format, String language) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling exportBoxIndex");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/export/schema/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "complete", complete));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "language", language));


    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Returns specified part of the DataroomJournal. Filtering by the event detail \&quot;classification changes\&quot; will show no results before release 1.4.0 (May 2022) Filtering by renaming of files will show results for renamed notes and folder, too, that were falsely recorded until release 1.4.0. Filtering by renaming of notes and folders will show no results before release 1.4.0 
   * @param boxId  (required)
   * @param count limit the number of entries in the result. (required)
   * @param referenceId can be used as anchor to the next entries in case user wants to have stable result (optional)
   * @param from when set, only entries younger than from (exclusive) will be returned. (optional)
   * @param to when set, only entries older than to (inclusive) will be returned. (optional)
   * @param start offset used for pagination (optional)
   * @param userNameFilterKey filter for username. (because of renaming these might be different users) Supposed these are name entries given back from backend: [{\&quot;filterKey\&quot;:\&quot;a-b\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:null,\&quot;name\&quot;:\&quot;ota\&quot;}},  {\&quot;filterKey\&quot;:\&quot;c-d\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:null,\&quot;name\&quot;:\&quot;gandoo-ota\&quot;}}, {\&quot;filterKey\&quot;:\&quot;e-f\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:\&quot;1\&quot;,\&quot;name\&quot;:\&quot;johnDoe\&quot;}}, {\&quot;filterKey\&quot;:\&quot;g-h\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:\&quot;1\&quot;,\&quot;name\&quot;:\&quot;johnDoeChanged\&quot;}}, {\&quot;filterKey\&quot;:\&quot;p-q\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:\&quot;2\&quot;,\&quot;name\&quot;:\&quot;johnDoe\&quot;}}, {\&quot;filterKey\&quot;:\&quot;k-l\&quot;,\&quot;userNameId\&quot;:{\&quot;id\&quot;:\&quot;3\&quot;,\&quot;name\&quot;:\&quot;Max\&quot;}}] The first 2 entries are ota users (no actual id). User with id&#x3D;1 have 2 different names. To view ota users with name \&quot;gandoo\&quot;, use \&quot;usernameFilterKey&#x3D;a-b\&quot; To view all ota users (includes \&quot;gandoo\&quot; &amp; \&quot;gandoo-ota\&quot;), use \&quot;userId&#x3D;0\&quot; To view only users with uid&#x3D;1 (which includes both \&quot;johnDoe and johnDoeChanged\&quot;), use \&quot;userId&#x3D;1\&quot; To view only users with uid&#x3D;1 and name is \&quot;johnDoe\&quot;, \&quot;usernameFilterKey&#x3D;e-f\&quot; To view all users with name \&quot;johnDoe\&quot; (which includes user with id&#x3D;1 and id&#x3D;2), use \&quot;usernameFilterKey&#x3D;e-f,p-q\&quot;, concatenating filter keys together with comma(\&quot;,\&quot;) to have multiple names included If both userId and usernameFilterKey parameters are present, only userId is considered  (optional)
   * @param userId filter for user. (because of license reassinging these might be different people) (optional)
   * @param eventCategory filter for events which concern files, folder or notes (optional)
   * @param eventDetail filter for details fitting to chosen event category. This parameter is ignored if no eventCategory is set (optional)
   * @return List&lt;JournalEntry&gt;
   * @throws ApiException if fails to make API call
   */
  public List<JournalEntry> filterJournal(String boxId, Integer count, Long referenceId, Long from, Long to, Integer start, String userNameFilterKey, String userId, String eventCategory, String eventDetail) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling filterJournal");
    }
    // verify the required parameter 'count' is set
    if (count == null) {
      throw new ApiException(400, "Missing the required parameter 'count' when calling filterJournal");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/journal/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "referenceId", referenceId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "start", start));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "count", count));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userNameFilterKey", userNameFilterKey));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "eventCategory", eventCategory));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "eventDetail", eventDetail));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<JournalEntry>> localVarReturnType = new GenericType<List<JournalEntry>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Generates and returns an DownloadID which can be used to download the Journal with &#x60;/uiapi/BoxAPI/v1/rest/journal/download&#x60;. Optional parameters can be used to limit the date range to download from the journal and preferred format (default is csv if not specified).
   * @param boxId  (required)
   * @param from  (optional)
   * @param to  (optional)
   * @param userNameFilterKey  (optional)
   * @param userId  (optional)
   * @param eventCategory  (optional)
   * @param eventDetail  (optional)
   * @param format  (optional)
   * @param language sets language for xlsx export (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String generateJournalDownloadId(String boxId, Long from, Long to, String userNameFilterKey, String userId, String eventCategory, String eventDetail, String format, String language) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling generateJournalDownloadId");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/journal/{boxId}/id"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userNameFilterKey", userNameFilterKey));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "eventCategory", eventCategory));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "eventDetail", eventDetail));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "language", language));


    final String[] localVarAccepts = {
      "text/plain", "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * list applied group permissions for this node, this could be the permissions explicitly set on that node or those of node&#x27;s closest parent if there&#x27;s nothing set for this node.
   * @param boxId  (required)
   * @param nodeId  (required)
   * @return List&lt;ERMNodePermission&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ERMNodePermission> getAssignedPermissionsForNode(String boxId, String nodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getAssignedPermissionsForNode");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling getAssignedPermissionsForNode");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/permissions/{nodeId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<ERMNodePermission>> localVarReturnType = new GenericType<List<ERMNodePermission>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Returns the Metadata of a box
   * @param boxId  (required)
   * @return BoxMetaData
   * @throws ApiException if fails to make API call
   */
  public BoxMetaData getBox(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getBox");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<BoxMetaData> localVarReturnType = new GenericType<BoxMetaData>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Get boxes
   * @return List&lt;IdgardBox&gt;
   * @throws ApiException if fails to make API call
   */
  public List<IdgardBox> getBoxes() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<IdgardBox>> localVarReturnType = new GenericType<List<IdgardBox>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Fetches the direct children of the specified Box. The given folder inside of given box will be loaded.
   * @param boxId  (required)
   * @param folderId  (required)
   * @return EntryList
   * @throws ApiException if fails to make API call
   */
  public EntryList getChildren(String boxId, String folderId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getChildren");
    }
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling getChildren");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}/{folderId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "folderId" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<EntryList> localVarReturnType = new GenericType<EntryList>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * get all the ids of the datarooms with their owner id that are not assigned a box reporter for a specific auditor
   * @param language  (optional)
   * @throws ApiException if fails to make API call
   */
  public void getDataroomsWithoutAuditor(String language) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/reporting/auditors/download";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "language", language));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Get lock on a file
   * @param boxId  (required)
   * @param childId  (required)
   * @return EntryLock
   * @throws ApiException if fails to make API call
   */
  public EntryLock getFileLock(String boxId, String childId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getFileLock");
    }
    // verify the required parameter 'childId' is set
    if (childId == null) {
      throw new ApiException(400, "Missing the required parameter 'childId' when calling getFileLock");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}/{childId}/lock"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "childId" + "\\}", apiClient.escapeString(childId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<EntryLock> localVarReturnType = new GenericType<EntryLock>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * get info of group (no users included).
   * @param boxId  (required)
   * @param groupId  (required)
   * @return ERMGroup
   * @throws ApiException if fails to make API call
   */
  public ERMGroup getGroup(String boxId, String groupId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getGroup");
    }
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/groups/{groupId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "groupId" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMGroup> localVarReturnType = new GenericType<ERMGroup>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Return Tags with their assignments which contains box members (same enterprise and visible for user)
   * @param boxId  (required)
   * @return List&lt;UserTagDetails&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserTagDetails> getGroupsOfBoxMember(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getGroupsOfBoxMember");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/membergroups/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<UserTagDetails>> localVarReturnType = new GenericType<List<UserTagDetails>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * list groups where this user (which should not be the box owner) belongs to 
   * @param boxId  (required)
   * @param uid  (required)
   * @return ERMGroupsUsers
   * @throws ApiException if fails to make API call
   */
  public ERMGroupsUsers getGroupsOfUser(String boxId, String uid) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getGroupsOfUser");
    }
    // verify the required parameter 'uid' is set
    if (uid == null) {
      throw new ApiException(400, "Missing the required parameter 'uid' when calling getGroupsOfUser");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/assignments/users/{uid}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "uid" + "\\}", apiClient.escapeString(uid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMGroupsUsers> localVarReturnType = new GenericType<ERMGroupsUsers>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Returns specified part of the DataroomJournal. If not specifying an referenceId, you will get the current state from the database. If using this for multiple pages, it could get racy if new entries were added during loading different pages. Therefore referenceId can be specified, which will result in fetching only entries, which are older than the one with given referenceId. Therefore new entries won&#x27;t influence returned pages.
   * @param count  (required)
   * @param boxId  (required)
   * @param body  (optional)
   * @param start  (optional)
   * @param referenceId  (optional)
   * @return List&lt;JournalEntry&gt;
   * @throws ApiException if fails to make API call
   */
  public List<JournalEntry> getJournal(Integer count, String boxId, JournalFilter body, Integer start, Long referenceId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'count' is set
    if (count == null) {
      throw new ApiException(400, "Missing the required parameter 'count' when calling getJournal");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getJournal");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/journal/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "start", start));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "count", count));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "referenceId", referenceId));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<JournalEntry>> localVarReturnType = new GenericType<List<JournalEntry>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Returns all users of the firm with a &#x27;joined&#x27; attribute. Owner and Manager of a box see all users, including ones, which are not joined, but able to join. Users with the permission to see members only get the ones currently in the box
   * @param boxId  (required)
   * @return List&lt;BoxMember&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BoxMember> getMembers(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getMembers");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/members/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<BoxMember>> localVarReturnType = new GenericType<List<BoxMember>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Returns the note specified by given id
   * @param boxId  (required)
   * @param parentId  (required)
   * @param childId  (required)
   * @return Note
   * @throws ApiException if fails to make API call
   */
  public Note getNote(String boxId, String parentId, String childId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getNote");
    }
    // verify the required parameter 'parentId' is set
    if (parentId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentId' when calling getNote");
    }
    // verify the required parameter 'childId' is set
    if (childId == null) {
      throw new ApiException(400, "Missing the required parameter 'childId' when calling getNote");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/notes/{boxId}/{parentId}/{childId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "parentId" + "\\}", apiClient.escapeString(parentId.toString()))
      .replaceAll("\\{" + "childId" + "\\}", apiClient.escapeString(childId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Note> localVarReturnType = new GenericType<Note>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Lists boxes of the current user depending of offset and chunk
   * @param offset  (optional, default to 0)
   * @param nbBoxes  (optional, default to 20)
   * @return BoxesListWithInfos
   * @throws ApiException if fails to make API call
   */
  public BoxesListWithInfos getPartialBoxes(Integer offset, Integer nbBoxes) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/partial_boxes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "nbBoxes", nbBoxes));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<BoxesListWithInfos> localVarReturnType = new GenericType<BoxesListWithInfos>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * get sharing info for this box, not to be called from OTA (which is always provided when request with the box link)
   * @param boxId  (required)
   * @return SharingInfo
   * @throws ApiException if fails to make API call
   */
  public SharingInfo getSharingInfo(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getSharingInfo");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/sharing/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<SharingInfo> localVarReturnType = new GenericType<SharingInfo>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * list users of this group (which should not contain box owner), the ALL group should contains ALL guests
   * @param boxId  (required)
   * @param gid  (required)
   * @return ERMUsersGroup
   * @throws ApiException if fails to make API call
   */
  public ERMUsersGroup getUsersOfGroup(String boxId, String gid) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling getUsersOfGroup");
    }
    // verify the required parameter 'gid' is set
    if (gid == null) {
      throw new ApiException(400, "Missing the required parameter 'gid' when calling getUsersOfGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/assignments/groups/{gid}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "gid" + "\\}", apiClient.escapeString(gid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMUsersGroup> localVarReturnType = new GenericType<ERMUsersGroup>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * record an attempt from a Dataroom user to send an email to some recipients  with some contents, the data is provided by client and there&#x27;s no guarantee about correctness of action, either about the recipients nor about the content, nor about the the sending of the email 
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void initEmailForMembers(MailToMembers body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling initEmailForMembers");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling initEmailForMembers");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/mailing/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * List groups for this box, there should be at least 2 auto-created groups and groups created by users.
   * @param boxId  (required)
   * @return ERMBoxGroups
   * @throws ApiException if fails to make API call
   */
  public ERMBoxGroups listGroups(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling listGroups");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/groups"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMBoxGroups> localVarReturnType = new GenericType<ERMBoxGroups>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * list unique user names in journal
   * @param boxId  (required)
   * @return List&lt;JournalUserId&gt;
   * @throws ApiException if fails to make API call
   */
  public List<JournalUserId> listUsersInJournals(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling listUsersInJournals");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/journal/{boxId}/names"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<JournalUserId>> localVarReturnType = new GenericType<List<JournalUserId>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Disconnects all Anonymous (Share-Link) users with an currently active Session
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void logoutAnonymousMembers(String boxId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling logoutAnonymousMembers");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/members/anonymous/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * set contingent and/or passcode for the box, result can be used by client to build invitation email for this box, if box should be opened and policy required a passcode, server will auto generate a passcode if not provides by client, return updated contingent and passcode. if SSI is enforce, boxLink will not be returned back.
   * @param body  (required)
   * @param boxId  (required)
   * @return OpenClose
   * @throws ApiException if fails to make API call
   */
  public OpenClose openOrCloseBox(OpenClose body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling openOrCloseBox");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling openOrCloseBox");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxes/{boxId}/sealing"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<OpenClose> localVarReturnType = new GenericType<OpenClose>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * send specified passcode to given number for boxmail feature
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void sendBoxPasscode(Sms body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling sendBoxPasscode");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling sendBoxPasscode");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/boxmail/{boxId}/sms"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * send an invitation with join link, requires booked license and enabled. user should be box owner or manager.
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void sendInvitationEmail(SSMInvitation body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling sendInvitationEmail");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling sendInvitationEmail");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/ssi/{boxId}/emails"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Set lock on a file
   * @param boxId  (required)
   * @param childId  (required)
   * @param body  (optional)
   * @return EntryLock
   * @throws ApiException if fails to make API call
   */
  public EntryLock setFileLock(String boxId, String childId, Boolean body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling setFileLock");
    }
    // verify the required parameter 'childId' is set
    if (childId == null) {
      throw new ApiException(400, "Missing the required parameter 'childId' when calling setFileLock");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/children/{boxId}/{childId}/lock"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "childId" + "\\}", apiClient.escapeString(childId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<EntryLock> localVarReturnType = new GenericType<EntryLock>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * set assign a group with specific permission for this node. if nodeAssignment.id references MANAGERS group, nodeAssignment.right value is irrelevant, MANAGERS has the least restrictive right.if nodeAssignment.right is less restrictive than those from the closest parent, this will throw exception.noInheritanceOnCreate is used if a new Group must be created from UserTag (nodeAssignment.id.type&#x3D;&#x3D;TAG)
   * @param body  (required)
   * @param boxId  (required)
   * @param nodeId  (required)
   * @param noInheritanceOnCreate  (optional)
   * @throws ApiException if fails to make API call
   */
  public void setPermissionForNode(ERMNodeAssignment body, String boxId, String nodeId, Boolean noInheritanceOnCreate) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setPermissionForNode");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling setPermissionForNode");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling setPermissionForNode");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/permissions/{nodeId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "noInheritanceOnCreate", noInheritanceOnCreate));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * set to or cancel quarantine state of the box. Setting it to quarantine will require that box is not in quarantine, booked-license, days at least as in specified policy. Quarantine can be abort by set days to 0.If set successfully, return result with time range from now until quarantine ends (where box will be automatically deleted)
   * @param boxId  (required)
   * @param days  (required)
   * @return Quarantine
   * @throws ApiException if fails to make API call
   */
  public Quarantine setQuarantineStatus(String boxId, String days) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling setQuarantineStatus");
    }
    // verify the required parameter 'days' is set
    if (days == null) {
      throw new ApiException(400, "Missing the required parameter 'days' when calling setQuarantineStatus");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/quarantine/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "days", days));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Quarantine> localVarReturnType = new GenericType<Quarantine>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * set sharing info for this box, set null to remove sharinginfo
   * @param boxId  (required)
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   */
  public void setSharingInfo(String boxId, SharingInfo body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling setSharingInfo");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/sharing/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Accept or deny a given type of Cta
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void setUsersCta(CTAResponse body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setUsersCta");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling setUsersCta");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/cta/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * toggle ERM feature for this box, requires booked-license, user must be box owner or manager of the same company as box owner&#x27;s
   * @param boxId  (required)
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   */
  public void switchERM(String boxId, Boolean body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling switchERM");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/switch"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * unassign/delete a group from node, nodeAssignment.group should not be the ALL group.
   * @param body  (required)
   * @param boxId  (required)
   * @param nodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void unassignPermissionForNode(ERMNodeAssignment body, String boxId, String nodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling unassignPermissionForNode");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling unassignPermissionForNode");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling unassignPermissionForNode");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/permissions/{nodeId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * unassign a box-guest from a group, all guests are always in ALL group, so there&#x27;s no need to assign/unasssign from/to this group.
   * @param body  (required)
   * @param boxId  (required)
   * @throws ApiException if fails to make API call
   */
  public void unassignUserFromGroup(ERMGroupAssignment body, String boxId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling unassignUserFromGroup");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling unassignUserFromGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/assignments"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * update group name/description, must not be group of type ALL or MANAGERS.
   * @param body  (required)
   * @param boxId  (required)
   * @param groupId  (required)
   * @return ERMGroup
   * @throws ApiException if fails to make API call
   */
  public ERMGroup updateGroup(ERMGroup body, String boxId, String groupId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateGroup");
    }
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling updateGroup");
    }
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling updateGroup");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/erm/{boxId}/groups/{groupId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()))
      .replaceAll("\\{" + "groupId" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<ERMGroup> localVarReturnType = new GenericType<ERMGroup>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Update only members specified request, returns valid members which are included in the list. Current users not in list won&#x27;t be changed.
   * @param boxId  (required)
   * @param body  (optional)
   * @return List&lt;BoxMember&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BoxMember> updateMembers(String boxId, List<BoxMemberEdition> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'boxId' is set
    if (boxId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxId' when calling updateMembers");
    }
    // create path and map variables
    String localVarPath = "/uiapi/BoxAPI/v1/rest/members/{boxId}"
      .replaceAll("\\{" + "boxId" + "\\}", apiClient.escapeString(boxId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<List<BoxMember>> localVarReturnType = new GenericType<List<BoxMember>>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
