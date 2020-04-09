package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.AttributesResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Avatar;
import ch.cyberduck.core.sds.io.swagger.client.model.ChangeUserPasswordRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerData;
import ch.cyberduck.core.sds.io.swagger.client.model.EnableCustomerEncryptionRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import java.io.File;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationConfigChangeRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationConfigList;
import ch.cyberduck.core.sds.io.swagger.client.model.OAuthAuthorization;
import ch.cyberduck.core.sds.io.swagger.client.model.PasswordPolicyViolationResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ProfileAttributes;
import ch.cyberduck.core.sds.io.swagger.client.model.ProfileAttributesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.SubscribedDownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.SubscribedDownloadShareList;
import ch.cyberduck.core.sds.io.swagger.client.model.SubscribedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.SubscribedNodeList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateUserAccountRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAccount;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class UserApi {
  private ApiClient apiClient;

  public UserApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UserApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Change avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description: Change the avatar.  ### Precondition: Authenticated user.  ### Effects: Avatar is changed.  ### &amp;#9432; Further Information: * Media type **MUST** be **&#x60;jpeg&#x60;** or **&#x60;png&#x60;** * File size **MUST** bei less than **&#x60;5 MB&#x60;** * Dimensions **MUST** be **&#x60;256x256 px&#x60;**
   * @param file File (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return Avatar
   * @throws ApiException if fails to make API call
   */
  public Avatar changeAvatar(File file, String xSdsAuthToken) throws ApiException {
    return changeAvatarWithHttpInfo(file, xSdsAuthToken).getData();
      }

  /**
   * Change avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description: Change the avatar.  ### Precondition: Authenticated user.  ### Effects: Avatar is changed.  ### &amp;#9432; Further Information: * Media type **MUST** be **&#x60;jpeg&#x60;** or **&#x60;png&#x60;** * File size **MUST** bei less than **&#x60;5 MB&#x60;** * Dimensions **MUST** be **&#x60;256x256 px&#x60;**
   * @param file File (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;Avatar&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Avatar> changeAvatarWithHttpInfo(File file, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling changeAvatar");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/account/avatar";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Avatar> localVarReturnType = new GenericType<Avatar>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Change user&#39;s password
   * ### Functional Description: Change the user&#39;s password.  ### Precondition: Authenticated user.  ### Effects: User&#39;s password is changed.  ### &amp;#9432; Further Information: The password **MUST** comply to configured password policies.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void changeUserPassword(ChangeUserPasswordRequest body, String xSdsAuthToken) throws ApiException {

    changeUserPasswordWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Change user&#39;s password
   * ### Functional Description: Change the user&#39;s password.  ### Precondition: Authenticated user.  ### Effects: User&#39;s password is changed.  ### &amp;#9432; Further Information: The password **MUST** comply to configured password policies.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> changeUserPasswordWithHttpInfo(ChangeUserPasswordRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling changeUserPassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/account/password";

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


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Reset avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description:   Reset (custom) avatar to default avatar.  ### Precondition: Authenticated user.  ### Effects: - User&#39;s avatar gets deleted.   - Default avatar is set.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return Avatar
   * @throws ApiException if fails to make API call
   */
  public Avatar deleteAvatar(String xSdsAuthToken) throws ApiException {
    return deleteAvatarWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Reset avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description:   Reset (custom) avatar to default avatar.  ### Precondition: Authenticated user.  ### Effects: - User&#39;s avatar gets deleted.   - Default avatar is set.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;Avatar&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Avatar> deleteAvatarWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/avatar";

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

    GenericType<Avatar> localVarReturnType = new GenericType<Avatar>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete a OAuth authorization
   * ### &amp;#128640; Since version 4.12.0  ### Functional Description: Delete a authorization.  ### Precondition: * Authenticated user * Valid client ID * Valid authorization ID  ### Effects: Authorization is revoked.  ### &amp;#9432; Further Information: None.
   * @param authorizationId OAuth authorization ID (required)
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteOAuthAuthorization(Long authorizationId, String clientId, String xSdsAuthToken) throws ApiException {

    deleteOAuthAuthorizationWithHttpInfo(authorizationId, clientId, xSdsAuthToken);
  }

  /**
   * Delete a OAuth authorization
   * ### &amp;#128640; Since version 4.12.0  ### Functional Description: Delete a authorization.  ### Precondition: * Authenticated user * Valid client ID * Valid authorization ID  ### Effects: Authorization is revoked.  ### &amp;#9432; Further Information: None.
   * @param authorizationId OAuth authorization ID (required)
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteOAuthAuthorizationWithHttpInfo(Long authorizationId, String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'authorizationId' is set
    if (authorizationId == null) {
      throw new ApiException(400, "Missing the required parameter 'authorizationId' when calling deleteOAuthAuthorization");
    }
    
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling deleteOAuthAuthorization");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/oauth/authorizations/{client_id}/{authorization_id}"
      .replaceAll("\\{" + "authorization_id" + "\\}", apiClient.escapeString(authorizationId.toString()))
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
   * Delete all OAuth authorizations of a client
   * ### Functional Description: Delete all authorizations of a client.  ### Precondition: * Authenticated user * Valid client ID  ### Effects: All authorizations for the client are revoked.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteOAuthAuthorizations(String clientId, String xSdsAuthToken) throws ApiException {

    deleteOAuthAuthorizationsWithHttpInfo(clientId, xSdsAuthToken);
  }

  /**
   * Delete all OAuth authorizations of a client
   * ### Functional Description: Delete all authorizations of a client.  ### Precondition: * Authenticated user * Valid client ID  ### Effects: All authorizations for the client are revoked.  ### &amp;#9432; Further Information: None.
   * @param clientId OAuth client ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteOAuthAuthorizationsWithHttpInfo(String clientId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'clientId' is set
    if (clientId == null) {
      throw new ApiException(400, "Missing the required parameter 'clientId' when calling deleteOAuthAuthorizations");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/oauth/authorizations/{client_id}"
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
   * Delete user profile attribute
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Delete custom user profile attribute.  ### Precondition: None.  ### Effects: Custom user profile attribute gets deleted.  ### &amp;#9432; Further Information: None.
   * @param key Key (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteProfileAttribute(String key, String xSdsAuthToken) throws ApiException {

    deleteProfileAttributeWithHttpInfo(key, xSdsAuthToken);
  }

  /**
   * Delete user profile attribute
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Delete custom user profile attribute.  ### Precondition: None.  ### Effects: Custom user profile attribute gets deleted.  ### &amp;#9432; Further Information: None.
   * @param key Key (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteProfileAttributeWithHttpInfo(String key, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'key' is set
    if (key == null) {
      throw new ApiException(400, "Missing the required parameter 'key' when calling deleteProfileAttribute");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/profileAttributes/{key}"
      .replaceAll("\\{" + "key" + "\\}", apiClient.escapeString(key.toString()));

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
   * Delete user&#39;s key pair
   * ### Functional Description:   Delete the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: This will also remove all file keys that were encrypted with the user&#39;s public key.   If the user had exclusive access to some files, those are removed as well since decrypting them became impossible.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteUserKeyPair(String xSdsAuthToken) throws ApiException {

    deleteUserKeyPairWithHttpInfo(xSdsAuthToken);
  }

  /**
   * Delete user&#39;s key pair
   * ### Functional Description:   Delete the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: This will also remove all file keys that were encrypted with the user&#39;s public key.   If the user had exclusive access to some files, those are removed as well since decrypting them became impossible.
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteUserKeyPairWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/keypair";

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
   * Activate client-side encryption for customer
   * ### Functional Description:   Activate client-side encryption for according customer.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Client-side encryption is enabled.  ### &amp;#9432; Further Information: Sets the ability for this customer to encrypt rooms.   Once enabled on customer level, it **CANNOT** be unset.   On activation, a customer rescue keypair **MUST** be set.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CustomerData
   * @throws ApiException if fails to make API call
   */
  public CustomerData enableCustomerEncryption(EnableCustomerEncryptionRequest body, String xSdsAuthToken) throws ApiException {
    return enableCustomerEncryptionWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Activate client-side encryption for customer
   * ### Functional Description:   Activate client-side encryption for according customer.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Client-side encryption is enabled.  ### &amp;#9432; Further Information: Sets the ability for this customer to encrypt rooms.   Once enabled on customer level, it **CANNOT** be unset.   On activation, a customer rescue keypair **MUST** be set.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;CustomerData&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CustomerData> enableCustomerEncryptionWithHttpInfo(EnableCustomerEncryptionRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling enableCustomerEncryption");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/account/customer";

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

    GenericType<CustomerData> localVarReturnType = new GenericType<CustomerData>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description: Get the avatar.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return Avatar
   * @throws ApiException if fails to make API call
   */
  public Avatar getAvatar(String xSdsAuthToken) throws ApiException {
    return getAvatarWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description: Get the avatar.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;Avatar&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Avatar> getAvatarWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/avatar";

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

    GenericType<Avatar> localVarReturnType = new GenericType<Avatar>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get customer information for user
   * ### Functional Description:   Use this API to get:  * customer name * used / free space * used / available * user account info  of the according customer.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return CustomerData
   * @throws ApiException if fails to make API call
   */
  public CustomerData getCustomerInfo(String xSdsAuthToken) throws ApiException {
    return getCustomerInfoWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get customer information for user
   * ### Functional Description:   Use this API to get:  * customer name * used / free space * used / available * user account info  of the according customer.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;CustomerData&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CustomerData> getCustomerInfoWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/customer";

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

    GenericType<CustomerData> localVarReturnType = new GenericType<CustomerData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get customer&#39;s key pair
   * ### Functional Description:   Retrieve the customer rescue key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param xSdsAuthToken Authentication token (optional)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer getCustomerKeyPair(String xSdsAuthToken) throws ApiException {
    return getCustomerKeyPairWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get customer&#39;s key pair
   * ### Functional Description:   Retrieve the customer rescue key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;UserKeyPairContainer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserKeyPairContainer> getCustomerKeyPairWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/customer/keypair";

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

    GenericType<UserKeyPairContainer> localVarReturnType = new GenericType<UserKeyPairContainer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of OAuth client authorizations
   * ### Functional Description:   Retrieve information about all OAuth client authorizations.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isStandard:eq:true&#x60;   Get standard OAuth clients.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isStandard&#x60;** | Standard client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;clientName:desc&#x60;   Sort by &#x60;clientName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;clientName&#x60;** | Client name |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return List&lt;OAuthAuthorization&gt;
   * @throws ApiException if fails to make API call
   */
  public List<OAuthAuthorization> getOAuthAuthorizations(String xSdsAuthToken, String xSdsDateFormat, String filter, String sort) throws ApiException {
    return getOAuthAuthorizationsWithHttpInfo(xSdsAuthToken, xSdsDateFormat, filter, sort).getData();
      }

  /**
   * Get list of OAuth client authorizations
   * ### Functional Description:   Retrieve information about all OAuth client authorizations.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;isStandard:eq:true&#x60;   Get standard OAuth clients.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isStandard&#x60;** | Standard client filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;clientName:desc&#x60;   Sort by &#x60;clientName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;clientName&#x60;** | Client name |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;List&lt;OAuthAuthorization&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<OAuthAuthorization>> getOAuthAuthorizationsWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/oauth/authorizations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<List<OAuthAuthorization>> localVarReturnType = new GenericType<List<OAuthAuthorization>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user profile attributes
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Retrieve a list of user profile attributes.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;key:cn:searchString_1|value:cn:searchString_2&#x60;   Filter by attribute key contains &#x60;searchString_1&#x60; **AND** attribute value contains &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;key&#x60;** | User profile attribute key filter | &#x60;cn, eq, sw&#x60; | Attribute key contains / equals / starts with value. | &#x60;search String&#x60; | | **&#x60;value&#x60;** | User profile attribute value filter | &#x60;cn, eq, sw&#x60; | Attribute value contains / equals / starts with value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;key:asc|value:desc&#x60;   Sort by &#x60;key&#x60; ascending **AND** by &#x60;value&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;key&#x60;** | User profile attribute key | | **&#x60;value&#x60;** | User profile attribute value |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return AttributesResponse
   * @throws ApiException if fails to make API call
   */
  public AttributesResponse getProfileAttributes(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getProfileAttributesWithHttpInfo(xSdsAuthToken, filter, limit, offset, sort).getData();
      }

  /**
   * Get user profile attributes
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Retrieve a list of user profile attributes.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;key:cn:searchString_1|value:cn:searchString_2&#x60;   Filter by attribute key contains &#x60;searchString_1&#x60; **AND** attribute value contains &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;key&#x60;** | User profile attribute key filter | &#x60;cn, eq, sw&#x60; | Attribute key contains / equals / starts with value. | &#x60;search String&#x60; | | **&#x60;value&#x60;** | User profile attribute value filter | &#x60;cn, eq, sw&#x60; | Attribute value contains / equals / starts with value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;key:asc|value:desc&#x60;   Sort by &#x60;key&#x60; ascending **AND** by &#x60;value&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;key&#x60;** | User profile attribute key | | **&#x60;value&#x60;** | User profile attribute value |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;AttributesResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AttributesResponse> getProfileAttributesWithHttpInfo(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/profileAttributes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
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

    GenericType<AttributesResponse> localVarReturnType = new GenericType<AttributesResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user account information
   * ### Functional Description:   Retrieves all information regarding the current user&#39;s account.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Setting the query parameter &#x60;more_info&#x60; to &#x60;true&#x60;, causes the API to return more details e.g. the user&#39;s groups.    &#x60;customer&#x60; (&#x60;CustomerData&#x60;) attribute in &#x60;UserAccount&#x60; response model is **&#x60;DEPRECATED&#x60;**. Please use response from &#x60;GET /user/account/customer&#x60; instead.
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param moreInfo Get more info for this user e.g. list of user groups (optional)
   * @return UserAccount
   * @throws ApiException if fails to make API call
   */
  public UserAccount getUserInfo(String xSdsAuthToken, String xSdsDateFormat, Boolean moreInfo) throws ApiException {
    return getUserInfoWithHttpInfo(xSdsAuthToken, xSdsDateFormat, moreInfo).getData();
      }

  /**
   * Get user account information
   * ### Functional Description:   Retrieves all information regarding the current user&#39;s account.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: Setting the query parameter &#x60;more_info&#x60; to &#x60;true&#x60;, causes the API to return more details e.g. the user&#39;s groups.    &#x60;customer&#x60; (&#x60;CustomerData&#x60;) attribute in &#x60;UserAccount&#x60; response model is **&#x60;DEPRECATED&#x60;**. Please use response from &#x60;GET /user/account/customer&#x60; instead.
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param moreInfo Get more info for this user e.g. list of user groups (optional)
   * @return ApiResponse&lt;UserAccount&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserAccount> getUserInfoWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, Boolean moreInfo) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "more_info", moreInfo));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UserAccount> localVarReturnType = new GenericType<UserAccount>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user&#39;s key pair
   * ### Functional Description:   Retrieve the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param xSdsAuthToken Authentication token (optional)
   * @return UserKeyPairContainer
   * @throws ApiException if fails to make API call
   */
  public UserKeyPairContainer getUserKeyPair(String xSdsAuthToken) throws ApiException {
    return getUserKeyPairWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get user&#39;s key pair
   * ### Functional Description:   Retrieve the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: None.  ### &amp;#9432; Further Information: The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;UserKeyPairContainer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserKeyPairContainer> getUserKeyPairWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/account/keypair";

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

    GenericType<UserKeyPairContainer> localVarReturnType = new GenericType<UserKeyPairContainer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List Download Share subscriptions 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of subscribed Download Shares for current user.   ### Precondition: Authenticated user.  ### Effects: List of subscribed Download Shares is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;authParentId:eq:#&#x60;   Get download shares where &#x60;authParentId&#x60; equals &#x60;#&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;downloadShareId&#x60;** | Download Share ID filter | &#x60;eq&#x60; | Download Share ID equals value. | &#x60;long value&#x60; | | **&#x60;authParentId&#x60;** | Auth parent ID filter | &#x60;eq&#x60; | Auth parent ID equals value. | &#x60;long value&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;downloadShareId:desc&#x60;   Sort by &#x60;downloadShareId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;downloadShareId&#x60;** | Download Share ID | | **&#x60;authParentId&#x60;** | Auth parent ID |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return SubscribedDownloadShareList
   * @throws ApiException if fails to make API call
   */
  public SubscribedDownloadShareList listDownloadShareSubscriptions(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return listDownloadShareSubscriptionsWithHttpInfo(xSdsAuthToken, filter, limit, offset, sort).getData();
      }

  /**
   * List Download Share subscriptions 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of subscribed Download Shares for current user.   ### Precondition: Authenticated user.  ### Effects: List of subscribed Download Shares is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;authParentId:eq:#&#x60;   Get download shares where &#x60;authParentId&#x60; equals &#x60;#&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;downloadShareId&#x60;** | Download Share ID filter | &#x60;eq&#x60; | Download Share ID equals value. | &#x60;long value&#x60; | | **&#x60;authParentId&#x60;** | Auth parent ID filter | &#x60;eq&#x60; | Auth parent ID equals value. | &#x60;long value&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;downloadShareId:desc&#x60;   Sort by &#x60;downloadShareId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;downloadShareId&#x60;** | Download Share ID | | **&#x60;authParentId&#x60;** | Auth parent ID |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;SubscribedDownloadShareList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SubscribedDownloadShareList> listDownloadShareSubscriptionsWithHttpInfo(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/download_shares";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
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

    GenericType<SubscribedDownloadShareList> localVarReturnType = new GenericType<SubscribedDownloadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List node subscriptions 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of subscribed nodes for current user.   ### Precondition: Authenticated user.  ### Effects: List of subscribed nodes is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;authParentId:eq:#&#x60;   Get nodes where &#x60;authParentId&#x60; equals &#x60;#&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID filter | &#x60;eq&#x60; | Node ID equals value. | &#x60;long value&#x60; | | **&#x60;authParentId&#x60;** | Auth parent ID filter | &#x60;eq&#x60; | Auth parent ID equals value. | &#x60;long value&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;nodeId:desc&#x60;   Sort by &#x60;nodeId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID | | **&#x60;authParentId&#x60;** | Auth parent ID |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return SubscribedNodeList
   * @throws ApiException if fails to make API call
   */
  public SubscribedNodeList listNodeSubscriptions(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return listNodeSubscriptionsWithHttpInfo(xSdsAuthToken, filter, limit, offset, sort).getData();
      }

  /**
   * List node subscriptions 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of subscribed nodes for current user.   ### Precondition: Authenticated user.  ### Effects: List of subscribed nodes is returned.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;authParentId:eq:#&#x60;   Get nodes where &#x60;authParentId&#x60; equals &#x60;#&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID filter | &#x60;eq&#x60; | Node ID equals value. | &#x60;long value&#x60; | | **&#x60;authParentId&#x60;** | Auth parent ID filter | &#x60;eq&#x60; | Auth parent ID equals value. | &#x60;long value&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;nodeId:desc&#x60;   Sort by &#x60;nodeId&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID | | **&#x60;authParentId&#x60;** | Auth parent ID |
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;SubscribedNodeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SubscribedNodeList> listNodeSubscriptionsWithHttpInfo(String xSdsAuthToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
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

    GenericType<SubscribedNodeList> localVarReturnType = new GenericType<SubscribedNodeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Request list of notification configurations 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of notification configurations for current user.   ### Precondition: Authenticated user.  ### Effects: List of available notification configurations is returned.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return NotificationConfigList
   * @throws ApiException if fails to make API call
   */
  public NotificationConfigList requestListOfNotificationConfigs(String xSdsAuthToken) throws ApiException {
    return requestListOfNotificationConfigsWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Request list of notification configurations 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of notification configurations for current user.   ### Precondition: Authenticated user.  ### Effects: List of available notification configurations is returned.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NotificationConfigList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationConfigList> requestListOfNotificationConfigsWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/notifications/config";

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

    GenericType<NotificationConfigList> localVarReturnType = new GenericType<NotificationConfigList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set user profile attributes
   * ## &amp;#9888; Deprecated since version 4.12.0  ### Functional Description:   Set custom user profile attributes.  ### Precondition: None.  ### Effects: Custom user profile attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing user profile attributes will be deleted.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**   * Maximum key length is **255**   * Maximum value length is **4096**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ProfileAttributes
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ProfileAttributes setAllProfileAttributes(ProfileAttributesRequest body, String xSdsAuthToken) throws ApiException {
    return setAllProfileAttributesWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Set user profile attributes
   * ## &amp;#9888; Deprecated since version 4.12.0  ### Functional Description:   Set custom user profile attributes.  ### Precondition: None.  ### Effects: Custom user profile attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing user profile attributes will be deleted.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**   * Maximum key length is **255**   * Maximum value length is **4096**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ProfileAttributes&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<ProfileAttributes> setAllProfileAttributesWithHttpInfo(ProfileAttributesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setAllProfileAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/profileAttributes";

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

    GenericType<ProfileAttributes> localVarReturnType = new GenericType<ProfileAttributes>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or edit user profile attributes
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Set custom user profile attributes.  ### Precondition: None.  ### Effects: Custom user profile attributes get added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry existed before, it will be overwritten.   Range submodel is never returned.  * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**   * Maximum key length is **255**   * Maximum value length is **4096**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ProfileAttributes
   * @throws ApiException if fails to make API call
   */
  public ProfileAttributes setProfileAttributes(ProfileAttributesRequest body, String xSdsAuthToken) throws ApiException {
    return setProfileAttributesWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Add or edit user profile attributes
   * ### &amp;#128640; Since version 4.7.0  ### Functional Description:   Set custom user profile attributes.  ### Precondition: None.  ### Effects: Custom user profile attributes get added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry existed before, it will be overwritten.   Range submodel is never returned.  * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**   * Maximum key length is **255**   * Maximum value length is **4096**
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;ProfileAttributes&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ProfileAttributes> setProfileAttributesWithHttpInfo(ProfileAttributesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setProfileAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/profileAttributes";

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

    GenericType<ProfileAttributes> localVarReturnType = new GenericType<ProfileAttributes>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set user&#39;s key pair
   * ### Functional Description:   Set the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: The key pair is set.  ### &amp;#9432; Further Information: Overwriting an existing key pair is **NOT** possible.   Please delete the existing key pair first.   The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void setUserKeyPair(UserKeyPairContainer body, String xSdsAuthToken) throws ApiException {

    setUserKeyPairWithHttpInfo(body, xSdsAuthToken);
  }

  /**
   * Set user&#39;s key pair
   * ### Functional Description:   Set the user&#39;s key pair.  ### Precondition: Authenticated user.  ### Effects: The key pair is set.  ### &amp;#9432; Further Information: Overwriting an existing key pair is **NOT** possible.   Please delete the existing key pair first.   The private key is password-based; encrypted with &#x60;AES256&#x60; / &#x60;PBKDF2&#x60;.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> setUserKeyPairWithHttpInfo(UserKeyPairContainer body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setUserKeyPair");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/account/keypair";

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
   * Subscribe Download Share for notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Subscribe Download Share for notifications.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target node.  ### Effects: Download Share is subscribed.   Notifications for this Download Share will be triggered in the future.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return SubscribedDownloadShare
   * @throws ApiException if fails to make API call
   */
  public SubscribedDownloadShare subscribeDownloadShare(Long shareId, String xSdsAuthToken) throws ApiException {
    return subscribeDownloadShareWithHttpInfo(shareId, xSdsAuthToken).getData();
      }

  /**
   * Subscribe Download Share for notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Subscribe Download Share for notifications.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target node.  ### Effects: Download Share is subscribed.   Notifications for this Download Share will be triggered in the future.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;SubscribedDownloadShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SubscribedDownloadShare> subscribeDownloadShareWithHttpInfo(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling subscribeDownloadShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/download_shares/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

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

    GenericType<SubscribedDownloadShare> localVarReturnType = new GenericType<SubscribedDownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Subscribe node for notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Subscribe node for notifications.  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: Node is subscribed.   Notifications for this node will be triggered in the future.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return SubscribedNode
   * @throws ApiException if fails to make API call
   */
  public SubscribedNode subscribeNode(Long nodeId, String xSdsAuthToken) throws ApiException {
    return subscribeNodeWithHttpInfo(nodeId, xSdsAuthToken).getData();
      }

  /**
   * Subscribe node for notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Subscribe node for notifications.  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: Node is subscribed.   Notifications for this node will be triggered in the future.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;SubscribedNode&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SubscribedNode> subscribeNodeWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling subscribeNode");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/nodes/{node_id}"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

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

    GenericType<SubscribedNode> localVarReturnType = new GenericType<SubscribedNode>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Unsubscribe Download Share from notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Unsubscribe Download Share from notifications.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target node.  ### Effects: Download Share is unsubscribed.   Notifications for this Download Share are disabled.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void unsubscribeDownloadShare(Long shareId, String xSdsAuthToken) throws ApiException {

    unsubscribeDownloadShareWithHttpInfo(shareId, xSdsAuthToken);
  }

  /**
   * Unsubscribe Download Share from notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Unsubscribe Download Share from notifications.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target node.  ### Effects: Download Share is unsubscribed.   Notifications for this Download Share are disabled.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> unsubscribeDownloadShareWithHttpInfo(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling unsubscribeDownloadShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/download_shares/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

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
   * Unsubscribe node from notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Unsubscribe node from notifications.  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: Node is unsubscribed.   Notifications for this node are disabled.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void unsubscribeNode(Long nodeId, String xSdsAuthToken) throws ApiException {

    unsubscribeNodeWithHttpInfo(nodeId, xSdsAuthToken);
  }

  /**
   * Unsubscribe node from notifications 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Unsubscribe node from notifications.  ### Precondition: User has _\&quot;read\&quot;_ permissions in auth parent room.  ### Effects: Node is unsubscribed.   Notifications for this node are disabled.  ### &amp;#9432; Further Information: None.
   * @param nodeId Node ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> unsubscribeNodeWithHttpInfo(Long nodeId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling unsubscribeNode");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/subscriptions/nodes/{node_id}"
      .replaceAll("\\{" + "node_id" + "\\}", apiClient.escapeString(nodeId.toString()));

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
   * Update notification configuration 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Update notification configuration for current user.   ### Precondition: Authenticated user.  ### Effects: Notification configuration is updated.  ### &amp;#9432; Further Information: Leave &#x60;channelIds&#x60; empty to disable notifications. Please note that channel &#x60;email instant&#x60; is not valid for &#x60;file.created&#x60; events
   * @param body body (required)
   * @param id Unique identifier for a notification configuration (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NotificationConfig
   * @throws ApiException if fails to make API call
   */
  public NotificationConfig updateNotificationConfig(NotificationConfigChangeRequest body, Long id, String xSdsAuthToken) throws ApiException {
    return updateNotificationConfigWithHttpInfo(body, id, xSdsAuthToken).getData();
      }

  /**
   * Update notification configuration 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Update notification configuration for current user.   ### Precondition: Authenticated user.  ### Effects: Notification configuration is updated.  ### &amp;#9432; Further Information: Leave &#x60;channelIds&#x60; empty to disable notifications. Please note that channel &#x60;email instant&#x60; is not valid for &#x60;file.created&#x60; events
   * @param body body (required)
   * @param id Unique identifier for a notification configuration (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NotificationConfig&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationConfig> updateNotificationConfigWithHttpInfo(NotificationConfigChangeRequest body, Long id, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateNotificationConfig");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updateNotificationConfig");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/notifications/config/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<NotificationConfig> localVarReturnType = new GenericType<NotificationConfig>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update user account
   * ### Functional Description:   Update current user&#39;s account.  ### Precondition: Authenticated user.  ### Effects: User&#39;s account updated.  ### &amp;#9432; Further Information: * All input fields are limited to **150** characters.   * **All** characters are allowed.    &#x60;customer&#x60; (&#x60;CustomerData&#x60;) attribute in &#x60;UserAccount&#x60; response model is **&#x60;DEPRECATED&#x60;**. Please use response from &#x60;GET /user/account/customer&#x60; instead.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return UserAccount
   * @throws ApiException if fails to make API call
   */
  public UserAccount updateUserAccount(UpdateUserAccountRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateUserAccountWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Update user account
   * ### Functional Description:   Update current user&#39;s account.  ### Precondition: Authenticated user.  ### Effects: User&#39;s account updated.  ### &amp;#9432; Further Information: * All input fields are limited to **150** characters.   * **All** characters are allowed.    &#x60;customer&#x60; (&#x60;CustomerData&#x60;) attribute in &#x60;UserAccount&#x60; response model is **&#x60;DEPRECATED&#x60;**. Please use response from &#x60;GET /user/account/customer&#x60; instead.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;UserAccount&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserAccount> updateUserAccountWithHttpInfo(UpdateUserAccountRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateUserAccount");
    }
    
    // create path and map variables
    String localVarPath = "/v4/user/account";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UserAccount> localVarReturnType = new GenericType<UserAccount>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Invalidate authentication token
   * ## &amp;#9888; Deprecated since version 4.12.0  ### Functional Description:   Log out a user.  ### Precondition: Authenticated user.  ### Effects: * User is logged out   * Authentication token gets invalidated.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @param everywhere Invalidate all tokens (optional)
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public void userLogout(String xSdsAuthToken, Boolean everywhere) throws ApiException {

    userLogoutWithHttpInfo(xSdsAuthToken, everywhere);
  }

  /**
   * Invalidate authentication token
   * ## &amp;#9888; Deprecated since version 4.12.0  ### Functional Description:   Log out a user.  ### Precondition: Authenticated user.  ### Effects: * User is logged out   * Authentication token gets invalidated.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @param everywhere Invalidate all tokens (optional)
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<Void> userLogoutWithHttpInfo(String xSdsAuthToken, Boolean everywhere) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/logout";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "everywhere", everywhere));

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
   * (authenticated) Ping
   * ### Functional Description: Test connection to DRACOON Server (while authenticated).  ### Precondition: Authenticated user.  ### Effects: &#x60;200 OK&#x60; with principal information is returned if successful.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String userPing(String xSdsAuthToken) throws ApiException {
    return userPingWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * (authenticated) Ping
   * ### Functional Description: Test connection to DRACOON Server (while authenticated).  ### Precondition: Authenticated user.  ### Effects: &#x60;200 OK&#x60; with principal information is returned if successful.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> userPingWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/user/ping";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
