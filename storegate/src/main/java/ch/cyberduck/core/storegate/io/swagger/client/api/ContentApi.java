package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.ContentList;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaContentList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
public class ContentApi {
  private ApiClient apiClient;

  public ContentApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ContentApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get website content locale is determined by user settings
   * 
   * @return ContentList
   * @throws ApiException if fails to make API call
   */
  public ContentList contentGetContent() throws ApiException {
    return contentGetContentWithHttpInfo().getData();
      }

  /**
   * Get website content locale is determined by user settings
   * 
   * @return ApiResponse&lt;ContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ContentList> contentGetContentWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content";

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

    GenericType<ContentList> localVarReturnType = new GenericType<ContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get website content locale is determined by user settings
   * 
   * @return MediaContentList
   * @throws ApiException if fails to make API call
   */
  public MediaContentList contentGetMedia() throws ApiException {
    return contentGetMediaWithHttpInfo().getData();
      }

  /**
   * Get website content locale is determined by user settings
   * 
   * @return ApiResponse&lt;MediaContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaContentList> contentGetMediaWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content/media";

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

    GenericType<MediaContentList> localVarReturnType = new GenericType<MediaContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get public content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ContentList
   * @throws ApiException if fails to make API call
   */
  public ContentList contentGetPublicContent(String partnerId, String retailerId) throws ApiException {
    return contentGetPublicContentWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get public content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;ContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ContentList> contentGetPublicContentWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content/public";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "partnerId", partnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "retailerId", retailerId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ContentList> localVarReturnType = new GenericType<ContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get public content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return MediaContentList
   * @throws ApiException if fails to make API call
   */
  public MediaContentList contentGetPublicMedia(String partnerId, String retailerId) throws ApiException {
    return contentGetPublicMediaWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get public content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;MediaContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaContentList> contentGetPublicMediaWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content/public/media";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "partnerId", partnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "retailerId", retailerId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaContentList> localVarReturnType = new GenericType<MediaContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get share content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ContentList
   * @throws ApiException if fails to make API call
   */
  public ContentList contentGetShareContent(String partnerId, String retailerId) throws ApiException {
    return contentGetShareContentWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get share content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;ContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ContentList> contentGetShareContentWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content/share";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "partnerId", partnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "retailerId", retailerId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ContentList> localVarReturnType = new GenericType<ContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get share content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return MediaContentList
   * @throws ApiException if fails to make API call
   */
  public MediaContentList contentGetShareMedia(String partnerId, String retailerId) throws ApiException {
    return contentGetShareMediaWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get share content, locale is determined by browser Accept-Language header
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;MediaContentList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaContentList> contentGetShareMediaWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/content/share/media";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "partnerId", partnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "retailerId", retailerId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaContentList> localVarReturnType = new GenericType<MediaContentList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
