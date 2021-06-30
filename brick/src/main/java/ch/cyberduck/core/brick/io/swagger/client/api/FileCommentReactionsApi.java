package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.FileCommentReactionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class FileCommentReactionsApi {
  private ApiClient apiClient;

  public FileCommentReactionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileCommentReactionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete File Comment Reaction
   * Delete File Comment Reaction
   * @param id File Comment Reaction ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFileCommentReactionsId(Integer id) throws ApiException {

    deleteFileCommentReactionsIdWithHttpInfo(id);
  }

  /**
   * Delete File Comment Reaction
   * Delete File Comment Reaction
   * @param id File Comment Reaction ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteFileCommentReactionsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteFileCommentReactionsId");
    }
    
    // create path and map variables
    String localVarPath = "/file_comment_reactions/{id}"
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
   * Create File Comment Reaction
   * Create File Comment Reaction
   * @param fileCommentId ID of file comment to attach reaction to. (required)
   * @param emoji Emoji to react with. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return FileCommentReactionEntity
   * @throws ApiException if fails to make API call
   */
  public FileCommentReactionEntity postFileCommentReactions(Integer fileCommentId, String emoji, Integer userId) throws ApiException {
    return postFileCommentReactionsWithHttpInfo(fileCommentId, emoji, userId).getData();
      }

  /**
   * Create File Comment Reaction
   * Create File Comment Reaction
   * @param fileCommentId ID of file comment to attach reaction to. (required)
   * @param emoji Emoji to react with. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return ApiResponse&lt;FileCommentReactionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileCommentReactionEntity> postFileCommentReactionsWithHttpInfo(Integer fileCommentId, String emoji, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileCommentId' is set
    if (fileCommentId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileCommentId' when calling postFileCommentReactions");
    }
    
    // verify the required parameter 'emoji' is set
    if (emoji == null) {
      throw new ApiException(400, "Missing the required parameter 'emoji' when calling postFileCommentReactions");
    }
    
    // create path and map variables
    String localVarPath = "/file_comment_reactions";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (fileCommentId != null)
      localVarFormParams.put("file_comment_id", fileCommentId);
if (emoji != null)
      localVarFormParams.put("emoji", emoji);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileCommentReactionEntity> localVarReturnType = new GenericType<FileCommentReactionEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
