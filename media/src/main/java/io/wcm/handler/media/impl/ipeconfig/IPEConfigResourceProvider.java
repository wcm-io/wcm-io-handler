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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

/**
 * Resource provider that overlays a IPE config resource with a dynamically generated
 * set of cropping aspect ratios derived from given set of media formats.
 * <p>
 * URL pattern for resource access:<br/>
 * <code>/wcmio:mediaHandler/ipeConfig/{mediaFormat1}/{mediaFormat2}/.../wcmio:content/{componentContentPath}</code>
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
        return buildAspectRatioItemResource(resolver, path, mediaFormatName);
      }
      else {
        return null;
      }
    }
    else {
      // return wrapped overlay resource
      Resource overlayResource = resolver.getResource(parser.getOverlayPath());
      if (overlayResource != null) {
        return new OverlayResourceWrapper(overlayResource, path);
      }
      else {
        return null;
      }
    }
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
        childMap.put(mediaFormatName, buildAspectRatioItemResource(resolver, path + "/" + mediaFormatName, mediaFormatName));
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
   * Gets children of overlayed resource and converts children to {@link OverlayResourceWrapper}.
   * @param resource Requested resources
   * @return Map with all children
   */
  private Map<String, Resource> getOverlayedResourceChilden(Resource resource) {
    Map<String, Resource> childMap = new LinkedHashMap<>();
    if (resource instanceof OverlayResourceWrapper) {
      Resource overlayResource = ((OverlayResourceWrapper)resource).getOverlayedResource();
      Iterator<Resource> childrenIterator = overlayResource.listChildren();
      while (childrenIterator.hasNext()) {
        Resource child = childrenIterator.next();
        childMap.put(child.getName(), new OverlayResourceWrapper(child,
            resource.getPath() + "/" + child.getName()));
      }
    }
    return childMap;
  }

  private Resource buildAspectRatiosResource(ResourceResolver resolver, String path) {
    return new SyntheticResource(resolver, path, null);
  }

  private Resource buildAspectRatioItemResource(ResourceResolver resolver, String path, String mediaFormatName) {
    // TODO: return media format properties
    return new SyntheticResource(resolver, path, null);
  }

}
