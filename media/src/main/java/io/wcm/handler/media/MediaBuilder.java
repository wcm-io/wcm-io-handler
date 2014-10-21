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
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlMode;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Define media handling request using builder pattern.
 */
@ProviderType
public interface MediaBuilder {

  /**
   * Sets additional arguments affection media resolving e.g. media formats.
   * @param mediaArgs Media arguments
   * @return Media builder
   */
  MediaBuilder args(MediaArgs mediaArgs);


  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  MediaBuilder mediaFormats(MediaFormat... values);

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #mediaFormatsMandatory(boolean)} is set to true.
   * @param values Media formats
   * @return this
   */
  MediaBuilder mandatoryMediaFormats(MediaFormat... values);

  /**
   * Sets a single media format to resolve to.
   * @param value Media format
   * @return this
   */
  MediaBuilder mediaFormat(MediaFormat value);

  /**
   * If set to true, media handler never returns a dummy image. Otherwise this can happen
   * in edit mode.
   * @param value Resolving of all media formats is mandatory.
   * @return this
   */
  MediaBuilder mediaFormatsMandatory(boolean value);

  /**
   * Sets list of media formats to resolve to.
   * @param values Media format names.
   * @return this
   */
  MediaBuilder mediaFormatNames(String... values);

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #mediaFormatsMandatory(boolean)} is set to true.
   * @param values Media format names.
   * @return this
   */
  MediaBuilder mandatoryMediaFormatNames(String... values);

  /**
   * Sets a single media format to resolve to.
   * @param value Media format name
   * @return this
   */
  MediaBuilder mediaFormatName(String value);

  /**
   * @param values File extensions
   * @return this
   */
  MediaBuilder fileExtensions(String... values);

  /**
   * @param value File extension
   * @return this
   */
  MediaBuilder fileExtension(String value);

  /**
   * @param value URS mode
   * @return this
   */
  MediaBuilder urlMode(UrlMode value);

  /**
   * Use fixed width instead of width from media format or original image
   * @param value Fixed width
   * @return this
   */
  MediaBuilder fixedWidth(long value);

  /**
   * Use fixed height instead of width from media format or original image
   * @param value Fixed height
   * @return this
   */
  MediaBuilder fixedHeight(long value);

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param widthValue Fixed width
   * @param heightValue Fixed height
   * @return this
   */
  MediaBuilder fixedDimension(long widthValue, long heightValue);

  /**
   * @param value Whether to set a "Content-Disposition" header for forcing a "Save as" dialog on the client
   * @return this
   */
  MediaBuilder forceDownload(boolean value);

  /**
   * Allows to specify a custom alternative text that is to be used instead of the one defined in the the media lib item
   * @param value Custom alternative text. If null or empty, the default alt text from media library is used.
   * @return this
   */
  MediaBuilder altText(String value);

  /**
   * @param value If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  MediaBuilder dummyImage(boolean value);

  /**
   * @param value Url of custom dummy image. If null default dummy image is used.
   * @return this
   */
  MediaBuilder dummyImageUrl(String value);

  /**
   * Sets the name of the property from which the media request is read, or node name for inline media.
   * @param refProperty Property or node name
   * @return Media builder
   */
  MediaBuilder refProperty(String refProperty);

  /**
   * Set the name of the property which contains the cropping parameters.
   * @param cropProperty Property name
   * @return Media builder
   */
  MediaBuilder cropProperty(String cropProperty);

  /**
   * Resolve media and return metadata objects that contains all results.
   * @return Media metadata object. Never null, if the resolving failed the isValid() method returns false.
   */
  Media build();

  /**
   * Resolve media and return directly the markup generated by the media markup builder.
   * @return HTML markup string (may by an img, div or any other element) or null if resolving was not successful.
   */
  String buildMarkup();

  /**
   * Resolve media and return directly the markup as DOM element generated by the media markup builder.
   * @return HTML element (may by an img, div or any other element) or null if resolving was not successful.
   */
  HtmlElement<?> buildElement();

  /**
   * Resolve media and get URL to reference it directly.
   * @return URL pointing to media object or null if resolving was not successful.
   */
  String buildUrl();

}
