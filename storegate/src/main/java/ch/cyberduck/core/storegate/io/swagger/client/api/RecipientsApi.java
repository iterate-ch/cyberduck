package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.Recipient;
import ch.cyberduck.core.storegate.io.swagger.client.model.RecipientContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.RecipientRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class RecipientsApi {
  private ApiClient apiClient;

  public RecipientsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RecipientsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Creates a new Recipient.
   * 
   * @param recipiantRequest RecipiantRequest (required)
   * @return Recipient
   * @throws ApiException if fails to make API call
   */
  public Recipient recipientsCreate(RecipientRequest recipiantRequest) throws ApiException {
    return recipientsCreateWithHttpInfo(recipiantRequest).getData();
      }

  /**
   * Creates a new Recipient.
   * 
   * @param recipiantRequest RecipiantRequest (required)
   * @return ApiResponse&lt;Recipient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Recipient> recipientsCreateWithHttpInfo(RecipientRequest recipiantRequest) throws ApiException {
    Object localVarPostBody = recipiantRequest;
    
    // verify the required parameter 'recipiantRequest' is set
    if (recipiantRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'recipiantRequest' when calling recipientsCreate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/recipients";

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

    GenericType<Recipient> localVarReturnType = new GenericType<Recipient>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Deletes a Recipient
   * 
   * @param id The ID of the recipient to delete (required)
   * @throws ApiException if fails to make API call
   */
  public void recipientsDelete(String id) throws ApiException {

    recipientsDeleteWithHttpInfo(id);
  }

  /**
   * Deletes a Recipient
   * 
   * @param id The ID of the recipient to delete (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> recipientsDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recipientsDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/recipients/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get fileContents.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;FirstName desc\&quot; is acceptable FirstName, LastName, Email, PersonalIdentifier, PhoneNumber (required)
   * @param searchCriteria Optional search criteria (required)
   * @return RecipientContents
   * @throws ApiException if fails to make API call
   */
  public RecipientContents recipientsGet(Integer pageIndex, Integer pageSize, String sortExpression, String searchCriteria) throws ApiException {
    return recipientsGetWithHttpInfo(pageIndex, pageSize, sortExpression, searchCriteria).getData();
      }

  /**
   * Get fileContents.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;FirstName desc\&quot; is acceptable FirstName, LastName, Email, PersonalIdentifier, PhoneNumber (required)
   * @param searchCriteria Optional search criteria (required)
   * @return ApiResponse&lt;RecipientContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RecipientContents> recipientsGetWithHttpInfo(Integer pageIndex, Integer pageSize, String sortExpression, String searchCriteria) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling recipientsGet");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling recipientsGet");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling recipientsGet");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling recipientsGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/recipients/contents";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RecipientContents> localVarReturnType = new GenericType<RecipientContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param id  (required)
   * @return Recipient
   * @throws ApiException if fails to make API call
   */
  public Recipient recipientsGetRecipient(String id) throws ApiException {
    return recipientsGetRecipientWithHttpInfo(id).getData();
      }

  /**
   * 
   * 
   * @param id  (required)
   * @return ApiResponse&lt;Recipient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Recipient> recipientsGetRecipientWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recipientsGetRecipient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/recipients/{id}"
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

    GenericType<Recipient> localVarReturnType = new GenericType<Recipient>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Recipient
   * 
   * @param id  (required)
   * @param recipiantRequest  (required)
   * @return Recipient
   * @throws ApiException if fails to make API call
   */
  public Recipient recipientsUpdate(String id, RecipientRequest recipiantRequest) throws ApiException {
    return recipientsUpdateWithHttpInfo(id, recipiantRequest).getData();
      }

  /**
   * Update Recipient
   * 
   * @param id  (required)
   * @param recipiantRequest  (required)
   * @return ApiResponse&lt;Recipient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Recipient> recipientsUpdateWithHttpInfo(String id, RecipientRequest recipiantRequest) throws ApiException {
    Object localVarPostBody = recipiantRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recipientsUpdate");
    }
    
    // verify the required parameter 'recipiantRequest' is set
    if (recipiantRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'recipiantRequest' when calling recipientsUpdate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/recipients/{id}"
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
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Recipient> localVarReturnType = new GenericType<Recipient>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
