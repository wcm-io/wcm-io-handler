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
package io.wcm.handler.media.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.handler.store.AssetStore;
import com.day.image.Layer;
import com.drew.lang.annotations.Nullable;

/**
 * Gets layer from rendition resource.
 * Fallback to AssetStore if direct layer adaption is not possible (e.g. for SVG in AEM 6.4).
 */
public final class ResourceLayerUtil {

  private ResourceLayerUtil() {
    // static methods only
  }

  /**
   * @param renditionResource Rendition resource
   * @param assetStore Asset store service
   * @return Layer or null
   */
  public static @Nullable Layer toLayer(@NotNull Resource renditionResource, @NotNull AssetStore assetStore) {
    Layer layer = renditionResource.adaptTo(Layer.class);
    if (layer == null) {
      // if direct adaption to Layer was not possible, relay to AssetHandler
      Rendition rendition = renditionResource.adaptTo(Rendition.class);
      if (rendition != null) {
        AssetHandler assetHandler = assetStore.getAssetHandler(rendition.getMimeType());
        if (assetHandler != null) {
          try {
            BufferedImage bufferedImage = assetHandler.getImage(rendition);
            layer = new Layer(bufferedImage);
          }
          catch (IOException ex) {
            // ignore - not supported
          }
        }
      }
    }
    return layer;
  }

}
