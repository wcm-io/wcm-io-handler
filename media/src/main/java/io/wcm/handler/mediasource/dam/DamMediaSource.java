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
package io.wcm.handler.mediasource.dam;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.editcontext.DropTargetImpl;
import io.wcm.handler.media.MediaArgsType;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaItem;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaReference;
import io.wcm.handler.media.source.AbstractMediaSource;
import io.wcm.handler.mediasource.dam.impl.DamMediaItem;
import io.wcm.sling.models.annotations.AemObject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.text.Text;

/**
 * Default implementation for media references to media items stored in CQ5 DAM.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public final class DamMediaSource extends AbstractMediaSource {

  @Self
  private Adaptable adaptable;
  @SlingObject
  private ResourceResolver resourceResolver;
  @SlingObject
  private Resource resource;
  @AemObject
  private WCMMode wcmMode;
  @AemObject(optional = true)
  private ComponentContext componentContext;

  /**
   * Media source ID
   */
  public static final String ID = "dam";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean accepts(String mediaRef) {
    return StringUtils.startsWith(mediaRef, "/content/dam/");
  }

  @Override
  public String getPrimaryMediaRefProperty() {
    return MediaNameConstants.PN_MEDIA_REF;
  }

  @Override
  public MediaMetadata resolveMedia(MediaMetadata mediaMetadata) {
    String mediaRef = getMediaRef(mediaMetadata.getMediaReference());
    MediaArgsType mediaArgs = mediaMetadata.getMediaReference().getMediaArgs();

    if (StringUtils.isNotBlank(mediaRef)) {

      // Check if there is a custom altText specified in the component's properties
      if (StringUtils.isEmpty(mediaArgs.getAltText())
          && mediaMetadata.getMediaReference().getResource() != null) {
        ValueMap props = mediaMetadata.getMediaReference().getResource().getValueMap();
        mediaArgs.setAltText(props.get(MediaNameConstants.PN_MEDIA_ALTTEXT, String.class));
      }

      // Check for crop dimensions
      mediaMetadata.setCropDimension(getMediaCropDimension(mediaMetadata.getMediaReference()));

      // get DAM Asset to check for available renditions
      Asset asset = null;
      Resource assetResource = resourceResolver.getResource(mediaRef);
      if (assetResource != null) {
        asset = assetResource.adaptTo(Asset.class);
      }
      if (asset != null) {
        MediaItem mediaItem = new DamMediaItem(asset, mediaMetadata, adaptable);
        mediaMetadata.setMediaItem(mediaItem);

        // resolve rendition
        mediaMetadata.setRendition(mediaItem.getRendition(mediaArgs));
      }

      // set media url
      if (mediaMetadata.getRendition() != null) {
        mediaMetadata.setMediaUrl(mediaMetadata.getRendition().getMediaUrl());
      }

    }

    // set media invalid reason
    if (StringUtils.isEmpty(mediaMetadata.getMediaUrl())) {
      if (mediaMetadata.getMediaItem() != null) {
        mediaMetadata.setMediaInvalidReason(MediaInvalidReason.NO_MATCHING_RENDITION);
      }
      else if (StringUtils.isNotEmpty(mediaRef)) {
        mediaMetadata.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
      }
      else {
        mediaMetadata.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_MISSING);
      }
    }

    return mediaMetadata;
  }

  @Override
  public void enableMediaDrop(HtmlElement element, MediaReference mediaReference) {
    if (wcmMode == WCMMode.DISABLED) {
      return;
    }

    String refProperty = getMediaRefProperty(mediaReference);
    if (!StringUtils.startsWith(refProperty, "./")) {
      refProperty = "./" + refProperty;
    }

    String cropProperty = getMediaCropProperty(mediaReference);
    if (!StringUtils.startsWith(cropProperty, "./")) {
      cropProperty = "./" + cropProperty;
    }

    String name = refProperty;
    if (StringUtils.contains(name, "/")) {
      name = Text.getName(name);
    }

    if (componentContext != null && componentContext.getEditContext() != null) {
      Component componentDefinition = WCMUtils.getComponent(resource);

      // set drop target - with path of current component as default resource type
      Map<String, String> params = new HashMap<String, String>();
      if (componentDefinition != null) {
        params.put("./" + ResourceResolver.PROPERTY_RESOURCE_TYPE, componentDefinition.getPath());

        // clear cropping parameters if a new image is inserted via drag&drop
        params.put(cropProperty, "");
      }

      DropTarget dropTarget = new DropTargetImpl(name, refProperty).setAccept(new String[] {
          "image/.*" // allow all image mime types
      }).setGroups(new String[] {
          "media" // allow drop from DAM contentfinder tab
      }).setParameters(params);

      componentContext.getEditContext().getEditConfig().getDropTargets().put(dropTarget.getId(), dropTarget);
    }
    if (element != null) {
      element.addCssClass(DropTarget.CSS_CLASS_PREFIX + name);
    }
  }

}
