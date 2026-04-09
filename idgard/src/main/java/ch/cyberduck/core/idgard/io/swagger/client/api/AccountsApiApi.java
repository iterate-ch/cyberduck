package ch.cyberduck.core.idgard.io.swagger.client.api;

import ch.cyberduck.core.idgard.io.swagger.client.ApiException;
import ch.cyberduck.core.idgard.io.swagger.client.ApiClient;
import ch.cyberduck.core.idgard.io.swagger.client.Configuration;
import ch.cyberduck.core.idgard.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.idgard.io.swagger.client.model.APIError;
import ch.cyberduck.core.idgard.io.swagger.client.model.Authentication;
import ch.cyberduck.core.idgard.io.swagger.client.model.InlineResponse200;
import ch.cyberduck.core.idgard.io.swagger.client.model.PasswordChange;
import ch.cyberduck.core.idgard.io.swagger.client.model.PasswordReset;
import ch.cyberduck.core.idgard.io.swagger.client.model.PasswordResetWithoutPUK;
import ch.cyberduck.core.idgard.io.swagger.client.model.PukChange;
import ch.cyberduck.core.idgard.io.swagger.client.model.SimpleUserInfo;
import ch.cyberduck.core.idgard.io.swagger.client.model.TotpConfirmBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccountsApiApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

  public AccountsApiApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AccountsApiApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public void setHeadersOverrides(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * 
   * Complete login with duo as 2FA.
   * @param challengeId  (optional)
   * @param sigRequest  (optional)
   * @param sigResponse  (optional)
   * @throws ApiException if fails to make API call
   */
  public void uiapiAccountsAPIV1Rest2faDuoPost(String challengeId, String sigRequest, String sigResponse) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/2fa/duo";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (challengeId != null)
      localVarFormParams.put("challengeId", challengeId);
    if (sigRequest != null)
      localVarFormParams.put("sig_request", sigRequest);
    if (sigResponse != null)
      localVarFormParams.put("sig_response", sigResponse);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }

  /**
   * 
   * Log in into the Server and creates a Session.
   * @param body Authentication (required)
   * @return SimpleUserInfo
   * @throws ApiException if fails to make API call
   */
  public SimpleUserInfo uiapiAccountsAPIV1RestLoginPost(Authentication body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling uiapiAccountsAPIV1RestLoginPost");
    }
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/login";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SimpleUserInfo> localVarReturnType = new GenericType<SimpleUserInfo>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Log out the user by invalidating the Session.
   * @param opt Logout option (optional)
   * @throws ApiException if fails to make API call
   */
  public void uiapiAccountsAPIV1RestLogoutPost(String opt) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/logout";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "opt", opt));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Reset the password and get a new PUK.
   * @param body  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uiapiAccountsAPIV1RestPasswordPukPost(PasswordReset body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling uiapiAccountsAPIV1RestPasswordPukPost");
    }
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/password/puk";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json", "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Change the password when logged in.
   * @param body  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uiapiAccountsAPIV1RestPasswordPut(PasswordChange body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling uiapiAccountsAPIV1RestPasswordPut");
    }
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/password";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "text/plain", "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Reset the password with username, old+new Password (e.g. after password change is required).
   * @param body  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uiapiAccountsAPIV1RestPasswordUsernamePost(PasswordResetWithoutPUK body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling uiapiAccountsAPIV1RestPasswordUsernamePost");
    }
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/password/username";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json", "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Generate a new PUK for the account.
   * @param body  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uiapiAccountsAPIV1RestPukPost(PukChange body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling uiapiAccountsAPIV1RestPukPost");
    }
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/puk";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json", "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Used for 2nd factor authentication. Resend SMS to user either on first or secondary SMS service.
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean uiapiAccountsAPIV1RestSmsResendGet() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/sms/resend";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Confirm the latest previously requested TOTP secret for this user in this session, overwriting any previous TOTP secrets. Allowed to the authenticated user itself. 
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   */
  public void uiapiAccountsAPIV1RestTotpConfirmPost(TotpConfirmBody body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/totp/confirm";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Request a TOTP secret for this user. Before this TOTP secret is active, it needs to be confirmed. A previously configured TOTP secret will remain valid until the new TOTP secret is confirmed. Allowed to the authenticated user itself. 
   * @return InlineResponse200
   * @throws ApiException if fails to make API call
   */
  public InlineResponse200 uiapiAccountsAPIV1RestTotpRequestPost() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/uiapi/AccountsAPI/v1/rest/totp/request";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "csrfToken", "idgardIdCookie", "jsessionIdCookie", "myidGidCookie" };

    GenericType<InlineResponse200> localVarReturnType = new GenericType<InlineResponse200>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
