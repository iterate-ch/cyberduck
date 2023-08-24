package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateMediaFolderRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateMediaItemRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaFolder;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaFolderContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaItem;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaItemContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateMediaFolderRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateMediaItemRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class MediaApi {
  private ApiClient apiClient;

  public MediaApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MediaApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete a mediafolder
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void mediaDelete(String id) throws ApiException {

    mediaDeleteWithHttpInfo(id);
  }

  /**
   * Delete a mediafolder
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> mediaDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete a photo from a specific photo album.
   * 
   * @param id The mediafolder id (required)
   * @param mediaItemId The mediaitem id (required)
   * @throws ApiException if fails to make API call
   */
  public void mediaDelete_0(String id, String mediaItemId) throws ApiException {

    mediaDelete_0WithHttpInfo(id, mediaItemId);
  }

  /**
   * Delete a photo from a specific photo album.
   * 
   * @param id The mediafolder id (required)
   * @param mediaItemId The mediaitem id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> mediaDelete_0WithHttpInfo(String id, String mediaItemId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaDelete_0");
    }
    
    // verify the required parameter 'mediaItemId' is set
    if (mediaItemId == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaItemId' when calling mediaDelete_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}/items/{mediaItemId}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()))
      .replaceAll("\\{" + "mediaItemId" + "\\}", apiClient.escapeString(mediaItemId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get a mediafolder
   * 
   * @param id  (required)
   * @return MediaFolder
   * @throws ApiException if fails to make API call
   */
  public MediaFolder mediaGet(String id) throws ApiException {
    return mediaGetWithHttpInfo(id).getData();
      }

  /**
   * Get a mediafolder
   * 
   * @param id  (required)
   * @return ApiResponse&lt;MediaFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaFolder> mediaGetWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaFolder> localVarReturnType = new GenericType<MediaFolder>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets a list of random photos
   * 
   * @param id The media folder ID Guid.Empty for random album (required)
   * @param numberOfPhotos Number of random photos (required)
   * @return List&lt;MediaItem&gt;
   * @throws ApiException if fails to make API call
   */
  public List<MediaItem> mediaGetRandomPhotos(String id, Integer numberOfPhotos) throws ApiException {
    return mediaGetRandomPhotosWithHttpInfo(id, numberOfPhotos).getData();
      }

  /**
   * Gets a list of random photos
   * 
   * @param id The media folder ID Guid.Empty for random album (required)
   * @param numberOfPhotos Number of random photos (required)
   * @return ApiResponse&lt;List&lt;MediaItem&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<MediaItem>> mediaGetRandomPhotosWithHttpInfo(String id, Integer numberOfPhotos) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaGetRandomPhotos");
    }
    
    // verify the required parameter 'numberOfPhotos' is set
    if (numberOfPhotos == null) {
      throw new ApiException(400, "Missing the required parameter 'numberOfPhotos' when calling mediaGetRandomPhotos");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}/randomphotos/{numberOfPhotos}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()))
      .replaceAll("\\{" + "numberOfPhotos" + "\\}", apiClient.escapeString(numberOfPhotos.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<MediaItem>> localVarReturnType = new GenericType<List<MediaItem>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List the media folders (albums) created by the user.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (DESC/ASC) (required)
   * @return MediaFolderContents
   * @throws ApiException if fails to make API call
   */
  public MediaFolderContents mediaGet_0(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    return mediaGet_0WithHttpInfo(pageIndex, pageSize, sortExpression).getData();
      }

  /**
   * List the media folders (albums) created by the user.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (DESC/ASC) (required)
   * @return ApiResponse&lt;MediaFolderContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaFolderContents> mediaGet_0WithHttpInfo(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling mediaGet_0");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling mediaGet_0");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling mediaGet_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaFolderContents> localVarReturnType = new GenericType<MediaFolderContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List the photos in a specific photo album.
   * 
   * @param id The selected mediafolder (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (ASC/DESC) (required)
   * @param includeParent Includes the parent (MediaFolder) in the response if true (required)
   * @return MediaItemContents
   * @throws ApiException if fails to make API call
   */
  public MediaItemContents mediaGet_1(String id, Integer pageIndex, Integer pageSize, String sortExpression, Boolean includeParent) throws ApiException {
    return mediaGet_1WithHttpInfo(id, pageIndex, pageSize, sortExpression, includeParent).getData();
      }

  /**
   * List the photos in a specific photo album.
   * 
   * @param id The selected mediafolder (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (ASC/DESC) (required)
   * @param includeParent Includes the parent (MediaFolder) in the response if true (required)
   * @return ApiResponse&lt;MediaItemContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaItemContents> mediaGet_1WithHttpInfo(String id, Integer pageIndex, Integer pageSize, String sortExpression, Boolean includeParent) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaGet_1");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling mediaGet_1");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling mediaGet_1");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling mediaGet_1");
    }
    
    // verify the required parameter 'includeParent' is set
    if (includeParent == null) {
      throw new ApiException(400, "Missing the required parameter 'includeParent' when calling mediaGet_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}/items"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeParent", includeParent));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaItemContents> localVarReturnType = new GenericType<MediaItemContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new album.
   * 
   * @param createMediaFolderRequest mediaFolderRequest (required)
   * @return MediaFolder
   * @throws ApiException if fails to make API call
   */
  public MediaFolder mediaPost(CreateMediaFolderRequest createMediaFolderRequest) throws ApiException {
    return mediaPostWithHttpInfo(createMediaFolderRequest).getData();
      }

  /**
   * Create a new album.
   * 
   * @param createMediaFolderRequest mediaFolderRequest (required)
   * @return ApiResponse&lt;MediaFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaFolder> mediaPostWithHttpInfo(CreateMediaFolderRequest createMediaFolderRequest) throws ApiException {
    Object localVarPostBody = createMediaFolderRequest;
    
    // verify the required parameter 'createMediaFolderRequest' is set
    if (createMediaFolderRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createMediaFolderRequest' when calling mediaPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaFolder> localVarReturnType = new GenericType<MediaFolder>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add a list of photos to a specific album.
   * 
   * @param id The mediafolder to create the item in (required)
   * @param createMediaItemRequests List of CreateMediaItemRequest (required)
   * @return List&lt;MediaItem&gt;
   * @throws ApiException if fails to make API call
   */
  public List<MediaItem> mediaPost_0(String id, List<CreateMediaItemRequest> createMediaItemRequests) throws ApiException {
    return mediaPost_0WithHttpInfo(id, createMediaItemRequests).getData();
      }

  /**
   * Add a list of photos to a specific album.
   * 
   * @param id The mediafolder to create the item in (required)
   * @param createMediaItemRequests List of CreateMediaItemRequest (required)
   * @return ApiResponse&lt;List&lt;MediaItem&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<MediaItem>> mediaPost_0WithHttpInfo(String id, List<CreateMediaItemRequest> createMediaItemRequests) throws ApiException {
    Object localVarPostBody = createMediaItemRequests;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaPost_0");
    }
    
    // verify the required parameter 'createMediaItemRequests' is set
    if (createMediaItemRequests == null) {
      throw new ApiException(400, "Missing the required parameter 'createMediaItemRequests' when calling mediaPost_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}/items"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<MediaItem>> localVarReturnType = new GenericType<List<MediaItem>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update a mediafolder
   * 
   * @param id  (required)
   * @param updateMediaFolderRequest mediaFolderRequest (required)
   * @return MediaFolder
   * @throws ApiException if fails to make API call
   */
  public MediaFolder mediaPut(String id, UpdateMediaFolderRequest updateMediaFolderRequest) throws ApiException {
    return mediaPutWithHttpInfo(id, updateMediaFolderRequest).getData();
      }

  /**
   * Update a mediafolder
   * 
   * @param id  (required)
   * @param updateMediaFolderRequest mediaFolderRequest (required)
   * @return ApiResponse&lt;MediaFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaFolder> mediaPutWithHttpInfo(String id, UpdateMediaFolderRequest updateMediaFolderRequest) throws ApiException {
    Object localVarPostBody = updateMediaFolderRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaPut");
    }
    
    // verify the required parameter 'updateMediaFolderRequest' is set
    if (updateMediaFolderRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateMediaFolderRequest' when calling mediaPut");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaFolder> localVarReturnType = new GenericType<MediaFolder>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update a photo in a specific photo album.
   * 
   * @param id The mediafolder id (required)
   * @param mediaItemId The mediaitem id (required)
   * @param updateMediaItemRequest  (required)
   * @return MediaItem
   * @throws ApiException if fails to make API call
   */
  public MediaItem mediaPut_0(String id, String mediaItemId, UpdateMediaItemRequest updateMediaItemRequest) throws ApiException {
    return mediaPut_0WithHttpInfo(id, mediaItemId, updateMediaItemRequest).getData();
      }

  /**
   * Update a photo in a specific photo album.
   * 
   * @param id The mediafolder id (required)
   * @param mediaItemId The mediaitem id (required)
   * @param updateMediaItemRequest  (required)
   * @return ApiResponse&lt;MediaItem&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaItem> mediaPut_0WithHttpInfo(String id, String mediaItemId, UpdateMediaItemRequest updateMediaItemRequest) throws ApiException {
    Object localVarPostBody = updateMediaItemRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaPut_0");
    }
    
    // verify the required parameter 'mediaItemId' is set
    if (mediaItemId == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaItemId' when calling mediaPut_0");
    }
    
    // verify the required parameter 'updateMediaItemRequest' is set
    if (updateMediaItemRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateMediaItemRequest' when calling mediaPut_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/media/{id}/items/{mediaItemId}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()))
      .replaceAll("\\{" + "mediaItemId" + "\\}", apiClient.escapeString(mediaItemId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaItem> localVarReturnType = new GenericType<MediaItem>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
