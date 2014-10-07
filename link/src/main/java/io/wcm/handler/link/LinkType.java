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

/**
 * Defines a link type supported by {@link LinkHandler}
 */
public interface LinkType {

  /**
   * @return Link type ID (is stored as identifier in repository)
   */
  String getId();

  /**
   * @return Name of the property in which the primary link reference is stored
   */
  String getPrimaryLinkRefProperty();

  /**
   * Checks whether a link reference can be handled by this link type
   * @param linkReference Link reference
   * @return true if this link type can handle the given link reference
   */
  boolean accepts(LinkReference linkReference);

  /**
   * Checks whether a link reference string can be handled by this link type
   * @param linkRef Link reference string
   * @return true if this link type can handle the given link reference
   */
  boolean accepts(String linkRef);

  /**
   * Resolves a link
   * @param linkMetadata Link metadata
   * @return Resolved link metadata. Never null.
   */
  LinkMetadata resolveLink(LinkMetadata linkMetadata);

}
