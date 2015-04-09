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
package io.wcm.handler.media.impl;

import io.wcm.wcm.commons.contenttype.FileExtension;

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import com.day.cq.wcm.commons.AbstractImageServlet;
import com.day.image.Font;
import com.day.image.Layer;
import com.day.image.font.AbstractFont;

/**
 * Servlet which is used to render a dummy image with information about the size of the image and media format.
 * Note: most of the values used for scaling are the result of experiments, how the image looks nice.
 */
@SlingServlet(
    resourceTypes = "/apps/wcm-io/handler/media/components/dummyImage",
    extensions = FileExtension.PNG)
public final class DummyImageServlet extends AbstractImageServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Dummy image content path
   */
  public static final String PATH = "/apps/wcm-io/handler/media/content/dummyImage";

  /**
   * Suffix for width
   */
  public static final String SUFFIX_WIDTH = "width";

  /**
   * Suffix for height
   */
  public static final String SUFFIX_HEIGHT = "height";

  /**
   * Suffix for Name of mediaformat (optional)
   */
  public static final String SUFFIX_MEDIA_FORMAT_NAME = "mediaformat";

  @Override
  protected Layer createLayer(ImageContext ctx) throws RepositoryException, IOException {
    Map<String, String> suffix = getSuffix(ctx);
    if (!suffix.containsKey(SUFFIX_HEIGHT) || !suffix.containsKey(SUFFIX_WIDTH) || !suffix.containsKey(SUFFIX_MEDIA_FORMAT_NAME)) {
      return new Layer(1, 1, null);
    }
    int height = NumberUtils.toInt(suffix.get(SUFFIX_HEIGHT));
    int width = NumberUtils.toInt(suffix.get(SUFFIX_WIDTH));
    if (height == 0 || width == 0) {
      return new Layer(1, 1, null);
    }
    Layer textLayer = getTextLayer(suffix);

    int x = (width - textLayer.getWidth()) / 2;
    int y = (height - textLayer.getHeight()) / 2;

    Layer layer = new Layer(width, height, Color.GRAY);

    // we now build a grey box for the text which is slightly bigger than the text
    int backgroundOffset = width / 120;
    Layer textBackground = new Layer(textLayer.getWidth() + (backgroundOffset * 2), textLayer.getHeight() + (backgroundOffset * 2), Color.DARK_GRAY);
    layer.blit(textBackground, x - backgroundOffset, y - backgroundOffset, textBackground.getWidth(), textBackground.getHeight(), 0, 0);

    // now we can add the text
    layer.blit(textLayer, x, y, textLayer.getWidth(), textLayer.getHeight(), 0, 0);
    return layer;
  }

  private Layer getTextLayer(Map<String, String> suffix) {
    int height = NumberUtils.toInt(suffix.get(SUFFIX_HEIGHT));
    int width = NumberUtils.toInt(suffix.get(SUFFIX_WIDTH));
    String text = suffix.get(SUFFIX_MEDIA_FORMAT_NAME) + "\n" + width + " x " + height;
    Font font = new Font("Arial", width / 30);
    int align = AbstractFont.ALIGN_BASE | AbstractFont.ALIGN_CENTER | AbstractFont.TTANTIALIASED;
    Layer ret = new Layer(1, 1, null);
    ret.setPaint(Color.WHITE);
    ret.drawText(0, 0, 0, 0, text, font, align, 0, 0);
    return ret;
  }

  @Override
  protected boolean checkModifiedSince(SlingHttpServletRequest pReq, SlingHttpServletResponse pResp) {
    return false;
  }

  private Map<String, String> getSuffix(ImageContext ctx) {
    try {
      Map<String, String> ret = new HashMap<>();
      String suffixRaw = ctx.request.getRequestPathInfo().getSuffix();
      suffixRaw = StringUtils.removeEnd(suffixRaw, "." + ctx.request.getRequestPathInfo().getExtension());
      String[] suffix = StringUtils.split(suffixRaw, "/");
      for (String suf : suffix) {
        String[] spli = StringUtils.split(suf, "=");
        if (spli.length == 2) {
          ret.put(spli[0], URLDecoder.decode(spli[1], CharEncoding.UTF_8));
        }
      }
      return ret;
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("Unsupported encoding.", ex);
    }
  }

}
