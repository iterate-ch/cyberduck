package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.InlineResponse400;
import ch.cyberduck.core.sds.io.swagger.client.model.RecoverUserNameRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordTokenValidateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ResetPasswordWithTokenRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AuthApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

  public AuthApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AuthApi(ApiClient apiClient) {
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
   * Recover username
   * ### Description:   Request an email with the user names of all accounts connected to the email.  ### Precondition: Valid email address.  ### Postcondition: An email is sent to the provided address, with a list of account user names connected to it.  ### Further Information: None. 
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void recoverUserName(RecoverUserNameRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling recoverUserName");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/recover_username";

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


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request password reset
   * ### Description:   Request an email with a password reset token for a certain user to reset password.  ### Precondition: Registered user account.  ### Postcondition: Provided user receives email with password reset token.  ### Further Information: None.
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void requestPasswordReset(ResetPasswordRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling requestPasswordReset");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/reset_password";

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


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Reset password
   * ### Description:   Resets user&#x27;s password.  ### Precondition: User received a password reset token.  ### Postcondition: User&#x27;s password is reset to the provided password.  ### Further Information: Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]
   * @param body  (required)
   * @param token Password reset token (required)
   * @throws ApiException if fails to make API call
   */
  public void resetPassword(ResetPasswordWithTokenRequest body, String token) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling resetPassword");
    }
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling resetPassword");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/reset_password/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

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


    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Validate information for password reset
   * ### Description:   Request all information for a password change dialogue e.g. real name of user.  ### Precondition: User received a password reset token.  ### Postcondition: Context information is returned.  ### Further Information: None.
   * @param token Password reset token (required)
   * @return ResetPasswordTokenValidateResponse
   * @throws ApiException if fails to make API call
   */
  public ResetPasswordTokenValidateResponse validateResetPasswordToken(String token) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling validateResetPasswordToken");
    }
    // create path and map variables
    String localVarPath = "/v4/auth/reset_password/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<ResetPasswordTokenValidateResponse> localVarReturnType = new GenericType<ResetPasswordTokenValidateResponse>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
