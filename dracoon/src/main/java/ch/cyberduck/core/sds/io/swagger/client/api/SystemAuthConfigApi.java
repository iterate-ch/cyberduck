package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ActiveDirectoryConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.ActiveDirectoryConfigList;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateActiveDirectoryConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateOAuthClientRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateOpenIdIdpConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.OAuthClient;
import ch.cyberduck.core.sds.io.swagger.client.model.OpenIdIdpConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.RadiusConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.RadiusConfigCreateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RadiusConfigUpdateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.TestActiveDirectoryConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.TestActiveDirectoryConfigResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateActiveDirectoryConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateOAuthClientRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateOpenIdIdpConfigRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class SystemAuthConfigApi {
  private ApiClient apiClient;

  public SystemAuthConfigApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemAuthConfigApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create RADIUS configuration
   * ### Functional Description:   Create new RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New RADIUS configuration is created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig create(RadiusConfigCreateRequest body, String xSdsAuthToken) throws ApiException {
    return createWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create RADIUS configuration
   * ### Functional Description:   Create new RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New RADIUS configuration is created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RadiusConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RadiusConfig> createWithHttpInfo(RadiusConfigCreateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling create");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/radius";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Active Directory configuration
   * ### Functional Description: Create a new Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New Active Directory configuration created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig createAdConfig(CreateActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    return createAdConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create Active Directory configuration
   * ### Functional Description: Create a new Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New Active Directory configuration created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ActiveDirectoryConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActiveDirectoryConfig> createAdConfigWithHttpInfo(CreateActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createAdConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/ads";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create OAuth client
   * ### Functional Description: Create a new OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New OAuth client created.  ### &amp;#9432; Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * **authorization_code**   * **implicit**   * **password**   * **client_credentials**   * **refresh_token**    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  If grant types **authorization_code** or **implicit** are used, a redirect URI **MUST** be provided!  Default access token validity: **8 hours**   Default refresh token validity: **30 days**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient createOAuthClient(CreateOAuthClientRequest body, String xSdsAuthToken) throws ApiException {
    return createOAuthClientWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create OAuth client
   * ### Functional Description: Create a new OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New OAuth client created.  ### &amp;#9432; Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * **authorization_code**   * **implicit**   * **password**   * **client_credentials**   * **refresh_token**    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  If grant types **authorization_code** or **implicit** are used, a redirect URI **MUST** be provided!  Default access token validity: **8 hours**   Default refresh token validity: **30 days**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OAuthClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OAuthClient> createOAuthClientWithHttpInfo(CreateOAuthClientRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createOAuthClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/oauth/clients";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create OpenID Connect IDP configuration
   * ### Functional Description: Create new OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New OpenID Connect IDP configuration is created.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   */
  public OpenIdIdpConfig createOpenIdIdpConfig(CreateOpenIdIdpConfigRequest body, String xSdsAuthToken) throws ApiException {
    return createOpenIdIdpConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create OpenID Connect IDP configuration
   * ### Functional Description: Create new OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New OpenID Connect IDP configuration is created.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OpenIdIdpConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OpenIdIdpConfig> createOpenIdIdpConfigWithHttpInfo(CreateOpenIdIdpConfigRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createOpenIdIdpConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/openid/idps";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete Active Directory configuration
   * ### Functional Description: Delete an existing Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: Active Directory configuration removed.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteAdConfig(Integer adId, String xSdsAuthToken) throws ApiException {

    deleteAdConfigWithHttpInfo(adId, xSdsAuthToken);
  }

  /**
   * Delete Active Directory configuration
   * ### Functional Description: Delete an existing Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: Active Directory configuration removed.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteAdConfigWithHttpInfo(Integer adId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling deleteAdConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/ads/{ad_id}"
      .replaceAll("\\{" + "ad_id" + "\\}", apiClient.escapeString(adId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete OAuth client
   * ### Functional Description: Delete an existing OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OAuth client removed.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteOAuthClient(String clientId, String xSdsAuthToken) throws ApiException {

    deleteOAuthClientWithHttpInfo(clientId, xSdsAuthToken);
  }

  /**
   * Delete OAuth client
   * ### Functional Description: Delete an existing OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OAuth client removed.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteOAuthClientWithHttpInfo(String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling deleteOAuthClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/oauth/clients/{client_id}"
      .replaceAll("\\{" + "client_id" + "\\}", apiClient.escapeString(clientId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete OpenID Connect IDP configuration
   * ### Functional Description: Delete an existing OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OpenID Connect IDP configuration removed.  ### &amp;#9432; Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteOpenIdIdpConfig(Integer idpId, String xSdsAuthToken) throws ApiException {

    deleteOpenIdIdpConfigWithHttpInfo(idpId, xSdsAuthToken);
  }

  /**
   * Delete OpenID Connect IDP configuration
   * ### Functional Description: Delete an existing OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OpenID Connect IDP configuration removed.  ### &amp;#9432; Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteOpenIdIdpConfigWithHttpInfo(Integer idpId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'idpId' is set
    if (idpId == null) {
      throw new ApiException(400, "Missing the required parameter 'idpId' when calling deleteOpenIdIdpConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/openid/idps/{idp_id}"
      .replaceAll("\\{" + "idp_id" + "\\}", apiClient.escapeString(idpId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete RADIUS configuration
   * ### Functional Description:   Delete existing RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: RADIUS configuration is deleted.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deteteRadiusConfig(String xSdsAuthToken) throws ApiException {

    deteteRadiusConfigWithHttpInfo(xSdsAuthToken);
  }

  /**
   * Delete RADIUS configuration
   * ### Functional Description:   Delete existing RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: RADIUS configuration is deleted.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deteteRadiusConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/radius";

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get list of Active Directory configurations
   * ### Functional Description:   Retrieve a list of configured Active Directories.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfigList
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfigList getAdConfigs(String xSdsAuthToken) throws ApiException {
    return getAdConfigsWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get list of Active Directory configurations
   * ### Functional Description:   Retrieve a list of configured Active Directories.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ActiveDirectoryConfigList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActiveDirectoryConfigList> getAdConfigsWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/ads";

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ActiveDirectoryConfigList> localVarReturnType = new GenericType<ActiveDirectoryConfigList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Active Directory configuration
   * ### Functional Description:   Retrieve the configuration of an Active Directory.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig getAuthAdSetting(Integer adId, String xSdsAuthToken) throws ApiException {
    return getAuthAdSettingWithHttpInfo(adId, xSdsAuthToken).getData();
      }

  /**
   * Get Active Directory configuration
   * ### Functional Description:   Retrieve the configuration of an Active Directory.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ActiveDirectoryConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActiveDirectoryConfig> getAuthAdSettingWithHttpInfo(Integer adId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling getAuthAdSetting");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/ads/{ad_id}"
      .replaceAll("\\{" + "ad_id" + "\\}", apiClient.escapeString(adId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get OAuth client
   * ### Functional Description:   Retrieve the configuration of an OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient getOAuthClient(String clientId, String xSdsAuthToken) throws ApiException {
    return getOAuthClientWithHttpInfo(clientId, xSdsAuthToken).getData();
      }

  /**
   * Get OAuth client
   * ### Functional Description:   Retrieve the configuration of an OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OAuthClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OAuthClient> getOAuthClientWithHttpInfo(String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling getOAuthClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/oauth/clients/{client_id}"
      .replaceAll("\\{" + "client_id" + "\\}", apiClient.escapeString(clientId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of OAuth clients
   * ### Functional Description:   Retrieve a list of configured OAuth clients.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isStandard:eq:true&#x60;   Get standard OAuth clients.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isStandard&#x60;** | Standard client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;isExternal&#x60;** | (**&#x60;NEW&#x60;**) External client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;isEnabled&#x60;** | (**&#x60;NEW&#x60;**) Enabled/disabled clients filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;clientName:desc&#x60;   Sort by &#x60;clientName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;clientName&#x60;** | Client name | | **&#x60;isStandard&#x60;** | (**&#x60;NEW&#x60;**) Is a standard client | | **&#x60;isExternal&#x60;** | (**&#x60;NEW&#x60;**) Is a external client | | **&#x60;isEnabled&#x60;** | (**&#x60;NEW&#x60;**) Is a enabled client | 
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return List&lt;OAuthClient&gt;
   * @throws ApiException if fails to make API call
   */
  public List<OAuthClient> getOAuthClients(String xSdsAuthToken, String filter, String sort) throws ApiException {
    return getOAuthClientsWithHttpInfo(xSdsAuthToken, filter, sort).getData();
      }

  /**
   * Get list of OAuth clients
   * ### Functional Description:   Retrieve a list of configured OAuth clients.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isStandard:eq:true&#x60;   Get standard OAuth clients.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isStandard&#x60;** | Standard client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;isExternal&#x60;** | (**&#x60;NEW&#x60;**) External client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;isEnabled&#x60;** | (**&#x60;NEW&#x60;**) Enabled/disabled clients filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;clientName:desc&#x60;   Sort by &#x60;clientName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;clientName&#x60;** | Client name | | **&#x60;isStandard&#x60;** | (**&#x60;NEW&#x60;**) Is a standard client | | **&#x60;isExternal&#x60;** | (**&#x60;NEW&#x60;**) Is a external client | | **&#x60;isEnabled&#x60;** | (**&#x60;NEW&#x60;**) Is a enabled client | 
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;List&lt;OAuthClient&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<OAuthClient>> getOAuthClientsWithHttpInfo(String xSdsAuthToken, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/oauth/clients";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<List<OAuthClient>> localVarReturnType = new GenericType<List<OAuthClient>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get OpenID Connect IDP configuration
   * ### Functional Description:   Retrieve an OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   */
  public OpenIdIdpConfig getOpenIdIdpConfig(Integer idpId, String xSdsAuthToken) throws ApiException {
    return getOpenIdIdpConfigWithHttpInfo(idpId, xSdsAuthToken).getData();
      }

  /**
   * Get OpenID Connect IDP configuration
   * ### Functional Description:   Retrieve an OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OpenIdIdpConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OpenIdIdpConfig> getOpenIdIdpConfigWithHttpInfo(Integer idpId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'idpId' is set
    if (idpId == null) {
      throw new ApiException(400, "Missing the required parameter 'idpId' when calling getOpenIdIdpConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/openid/idps/{idp_id}"
      .replaceAll("\\{" + "idp_id" + "\\}", apiClient.escapeString(idpId.toString()));

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of OpenID Connect IDP configurations
   * ### Functional Description:   Retrieve a list of configured OpenID Connect IDPs.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return List&lt;OpenIdIdpConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public List<OpenIdIdpConfig> getOpenIdIdpConfigs(String xSdsAuthToken) throws ApiException {
    return getOpenIdIdpConfigsWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get list of OpenID Connect IDP configurations
   * ### Functional Description:   Retrieve a list of configured OpenID Connect IDPs.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;List&lt;OpenIdIdpConfig&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<OpenIdIdpConfig>> getOpenIdIdpConfigsWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/openid/idps";

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<List<OpenIdIdpConfig>> localVarReturnType = new GenericType<List<OpenIdIdpConfig>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get RADIUS configuration
   * ### Functional Description:   Retrieve a RADIUS configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig getRadiusConfig(String xSdsAuthToken) throws ApiException {
    return getRadiusConfigWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get RADIUS configuration
   * ### Functional Description:   Retrieve a RADIUS configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RadiusConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RadiusConfig> getRadiusConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/radius";

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

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Test Active Directory configuration
   * ### Functional Description:   Test Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: DRACOON tries to establish a connection with the provided information.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return TestActiveDirectoryConfigResponse
   * @throws ApiException if fails to make API call
   */
  public TestActiveDirectoryConfigResponse testAdConfig(TestActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    return testAdConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Test Active Directory configuration
   * ### Functional Description:   Test Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: DRACOON tries to establish a connection with the provided information.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;TestActiveDirectoryConfigResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<TestActiveDirectoryConfigResponse> testAdConfigWithHttpInfo(TestActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling testAdConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/actions/test/ad";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<TestActiveDirectoryConfigResponse> localVarReturnType = new GenericType<TestActiveDirectoryConfigResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Test RADIUS server availability
   * ### Functional Description:   Test RADIUS configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: DRACOON tries to establish a connection with the provided information.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void testRadiusConfig(String xSdsAuthToken) throws ApiException {

    testRadiusConfigWithHttpInfo(xSdsAuthToken);
  }

  /**
   * Test RADIUS server availability
   * ### Functional Description:   Test RADIUS configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: DRACOON tries to establish a connection with the provided information.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> testRadiusConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/actions/test/radius";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update Active Directory configuration
   * ### Functional Description:   Update an existing Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: Active Directory configuration updated.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig updateAuthAdSetting(Integer adId, UpdateActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    return updateAuthAdSettingWithHttpInfo(adId, body, xSdsAuthToken).getData();
      }

  /**
   * Update Active Directory configuration
   * ### Functional Description:   Update an existing Active Directory configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: Active Directory configuration updated.  ### &amp;#9432; Further Information: None.
   * @param adId Active Directory ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ActiveDirectoryConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActiveDirectoryConfig> updateAuthAdSettingWithHttpInfo(Integer adId, UpdateActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling updateAuthAdSetting");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateAuthAdSetting");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/ads/{ad_id}"
      .replaceAll("\\{" + "ad_id" + "\\}", apiClient.escapeString(adId.toString()));

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update OAuth client
   * ### Functional Description:   Update an existing OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OAuth client updated.  ### &amp;#9432; Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * **authorization_code**   * **implicit**   * **password**   * **client_credentials**   * **refresh_token**    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  If grant types **authorization_code** or **implicit** are used, a redirect URI **MUST** be provided! 
   * @param body body (required)
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient updateOAuthClient(UpdateOAuthClientRequest body, String clientId, String xSdsAuthToken) throws ApiException {
    return updateOAuthClientWithHttpInfo(body, clientId, xSdsAuthToken).getData();
      }

  /**
   * Update OAuth client
   * ### Functional Description:   Update an existing OAuth client.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OAuth client updated.  ### &amp;#9432; Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * **authorization_code**   * **implicit**   * **password**   * **client_credentials**   * **refresh_token**    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  If grant types **authorization_code** or **implicit** are used, a redirect URI **MUST** be provided! 
   * @param body body (required)
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OAuthClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OAuthClient> updateOAuthClientWithHttpInfo(UpdateOAuthClientRequest body, String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateOAuthClient");
    }
    
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling updateOAuthClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/oauth/clients/{client_id}"
      .replaceAll("\\{" + "client_id" + "\\}", apiClient.escapeString(clientId.toString()));

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update OpenID Connect IDP configuration
   * ### Functional Description:   Update an existing OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OpenID Connect IDP configuration is updated.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param body body (required)
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   */
  public OpenIdIdpConfig updateOpenIdIdpConfig(UpdateOpenIdIdpConfigRequest body, Integer idpId, String xSdsAuthToken) throws ApiException {
    return updateOpenIdIdpConfigWithHttpInfo(body, idpId, xSdsAuthToken).getData();
      }

  /**
   * Update OpenID Connect IDP configuration
   * ### Functional Description:   Update an existing OpenID Connect IDP configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: OpenID Connect IDP configuration is updated.  ### &amp;#9432; Further Information: See [http://openid.net/developers/specs](http://openid.net/developers/specs) for further information.
   * @param body body (required)
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;OpenIdIdpConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OpenIdIdpConfig> updateOpenIdIdpConfigWithHttpInfo(UpdateOpenIdIdpConfigRequest body, Integer idpId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateOpenIdIdpConfig");
    }
    
    // verify the required parameter 'idpId' is set
    if (idpId == null) {
      throw new ApiException(400, "Missing the required parameter 'idpId' when calling updateOpenIdIdpConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/openid/idps/{idp_id}"
      .replaceAll("\\{" + "idp_id" + "\\}", apiClient.escapeString(idpId.toString()));

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update RADIUS configuration
   * ### Functional Description:   Update existing RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: RADIUS configuration is updated.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig updateRadiusConfig(RadiusConfigUpdateRequest body, String xSdsAuthToken) throws ApiException {
    return updateRadiusConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Update RADIUS configuration
   * ### Functional Description:   Update existing RADIUS configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: RADIUS configuration is updated.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RadiusConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RadiusConfig> updateRadiusConfigWithHttpInfo(RadiusConfigUpdateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateRadiusConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/auth/radius";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
