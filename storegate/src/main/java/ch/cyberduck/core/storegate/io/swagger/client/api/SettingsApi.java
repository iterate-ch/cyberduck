package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.Branding;
import ch.cyberduck.core.storegate.io.swagger.client.model.ModelConfiguration;
import ch.cyberduck.core.storegate.io.swagger.client.model.PublicConfiguration;
import ch.cyberduck.core.storegate.io.swagger.client.model.PublicWebUrls;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;
import ch.cyberduck.core.storegate.io.swagger.client.model.SubscriptionInfo;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateConfiguration;
import ch.cyberduck.core.storegate.io.swagger.client.model.UserStorage;
import ch.cyberduck.core.storegate.io.swagger.client.model.WebUrls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class SettingsApi {
  private ApiClient apiClient;

  public SettingsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SettingsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get branding if user has it.
   * 
   * @return Branding
   * @throws ApiException if fails to make API call
   */
  public Branding settingsGetBranding() throws ApiException {
    return settingsGetBrandingWithHttpInfo().getData();
      }

  /**
   * Get branding if user has it.
   * 
   * @return ApiResponse&lt;Branding&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Branding> settingsGetBrandingWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/branding";

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

    GenericType<Branding> localVarReturnType = new GenericType<Branding>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get settings for turning features on or off in the client.
   * 
   * @return ModelConfiguration
   * @throws ApiException if fails to make API call
   */
  public ModelConfiguration settingsGetConfiguration() throws ApiException {
    return settingsGetConfigurationWithHttpInfo().getData();
      }

  /**
   * Get settings for turning features on or off in the client.
   * 
   * @return ApiResponse&lt;ModelConfiguration&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ModelConfiguration> settingsGetConfigurationWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/configuration";

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

    GenericType<ModelConfiguration> localVarReturnType = new GenericType<ModelConfiguration>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get settings for turning features on or off in the client.
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return PublicConfiguration
   * @throws ApiException if fails to make API call
   */
  public PublicConfiguration settingsGetPublicConfiguration(String partnerId, String retailerId) throws ApiException {
    return settingsGetPublicConfigurationWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get settings for turning features on or off in the client.
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;PublicConfiguration&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicConfiguration> settingsGetPublicConfigurationWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/public/configuration";

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

    GenericType<PublicConfiguration> localVarReturnType = new GenericType<PublicConfiguration>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get a list or urls to use
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return PublicWebUrls
   * @throws ApiException if fails to make API call
   */
  public PublicWebUrls settingsGetPublicWebUrls(String partnerId, String retailerId) throws ApiException {
    return settingsGetPublicWebUrlsWithHttpInfo(partnerId, retailerId).getData();
      }

  /**
   * Get a list or urls to use
   * 
   * @param partnerId  (optional)
   * @param retailerId  (optional)
   * @return ApiResponse&lt;PublicWebUrls&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicWebUrls> settingsGetPublicWebUrlsWithHttpInfo(String partnerId, String retailerId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/public/weburls";

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

    GenericType<PublicWebUrls> localVarReturnType = new GenericType<PublicWebUrls>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get the list of root folders on the account
   * 
   * @return List&lt;RootFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public List<RootFolder> settingsGetRootfolders() throws ApiException {
    return settingsGetRootfoldersWithHttpInfo().getData();
      }

  /**
   * Get the list of root folders on the account
   * 
   * @return ApiResponse&lt;List&lt;RootFolder&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<RootFolder>> settingsGetRootfoldersWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/rootfolders";

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

    GenericType<List<RootFolder>> localVarReturnType = new GenericType<List<RootFolder>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get storage information
   * 
   * @return UserStorage
   * @throws ApiException if fails to make API call
   */
  public UserStorage settingsGetStorageInfo() throws ApiException {
    return settingsGetStorageInfoWithHttpInfo().getData();
      }

  /**
   * Get storage information
   * 
   * @return ApiResponse&lt;UserStorage&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserStorage> settingsGetStorageInfoWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/storage";

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

    GenericType<UserStorage> localVarReturnType = new GenericType<UserStorage>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get subscriptionInfo
   * 
   * @return SubscriptionInfo
   * @throws ApiException if fails to make API call
   */
  public SubscriptionInfo settingsGetSubscriptionInfo() throws ApiException {
    return settingsGetSubscriptionInfoWithHttpInfo().getData();
      }

  /**
   * Get subscriptionInfo
   * 
   * @return ApiResponse&lt;SubscriptionInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SubscriptionInfo> settingsGetSubscriptionInfoWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/subscription";

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

    GenericType<SubscriptionInfo> localVarReturnType = new GenericType<SubscriptionInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get a list or urls to use
   * 
   * @return WebUrls
   * @throws ApiException if fails to make API call
   */
  public WebUrls settingsGetWebUrls() throws ApiException {
    return settingsGetWebUrlsWithHttpInfo().getData();
      }

  /**
   * Get a list or urls to use
   * 
   * @return ApiResponse&lt;WebUrls&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebUrls> settingsGetWebUrlsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/weburls";

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

    GenericType<WebUrls> localVarReturnType = new GenericType<WebUrls>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update settings, only included parameters will be updated.
   * 
   * @param updateConfiguration  (required)
   * @throws ApiException if fails to make API call
   */
  public void settingsUpdateSettings(UpdateConfiguration updateConfiguration) throws ApiException {

    settingsUpdateSettingsWithHttpInfo(updateConfiguration);
  }

  /**
   * Update settings, only included parameters will be updated.
   * 
   * @param updateConfiguration  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> settingsUpdateSettingsWithHttpInfo(UpdateConfiguration updateConfiguration) throws ApiException {
    Object localVarPostBody = updateConfiguration;
    
    // verify the required parameter 'updateConfiguration' is set
    if (updateConfiguration == null) {
      throw new ApiException(400, "Missing the required parameter 'updateConfiguration' when calling settingsUpdateSettings");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/settings/configuration";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
