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
package io.wcm.handler.media.spi;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaRequest;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Defines a media source supported by {@link MediaHandler}.
 * <p>
 * This interface has to be implemented by a Sling Model class, optional with @Application annotation. The adaptables
 * should be {@link org.apache.sling.api.SlingHttpServletRequest} and {@link org.apache.sling.api.resource.Resource}.
 * </p>
 */
@ConsumerType
public interface MediaSource {

  /**
   * @return Media source ID
   */
  String getId();

  /**
   * @return Name of the property in which the primary media request is stored
   */
  String getPrimaryMediaRefProperty();

  /**
   * Checks whether a media request can be handled by this media source
   * @param mediaRequest Media request
   * @return true if this media source can handle the given media request
   */
  boolean accepts(MediaRequest mediaRequest);

  /**
   * Checks whether a media request string can be handled by this media source
   * @param mediaRef Media request string
   * @return true if this media source can handle the given media request
   */
  boolean accepts(String mediaRef);

  /**
   * Resolves a media request
   * @param media Media metadata
   * @return Resolved media metadata. Never null.
   */
  Media resolveMedia(Media media);

  /**
   * Create a ExtJS drop area for given HTML element to enable drag&drop of media library items
   * from content finder to this element.
   * @param element Html element
   * @param mediaRequest Media request to detect media args and property names
   */
  void enableMediaDrop(HtmlElement<?> element, MediaRequest mediaRequest);

}
