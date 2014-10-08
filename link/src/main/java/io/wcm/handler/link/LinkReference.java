/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.link;

import io.wcm.handler.link.args.LinkArgsType;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.wcm.api.Page;

/**
 * Defines a link reference
 */
public final class LinkReference implements Cloneable {

  private Resource resource;
  private ValueMap resourceProperties;
  private Page page;
  private LinkArgsType linkArgs;

  /**
   * @param resource Resource with target link properties
   */
  public LinkReference(Resource resource) {
    this.resource = resource;
  }

  /**
   * @param resource Resource with target link properties
   * @param linkArgs Link args
   */
  public LinkReference(Resource resource, LinkArgsType linkArgs) {
    this.resource = resource;
    this.linkArgs = linkArgs;
  }

  /**
   * @param page Target page of the link
   */
  public LinkReference(Page page) {
    this.page = page;
  }

  /**
   * @param page Target page of the link
   * @param linkArgs Link args
   */
  public LinkReference(Page page, LinkArgsType linkArgs) {
    this.page = page;
    this.linkArgs = linkArgs;
  }

  /**
   * @return Resource with target link properties
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @param resource Resource with target link properties
   */
  public void setResource(Resource resource) {
    this.resource = resource;
    // clear "cached" resource properties map as well
    this.resourceProperties = null;
  }

  /**
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<String, Object>());
      if (this.resource != null) {
        ValueMap props = this.resource.adaptTo(ValueMap.class);
        if (props != null) {
          this.resourceProperties.putAll(props);
        }
      }
    }
    return this.resourceProperties;
  }

  /**
   * @return Target page of the link
   */
  public Page getPage() {
    return this.page;
  }

  /**
   * @param page Target page of the link
   */
  public void setPage(Page page) {
    this.page = page;
  }

  /**
   * @return Link arguments
   */
  public LinkArgsType getLinkArgs() {
    return this.linkArgs;
  }

  /**
   * @param linkArgs Link arguments
   */
  public void setLinkArgs(LinkArgsType linkArgs) {
    this.linkArgs = linkArgs;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    LinkReference clone = (LinkReference)super.clone();

    // explicitly clone LinkArgs contents (primitive properties are properly cloned by super.clone()
    if (getLinkArgs() != null) {
      clone.setLinkArgs((LinkArgsType)getLinkArgs().clone());
    }

    return clone;
  }

}
