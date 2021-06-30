package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.MessageCommentEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class MessageCommentsApi {
  private ApiClient apiClient;

  public MessageCommentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MessageCommentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Message Comment
   * Delete Message Comment
   * @param id Message Comment ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteMessageCommentsId(Integer id) throws ApiException {

    deleteMessageCommentsIdWithHttpInfo(id);
  }

  /**
   * Delete Message Comment
   * Delete Message Comment
   * @param id Message Comment ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteMessageCommentsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteMessageCommentsId");
    }
    
    // create path and map variables
    String localVarPath = "/message_comments/{id}"
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
   * List Message Comments
   * List Message Comments
   * @param messageId Message comment to return comments for. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;MessageCommentEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<MessageCommentEntity> getMessageComments(Integer messageId, Integer userId, String cursor, Integer perPage) throws ApiException {
    return getMessageCommentsWithHttpInfo(messageId, userId, cursor, perPage).getData();
      }

  /**
   * List Message Comments
   * List Message Comments
   * @param messageId Message comment to return comments for. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;MessageCommentEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<MessageCommentEntity>> getMessageCommentsWithHttpInfo(Integer messageId, Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'messageId' is set
    if (messageId == null) {
      throw new ApiException(400, "Missing the required parameter 'messageId' when calling getMessageComments");
    }
    
    // create path and map variables
    String localVarPath = "/message_comments";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "message_id", messageId));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<MessageCommentEntity>> localVarReturnType = new GenericType<List<MessageCommentEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Message Comment
   * Show Message Comment
   * @param id Message Comment ID. (required)
   * @return MessageCommentEntity
   * @throws ApiException if fails to make API call
   */
  public MessageCommentEntity getMessageCommentsId(Integer id) throws ApiException {
    return getMessageCommentsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Message Comment
   * Show Message Comment
   * @param id Message Comment ID. (required)
   * @return ApiResponse&lt;MessageCommentEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageCommentEntity> getMessageCommentsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getMessageCommentsId");
    }
    
    // create path and map variables
    String localVarPath = "/message_comments/{id}"
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

    GenericType<MessageCommentEntity> localVarReturnType = new GenericType<MessageCommentEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Message Comment
   * Update Message Comment
   * @param id Message Comment ID. (required)
   * @param body Comment body. (required)
   * @return MessageCommentEntity
   * @throws ApiException if fails to make API call
   */
  public MessageCommentEntity patchMessageCommentsId(Integer id, String body) throws ApiException {
    return patchMessageCommentsIdWithHttpInfo(id, body).getData();
      }

  /**
   * Update Message Comment
   * Update Message Comment
   * @param id Message Comment ID. (required)
   * @param body Comment body. (required)
   * @return ApiResponse&lt;MessageCommentEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageCommentEntity> patchMessageCommentsIdWithHttpInfo(Integer id, String body) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchMessageCommentsId");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling patchMessageCommentsId");
    }
    
    // create path and map variables
    String localVarPath = "/message_comments/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
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

    GenericType<MessageCommentEntity> localVarReturnType = new GenericType<MessageCommentEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Message Comment
   * Create Message Comment
   * @param body Comment body. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return MessageCommentEntity
   * @throws ApiException if fails to make API call
   */
  public MessageCommentEntity postMessageComments(String body, Integer userId) throws ApiException {
    return postMessageCommentsWithHttpInfo(body, userId).getData();
      }

  /**
   * Create Message Comment
   * Create Message Comment
   * @param body Comment body. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return ApiResponse&lt;MessageCommentEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MessageCommentEntity> postMessageCommentsWithHttpInfo(String body, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postMessageComments");
    }
    
    // create path and map variables
    String localVarPath = "/message_comments";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
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

    GenericType<MessageCommentEntity> localVarReturnType = new GenericType<MessageCommentEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
