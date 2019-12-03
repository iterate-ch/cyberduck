package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.AccountInfo;
import ch.cyberduck.core.storegate.io.swagger.client.model.AccountSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.AccountStorage;
import ch.cyberduck.core.storegate.io.swagger.client.model.ChangePasswordRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.MultiAccountStorage;
import ch.cyberduck.core.storegate.io.swagger.client.model.MultiSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.PartnerRetailer;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateAccountInfoRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateAccountSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateMultiSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateRegNumberRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
public class AccountSettingsApi {
  private ApiClient apiClient;

  public AccountSettingsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AccountSettingsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Change password.
   * 
   * @param changePasswordRequest changePasswordRequest (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsChangePassword(ChangePasswordRequest changePasswordRequest) throws ApiException {

    accountSettingsChangePasswordWithHttpInfo(changePasswordRequest);
  }

  /**
   * Change password.
   * 
   * @param changePasswordRequest changePasswordRequest (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsChangePasswordWithHttpInfo(ChangePasswordRequest changePasswordRequest) throws ApiException {
    Object localVarPostBody = changePasswordRequest;
    
    // verify the required parameter 'changePasswordRequest' is set
    if (changePasswordRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'changePasswordRequest' when calling accountSettingsChangePassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/password";

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
  /**
   * Get Account info.
   * 
   * @return AccountInfo
   * @throws ApiException if fails to make API call
   */
  public AccountInfo accountSettingsGetAccountInfo() throws ApiException {
    return accountSettingsGetAccountInfoWithHttpInfo().getData();
      }

  /**
   * Get Account info.
   * 
   * @return ApiResponse&lt;AccountInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AccountInfo> accountSettingsGetAccountInfoWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/account/info";

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

    GenericType<AccountInfo> localVarReturnType = new GenericType<AccountInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Account settings.
   * 
   * @return AccountSettings
   * @throws ApiException if fails to make API call
   */
  public AccountSettings accountSettingsGetAccountSettings() throws ApiException {
    return accountSettingsGetAccountSettingsWithHttpInfo().getData();
      }

  /**
   * Get Account settings.
   * 
   * @return ApiResponse&lt;AccountSettings&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AccountSettings> accountSettingsGetAccountSettingsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/account/settings";

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

    GenericType<AccountSettings> localVarReturnType = new GenericType<AccountSettings>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Account storage.
   * 
   * @return AccountStorage
   * @throws ApiException if fails to make API call
   */
  public AccountStorage accountSettingsGetAccountStorage() throws ApiException {
    return accountSettingsGetAccountStorageWithHttpInfo().getData();
      }

  /**
   * Get Account storage.
   * 
   * @return ApiResponse&lt;AccountStorage&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AccountStorage> accountSettingsGetAccountStorageWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/account/storage";

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

    GenericType<AccountStorage> localVarReturnType = new GenericType<AccountStorage>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get multi settings.
   * 
   * @return MultiSettings
   * @throws ApiException if fails to make API call
   */
  public MultiSettings accountSettingsGetMultiSettings() throws ApiException {
    return accountSettingsGetMultiSettingsWithHttpInfo().getData();
      }

  /**
   * Get multi settings.
   * 
   * @return ApiResponse&lt;MultiSettings&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MultiSettings> accountSettingsGetMultiSettingsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/account/multisettings";

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

    GenericType<MultiSettings> localVarReturnType = new GenericType<MultiSettings>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Multi storage.
   * 
   * @return MultiAccountStorage
   * @throws ApiException if fails to make API call
   */
  public MultiAccountStorage accountSettingsGetMultiStorage() throws ApiException {
    return accountSettingsGetMultiStorageWithHttpInfo().getData();
      }

  /**
   * Get Multi storage.
   * 
   * @return ApiResponse&lt;MultiAccountStorage&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MultiAccountStorage> accountSettingsGetMultiStorageWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/account/multistorage";

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

    GenericType<MultiAccountStorage> localVarReturnType = new GenericType<MultiAccountStorage>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Reset the password
   * 
   * @param id  (required)
   * @param password  (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsResetPassword(String id, String password) throws ApiException {

    accountSettingsResetPasswordWithHttpInfo(id, password);
  }

  /**
   * Reset the password
   * 
   * @param id  (required)
   * @param password  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsResetPasswordWithHttpInfo(String id, String password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling accountSettingsResetPassword");
    }
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling accountSettingsResetPassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/public/resetpassword/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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
  /**
   * Update account info, only included parameters will be updated.
   * 
   * @param updateAccountInfo  (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsUpdateInfo(UpdateAccountInfoRequest updateAccountInfo) throws ApiException {

    accountSettingsUpdateInfoWithHttpInfo(updateAccountInfo);
  }

  /**
   * Update account info, only included parameters will be updated.
   * 
   * @param updateAccountInfo  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsUpdateInfoWithHttpInfo(UpdateAccountInfoRequest updateAccountInfo) throws ApiException {
    Object localVarPostBody = updateAccountInfo;
    
    // verify the required parameter 'updateAccountInfo' is set
    if (updateAccountInfo == null) {
      throw new ApiException(400, "Missing the required parameter 'updateAccountInfo' when calling accountSettingsUpdateInfo");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/info";

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
  /**
   * Update multi settings, only included parameters will be updated.
   * 
   * @param updateMultiSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsUpdateMultiSettings(UpdateMultiSettings updateMultiSettings) throws ApiException {

    accountSettingsUpdateMultiSettingsWithHttpInfo(updateMultiSettings);
  }

  /**
   * Update multi settings, only included parameters will be updated.
   * 
   * @param updateMultiSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsUpdateMultiSettingsWithHttpInfo(UpdateMultiSettings updateMultiSettings) throws ApiException {
    Object localVarPostBody = updateMultiSettings;
    
    // verify the required parameter 'updateMultiSettings' is set
    if (updateMultiSettings == null) {
      throw new ApiException(400, "Missing the required parameter 'updateMultiSettings' when calling accountSettingsUpdateMultiSettings");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/multisettings";

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
  /**
   * Update account info reg number, only included parameters will be updated.
   * 
   * @param updateRegNumber  (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsUpdateRegNumber(UpdateRegNumberRequest updateRegNumber) throws ApiException {

    accountSettingsUpdateRegNumberWithHttpInfo(updateRegNumber);
  }

  /**
   * Update account info reg number, only included parameters will be updated.
   * 
   * @param updateRegNumber  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsUpdateRegNumberWithHttpInfo(UpdateRegNumberRequest updateRegNumber) throws ApiException {
    Object localVarPostBody = updateRegNumber;
    
    // verify the required parameter 'updateRegNumber' is set
    if (updateRegNumber == null) {
      throw new ApiException(400, "Missing the required parameter 'updateRegNumber' when calling accountSettingsUpdateRegNumber");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/info/regnumber";

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
  /**
   * Update account settings, only included parameters will be updated.
   * 
   * @param updateAccountSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public void accountSettingsUpdateSettings(UpdateAccountSettings updateAccountSettings) throws ApiException {

    accountSettingsUpdateSettingsWithHttpInfo(updateAccountSettings);
  }

  /**
   * Update account settings, only included parameters will be updated.
   * 
   * @param updateAccountSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> accountSettingsUpdateSettingsWithHttpInfo(UpdateAccountSettings updateAccountSettings) throws ApiException {
    Object localVarPostBody = updateAccountSettings;
    
    // verify the required parameter 'updateAccountSettings' is set
    if (updateAccountSettings == null) {
      throw new ApiException(400, "Missing the required parameter 'updateAccountSettings' when calling accountSettingsUpdateSettings");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/settings";

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
  /**
   * Validate a reset password request
   * 
   * @param id id (required)
   * @return PartnerRetailer
   * @throws ApiException if fails to make API call
   */
  public PartnerRetailer accountSettingsValidatePasswordRequest(String id) throws ApiException {
    return accountSettingsValidatePasswordRequestWithHttpInfo(id).getData();
      }

  /**
   * Validate a reset password request
   * 
   * @param id id (required)
   * @return ApiResponse&lt;PartnerRetailer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PartnerRetailer> accountSettingsValidatePasswordRequestWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling accountSettingsValidatePasswordRequest");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/public/resetpassword/{id}"
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

    GenericType<PartnerRetailer> localVarReturnType = new GenericType<PartnerRetailer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Verify password
   * 
   * @param password password (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean accountSettingsVerifyPassword(String password) throws ApiException {
    return accountSettingsVerifyPasswordWithHttpInfo(password).getData();
      }

  /**
   * Verify password
   * 
   * @param password password (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> accountSettingsVerifyPasswordWithHttpInfo(String password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling accountSettingsVerifyPassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4/account/verifypassword";

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

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
