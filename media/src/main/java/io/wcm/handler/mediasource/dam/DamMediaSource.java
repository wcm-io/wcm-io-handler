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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.api.components.InplaceEditingConfig;
import com.day.cq.wcm.commons.WCMUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.editcontext.DropTargetImpl;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.MediaRequest.MediaPropertyNames;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.handler.media.impl.ipeconfig.CroppingRatios;
import io.wcm.handler.media.impl.ipeconfig.IPEConfigResourceProvider;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.mediasource.dam.impl.DamAsset;
import io.wcm.sling.models.annotations.AemObject;

/**
 * Default implementation for media requests to media items stored in CQ5 DAM.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class DamMediaSource extends MediaSource {

  @Self
  private Adaptable adaptable;
  @SlingObject
  private ResourceResolver resourceResolver;
  @SlingObject
  private Resource resource;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private WCMMode wcmMode;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private ComponentContext componentContext;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @Self
  private MediaFormatHandler mediaFormatHandler;

  private static final Logger log = LoggerFactory.getLogger(DamMediaSource.class);

  /**
   * Media source ID
   */
  public static final @NotNull String ID = "dam";

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public boolean accepts(@Nullable String mediaRef) {
    return StringUtils.startsWith(mediaRef, "/content/dam/");
  }

  @Override
  public @NotNull String getPrimaryMediaRefProperty() {
    return mediaHandlerConfig.getMediaRefProperty();
  }

  @Override
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public @NotNull Media resolveMedia(@NotNull Media media) {
    String mediaRef = getMediaRef(media.getMediaRequest(), mediaHandlerConfig);
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();

    boolean renditionsResolved = false;
    if (StringUtils.isNotBlank(mediaRef)) {

      // Check if there is a custom altText specified in the component's properties
      if (StringUtils.isEmpty(mediaArgs.getAltText())
          && media.getMediaRequest().getResource() != null) {
        ValueMap props = media.getMediaRequest().getResource().getValueMap();
        mediaArgs.altText(props.get(mediaHandlerConfig.getMediaAltTextProperty(), String.class));
      }

      // Check for transformations
      media.setCropDimension(getMediaCropDimension(media.getMediaRequest(), mediaHandlerConfig));
      media.setRotation(getMediaRotation(media.getMediaRequest(), mediaHandlerConfig));
      media.setMap(getMediaMap(media.getMediaRequest(), mediaHandlerConfig));

      // get DAM Asset to check for available renditions
      com.day.cq.dam.api.Asset damAsset = null;
      Resource assetResource = resourceResolver.getResource(mediaRef);
      if (assetResource != null) {
        damAsset = assetResource.adaptTo(com.day.cq.dam.api.Asset.class);
      }
      if (damAsset != null) {
        Asset asset = new DamAsset(damAsset, media, adaptable);
        media.setAsset(asset);

        // resolve rendition(s)
        renditionsResolved = resolveRenditions(media, asset, mediaArgs);
      }

    }

    // set media invalid reason
    if (!renditionsResolved) {
      if (media.getAsset() != null) {
        if (media.getRenditions().isEmpty()) {
          media.setMediaInvalidReason(MediaInvalidReason.NO_MATCHING_RENDITION);
        }
        else {
          media.setMediaInvalidReason(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS);
        }
      }
      else if (StringUtils.isNotEmpty(mediaRef)) {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
      }
      else {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_MISSING);
      }
    }

    return media;
  }

  @Override
  @SuppressWarnings("null")
  public void enableMediaDrop(@NotNull HtmlElement element, @NotNull MediaRequest mediaRequest) {
    if (wcmMode == WCMMode.DISABLED || wcmMode == null) {
      return;
    }

    if (componentContext != null && componentContext.getEditContext() != null
        && MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext)) {

      String refProperty = prependDotSlash(getMediaRefProperty(mediaRequest, mediaHandlerConfig));
      String cropProperty = prependDotSlash(getMediaCropProperty(mediaRequest, mediaHandlerConfig));
      String rotationProperty = prependDotSlash(getMediaRotationProperty(mediaRequest, mediaHandlerConfig));
      String mapProperty = prependDotSlash(getMediaMapProperty(mediaRequest, mediaHandlerConfig));

      String name = refProperty;
      if (StringUtils.contains(name, "/")) {
        name = Text.getName(name);
      }

      // check of drop target for "media" group already exists - get it's id for the cq-dd- css class
      Optional<String> dropTargetCssClass = getMediaDropTargetID();
      if (!dropTargetCssClass.isPresent()) {
        // otherwise add a new drop target and get it's id
        MediaPropertyNames mediaPropertyNames = new MediaPropertyNames()
            .refProperty(refProperty)
            .cropProperty(cropProperty)
            .rotationProperty(rotationProperty)
            .mapProperty(mapProperty);
        dropTargetCssClass = addMediaDroptarget(refProperty, mediaPropertyNames, name);
      }

      if (element != null) {
        element.addCssClass(dropTargetCssClass.get());
      }
    }
  }

  @Override
  public void setCustomIPECropRatios(@NotNull HtmlElement<?> element, @NotNull MediaRequest mediaRequest) {
    if (wcmMode == WCMMode.DISABLED || wcmMode == null) {
      return;
    }

    if (componentContext != null
        && MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext)) {
      // overlay IPE config with cropping ratios for each media format with a valid ratio
      CroppingRatios croppingRatios = new CroppingRatios(mediaFormatHandler);
      Set<String> mediaFormatNames = croppingRatios.getMediaFormatsForCropping(mediaRequest);
      if (!mediaFormatNames.isEmpty()) {
        // build custom IPE config path containing both the resource context path and the
        // configured media formats. The path is served by a custom resource provider, because
        // there is no other interface to pass in a dynamic IPE configuration
        String ipeConfigPath = IPEConfigResourceProvider.buildPath(componentContext.getResource().getPath(), mediaFormatNames);
        // clone IPE config and overwrite config path via reflection (no API available for this)
        InplaceEditingConfig customIpeConfig = new InplaceEditingConfig(componentContext
            .getEditContext().getEditConfig().getInplaceEditingConfig());
        try {
          Field configPathField = InplaceEditingConfig.class.getDeclaredField("configPath");
          configPathField.setAccessible(true);
          configPathField.set(customIpeConfig, ipeConfigPath);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
          log.warn("Unable to set custom IPE config via reflection for {}", componentContext.getResource().getPath(), ex);
        }
        componentContext.getEditContext().getEditConfig().setInplaceEditingConfig(customIpeConfig);
      }

    }
  }

  private String prependDotSlash(String property) {
    if (!StringUtils.startsWith(property, "./")) {
      return "./" + property;
    }
    else {
      return property;
    }
  }

  private Optional<String> getMediaDropTargetID() {
    return componentContext.getEditContext().getEditConfig().getDropTargets().values().stream()
        .filter(item -> ArrayUtils.contains(item.getGroups(), "media"))
        .map(item -> item.getId())
        .findFirst();
  }

  private Optional<String> addMediaDroptarget(String refProperty, MediaPropertyNames mediaPropertyNames, String name) {
    Component componentDefinition = WCMUtils.getComponent(resource);

    // set drop target - with path of current component as default resource type
    Map<String, String> params = new HashMap<String, String>();
    if (componentDefinition != null) {
      params.put("./" + ResourceResolver.PROPERTY_RESOURCE_TYPE, componentDefinition.getPath());

      // clear cropping parameters if a new image is inserted via drag&drop
      params.put(mediaPropertyNames.getCropProperty(), "");
      params.put(mediaPropertyNames.getRotationProperty(), "");
      params.put(mediaPropertyNames.getMapProperty(), "");
    }

    DropTarget dropTarget = new DropTargetImpl(name, refProperty).setAccept(
        MediaFileType.getImageContentTypes().stream().toArray(size -> new String[size]) // allow all image mime types
    ).setGroups(new String[] {
        "media" // allow drop from DAM contentfinder tab
    }).setParameters(params);

    componentContext.getEditContext().getEditConfig().getDropTargets().put(dropTarget.getId(), dropTarget);

    return Optional.of(dropTarget.getId());
  }

  @Override
  public String toString() {
    return ID;
  }

}
