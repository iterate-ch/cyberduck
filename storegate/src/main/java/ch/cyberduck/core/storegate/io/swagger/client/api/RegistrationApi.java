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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
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
    String localVarPath = "/v4/registration/isusernameavailable";

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
   * @param partnerId  (required)
   * @param salepackageId  (required)
   * @param retailerId  (optional)
   * @param trialId  (optional)
   * @param campaignId  (optional)
   * @param storageId  (optional)
   * @param multiId  (optional)
   * @param backupId  (optional)
   * @param syncId  (optional)
   * @param bankIDId  (optional)
   * @return RegistrationInformation
   * @throws ApiException if fails to make API call
   */
  public RegistrationInformation registrationRegisterAccount(String partnerId, String salepackageId, String retailerId, String trialId, String campaignId, String storageId, String multiId, String backupId, String syncId, String bankIDId) throws ApiException {
    return registrationRegisterAccountWithHttpInfo(partnerId, salepackageId, retailerId, trialId, campaignId, storageId, multiId, backupId, syncId, bankIDId).getData();
      }

  /**
   * Get information
   * 
   * @param partnerId  (required)
   * @param salepackageId  (required)
   * @param retailerId  (optional)
   * @param trialId  (optional)
   * @param campaignId  (optional)
   * @param storageId  (optional)
   * @param multiId  (optional)
   * @param backupId  (optional)
   * @param syncId  (optional)
   * @param bankIDId  (optional)
   * @return ApiResponse&lt;RegistrationInformation&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RegistrationInformation> registrationRegisterAccountWithHttpInfo(String partnerId, String salepackageId, String retailerId, String trialId, String campaignId, String storageId, String multiId, String backupId, String syncId, String bankIDId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'partnerId' is set
    if (partnerId == null) {
      throw new ApiException(400, "Missing the required parameter 'partnerId' when calling registrationRegisterAccount");
    }
    
    // verify the required parameter 'salepackageId' is set
    if (salepackageId == null) {
      throw new ApiException(400, "Missing the required parameter 'salepackageId' when calling registrationRegisterAccount");
    }
    
    // create path and map variables
    String localVarPath = "/v4/registration";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "PartnerId", partnerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SalepackageId", salepackageId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "RetailerId", retailerId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "TrialId", trialId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "CampaignId", campaignId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "StorageId", storageId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "MultiId", multiId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "BackupId", backupId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SyncId", syncId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "BankIDId", bankIDId));

    
    
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
    String localVarPath = "/v4/registration/campaign/{code}"
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
    String localVarPath = "/v4/registration/campaign/{code}"
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
    String localVarPath = "/v4/registration/signup/{signupId}"
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
    String localVarPath = "/v4/registration/signup/{signupId}"
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
    String localVarPath = "/v4/registration/user/{userid}"
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
    String localVarPath = "/v4/registration/user/{userid}"
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
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String registrationRegisterAccount_0(CreateAccountRequest createAccountRequest) throws ApiException {
    return registrationRegisterAccount_0WithHttpInfo(createAccountRequest).getData();
      }

  /**
   * Register a new account.
   * 
   * @param createAccountRequest RegisterAccountRequest (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> registrationRegisterAccount_0WithHttpInfo(CreateAccountRequest createAccountRequest) throws ApiException {
    Object localVarPostBody = createAccountRequest;
    
    // verify the required parameter 'createAccountRequest' is set
    if (createAccountRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createAccountRequest' when calling registrationRegisterAccount_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/registration";

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
    String localVarPath = "/v4/registration/paymentstatus";

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
    String localVarPath = "/v4/registration/campaign/{code}"
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
}
