package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateMediaShareRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaShare;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateShareRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class MediaSharesApi {
  private ApiClient apiClient;

  public MediaSharesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MediaSharesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete the media share.
   * 
   * @param id The media share id (required)
   * @throws ApiException if fails to make API call
   */
  public void mediaSharesDelete(String id) throws ApiException {

    mediaSharesDeleteWithHttpInfo(id);
  }

  /**
   * Delete the media share.
   * 
   * @param id The media share id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> mediaSharesDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaSharesDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares/{id}"
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
   * Returns a MediaShare with share information and album.
   * 
   * @param id The Share Id (required)
   * @return MediaShare
   * @throws ApiException if fails to make API call
   */
  public MediaShare mediaSharesGetById(String id) throws ApiException {
    return mediaSharesGetByIdWithHttpInfo(id).getData();
      }

  /**
   * Returns a MediaShare with share information and album.
   * 
   * @param id The Share Id (required)
   * @return ApiResponse&lt;MediaShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaShare> mediaSharesGetByIdWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaSharesGetById");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares/{id}"
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

    GenericType<MediaShare> localVarReturnType = new GenericType<MediaShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Returns a MediaShare with share information and album.
   * 
   * @param id The MediaFolder Id (required)
   * @return MediaShare
   * @throws ApiException if fails to make API call
   */
  public MediaShare mediaSharesGetByMediaId(String id) throws ApiException {
    return mediaSharesGetByMediaIdWithHttpInfo(id).getData();
      }

  /**
   * Returns a MediaShare with share information and album.
   * 
   * @param id The MediaFolder Id (required)
   * @return ApiResponse&lt;MediaShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaShare> mediaSharesGetByMediaIdWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaSharesGetByMediaId");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares/mediaid/{id}"
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

    GenericType<MediaShare> localVarReturnType = new GenericType<MediaShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new media share without settings.
   * 
   * @param id The FileId (required)
   * @return MediaShare
   * @throws ApiException if fails to make API call
   */
  public MediaShare mediaSharesPost(String id) throws ApiException {
    return mediaSharesPostWithHttpInfo(id).getData();
      }

  /**
   * Create a new media share without settings.
   * 
   * @param id The FileId (required)
   * @return ApiResponse&lt;MediaShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaShare> mediaSharesPostWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaSharesPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares/mediaid/{id}"
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

    GenericType<MediaShare> localVarReturnType = new GenericType<MediaShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new media share with settings.
   * 
   * @param createMediaShareRequest The parameters (required)
   * @return MediaShare
   * @throws ApiException if fails to make API call
   */
  public MediaShare mediaSharesPost_0(CreateMediaShareRequest createMediaShareRequest) throws ApiException {
    return mediaSharesPost_0WithHttpInfo(createMediaShareRequest).getData();
      }

  /**
   * Create a new media share with settings.
   * 
   * @param createMediaShareRequest The parameters (required)
   * @return ApiResponse&lt;MediaShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaShare> mediaSharesPost_0WithHttpInfo(CreateMediaShareRequest createMediaShareRequest) throws ApiException {
    Object localVarPostBody = createMediaShareRequest;
    
    // verify the required parameter 'createMediaShareRequest' is set
    if (createMediaShareRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createMediaShareRequest' when calling mediaSharesPost_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares";

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

    GenericType<MediaShare> localVarReturnType = new GenericType<MediaShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update the media share.
   * 
   * @param id The media share id (required)
   * @param updateMediaShareRequest The parameters (required)
   * @return MediaShare
   * @throws ApiException if fails to make API call
   */
  public MediaShare mediaSharesPut(String id, UpdateShareRequest updateMediaShareRequest) throws ApiException {
    return mediaSharesPutWithHttpInfo(id, updateMediaShareRequest).getData();
      }

  /**
   * Update the media share.
   * 
   * @param id The media share id (required)
   * @param updateMediaShareRequest The parameters (required)
   * @return ApiResponse&lt;MediaShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaShare> mediaSharesPutWithHttpInfo(String id, UpdateShareRequest updateMediaShareRequest) throws ApiException {
    Object localVarPostBody = updateMediaShareRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling mediaSharesPut");
    }
    
    // verify the required parameter 'updateMediaShareRequest' is set
    if (updateMediaShareRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateMediaShareRequest' when calling mediaSharesPut");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/mediashares/{id}"
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

    GenericType<MediaShare> localVarReturnType = new GenericType<MediaShare>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
