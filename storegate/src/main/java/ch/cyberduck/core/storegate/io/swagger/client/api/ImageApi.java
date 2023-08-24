package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class ImageApi {
  private ApiClient apiClient;

  public ImageApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ImageApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get thumbnail for file, if image.
   * 
   * @param id The file id (required)
   * @param version File version (optional)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @param quality Thumbnail quality (90 is default) (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String imageGetImageFile(String id, Integer version, Integer width, Integer height, Integer rotate, Integer quality) throws ApiException {
    return imageGetImageFileWithHttpInfo(id, version, width, height, rotate, quality).getData();
      }

  /**
   * Get thumbnail for file, if image.
   * 
   * @param id The file id (required)
   * @param version File version (optional)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @param quality Thumbnail quality (90 is default) (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> imageGetImageFileWithHttpInfo(String id, Integer version, Integer width, Integer height, Integer rotate, Integer quality) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling imageGetImageFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/images/fileid/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rotate", rotate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "quality", quality));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get thumbnail for file in share, if image.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String imageGetImageFileShare(String shareid, String id, Integer width, Integer height, Integer rotate) throws ApiException {
    return imageGetImageFileShareWithHttpInfo(shareid, id, width, height, rotate).getData();
      }

  /**
   * Get thumbnail for file in share, if image.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> imageGetImageFileShareWithHttpInfo(String shareid, String id, Integer width, Integer height, Integer rotate) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling imageGetImageFileShare");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling imageGetImageFileShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/images/shares/{shareid}/fileid/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rotate", rotate));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get thumbnail for media file (image)
   * 
   * @param mediaid The media id (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String imageGetImageMedia(String mediaid, Integer width, Integer height, Integer rotate) throws ApiException {
    return imageGetImageMediaWithHttpInfo(mediaid, width, height, rotate).getData();
      }

  /**
   * Get thumbnail for media file (image)
   * 
   * @param mediaid The media id (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> imageGetImageMediaWithHttpInfo(String mediaid, Integer width, Integer height, Integer rotate) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling imageGetImageMedia");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/images/mediaid/{mediaid}"
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rotate", rotate));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get thumbnail for media file (image) in share
   * 
   * @param shareid The share id (required)
   * @param mediaid File version (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String imageGetImageMediaShare(String shareid, String mediaid, Integer width, Integer height, Integer rotate) throws ApiException {
    return imageGetImageMediaShareWithHttpInfo(shareid, mediaid, width, height, rotate).getData();
      }

  /**
   * Get thumbnail for media file (image) in share
   * 
   * @param shareid The share id (required)
   * @param mediaid File version (required)
   * @param width Thumbnail width (optional)
   * @param height Thumbnail height (optional)
   * @param rotate Thumbnail rotation (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> imageGetImageMediaShareWithHttpInfo(String shareid, String mediaid, Integer width, Integer height, Integer rotate) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling imageGetImageMediaShare");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling imageGetImageMediaShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/images/shares/{shareid}/mediaid/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rotate", rotate));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
