package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ActiveDirectoryConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.ActiveDirectoryConfigList;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateActiveDirectoryConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateOAuthClientRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateOpenIdIdpConfigRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.InlineResponse400;
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
   * Create Active Directory configuration
   * ### Description: Create a new Active Directory configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: New Active Directory configuration created.  ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig createAdConfig(CreateActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create OAuth client
   * ### Description: Create a new OAuth client.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: New OAuth client created.  ### Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * &#x60;authorization_code&#x60;   * &#x60;implicit&#x60;   * &#x60;password&#x60;   * &#x60;client_credentials&#x60;   * &#x60;refresh_token&#x60;    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  Allowed characters for client ID are: &#x60;[a-zA-Z0-9_-]&#x60;  If grant types &#x60;authorization_code&#x60; or &#x60;implicit&#x60; are used, a redirect URI **MUST** be provided!  Default access token validity: **8 hours**   Default refresh token validity: **30 days** Default approval validity: **½ year**
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient createOAuthClient(CreateOAuthClientRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create OpenID Connect IDP configuration
   * ### Description: Create new OpenID Connect IDP configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: New OpenID Connect IDP configuration is created.  ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Create OpenID Connect IDP configuration Documentation</a>
   */
  public OpenIdIdpConfig createOpenIdIdpConfig(CreateOpenIdIdpConfigRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create RADIUS configuration
   * ### Description:   Create new RADIUS configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: New RADIUS configuration is created.  ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig createRadiusConfig(RadiusConfigCreateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createRadiusConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Remove Active Directory configuration
   * ### Description: Delete an existing Active Directory configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Active Directory configuration is removed.  ### Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeAdConfig(Integer adId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling removeAdConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove OAuth client
   * ### Description: Delete an existing OAuth client.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OAuth client is removed.  ### Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeOAuthClient(String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling removeOAuthClient");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove OpenID Connect IDP configuration
   * ### Description: Delete an existing OpenID Connect IDP configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OpenID Connect IDP configuration is removed.  ### Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Remove OpenID Connect IDP configuration Documentation</a>
   */
  public void removeOpenIdIdpConfig(Integer idpId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'idpId' is set
    if (idpId == null) {
      throw new ApiException(400, "Missing the required parameter 'idpId' when calling removeOpenIdIdpConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove RADIUS configuration
   * ### Description:   Delete existing RADIUS configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: RADIUS configuration is deleted.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeRadiusConfig(String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request Active Directory configuration
   * ### Description:   Retrieve the configuration of an Active Directory.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Active Directory configuration is returned.  ### Further Information: None.
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig requestAdConfig(Integer adId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling requestAdConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of Active Directory configurations
   * ### Description:   Retrieve a list of configured Active Directories.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of Active Directory configurations is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfigList
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfigList requestAdConfigs(String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ActiveDirectoryConfigList> localVarReturnType = new GenericType<ActiveDirectoryConfigList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request OAuth client
   * ### Description:   Retrieve the configuration of an OAuth client.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OAuth client is returned.  ### Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient requestOAuthClient(String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling requestOAuthClient");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of OAuth clients
   * ### Description:   Retrieve a list of configured OAuth clients.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of OAuth clients is returned.  ### Further Information:  ### Filtering: All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;isStandard:eq:true&#x60;   Get standard OAuth clients.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;isStandard&#x60; | Standard client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | &#x60;isExternal&#x60; | External client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | &#x60;isEnabled&#x60; | Enabled/disabled clients filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort criteria are possible.   Fields are connected via logical conjunction **AND**.  &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;clientName:desc|isStandard:asc&#x60;   Sort by &#x60;clientName&#x60; descending **AND** &#x60;isStandard&#x60; ascending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;clientName&#x60; | Client name | | &#x60;isStandard&#x60; | Is a standard client | | &#x60;isExternal&#x60; | Is a external client | | &#x60;isEnabled&#x60; | Is a enabled client |  &lt;/details&gt;
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return List&lt;OAuthClient&gt;
   * @throws ApiException if fails to make API call
   */
  public List<OAuthClient> requestOAuthClients(String filter, String sort, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<OAuthClient>> localVarReturnType = new GenericType<List<OAuthClient>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request OpenID Connect IDP configuration
   * ### Description:   Retrieve an OpenID Connect IDP configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OpenID Connect IDP configuration is returned.  ### Further Information: None.
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Request OpenID Connect IDP configuration Documentation</a>
   */
  public OpenIdIdpConfig requestOpenIdIdpConfig(Integer idpId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'idpId' is set
    if (idpId == null) {
      throw new ApiException(400, "Missing the required parameter 'idpId' when calling requestOpenIdIdpConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of OpenID Connect IDP configurations
   * ### Description:   Retrieve a list of configured OpenID Connect IDPs.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: List of OpenID Connect IDP configurations is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return List&lt;OpenIdIdpConfig&gt;
   * @throws ApiException if fails to make API call
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Request list of OpenID Connect IDP configurations Documentation</a>
   */
  public List<OpenIdIdpConfig> requestOpenIdIdpConfigs(String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<OpenIdIdpConfig>> localVarReturnType = new GenericType<List<OpenIdIdpConfig>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request RADIUS configuration
   * ### Description:   Retrieve a RADIUS configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: RADIUS configuration is returned.  ### Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig requestRadiusConfig(String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Test Active Directory configuration
   * ### Description:   Test Active Directory configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Active Directory configuration is returned if successful.  ### Further Information: DRACOON tries to establish a connection with the provided information.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return TestActiveDirectoryConfigResponse
   * @throws ApiException if fails to make API call
   */
  public TestActiveDirectoryConfigResponse testAdConfig(TestActiveDirectoryConfigRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<TestActiveDirectoryConfigResponse> localVarReturnType = new GenericType<TestActiveDirectoryConfigResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Test RADIUS server availability
   * ### Description:   Test RADIUS configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: RADIUS configuration is returned if successful.  ### Further Information: DRACOON tries to establish a connection with the provided information.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void testRadiusConfig(String xSdsAuthToken) throws ApiException {
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
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update Active Directory configuration
   * ### Description:   Update an existing Active Directory configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; of the Provider Customer required.  ### Postcondition: Active Directory configuration updated.  ### Further Information: None.
   * @param body  (required)
   * @param adId Active Directory ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ActiveDirectoryConfig
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryConfig updateAdConfig(UpdateActiveDirectoryConfigRequest body, Integer adId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateAdConfig");
    }
    // verify the required parameter 'adId' is set
    if (adId == null) {
      throw new ApiException(400, "Missing the required parameter 'adId' when calling updateAdConfig");
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ActiveDirectoryConfig> localVarReturnType = new GenericType<ActiveDirectoryConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update OAuth client
   * ### Description:   Update an existing OAuth client.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OAuth client updated.  ### Further Information:   Client secret **MUST** have:   * at least 12 characters, at most 32 characters   * only lower case characters, upper case characters and digits   * at least 1 lower case character, 1 upper case character and 1 digit    The client secret is optional and will be generated if it is left empty.    Valid grant types are:   * &#x60;authorization_code&#x60;   * &#x60;implicit&#x60;   * &#x60;password&#x60;   * &#x60;client_credentials&#x60;   * &#x60;refresh_token&#x60;    Grant type &#x60;client_credentials&#x60; is currently **NOT** permitted!  If grant types &#x60;authorization_code&#x60; or &#x60;implicit&#x60; are used, a redirect URI **MUST** be provided! 
   * @param body  (required)
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OAuthClient
   * @throws ApiException if fails to make API call
   */
  public OAuthClient updateOAuthClient(UpdateOAuthClientRequest body, String clientId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OAuthClient> localVarReturnType = new GenericType<OAuthClient>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update OpenID Connect IDP configuration
   * ### Description:   Update an existing OpenID Connect IDP configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: OpenID Connect IDP configuration is updated.  ### Further Information: None.
   * @param body  (required)
   * @param idpId OpenID Connect IDP configuration ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return OpenIdIdpConfig
   * @throws ApiException if fails to make API call
   * OpenID Specifications
   * @see <a href="http://openid.net/developers/specs">Update OpenID Connect IDP configuration Documentation</a>
   */
  public OpenIdIdpConfig updateOpenIdIdpConfig(UpdateOpenIdIdpConfigRequest body, Integer idpId, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<OpenIdIdpConfig> localVarReturnType = new GenericType<OpenIdIdpConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update RADIUS configuration
   * ### Description:   Update existing RADIUS configuration.  ### Precondition: Right &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; change global config&lt;/span&gt; and role &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128100; Config Manager&lt;/span&gt; of the Provider Customer required.  ### Postcondition: RADIUS configuration is updated.  ### Further Information: None.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RadiusConfig
   * @throws ApiException if fails to make API call
   */
  public RadiusConfig updateRadiusConfig(RadiusConfigUpdateRequest body, String xSdsAuthToken) throws ApiException {
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RadiusConfig> localVarReturnType = new GenericType<RadiusConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
