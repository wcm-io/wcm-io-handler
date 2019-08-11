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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.url.UrlMode;

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
  @NotNull
  MediaBuilder args(@NotNull MediaArgs mediaArgs);

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormats(@NotNull MediaFormat @NotNull... values);

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #mediaFormatsMandatory(boolean)} is set to true.
   * @param values Media formats
   * @return this
   */
  @NotNull
  MediaBuilder mandatoryMediaFormats(@NotNull MediaFormat @NotNull... values);

  /**
   * Sets a single media format to resolve to.
   * @param value Media format
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormat(@NotNull MediaFormat value);

  /**
   * If set to true, media handler never returns a dummy image. Otherwise this can happen
   * in edit mode.
   * @param value Resolving of all media formats is mandatory.
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormatsMandatory(boolean value);

  /**
   * Sets list of media formats to resolve to.
   * @param values Media format names.
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormatNames(@NotNull String @NotNull... values);

  /**
   * Sets list of media formats to resolve to.
   * Additionally {@link #mediaFormatsMandatory(boolean)} is set to true.
   * @param values Media format names.
   * @return this
   */
  @NotNull
  MediaBuilder mandatoryMediaFormatNames(@NotNull String @NotNull... values);

  /**
   * Sets a single media format to resolve to.
   * @param value Media format name
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormatName(@NotNull String value);

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  @NotNull
  MediaBuilder mediaFormatOptions(@NotNull MediaFormatOption @NotNull... values);

  /**
   * @param value Enables "auto-cropping" mode. If no matching rendition is found
   *          it is tried to generate one by automatically cropping another one.
   * @return this
   */
  @NotNull
  MediaBuilder autoCrop(boolean value);

  /**
   * @param values File extensions
   * @return this
   */
  @NotNull
  MediaBuilder fileExtensions(@NotNull String @NotNull... values);

  /**
   * @param value File extension
   * @return this
   */
  @NotNull
  MediaBuilder fileExtension(@NotNull String value);

  /**
   * @param value URS mode
   * @return this
   */
  @NotNull
  MediaBuilder urlMode(@NotNull UrlMode value);

  /**
   * Use fixed width instead of width from media format or original image
   * @param value Fixed width
   * @return this
   */
  @NotNull
  MediaBuilder fixedWidth(long value);

  /**
   * Use fixed height instead of width from media format or original image
   * @param value Fixed height
   * @return this
   */
  @NotNull
  MediaBuilder fixedHeight(long value);

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param widthValue Fixed width
   * @param heightValue Fixed height
   * @return this
   */
  @NotNull
  MediaBuilder fixedDimension(long widthValue, long heightValue);

  /**
   * @param value Whether to set a "Content-Disposition" header to "attachment"
   *          for forcing a "Save as" dialog on the client
   * @return this
   */
  @NotNull
  MediaBuilder contentDispositionAttachment(boolean value);

  /**
   * Allows to specify a custom alternative text that is to be used instead of the one defined in the the media lib item
   * @param value Custom alternative text. If null or empty, the default alt text from media library is used.
   * @return this
   */
  @NotNull
  MediaBuilder altText(@NotNull String value);

  /**
   * @param value If set to false, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  @NotNull
  MediaBuilder dummyImage(boolean value);

  /**
   * @param value Url of custom dummy image. If null default dummy image is used.
   * @return this
   */
  @NotNull
  MediaBuilder dummyImageUrl(@NotNull String value);

  /**
   * @param value If set to true, thumbnail generated by the DAM asset workflows (with cq5dam.thumbnail prefix) are
   *          taken into account as well when trying to resolve the media request.
   * @return Media builder
   */
  @NotNull
  MediaBuilder includeAssetThumbnails(boolean value);

  /**
   * @param value If set to true, web renditions generated by the DAM asset workflows (with cq5dam.web prefix) are
   *          taken into account as well when trying to resolve the media request.
   * @return Media builder
   */
  @NotNull
  MediaBuilder includeAssetWebRenditions(boolean value);

  /**
   * Drag&amp;Drop support for media builder.
   * @param value Drag&amp;Drop support
   * @return Media builder
   */
  @NotNull
  MediaBuilder dragDropSupport(@NotNull DragDropSupport value);

  /**
   * Adds a custom property that my be used by application-specific markup builders or processors.
   * @param key Property key
   * @param value Property value
   * @return Media builder
   */
  @NotNull
  MediaBuilder property(@NotNull String key, @Nullable Object value);

  /**
   * Apply responsive image handling for the <code>img</code> element based on the primary media format given.
   * If multiple media formats are given the primary media format is the first media format with a ratio.
   * <p>
   * It will add a <code>srcset</code> attribute to the <code>img</code> element with renditions for each width given,
   * and set the <code>sizes</code> attribute to the sizes string given.
   * </p>
   * @param sizes A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
   *          source size list</a>
   * @param widths Widths for the renditions in the <code>srcset</code> attribute (all mandatory).
   *          All renditions will use the ratio of the primary media format.
   * @return this
   */
  @NotNull
  MediaBuilder imageSizes(@NotNull String sizes, long @NotNull... widths);


  /**
   * Apply responsive image handling for the <code>img</code> element based on the primary media format given.
   * If multiple media formats are given the primary media format is the first media format with a ratio.
   * <p>
   * It will add a <code>srcset</code> attribute to the <code>img</code> element with renditions for each width given,
   * and set the <code>sizes</code> attribute to the sizes string given.
   * </p>
   * @param sizes A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
   *          source size list</a>
   * @param widthOptions Widths for the renditions in the <code>srcset</code> attribute.
   *          All renditions will use the ratio of the primary media format.
   * @return this
   */
  @NotNull
  MediaBuilder imageSizes(@NotNull String sizes, @NotNull WidthOption @NotNull... widthOptions);

  /**
   * Apply responsive image handling using <code>picture</code> and <code>source</code> elements.
   * This will add one <code>source</code> element with an <code>media</code> attribute set to the given media
   * string, and a <code>srcset</code> attribute with renditions for each width given based on the given media format.
   * @param pictureSource Picture source element
   * @return this
   */
  @NotNull
  MediaBuilder pictureSource(@NotNull PictureSource pictureSource);

  /**
   * Apply responsive image handling using <code>picture</code> and <code>source</code> elements.
   * This will add one <code>source</code> element with an <code>media</code> attribute set to the given media
   * string, and a <code>srcset</code> attribute with renditions for each width given based on the given media format.
   * @param mediaFormat Media format with ratio for the renditions of the <code>source</code> element
   * @param media A <a href="http://w3c.github.io/html/infrastructure.html#valid-media-query-list">valid media query
   *          list</a>
   * @param widths Widths for the renditions in the <code>srcset</code> attribute.
   *          All renditions will use the ratio of the given media format.
   * @return this
   * @deprecated Use {@link #pictureSource(io.wcm.handler.media.MediaArgs.PictureSource)}
   */
  @Deprecated
  @NotNull
  MediaBuilder pictureSource(@NotNull MediaFormat mediaFormat, @NotNull String media, long @NotNull... widths);

  /**
   * Apply responsive image handling using <code>picture</code> and <code>source</code> elements.
   * This will add one <code>source</code> element without an <code>media</code> attribute, and a <code>srcset</code>
   * attribute with renditions for each width given based on the given media format.
   * @param mediaFormat Media format with ratio for the renditions of the <code>source</code> element
   * @param widths Widths for the renditions in the <code>srcset</code> attribute.
   *          All renditions will use the ratio of the given media format.
   * @return this
   * @deprecated Use {@link #pictureSource(io.wcm.handler.media.MediaArgs.PictureSource)}
   */
  @Deprecated
  @NotNull
  MediaBuilder pictureSource(@NotNull MediaFormat mediaFormat, long @NotNull... widths);

  /**
   * Sets the name of the property from which the media reference path is read, or node name for inline media.
   * @param refProperty Property or node name
   * @return Media builder
   */
  @NotNull
  MediaBuilder refProperty(@NotNull String refProperty);

  /**
   * Set the name of the property which contains the cropping parameters.
   * @param cropProperty Property name
   * @return Media builder
   */
  @NotNull
  MediaBuilder cropProperty(@NotNull String cropProperty);

  /**
   * Set the name of the property which contains the rotation parameter.
   * @param rotationProperty Property name
   * @return Media builder
   */
  @NotNull
  MediaBuilder rotationProperty(@NotNull String rotationProperty);

  /**
   * Set the name of the property which contains the image map data.
   * @param mapProperty Property name
   * @return Media builder
   */
  @NotNull
  MediaBuilder mapProperty(@NotNull String mapProperty);

  /**
   * Resolve media and return metadata objects that contains all results.
   * @return Media metadata object. Never null, if the resolving failed the isValid() method returns false.
   */
  @NotNull
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
