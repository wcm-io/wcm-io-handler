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
package io.wcm.handler.media;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.url.UrlMode;

import org.apache.sling.api.resource.Resource;

/**
 * Manages media item resolving and markup generation.
 * General usage: <li>getMedia: Get media markup element. This HTML element can be either a IMG element, or a multimedia
 * markup element like OBJECT or VIDEO, or a custom DIV element containing metadata for frontend progressive
 * enhancement.</li> <li>getMediaUrl: Get URL to directly reference the media item/rendition.</li> <li>getMediaMetadata:
 * Returns both media markup element and URL and additional media resolving metadata.</li>
 */
public interface MediaHandler {

  /**
   * Get media markup element (IMG, DIV or other multimedia markup element) for first rendition.
   * @param mediaRef Path to media library item
   * @return Html element or null if media reference is invalid
   */
  HtmlElement<?> getMedia(String mediaRef);

  /**
   * Get media markup element (IMG, DIV or other multimedia markup element) for best matching rendition.
   * @param mediaRef Path to media library item
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   * @return Html element or null if media reference is invalid
   */
  HtmlElement<?> getMedia(String mediaRef, MediaArgsType mediaArgs);

  /**
   * Get media markup element (IMG, DIV or other multimedia markup element) for best matching rendition.
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text.
   *          The media references is is read from a property named "mediaRef".
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   * @return Html element or null if media reference is invalid
   */
  HtmlElement<?> getMedia(Resource resource, MediaArgsType mediaArgs);

  /**
   * Get media markup element (IMG, DIV or other multimedia markup element) for best matching rendition.
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for
   *          inline media.
   * @param mediaArgs Affects rendition selection, media URL and markup generation
   * @return Html element or null if media reference is invalid
   */
  HtmlElement<?> getMedia(Resource resource, String refProperty, MediaArgsType mediaArgs);

  /**
   * Get externalized media URL of first rendition.
   * @param mediaRef Path to media library item
   * @return Media URL or null if the media reference is invalid
   */
  String getMediaUrl(String mediaRef);

  /**
   * Get externalized media URL of best matching rendition.
   * @param mediaRef Path to media library item
   * @param pUrlMode Controls how the media URL is build
   * @return Media URL or null if the media reference is invalid
   */
  String getMediaUrl(String mediaRef, UrlMode pUrlMode);

  /**
   * Get externalized media URL of best matching rendition.
   * @param mediaRef Path to media library item
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Media URL or null if the media reference is invalid
   */
  String getMediaUrl(String mediaRef, MediaArgsType mediaArgs);

  /**
   * Get externalized media URL of best matching rendition.
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text.
   *          The media references is is read from a property named "mediaRef".
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Media URL or null if the media reference is invalid
   */
  String getMediaUrl(Resource resource, MediaArgsType mediaArgs);

  /**
   * Get externalized media URL of best matching rendition.
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for
   *          inline media.
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Media URL or null if the media reference is invalid
   */
  String getMediaUrl(Resource resource, String refProperty, MediaArgsType mediaArgs);

  /**
   * Get all metadata that can be resolved for this media (first rendition).
   * @param mediaRef Path to media library item
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(String mediaRef);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param mediaRef Path to media library item
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(String mediaRef, MediaArgsType mediaArgs);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text.
   *          The media references is is read from a property named "mediaRef".
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(Resource resource);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text.
   *          The media references is is read from a property named "mediaRef".
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(Resource resource, MediaArgsType mediaArgs);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for
   *          inline media.
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(Resource resource, String refProperty);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param resource Resource containing reference to media library item and optionally further properties like Alt.
   *          text
   *          If the resource contains an inline media element directly within the page this is used instead of a media
   *          reference.
   * @param refProperty Defines the name of the property from which the media reference is read, or node name for
   *          inline media.
   * @param mediaArgs Affects rendition selection and media URL generation
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(Resource resource, String refProperty, MediaArgsType mediaArgs);

  /**
   * Get all metadata that can be resolved for this media (best matching rendition).
   * @param mediaReference Media reference
   * @return Resolved media and rendition metadata. Never null.
   */
  MediaMetadata getMediaMetadata(MediaReference mediaReference);

  /**
   * Checks if the given HTML element is valid.
   * It is treated as invalid if it is null, or if it e.g. contains only a dummy image (depending on markup builder).
   * @param element Media markup element.
   * @return true if media element is invalid
   */
  boolean isValidMedia(HtmlElement<?> element);

}
