package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body23;
import ch.cyberduck.core.box.io.swagger.client.model.Body24;
import ch.cyberduck.core.box.io.swagger.client.model.Body25;
import ch.cyberduck.core.box.io.swagger.client.model.Body27;
import ch.cyberduck.core.box.io.swagger.client.model.ClassificationTemplate;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class ClassificationsApi {
  private ApiClient apiClient;

  public ClassificationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ClassificationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete all classifications
   * Delete all classifications by deleting the classification metadata template.
   * @throws ApiException if fails to make API call
   */
  public void deleteMetadataTemplatesEnterpriseSecurityClassification6VMVochwUWoSchema() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise/securityClassification-6VMVochwUWo/schema";

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
   * List all classifications
   * Retrieves the classification metadata template and lists all the classifications available to this enterprise.  This API can also be called by including the enterprise ID in the URL explicitly, for example &#x60;/metadata_templates/enterprise_12345/securityClassification-6VMVochwUWo/schema&#x60;.
   * @return ClassificationTemplate
   * @throws ApiException if fails to make API call
   */
  public ClassificationTemplate getMetadataTemplatesEnterpriseSecurityClassification6VMVochwUWoSchema() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise/securityClassification-6VMVochwUWo/schema";

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

    GenericType<ClassificationTemplate> localVarReturnType = new GenericType<ClassificationTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add initial classifications
   * When an enterprise does not yet have any classifications, this API call initializes the classification template with an initial set of classifications.  If an enterprise already has a classification, the template will already exist and instead an API call should be made to add additional classifications.
   * @param body  (optional)
   * @return ClassificationTemplate
   * @throws ApiException if fails to make API call
   */
  public ClassificationTemplate postMetadataTemplatesSchemaClassifications(Body27 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_templates/schema#classifications";

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

    GenericType<ClassificationTemplate> localVarReturnType = new GenericType<ClassificationTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add classification
   * Adds one or more new classifications to the list of classifications available to the enterprise.  This API can also be called by including the enterprise ID in the URL explicitly, for example &#x60;/metadata_templates/enterprise_12345/securityClassification-6VMVochwUWo/schema&#x60;.
   * @param body  (optional)
   * @return ClassificationTemplate
   * @throws ApiException if fails to make API call
   */
  public ClassificationTemplate putMetadataTemplatesEnterpriseSecurityClassification6VMVochwUWoSchemaAdd(List<Body23> body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise/securityClassification-6VMVochwUWo/schema#add";

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

    GenericType<ClassificationTemplate> localVarReturnType = new GenericType<ClassificationTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Delete classification
   * Removes a classification from the list of classifications available to the enterprise.  This API can also be called by including the enterprise ID in the URL explicitly, for example &#x60;/metadata_templates/enterprise_12345/securityClassification-6VMVochwUWo/schema&#x60;.
   * @param body  (optional)
   * @return ClassificationTemplate
   * @throws ApiException if fails to make API call
   */
  public ClassificationTemplate putMetadataTemplatesEnterpriseSecurityClassification6VMVochwUWoSchemaDelete(List<Body25> body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise/securityClassification-6VMVochwUWo/schema#delete";

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

    GenericType<ClassificationTemplate> localVarReturnType = new GenericType<ClassificationTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update classification
   * Updates the labels and descriptions of one or more classifications available to the enterprise.  This API can also be called by including the enterprise ID in the URL explicitly, for example &#x60;/metadata_templates/enterprise_12345/securityClassification-6VMVochwUWo/schema&#x60;.
   * @param body  (optional)
   * @return ClassificationTemplate
   * @throws ApiException if fails to make API call
   */
  public ClassificationTemplate putMetadataTemplatesEnterpriseSecurityClassification6VMVochwUWoSchemaUpdate(List<Body24> body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_templates/enterprise/securityClassification-6VMVochwUWo/schema#update";

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

    GenericType<ClassificationTemplate> localVarReturnType = new GenericType<ClassificationTemplate>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
