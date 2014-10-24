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
package io.wcm.handler.mediasource.inline;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.jcr.JcrBinary;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.spi.helpers.AbstractMediaSource;
import io.wcm.sling.commons.util.Escape;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Default implementation for media references to binaries stored in a node inside the content page.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class InlineMediaSource extends AbstractMediaSource {

  @Self
  private Adaptable adaptable;
  @OSGiService(optional = true)
  private MimeTypeService mimeTypeService;

  /**
   * Media source ID
   */
  public static final String ID = "inline";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean accepts(MediaRequest mediaRequest) {
    // if no media source id is defined fallback to auto-detection of inline media object in resource
    String mediaSourceId = mediaRequest.getResourceProperties().get(MediaNameConstants.PN_MEDIA_SOURCE, String.class);
    if (StringUtils.isEmpty(mediaSourceId)) {
      // accept for inline media if "mediaInline" child node is present
      return getMediaInlineResource(mediaRequest) != null;
    }
    else {
      return super.accepts(mediaRequest);
    }
  }

  @Override
  public boolean accepts(String mediaRef) {
    // not supported
    return false;
  }

  @Override
  public String getPrimaryMediaRefProperty() {
    // not supported
    return null;
  }

  @Override
  public Media resolveMedia(Media media) {
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();

    // the resource that was referenced originally (and may contain additional attributes)
    Resource referencedResource = media.getMediaRequest().getResource();
    Resource ntFileResource = null;
    Resource ntResourceResource = null;

    // get and check resource holding binary data (with primary node type nt:resource)
    Resource mediaInlineResource = getMediaInlineResource(media.getMediaRequest());
    if (mediaInlineResource != null) {
      if (JcrBinary.isNtFile(mediaInlineResource)) {
        ntFileResource = mediaInlineResource;
        ntResourceResource = mediaInlineResource.getChild(JcrConstants.JCR_CONTENT);
      }
      else if (JcrBinary.isNtResource(mediaInlineResource)) {
        ntResourceResource = mediaInlineResource;
      }
    }

    // skip further processing if nor binary resource found
    if (ntResourceResource == null) {
      media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
      return media;
    }

    // Alternative text - if there is a custom alt text specified in the media args then use that one
    if (StringUtils.isEmpty(mediaArgs.getAltText()) && referencedResource != null) {
      // otherwise check if there is a custom altText specified in the component's properties
      ValueMap props = referencedResource.getValueMap();
      mediaArgs.altText(props.get(MediaNameConstants.PN_MEDIA_ALTTEXT, String.class));
    }

    // Check for crop dimensions
    media.setCropDimension(getMediaCropDimension(media.getMediaRequest()));

    // detect and clean up file name
    String fileName = detectFileName(referencedResource, ntFileResource, ntResourceResource);
    fileName = cleanupFileName(fileName);

    // generate media item and rendition for inline media
    Asset asset = getInlineAsset(ntResourceResource, media, fileName);
    media.setAsset(asset);

    // resolve rendition
    boolean renditionsResolved = resolveRenditions(media, asset, mediaArgs);

    // set media invalid reason
    if (!renditionsResolved) {
      if (media.getRenditions().isEmpty()) {
        media.setMediaInvalidReason(MediaInvalidReason.NO_MATCHING_RENDITION);
      }
      else {
        media.setMediaInvalidReason(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS);
      }
    }

    return media;
  }

  /**
   * Get implementation of inline media item
   * @param ntResourceResource nt:resource node
   * @param media Media metadata
   * @param fileName File name
   * @return Inline media item instance
   */
  private Asset getInlineAsset(Resource ntResourceResource, Media media, String fileName) {
    return new InlineAsset(ntResourceResource, media, fileName, adaptable);
  }

  /**
   * Detect filename for inline binary.
   * @param referencedResource Resource that was referenced in media reference and may contain file name property.
   * @param ntFileResource nt:file resource (optional, null if not existent)
   * @param ntResourceResource nt:resource resource
   * @return Detected or virtual filename. Never null.
   */
  private String detectFileName(Resource referencedResource, Resource ntFileResource,
      Resource ntResourceResource) {
    // detect file name
    String fileName = null;
    // if referenced resource is not the nt:file node check for <nodename>Name property
    if (ntFileResource != null && !referencedResource.equals(ntFileResource)) {
      fileName = referencedResource.getValueMap().get(ntFileResource.getName() + "Name", String.class);
    }
    // if not nt:file node exists and the referenced resource is not the nt:resource node check for <nodename>Name property
    else if (ntFileResource == null && !referencedResource.equals(ntResourceResource)) {
      fileName = referencedResource.getValueMap().get(ntResourceResource.getName() + "Name", String.class);
    }
    // otherwise use node name of nt:file resource if it exists
    else if (ntFileResource != null) {
      fileName = ntFileResource.getName();
    }
    // make sure filename has an extension, otherwise build virtual file name
    if (!StringUtils.contains(fileName, ".")) {
      fileName = null;
    }

    // if no filename found detect extension from mime type and build virtual filename
    if (StringUtils.isBlank(fileName)) {
      String fileExtension = null;
      if (ntResourceResource != null) {
        String mimeType = ntResourceResource.getValueMap().get(JcrConstants.JCR_MIMETYPE, String.class);
        if (StringUtils.isNotEmpty(mimeType) && mimeTypeService != null) {
          fileExtension = mimeTypeService.getExtension(mimeType);
        }
      }
      if (StringUtils.isEmpty(fileExtension)) {
        fileExtension = "bin";
      }
      fileName = "file." + fileExtension;
    }

    return fileName;
  }

  /**
   * Make sure filename contains no invalid characters or path parts
   * @param fileName File name
   * @return Cleaned up file name
   */
  private String cleanupFileName(String fileName) {
    String processedFileName = fileName;

    // strip off path parts
    if (StringUtils.contains(processedFileName, "/")) {
      processedFileName = StringUtils.substringAfterLast(processedFileName, "/");
    }
    if (StringUtils.contains(processedFileName, "\\")) {
      processedFileName = StringUtils.substringAfterLast(processedFileName, "\\");
    }

    // make sure filename does not contain any invalid characters
    processedFileName = Escape.validFilename(processedFileName);

    return processedFileName;
  }

  /**
   * Get resource with media inline data (nt:file node).
   * @param mediaRequest Media reference
   * @return Resource or null if not present
   */
  private Resource getMediaInlineResource(MediaRequest mediaRequest) {
    Resource resource = mediaRequest.getResource();
    if (resource == null) {
      return null;
    }

    // check if resource itself is a nt:file node
    if (JcrBinary.isNtFileOrResource(resource)) {
      return resource;
    }

    // check if child node exists which is a nt:file node
    String refProperty = StringUtils.defaultString(mediaRequest.getRefProperty(), MediaNameConstants.NN_MEDIA_INLINE);
    Resource mediaInlineResource = resource.getChild(refProperty);
    if (JcrBinary.isNtFileOrResource(mediaInlineResource)) {
      return mediaInlineResource;
    }

    // not found
    return null;
  }

  @Override
  public void enableMediaDrop(HtmlElement element, MediaRequest mediaRequest) {
    // not supported
  }

  @Override
  public String toString() {
    return ID;
  }

}
