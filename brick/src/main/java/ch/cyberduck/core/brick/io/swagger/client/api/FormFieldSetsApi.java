package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.FormFieldSetEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PatchFormFieldSets;
import ch.cyberduck.core.brick.io.swagger.client.model.PostFormFieldSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class FormFieldSetsApi {
  private ApiClient apiClient;

  public FormFieldSetsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FormFieldSetsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Form Field Set
   * Delete Form Field Set
   * @param id Form Field Set ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFormFieldSetsId(Integer id) throws ApiException {

    deleteFormFieldSetsIdWithHttpInfo(id);
  }

  /**
   * Delete Form Field Set
   * Delete Form Field Set
   * @param id Form Field Set ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteFormFieldSetsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteFormFieldSetsId");
    }
    
    // create path and map variables
    String localVarPath = "/form_field_sets/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Form Field Sets
   * List Form Field Sets
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;FormFieldSetEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FormFieldSetEntity> getFormFieldSets(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getFormFieldSetsWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List Form Field Sets
   * List Form Field Sets
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;FormFieldSetEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<FormFieldSetEntity>> getFormFieldSetsWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/form_field_sets";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<FormFieldSetEntity>> localVarReturnType = new GenericType<List<FormFieldSetEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Form Field Set
   * Show Form Field Set
   * @param id Form Field Set ID. (required)
   * @return FormFieldSetEntity
   * @throws ApiException if fails to make API call
   */
  public FormFieldSetEntity getFormFieldSetsId(Integer id) throws ApiException {
    return getFormFieldSetsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Form Field Set
   * Show Form Field Set
   * @param id Form Field Set ID. (required)
   * @return ApiResponse&lt;FormFieldSetEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FormFieldSetEntity> getFormFieldSetsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getFormFieldSetsId");
    }
    
    // create path and map variables
    String localVarPath = "/form_field_sets/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<FormFieldSetEntity> localVarReturnType = new GenericType<FormFieldSetEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Form Field Set
   * Update Form Field Set
   * @param id Form Field Set ID. (required)
   * @param formFieldSets  (required)
   * @return FormFieldSetEntity
   * @throws ApiException if fails to make API call
   */
  public FormFieldSetEntity patchFormFieldSetsId(Integer id, PatchFormFieldSets formFieldSets) throws ApiException {
    return patchFormFieldSetsIdWithHttpInfo(id, formFieldSets).getData();
      }

  /**
   * Update Form Field Set
   * Update Form Field Set
   * @param id Form Field Set ID. (required)
   * @param formFieldSets  (required)
   * @return ApiResponse&lt;FormFieldSetEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FormFieldSetEntity> patchFormFieldSetsIdWithHttpInfo(Integer id, PatchFormFieldSets formFieldSets) throws ApiException {
    Object localVarPostBody = formFieldSets;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchFormFieldSetsId");
    }
    
    // verify the required parameter 'formFieldSets' is set
    if (formFieldSets == null) {
      throw new ApiException(400, "Missing the required parameter 'formFieldSets' when calling patchFormFieldSetsId");
    }
    
    // create path and map variables
    String localVarPath = "/form_field_sets/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FormFieldSetEntity> localVarReturnType = new GenericType<FormFieldSetEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Form Field Set
   * Create Form Field Set
   * @param formFieldSets  (required)
   * @return FormFieldSetEntity
   * @throws ApiException if fails to make API call
   */
  public FormFieldSetEntity postFormFieldSets(PostFormFieldSets formFieldSets) throws ApiException {
    return postFormFieldSetsWithHttpInfo(formFieldSets).getData();
      }

  /**
   * Create Form Field Set
   * Create Form Field Set
   * @param formFieldSets  (required)
   * @return ApiResponse&lt;FormFieldSetEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FormFieldSetEntity> postFormFieldSetsWithHttpInfo(PostFormFieldSets formFieldSets) throws ApiException {
    Object localVarPostBody = formFieldSets;
    
    // verify the required parameter 'formFieldSets' is set
    if (formFieldSets == null) {
      throw new ApiException(400, "Missing the required parameter 'formFieldSets' when calling postFormFieldSets");
    }
    
    // create path and map variables
    String localVarPath = "/form_field_sets";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FormFieldSetEntity> localVarReturnType = new GenericType<FormFieldSetEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
