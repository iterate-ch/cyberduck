/*
 * ReSTFS
 * ReSTFS Open API 3.0 Spec
 *
 * OpenAPI spec version: 1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.eue.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import ch.cyberduck.core.eue.io.swagger.client.model.SharePermission;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * ShareModel
 */


public class ShareModel {
  @JsonProperty("ownerName")
  private String ownerName = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("expirationMillis")
  private Long expirationMillis = null;

  @JsonProperty("resourcePermission")
  private SharePermission resourcePermission = null;

  @JsonProperty("hasPin")
  private Boolean hasPin = null;

  public ShareModel ownerName(String ownerName) {
    this.ownerName = ownerName;
    return this;
  }

   /**
   * Get ownerName
   * @return ownerName
  **/
  @Schema(description = "")
  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public ShareModel name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @Schema(description = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ShareModel expirationMillis(Long expirationMillis) {
    this.expirationMillis = expirationMillis;
    return this;
  }

   /**
   * Get expirationMillis
   * @return expirationMillis
  **/
  @Schema(description = "")
  public Long getExpirationMillis() {
    return expirationMillis;
  }

  public void setExpirationMillis(Long expirationMillis) {
    this.expirationMillis = expirationMillis;
  }

  public ShareModel resourcePermission(SharePermission resourcePermission) {
    this.resourcePermission = resourcePermission;
    return this;
  }

   /**
   * Get resourcePermission
   * @return resourcePermission
  **/
  @Schema(description = "")
  public SharePermission getResourcePermission() {
    return resourcePermission;
  }

  public void setResourcePermission(SharePermission resourcePermission) {
    this.resourcePermission = resourcePermission;
  }

  public ShareModel hasPin(Boolean hasPin) {
    this.hasPin = hasPin;
    return this;
  }

   /**
   * Get hasPin
   * @return hasPin
  **/
  @Schema(description = "")
  public Boolean isHasPin() {
    return hasPin;
  }

  public void setHasPin(Boolean hasPin) {
    this.hasPin = hasPin;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ShareModel shareModel = (ShareModel) o;
    return Objects.equals(this.ownerName, shareModel.ownerName) &&
        Objects.equals(this.name, shareModel.name) &&
        Objects.equals(this.expirationMillis, shareModel.expirationMillis) &&
        Objects.equals(this.resourcePermission, shareModel.resourcePermission) &&
        Objects.equals(this.hasPin, shareModel.hasPin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerName, name, expirationMillis, resourcePermission, hasPin);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ShareModel {\n");
    
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    expirationMillis: ").append(toIndentedString(expirationMillis)).append("\n");
    sb.append("    resourcePermission: ").append(toIndentedString(resourcePermission)).append("\n");
    sb.append("    hasPin: ").append(toIndentedString(hasPin)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
