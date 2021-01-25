package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.InlineResponse200;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataFilter;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataQuery;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataQueryResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class SearchApi {
  private ApiClient apiClient;

  public SearchApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SearchApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Search for content
   * Searches for files, folders, web links, and shared files across the users content or across the entire enterprise.
   * @param query The string to search for. This query is matched against item names, descriptions, text content of files, and various other fields of the different item types.  This parameter supports a variety of operators to further refine the results returns.  * &#x60;\&quot;\&quot;&#x60; - by wrapping a query in double quotes only exact matches are   returned by the API. Exact searches do not return search matches   based on specific character sequences. Instead, they return   matches based on phrases, that is, word sequences. For example:   A search for &#x60;\&quot;Blue-Box\&quot;&#x60; may return search results including   the sequence &#x60;\&quot;blue.box\&quot;&#x60;, &#x60;\&quot;Blue Box\&quot;&#x60;, and &#x60;\&quot;Blue-Box\&quot;&#x60;;   any item containing the words &#x60;Blue&#x60; and &#x60;Box&#x60; consecutively, in   the order specified. * &#x60;AND&#x60; - returns items that contain both the search terms. For   example, a search for &#x60;marketing AND BoxWorks&#x60; returns items   that have both &#x60;marketing&#x60; and &#x60;BoxWorks&#x60; within its text in any order.   It does not return a result that only has &#x60;BoxWorks&#x60; in its text. * &#x60;OR&#x60; - returns items that contain either of the search terms. For   example, a search for &#x60;marketing OR BoxWorks&#x60; returns a result that   has either &#x60;marketing&#x60; or &#x60;BoxWorks&#x60; within its text. Using this   operator is not necessary as we implicitly interpret multi-word   queries as &#x60;OR&#x60; unless another supported boolean term is used. * &#x60;NOT&#x60; - returns items that do not contain the search term provided.   For example, a search for &#x60;marketing AND NOT BoxWorks&#x60; returns a result   that has only &#x60;marketing&#x60; within its text. Results containing   &#x60;BoxWorks&#x60; are omitted.  Please note that we do not support lower case (that is, &#x60;and&#x60;, &#x60;or&#x60;, and &#x60;not&#x60;) or mixed case (that is, &#x60;And&#x60;, &#x60;Or&#x60;, and &#x60;Not&#x60;) operators.  This field is required unless the &#x60;mdfilters&#x60; parameter is defined. (optional)
   * @param scope Limits the search results to either the files that the user has access to, or to files available to the entire enterprise.  The scope defaults to &#x60;user_content&#x60;, which limits the search results to content that is available to the currently authenticated user.  The &#x60;enterprise_content&#x60; can be requested by an admin through our support channels. Once this scope has been enabled for a user, it will allow that use to query for content across the entire enterprise and not only the content that they have access to. (optional, default to user_content)
   * @param fileExtensions Limits the search results to any files that match any of the provided file extensions. This list is a comma-separated list of file extensions without the dots. (optional)
   * @param createdAtRange Limits the search results to any items created within a given date range.  Date ranges are defined as comma separated RFC3339 timestamps.  If the the start date is omitted (&#x60;,2014-05-17T13:35:01-07:00&#x60;) anything created before the end date will be returned.  If the end date is omitted (&#x60;2014-05-15T13:35:01-07:00,&#x60;) the current date will be used as the end date instead. (optional)
   * @param updatedAtRange Limits the search results to any items updated within a given date range.  Date ranges are defined as comma separated RFC3339 timestamps.  If the start date is omitted (&#x60;,2014-05-17T13:35:01-07:00&#x60;) anything updated before the end date will be returned.  If the end date is omitted (&#x60;2014-05-15T13:35:01-07:00,&#x60;) the current date will be used as the end date instead. (optional)
   * @param sizeRange Limits the search results to any items with a size within a given file size range. This applied to files and folders.  Size ranges are defined as comma separated list of a lower and upper byte size limit (inclusive).  The upper and lower bound can be omitted to create open ranges. (optional)
   * @param ownerUserIds Limits the search results to any items that are owned by the given list of owners, defined as a list of comma separated user IDs.  Please note that the items still need to be owned or shared with the currently authenticated user for them to show up in the search results. If the user does not have access to any files owned by any of the users an empty result set will be returned.  To search across an entire enterprise, we recommend using the &#x60;enterprise_content&#x60; scope parameter which can be requested with our support team. (optional)
   * @param ancestorFolderIds Limits the search results to items within the given list of folders, defined as a comma separated lists of folder IDs.  Search results will also include items within any subfolders of those ancestor folders.  Please note that the folders still need to be owned or shared with the currently authenticated user. If the folder is not accessible by this user, or it does not exist, a &#x60;HTTP 404&#x60; error code will be returned instead.  To search across an entire enterprise, we recommend using the &#x60;enterprise_content&#x60; scope parameter which can be requested with our support team. (optional)
   * @param contentTypes Limits the search results to any items that match the search query for a specific part of the file, for example the file description.  Content types are defined as a comma separated lists of Box recognized content types. The allowed content types are as follows.  * &#x60;name&#x60; - The name of the item, as defined by its &#x60;name&#x60; field. * &#x60;description&#x60; - The description of the item, as defined by its   &#x60;description&#x60; field. * &#x60;file_content&#x60; - The actual content of the file. * &#x60;comments&#x60; - The content of any of the comments on a file or    folder. * &#x60;tag&#x60; - Any tags that are applied to an item, as defined by its    &#x60;tag&#x60; field. (optional)
   * @param type Limits the search results to any items of this type. This parameter only takes one value. By default the API returns items that match any of these types.  * &#x60;file&#x60; - Limits the search results to files * &#x60;folder&#x60; - Limits the search results to folders * &#x60;web_link&#x60; - Limits the search results to web links, also known    as bookmarks (optional)
   * @param trashContent Determines if the search should look in the trash for items.  By default, this API only returns search results for items not currently in the trash (&#x60;non_trashed_only&#x60;).  * &#x60;trashed_only&#x60; - Only searches for items currently in the trash * &#x60;non_trashed_only&#x60; - Only searches for items currently not in   the trash (optional, default to non_trashed_only)
   * @param mdfilters Limits the search results to any items for which the metadata matches the provided filter.  This parameter contains a list of 1 metadata template to filter the search results by. This list can currently only contain one entry, though this might be expanded in the future.  This parameter is required unless the &#x60;query&#x60; parameter is provided. (optional)
   * @param sort Defines the order in which search results are returned. This API defaults to returning items by relevance unless this parameter is explicitly specified.  * &#x60;relevance&#x60; (default) returns the results sorted by relevance to the query search term. The relevance is based on the occurrence of the search term in the items name, description, content, and additional properties. * &#x60;modified_at&#x60; returns the results ordered in descending order by date at which the item was last modified. (optional, default to relevance)
   * @param direction Defines the direction in which search results are ordered. This API defaults to returning items in descending (&#x60;DESC&#x60;) order unless this parameter is explicitly specified.  When results are sorted by &#x60;relevance&#x60; the ordering is locked to returning items in descending order of relevance, and this parameter is ignored. (optional, default to DESC)
   * @param limit Defines the maximum number of items to return as part of a page of results. (optional, default to 30)
   * @param includeRecentSharedLinks Defines whether the search results should include any items that the user recently accessed through a shared link.  Please note that when this parameter has been set to true, the format of the response of this API changes to return a list of [Search Results with Shared Links](r://search_results_with_shared_links) (optional, default to false)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @return InlineResponse200
   * @throws ApiException if fails to make API call
   */
  public InlineResponse200 getSearch(String query, String scope, List<String> fileExtensions, List<String> createdAtRange, List<String> updatedAtRange, List<Integer> sizeRange, List<String> ownerUserIds, List<String> ancestorFolderIds, List<String> contentTypes, String type, String trashContent, List<MetadataFilter> mdfilters, String sort, String direction, Long limit, Boolean includeRecentSharedLinks, List<String> fields, Long offset) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/search";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "query", query));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "scope", scope));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "file_extensions", fileExtensions));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "created_at_range", createdAtRange));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "updated_at_range", updatedAtRange));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "size_range", sizeRange));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "owner_user_ids", ownerUserIds));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "ancestor_folder_ids", ancestorFolderIds));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "content_types", contentTypes));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "trash_content", trashContent));
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mdfilters", mdfilters));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "direction", direction));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_recent_shared_links", includeRecentSharedLinks));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<InlineResponse200> localVarReturnType = new GenericType<InlineResponse200>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Query files/folders by metadata
   * Create a search using SQL-like syntax to return items that match specific metadata.  By default, this endpoint returns only the most basic info about the items for which the query matches. To get additional fields for each item, including any of the metadata, use the &#x60;fields&#x60; attribute in the query.
   * @param body  (optional)
   * @return MetadataQueryResults
   * @throws ApiException if fails to make API call
   */
  public MetadataQueryResults postMetadataQueriesExecuteRead(MetadataQuery body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_queries/execute_read";

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

    GenericType<MetadataQueryResults> localVarReturnType = new GenericType<MetadataQueryResults>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
