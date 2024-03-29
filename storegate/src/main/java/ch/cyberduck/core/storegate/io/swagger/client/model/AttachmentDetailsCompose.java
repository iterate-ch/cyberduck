/*
 * Storegate api v4.2
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v4.2
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.storegate.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 */
@ApiModel(description = "")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class AttachmentDetailsCompose {
  @JsonProperty("attachmentType")
  private String attachmentType = null;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("isInline")
  private Boolean isInline = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("size")
  private Integer size = null;

  @JsonProperty("url")
  private String url = null;

  public AttachmentDetailsCompose attachmentType(String attachmentType) {
    this.attachmentType = attachmentType;
    return this;
  }

   /**
   * 
   * @return attachmentType
  **/
  @ApiModelProperty(value = "")
  public String getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(String attachmentType) {
    this.attachmentType = attachmentType;
  }

  public AttachmentDetailsCompose id(String id) {
    this.id = id;
    return this;
  }

   /**
   * 
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AttachmentDetailsCompose isInline(Boolean isInline) {
    this.isInline = isInline;
    return this;
  }

   /**
   * 
   * @return isInline
  **/
  @ApiModelProperty(value = "")
  public Boolean isIsInline() {
    return isInline;
  }

  public void setIsInline(Boolean isInline) {
    this.isInline = isInline;
  }

  public AttachmentDetailsCompose name(String name) {
    this.name = name;
    return this;
  }

   /**
   * 
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AttachmentDetailsCompose size(Integer size) {
    this.size = size;
    return this;
  }

   /**
   * 
   * @return size
  **/
  @ApiModelProperty(value = "")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public AttachmentDetailsCompose url(String url) {
    this.url = url;
    return this;
  }

   /**
   * 
   * @return url
  **/
  @ApiModelProperty(value = "")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttachmentDetailsCompose attachmentDetailsCompose = (AttachmentDetailsCompose) o;
    return Objects.equals(this.attachmentType, attachmentDetailsCompose.attachmentType) &&
        Objects.equals(this.id, attachmentDetailsCompose.id) &&
        Objects.equals(this.isInline, attachmentDetailsCompose.isInline) &&
        Objects.equals(this.name, attachmentDetailsCompose.name) &&
        Objects.equals(this.size, attachmentDetailsCompose.size) &&
        Objects.equals(this.url, attachmentDetailsCompose.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attachmentType, id, isInline, name, size, url);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AttachmentDetailsCompose {\n");
    
    sb.append("    attachmentType: ").append(toIndentedString(attachmentType)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    isInline: ").append(toIndentedString(isInline)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

