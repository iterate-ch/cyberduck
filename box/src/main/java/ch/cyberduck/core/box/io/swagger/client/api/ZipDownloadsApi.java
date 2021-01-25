package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.ZipDownload;
import ch.cyberduck.core.box.io.swagger.client.model.ZipDownloadRequest;
import ch.cyberduck.core.box.io.swagger.client.model.ZipDownloadStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class ZipDownloadsApi {
  private ApiClient apiClient;

  public ZipDownloadsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ZipDownloadsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Download zip archive
   * Returns the contents of a &#x60;zip&#x60; archive in binary format. This URL does not require any form of authentication and could be used in a user&#x27;s browser to download the archive to a user&#x27;s device.  By default, this URL is only valid for a few seconds from the creation of the request for this archive. Once a download has started it can not be stopped and resumed, instead a new request for a zip archive would need to be created.  The URL of this endpoint should not be considered as fixed. Instead, use the [Create zip download](e://post_zip_downloads) API to request to create a &#x60;zip&#x60; archive, and then follow the &#x60;download_url&#x60; field in the response to this endpoint.
   * @param zipDownloadId The unique identifier that represent this &#x60;zip&#x60; archive. (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File getZipDownloadsIdContent(String zipDownloadId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'zipDownloadId' is set
    if (zipDownloadId == null) {
      throw new ApiException(400, "Missing the required parameter 'zipDownloadId' when calling getZipDownloadsIdContent");
    }
    // create path and map variables
    String localVarPath = "/zip_downloads/{zip_download_id}/content"
      .replaceAll("\\{" + "zip_download_id" + "\\}", apiClient.escapeString(zipDownloadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/octet-stream", "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get zip download status
   * Returns the download status of a &#x60;zip&#x60; archive, allowing an application to inspect the progress of the download as well as the number of items that might have been skipped.  This endpoint can only be accessed once the download has started. Subsequently this endpoint is valid for 12 hours from the start of the download.  The URL of this endpoint should not be considered as fixed. Instead, use the [Create zip download](e://post_zip_downloads) API to request to create a &#x60;zip&#x60; archive, and then follow the &#x60;status_url&#x60; field in the response to this endpoint.
   * @param zipDownloadId The unique identifier that represent this &#x60;zip&#x60; archive. (required)
   * @return ZipDownloadStatus
   * @throws ApiException if fails to make API call
   */
  public ZipDownloadStatus getZipDownloadsIdStatus(String zipDownloadId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'zipDownloadId' is set
    if (zipDownloadId == null) {
      throw new ApiException(400, "Missing the required parameter 'zipDownloadId' when calling getZipDownloadsIdStatus");
    }
    // create path and map variables
    String localVarPath = "/zip_downloads/{zip_download_id}/status"
      .replaceAll("\\{" + "zip_download_id" + "\\}", apiClient.escapeString(zipDownloadId.toString()));

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

    GenericType<ZipDownloadStatus> localVarReturnType = new GenericType<ZipDownloadStatus>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create zip download
   * Creates a request to download multiple files and folders as a single &#x60;zip&#x60; archive file. This API does not return the archive but instead performs all the checks to ensure that the user has access to all the items, and then returns a &#x60;download_url&#x60; and a &#x60;status_url&#x60; that can be used to download the archive.  The limit for an archive is either 32GB or 10,000 files, whichever limitation is met first.
   * @param body  (optional)
   * @return ZipDownload
   * @throws ApiException if fails to make API call
   */
  public ZipDownload postZipDownloads(ZipDownloadRequest body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/zip_downloads";

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

    GenericType<ZipDownload> localVarReturnType = new GenericType<ZipDownload>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
