package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.MessageEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class MessagesApi {
  private ApiClient apiClient;

  public MessagesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MessagesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Message
   * Delete Message
   * @param id Message ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteMessagesId(Integer id) throws ApiException {

    deleteMessagesIdWithHttpInfo(id);
  }

  /**
   * Delete Message
   * Delete Message
   * @param id Message ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteMessagesIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteMessagesId");
    }
    
    // create path and map variables
    String localVarPath = "/messages/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Messages
   * List Messages
   * @param projectId Project for which to return messages. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;MessageEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<MessageEntity> getMessages(Integer projectId, Integer userId, String cursor, Integer perPage) throws ApiException {
    return getMessagesWithHttpInfo(projectId, userId, cursor, perPage).getData();
      }

  /**
   * List Messages
   * List Messages
   * @param projectId Project for which to return messages. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;MessageEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<MessageEntity>> getMessagesWithHttpInfo(Integer projectId, Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'projectId' is set
    if (projectId == null) {
      throw new ApiException(400, "Missing the required parameter 'projectId' when calling getMessages");
    }
    
    // create path and map variables
    String localVarPath = "/messages";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "project_id", projectId));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<MessageEntity>> localVarReturnType = new GenericType<List<MessageEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Message
   * Show Message
   * @param id Message ID. (required)
   * @return MessageEntity
   * @throws ApiException if fails to make API call
   */
  public MessageEntity getMessagesId(Integer id) throws ApiException {
    return getMessagesIdWithHttpInfo(id).getData();
      }

  /**
   * Show Message
   * Show Message
   * @param id Message ID. (required)
   * @return ApiResponse&lt;MessageEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageEntity> getMessagesIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getMessagesId");
    }
    
    // create path and map variables
    String localVarPath = "/messages/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<MessageEntity> localVarReturnType = new GenericType<MessageEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Message
   * Update Message
   * @param id Message ID. (required)
   * @param projectId Project to which the message should be attached. (required)
   * @param subject Message subject. (required)
   * @param body Message body. (required)
   * @return MessageEntity
   * @throws ApiException if fails to make API call
   */
  public MessageEntity patchMessagesId(Integer id, Integer projectId, String subject, String body) throws ApiException {
    return patchMessagesIdWithHttpInfo(id, projectId, subject, body).getData();
      }

  /**
   * Update Message
   * Update Message
   * @param id Message ID. (required)
   * @param projectId Project to which the message should be attached. (required)
   * @param subject Message subject. (required)
   * @param body Message body. (required)
   * @return ApiResponse&lt;MessageEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageEntity> patchMessagesIdWithHttpInfo(Integer id, Integer projectId, String subject, String body) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchMessagesId");
    }
    
    // verify the required parameter 'projectId' is set
    if (projectId == null) {
      throw new ApiException(400, "Missing the required parameter 'projectId' when calling patchMessagesId");
    }
    
    // verify the required parameter 'subject' is set
    if (subject == null) {
      throw new ApiException(400, "Missing the required parameter 'subject' when calling patchMessagesId");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling patchMessagesId");
    }
    
    // create path and map variables
    String localVarPath = "/messages/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (projectId != null)
      localVarFormParams.put("project_id", projectId);
if (subject != null)
      localVarFormParams.put("subject", subject);
if (body != null)
      localVarFormParams.put("body", body);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<MessageEntity> localVarReturnType = new GenericType<MessageEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Message
   * Create Message
   * @param projectId Project to which the message should be attached. (required)
   * @param subject Message subject. (required)
   * @param body Message body. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return MessageEntity
   * @throws ApiException if fails to make API call
   */
  public MessageEntity postMessages(Integer projectId, String subject, String body, Integer userId) throws ApiException {
    return postMessagesWithHttpInfo(projectId, subject, body, userId).getData();
      }

  /**
   * Create Message
   * Create Message
   * @param projectId Project to which the message should be attached. (required)
   * @param subject Message subject. (required)
   * @param body Message body. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return ApiResponse&lt;MessageEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageEntity> postMessagesWithHttpInfo(Integer projectId, String subject, String body, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'projectId' is set
    if (projectId == null) {
      throw new ApiException(400, "Missing the required parameter 'projectId' when calling postMessages");
    }
    
    // verify the required parameter 'subject' is set
    if (subject == null) {
      throw new ApiException(400, "Missing the required parameter 'subject' when calling postMessages");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postMessages");
    }
    
    // create path and map variables
    String localVarPath = "/messages";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (projectId != null)
      localVarFormParams.put("project_id", projectId);
if (subject != null)
      localVarFormParams.put("subject", subject);
if (body != null)
      localVarFormParams.put("body", body);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<MessageEntity> localVarReturnType = new GenericType<MessageEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
