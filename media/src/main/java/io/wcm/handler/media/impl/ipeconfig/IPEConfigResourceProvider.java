/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.media.impl.ipeconfig;

import static io.wcm.handler.media.impl.ipeconfig.PathParser.NN_ASPECT_RATIOS;
import static io.wcm.handler.media.impl.ipeconfig.PathParser.NN_CONFIG;
import static io.wcm.handler.media.impl.ipeconfig.PathParser.NN_MEDIA_FORMAT;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.components.ComponentManager;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Resource provider that overlays a IPE config resource with a dynamically generated
 * set of cropping aspect ratios derived from given set of media formats.
 * <p>
 * URL pattern for resource access:<br>
 * <code>/wcmio:mediaHandler/ipeConfig/{componentContentPath}/wcmio:mediaFormat/{mf1}/{mf2}/.../wcmio:config/{relativeConfigPath}</code>
 * </p>
 */
@Component(service = ResourceProvider.class, property = {
    ResourceProvider.PROPERTY_NAME + "=wcmioHandlerIPEConfig",
    ResourceProvider.PROPERTY_ROOT + "=" + IPEConfigResourceProvider.IPECONFIG_OVERLAY_ROOTPATH
})
public class IPEConfigResourceProvider extends ResourceProvider {

  /**
   * Root path for IPE config overlay resources.
   */
  public static final String IPECONFIG_OVERLAY_ROOTPATH = "/wcmio:mediaHandler/ipeConfig";

  @Override
  public @Nullable Resource getResource(@NotNull ResolveContext resolveContext, @NotNull String path,
      @NotNull ResourceContext resourceContext, @Nullable Resource parent) {

    PathParser parser = new PathParser(path);
    if (!parser.isValid()) {
      return null;
    }

    ResourceResolver resolver = resolveContext.getResourceResolver();
    if (parser.isAspectRatiosNode()) {
      // simulate 'aspectRatios' node
      return buildAspectRatiosResource(resolver, path);
    }
    else if (parser.isAspectRatioItem()) {
      // simulate 'aspectRatios/xxx' node
      String mediaFormatName = parser.getAspectRatioItemName();
      if (parser.getMediaFormatNames().contains(mediaFormatName)) {
        return buildAspectRatioItemResource(resolver, path, mediaFormatName, parser);
      }
    }
    else {
      // return wrapped overlaid resource
      String overlayResourcePath = getIpeConfigPath(resolver, parser);
      if (StringUtils.isNotEmpty(overlayResourcePath)) {
        Resource overlayResource = resolver.getResource(overlayResourcePath);
        if (overlayResource != null) {
          return new OverlayResource(overlayResource, path);
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable Iterator<Resource> listChildren(@NotNull ResolveContext resolveContext, @NotNull Resource resource) {
    Map<String, Resource> childMap = getOverlayedResourceChilden(resource);

    String path = resource.getPath();
    PathParser parser = new PathParser(path);
    if (!parser.isValid()) {
      return null;
    }

    ResourceResolver resolver = resolveContext.getResourceResolver();
    if (parser.isPluginsCropNode()) {
      // add simulated 'aspectRatios' node
      childMap.put(NN_ASPECT_RATIOS, buildAspectRatiosResource(resolver, path + "/" + NN_ASPECT_RATIOS));
    }
    else if (parser.isAspectRatiosNode()) {
      // add simulated 'aspectRatios/xxx' nodes
      childMap.clear();
      for (String mediaFormatName : parser.getMediaFormatNames()) {
        Resource item = buildAspectRatioItemResource(resolver, path + "/" + mediaFormatName, mediaFormatName, parser);
        if (item != null) {
          childMap.put(mediaFormatName, item);
        }
      }
    }

    if (childMap.isEmpty()) {
      return null;
    }
    else {
      return childMap.values().iterator();
    }
  }

  /**
   * Gets children of overlaid resource and converts children to {@link OverlayResource}.
   * @param resource Requested resources
   * @return Map with all children
   */
  private Map<String, Resource> getOverlayedResourceChilden(Resource resource) {
    Map<String, Resource> childMap = new LinkedHashMap<>();
    if (resource instanceof OverlayResource) {
      Resource overlayResource = ((OverlayResource)resource).getOverlayedResource();
      Iterator<Resource> childrenIterator = overlayResource.listChildren();
      while (childrenIterator.hasNext()) {
        Resource child = childrenIterator.next();
        childMap.put(child.getName(), new OverlayResource(child,
            resource.getPath() + "/" + child.getName()));
      }
    }
    return childMap;
  }

  /**
   * Build resource for /aspectRatios node
   * @param resolver Resource resolver
   * @param path Path
   * @return Resource
   */
  private Resource buildAspectRatiosResource(ResourceResolver resolver, String path) {
    return new SyntheticResource(resolver, path, null);
  }

  /**
   * Build virtual resource with name and aspect ratio of given media format.
   * @param resolver Resource resolver
   * @param path Path
   * @param mediaFormatName Media format name
   * @param parser Path parser
   * @return Resource or null if media format not found or has no valid ratio
   */
  private Resource buildAspectRatioItemResource(ResourceResolver resolver, String path, String mediaFormatName,
      PathParser parser) {
    Resource componentContent = resolver.getResource(parser.getComponentContentPath());
    if (componentContent != null) {
      MediaFormatHandler mediaFormatHandler = AdaptTo.notNull(componentContent, MediaFormatHandler.class);
      MediaFormat mediaFormat = mediaFormatHandler.getMediaFormat(mediaFormatName);
      if (mediaFormat != null) {
        return new AspectRatioResource(resolver, mediaFormat, path);
      }
    }
    return null;
  }

  /**
   * Get IPE config path from component associated with given resource and append the relative
   * config path from current resource request.
   * @param resolver Resource resolver
   * @param parser Path parser
   * @return Path or null
   */
  private String getIpeConfigPath(ResourceResolver resolver, PathParser parser) {
    Resource componentContent = resolver.getResource(parser.getComponentContentPath());
    if (componentContent != null) {
      ComponentManager componentManager = AdaptTo.notNull(resolver, ComponentManager.class);
      com.day.cq.wcm.api.components.Component component = componentManager.getComponentOfResource(componentContent);
      if (component != null
          && component.getEditConfig() != null
          && component.getEditConfig().getInplaceEditingConfig() != null) {
        String ipeConfigPath = component.getEditConfig().getInplaceEditingConfig().getConfigPath();
        if (StringUtils.isNotEmpty(ipeConfigPath)) {
          return ipeConfigPath + StringUtils.defaultString(parser.getRelativeConfigPath());
        }
      }
    }
    return null;
  }

  /**
   * Build path to overlaid IPE configuration services by this resource provider.
   * @param componentContentPath Content resource path containing reference component with image IPE enabled
   * @param mediaFormatNames Media format names
   * @return Path
   */
  public static String buildPath(String componentContentPath, Set<String> mediaFormatNames) {
    SortedSet<String> sortedMediaFormatNames = new TreeSet<>(mediaFormatNames);
    return IPECONFIG_OVERLAY_ROOTPATH + componentContentPath
        + "/" + NN_MEDIA_FORMAT + "/" + StringUtils.join(sortedMediaFormatNames, "/")
        + "/" + NN_CONFIG;
  }

}
