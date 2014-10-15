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
package io.wcm.handler.richtext;

import io.wcm.handler.media.args.MediaArgsType;
import io.wcm.handler.url.UrlMode;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Holds all properties that are part of a rich text handling request.
 */
@ProviderType
public final class RichTextRequest {

  private final Resource resource;
  private final String text;
  private final UrlMode urlMode;
  private final TextMode textMode;
  private final MediaArgsType mediaArgs;

  private ValueMap resourceProperties;

  /**
   * @param resource Resource containing rich text
   * @param text Raw rich text
   * @param urlMode URL mode
   * @param textMode Text mode
   * @param mediaArgs Media arguments
   */
  public RichTextRequest(Resource resource, String text, UrlMode urlMode, TextMode textMode, MediaArgsType mediaArgs) {
    this.resource = resource;
    this.text = text;
    this.urlMode = urlMode;
    this.textMode = textMode;
    this.mediaArgs = mediaArgs;

    // validate parameters
    if (this.resource != null && this.text != null) {
      throw new IllegalArgumentException("Set resource or text, not both.");
    }
  }

  /**
   * @return Resource containing rich text
   */
  public Resource getResource() {
    return this.resource;
  }

  /**
   * @return Raw rich text
   */
  public String getText() {
    return this.text;
  }

  /**
   * @return URL mode
   */
  public UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @return Text mode
   */
  public TextMode getTextMode() {
    return this.textMode;
  }

  /**
   * @return Media arguments
   */
  public MediaArgsType getMediaArgs() {
    return this.mediaArgs;
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
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

}
