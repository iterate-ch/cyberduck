package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.AMetadataTemplateUpdateOperation;
import ch.cyberduck.core.box.io.swagger.client.model.Body26;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataTemplate;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataTemplates;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class MetadataTemplatesApi {
  private ApiClient apiClient;

  public MetadataTemplatesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MetadataTemplatesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove metadata template
   * Delete a metadata template and its instances. This deletion is permanent and can not be reversed.
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteMetadataTemplatesIdIdSchema(String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling deleteMetadataTemplatesIdIdSchema");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling deleteMetadataTemplatesIdIdSchema");
    }
    // create path and map variables
    String localVarPath = "/metadata_templates/{scope}/{template_key}/schema"
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Find metadata template by instance ID
   * Finds a metadata template by searching for the ID of an instance of the template.
   * @param metadataInstanceId The ID of an instance of the metadata template to find. (required)
   * @return MetadataTemplates
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplates getMetadataTemplates(UUID metadataInstanceId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'metadataInstanceId' is set
    if (metadataInstanceId == null) {
      throw new ApiException(400, "Missing the required parameter 'metadataInstanceId' when calling getMetadataTemplates");
    }
    // create path and map variables
    String localVarPath = "/metadata_templates";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "metadata_instance_id", metadataInstanceId));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplates> localVarReturnType = new GenericType<MetadataTemplates>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List all metadata templates for enterprise
   * Used to retrieve all metadata templates created to be used specifically within the user&#x27;s enterprise
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return MetadataTemplates
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplates getMetadataTemplatesEnterprise(String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplates> localVarReturnType = new GenericType<MetadataTemplates>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List all global metadata templates
   * Used to retrieve all generic, global metadata templates available to all enterprises using Box.
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return MetadataTemplates
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplates getMetadataTemplatesGlobal(String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/metadata_templates/global";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplates> localVarReturnType = new GenericType<MetadataTemplates>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get metadata template by ID
   * Retrieves a metadata template by its ID.
   * @param templateId The ID of the template (required)
   * @return MetadataTemplate
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplate getMetadataTemplatesId(String templateId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'templateId' is set
    if (templateId == null) {
      throw new ApiException(400, "Missing the required parameter 'templateId' when calling getMetadataTemplatesId");
    }
    // create path and map variables
    String localVarPath = "/metadata_templates/{template_id}"
      .replaceAll("\\{" + "template_id" + "\\}", apiClient.escapeString(templateId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplate> localVarReturnType = new GenericType<MetadataTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get metadata template by name
   * Retrieves a metadata template by its &#x60;scope&#x60; and &#x60;templateKey&#x60; values.  To find the &#x60;scope&#x60; and &#x60;templateKey&#x60; for a template, list all templates for an enterprise or globally, or list all templates applied to a file or folder.
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @return MetadataTemplate
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplate getMetadataTemplatesIdIdSchema(String scope, String templateKey) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling getMetadataTemplatesIdIdSchema");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling getMetadataTemplatesIdIdSchema");
    }
    // create path and map variables
    String localVarPath = "/metadata_templates/{scope}/{template_key}/schema"
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplate> localVarReturnType = new GenericType<MetadataTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create metadata template
   * Creates a new metadata template that can be applied to files and folders.
   * @param body  (optional)
   * @return MetadataTemplate
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplate postMetadataTemplatesSchema(Body26 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_templates/schema";

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplate> localVarReturnType = new GenericType<MetadataTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update metadata template
   * Updates a metadata template.  The metadata template can only be updated if the template already exists.  The update is applied atomically. If any errors occur during the application of the operations, the metadata template will not be changed.
   * @param scope The scope of the metadata template (required)
   * @param templateKey The name of the metadata template (required)
   * @param body  (optional)
   * @return MetadataTemplate
   * @throws ApiException if fails to make API call
   */
  public MetadataTemplate putMetadataTemplatesIdIdSchema(String scope, String templateKey, List<AMetadataTemplateUpdateOperation> body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'scope' is set
    if (scope == null) {
      throw new ApiException(400, "Missing the required parameter 'scope' when calling putMetadataTemplatesIdIdSchema");
    }
    // verify the required parameter 'templateKey' is set
    if (templateKey == null) {
      throw new ApiException(400, "Missing the required parameter 'templateKey' when calling putMetadataTemplatesIdIdSchema");
    }
    // create path and map variables
    String localVarPath = "/metadata_templates/{scope}/{template_key}/schema"
      .replaceAll("\\{" + "scope" + "\\}", apiClient.escapeString(scope.toString()))
      .replaceAll("\\{" + "template_key" + "\\}", apiClient.escapeString(templateKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json-patch+json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataTemplate> localVarReturnType = new GenericType<MetadataTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
