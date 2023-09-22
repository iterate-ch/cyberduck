package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateAccountRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateAccountwithProductsRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateSubAccountRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.IsUsernameAvailableRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.RegistrationInformation;
import ch.cyberduck.core.storegate.io.swagger.client.model.RegistrationInformationSubuser;
import ch.cyberduck.core.storegate.io.swagger.client.model.SetPaymentStatusRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class RegistrationApi {
  private ApiClient apiClient;

  public RegistrationApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RegistrationApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Check if the username is available
   * 
   * @param usernameRequest The username request. (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean registrationIsUserNameAvailable(IsUsernameAvailableRequest usernameRequest) throws ApiException {
    return registrationIsUserNameAvailableWithHttpInfo(usernameRequest).getData();
      }

  /**
   * Check if the username is available
   * 
   * @param usernameRequest The username request. (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> registrationIsUserNameAvailableWithHttpInfo(IsUsernameAvailableRequest usernameRequest) throws ApiException {
    Object localVarPostBody = usernameRequest;
    
    // verify the required parameter 'usernameRequest' is set
    if (usernameRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'usernameRequest' when calling registrationIsUserNameAvailable");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/isusernameavailable";

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
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information
   * 
   * @param direct Optional (optional)
   * @param createAccountSalepackagePartnerId PartnerId (optional)
   * @param createAccountSalepackageRetailerId Optional RetailerId (optional)
   * @param createAccountSalepackageSalepackageId SalepackageId (optional)
   * @param createAccountSalepackageTrialId Optional TrialId (optional)
   * @param createAccountSalepackageCampaignId Optional CampaignId (optional)
   * @param createAccountSalepackageStorageId Optional StorageId (optional)
   * @param createAccountSalepackageMultiId Optional MultiId (optional)
   * @param createAccountSalepackageBackupId Optional BackupId (optional)
   * @param createAccountSalepackageSyncId Optional SyncId (optional)
   * @param createAccountSalepackageBankIDId Optional BankIDId (optional)
   * @param createAccountSalepackageBrandingId Optional BrandingId (optional)
   * @param createAccountSalepackageSigningId Optional SigningId (optional)
   * @param createAccountSalepackageAccessId Optional AccessId (optional)
   * @return RegistrationInformation
   * @throws ApiException if fails to make API call
   */
  public RegistrationInformation registrationRegisterAccount(Boolean direct, String createAccountSalepackagePartnerId, String createAccountSalepackageRetailerId, String createAccountSalepackageSalepackageId, String createAccountSalepackageTrialId, String createAccountSalepackageCampaignId, String createAccountSalepackageStorageId, String createAccountSalepackageMultiId, String createAccountSalepackageBackupId, String createAccountSalepackageSyncId, String createAccountSalepackageBankIDId, String createAccountSalepackageBrandingId, String createAccountSalepackageSigningId, String createAccountSalepackageAccessId) throws ApiException {
    return registrationRegisterAccountWithHttpInfo(direct, createAccountSalepackagePartnerId, createAccountSalepackageRetailerId, createAccountSalepackageSalepackageId, createAccountSalepackageTrialId, createAccountSalepackageCampaignId, createAccountSalepackageStorageId, createAccountSalepackageMultiId, createAccountSalepackageBackupId, createAccountSalepackageSyncId, createAccountSalepackageBankIDId, createAccountSalepackageBrandingId, createAccountSalepackageSigningId, createAccountSalepackageAccessId).getData();
      }

  /**
   * Get information
   * 
   * @param direct Optional (optional)
   * @param createAccountSalepackagePartnerId PartnerId (optional)
   * @param createAccountSalepackageRetailerId Optional RetailerId (optional)
   * @param createAccountSalepackageSalepackageId SalepackageId (optional)
   * @param createAccountSalepackageTrialId Optional TrialId (optional)
   * @param createAccountSalepackageCampaignId Optional CampaignId (optional)
   * @param createAccountSalepackageStorageId Optional StorageId (optional)
   * @param createAccountSalepackageMultiId Optional MultiId (optional)
   * @param createAccountSalepackageBackupId Optional BackupId (optional)
   * @param createAccountSalepackageSyncId Optional SyncId (optional)
   * @param createAccountSalepackageBankIDId Optional BankIDId (optional)
   * @param createAccountSalepackageBrandingId Optional BrandingId (optional)
   * @param createAccountSalepackageSigningId Optional SigningId (optional)
   * @param createAccountSalepackageAccessId Optional AccessId (optional)
   * @return ApiResponse&lt;RegistrationInformation&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegistrationInformation> registrationRegisterAccountWithHttpInfo(Boolean direct, String createAccountSalepackagePartnerId, String createAccountSalepackageRetailerId, String createAccountSalepackageSalepackageId, String createAccountSalepackageTrialId, String createAccountSalepackageCampaignId, String createAccountSalepackageStorageId, String createAccountSalepackageMultiId, String createAccountSalepackageBackupId, String createAccountSalepackageSyncId, String createAccountSalepackageBankIDId, String createAccountSalepackageBrandingId, String createAccountSalepackageSigningId, String createAccountSalepackageAccessId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/registration";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "direct", direct));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.partnerId", createAccountSalepackagePartnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.retailerId", createAccountSalepackageRetailerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.salepackageId", createAccountSalepackageSalepackageId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.trialId", createAccountSalepackageTrialId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.campaignId", createAccountSalepackageCampaignId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.storageId", createAccountSalepackageStorageId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.multiId", createAccountSalepackageMultiId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.backupId", createAccountSalepackageBackupId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.syncId", createAccountSalepackageSyncId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.bankIDId", createAccountSalepackageBankIDId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.brandingId", createAccountSalepackageBrandingId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.signingId", createAccountSalepackageSigningId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "createAccountSalepackage.accessId", createAccountSalepackageAccessId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RegistrationInformation> localVarReturnType = new GenericType<RegistrationInformation>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get campaign info
   * 
   * @param code  (required)
   * @return RegistrationInformation
   * @throws ApiException if fails to make API call
   */
  public RegistrationInformation registrationRegisterAccountCampaign(String code) throws ApiException {
    return registrationRegisterAccountCampaignWithHttpInfo(code).getData();
      }

  /**
   * Get campaign info
   * 
   * @param code  (required)
   * @return ApiResponse&lt;RegistrationInformation&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegistrationInformation> registrationRegisterAccountCampaignWithHttpInfo(String code) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'code' is set
    if (code == null) {
      throw new ApiException(400, "Missing the required parameter 'code' when calling registrationRegisterAccountCampaign");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/campaign/{code}"
      .replaceAll("\\{" + "code" + "\\}", apiClient.escapeString(code.toString()));

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

    GenericType<RegistrationInformation> localVarReturnType = new GenericType<RegistrationInformation>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Register a new account with campaign.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param code  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationRegisterAccountCampaign_0(CreateAccountwithProductsRequest createAccountRequest, String code) throws ApiException {
    return registrationRegisterAccountCampaign_0WithHttpInfo(createAccountRequest, code).getData();
      }

  /**
   * Register a new account with campaign.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param code  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationRegisterAccountCampaign_0WithHttpInfo(CreateAccountwithProductsRequest createAccountRequest, String code) throws ApiException {
    Object localVarPostBody = createAccountRequest;
    
    // verify the required parameter 'createAccountRequest' is set
    if (createAccountRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createAccountRequest' when calling registrationRegisterAccountCampaign_0");
    }
    
    // verify the required parameter 'code' is set
    if (code == null) {
      throw new ApiException(400, "Missing the required parameter 'code' when calling registrationRegisterAccountCampaign_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/campaign/{code}"
      .replaceAll("\\{" + "code" + "\\}", apiClient.escapeString(code.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get signup info
   * 
   * @param signupId  (required)
   * @return RegistrationInformation
   * @throws ApiException if fails to make API call
   */
  public RegistrationInformation registrationRegisterAccountSignup(String signupId) throws ApiException {
    return registrationRegisterAccountSignupWithHttpInfo(signupId).getData();
      }

  /**
   * Get signup info
   * 
   * @param signupId  (required)
   * @return ApiResponse&lt;RegistrationInformation&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegistrationInformation> registrationRegisterAccountSignupWithHttpInfo(String signupId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'signupId' is set
    if (signupId == null) {
      throw new ApiException(400, "Missing the required parameter 'signupId' when calling registrationRegisterAccountSignup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/signup/{signupId}"
      .replaceAll("\\{" + "signupId" + "\\}", apiClient.escapeString(signupId.toString()));

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

    GenericType<RegistrationInformation> localVarReturnType = new GenericType<RegistrationInformation>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Register a new account with signup.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param signupId  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationRegisterAccountSignup_0(CreateAccountwithProductsRequest createAccountRequest, String signupId) throws ApiException {
    return registrationRegisterAccountSignup_0WithHttpInfo(createAccountRequest, signupId).getData();
      }

  /**
   * Register a new account with signup.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param signupId  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationRegisterAccountSignup_0WithHttpInfo(CreateAccountwithProductsRequest createAccountRequest, String signupId) throws ApiException {
    Object localVarPostBody = createAccountRequest;
    
    // verify the required parameter 'createAccountRequest' is set
    if (createAccountRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createAccountRequest' when calling registrationRegisterAccountSignup_0");
    }
    
    // verify the required parameter 'signupId' is set
    if (signupId == null) {
      throw new ApiException(400, "Missing the required parameter 'signupId' when calling registrationRegisterAccountSignup_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/signup/{signupId}"
      .replaceAll("\\{" + "signupId" + "\\}", apiClient.escapeString(signupId.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information on sub user registration
   * 
   * @param userid  (required)
   * @return RegistrationInformationSubuser
   * @throws ApiException if fails to make API call
   */
  public RegistrationInformationSubuser registrationRegisterAccountSubuser(String userid) throws ApiException {
    return registrationRegisterAccountSubuserWithHttpInfo(userid).getData();
      }

  /**
   * Get information on sub user registration
   * 
   * @param userid  (required)
   * @return ApiResponse&lt;RegistrationInformationSubuser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegistrationInformationSubuser> registrationRegisterAccountSubuserWithHttpInfo(String userid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userid' is set
    if (userid == null) {
      throw new ApiException(400, "Missing the required parameter 'userid' when calling registrationRegisterAccountSubuser");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/user/{userid}"
      .replaceAll("\\{" + "userid" + "\\}", apiClient.escapeString(userid.toString()));

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

    GenericType<RegistrationInformationSubuser> localVarReturnType = new GenericType<RegistrationInformationSubuser>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Register a new sub account.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param userid  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationRegisterAccountSubuser_0(CreateSubAccountRequest createAccountRequest, String userid) throws ApiException {
    return registrationRegisterAccountSubuser_0WithHttpInfo(createAccountRequest, userid).getData();
      }

  /**
   * Register a new sub account.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param userid  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationRegisterAccountSubuser_0WithHttpInfo(CreateSubAccountRequest createAccountRequest, String userid) throws ApiException {
    Object localVarPostBody = createAccountRequest;
    
    // verify the required parameter 'createAccountRequest' is set
    if (createAccountRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createAccountRequest' when calling registrationRegisterAccountSubuser_0");
    }
    
    // verify the required parameter 'userid' is set
    if (userid == null) {
      throw new ApiException(400, "Missing the required parameter 'userid' when calling registrationRegisterAccountSubuser_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/user/{userid}"
      .replaceAll("\\{" + "userid" + "\\}", apiClient.escapeString(userid.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Register a new account.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param direct  (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationRegisterAccount_0(CreateAccountRequest createAccountRequest, Boolean direct) throws ApiException {
    return registrationRegisterAccount_0WithHttpInfo(createAccountRequest, direct).getData();
      }

  /**
   * Register a new account.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @param direct  (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationRegisterAccount_0WithHttpInfo(CreateAccountRequest createAccountRequest, Boolean direct) throws ApiException {
    Object localVarPostBody = createAccountRequest;
    
    // verify the required parameter 'createAccountRequest' is set
    if (createAccountRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createAccountRequest' when calling registrationRegisterAccount_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "direct", direct));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update payment information
   * 
   * @param updatePayment  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationSetPaymentStatus(SetPaymentStatusRequest updatePayment) throws ApiException {
    return registrationSetPaymentStatusWithHttpInfo(updatePayment).getData();
      }

  /**
   * Update payment information
   * 
   * @param updatePayment  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationSetPaymentStatusWithHttpInfo(SetPaymentStatusRequest updatePayment) throws ApiException {
    Object localVarPostBody = updatePayment;
    
    // verify the required parameter 'updatePayment' is set
    if (updatePayment == null) {
      throw new ApiException(400, "Missing the required parameter 'updatePayment' when calling registrationSetPaymentStatus");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/paymentstatus";

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Validate campaign code
   * 
   * @param code  (required)
   * @throws ApiException if fails to make API call
   */
  public void registrationValidateAccountCampaign(String code) throws ApiException {

    registrationValidateAccountCampaignWithHttpInfo(code);
  }

  /**
   * Validate campaign code
   * 
   * @param code  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> registrationValidateAccountCampaignWithHttpInfo(String code) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'code' is set
    if (code == null) {
      throw new ApiException(400, "Missing the required parameter 'code' when calling registrationValidateAccountCampaign");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/campaign/{code}"
      .replaceAll("\\{" + "code" + "\\}", apiClient.escapeString(code.toString()));

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


    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Verify email
   * 
   * @param email The transaction id (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean registrationVerifyEmail(String email) throws ApiException {
    return registrationVerifyEmailWithHttpInfo(email).getData();
      }

  /**
   * Verify email
   * 
   * @param email The transaction id (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> registrationVerifyEmailWithHttpInfo(String email) throws ApiException {
    Object localVarPostBody = email;
    
    // verify the required parameter 'email' is set
    if (email == null) {
      throw new ApiException(400, "Missing the required parameter 'email' when calling registrationVerifyEmail");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/registration/verify/email";

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
