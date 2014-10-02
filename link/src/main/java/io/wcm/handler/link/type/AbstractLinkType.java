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
package io.wcm.handler.link.type;

import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.LinkReference;
import io.wcm.handler.link.LinkType;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common link type functionality
 */
public abstract class AbstractLinkType implements LinkType {

  @Override
  public boolean accepts(LinkReference linkReference) {
    ValueMap props = linkReference.getResourceProperties();
    // check for matching link type ID in link resource
    String linkTypeId = props.get(LinkNameConstants.PN_LINK_TYPE, String.class);
    if (StringUtils.isNotEmpty(linkTypeId)) {
      return StringUtils.equals(linkTypeId, getId());
    }
    // if not link type is set at all check if link ref attribute contains a valid link
    else {
      String linkRef = props.get(getPrimaryLinkRefProperty(), String.class);
      return accepts(linkRef);
    }
  }

}
