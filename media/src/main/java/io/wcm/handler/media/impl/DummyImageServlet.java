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

import io.wcm.handler.url.suffix.SuffixParser;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.awt.Color;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
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
  public static final String SUFFIX_MEDIA_FORMAT_NAME = "mf";

  @Override
  protected Layer createLayer(ImageContext ctx) throws RepositoryException, IOException {
    SuffixParser parser = new SuffixParser(ctx.request);
    int width = parser.getPart(SUFFIX_WIDTH, 0);
    int height = parser.getPart(SUFFIX_HEIGHT, 0);
    String name = parser.getPart(SUFFIX_MEDIA_FORMAT_NAME, (String)null);

    // validate with/height
    if (width < 1 || height < 1) {
      return new Layer(1, 1, null);
    }

    Layer textLayer = getTextLayer(width, height, name);

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

  private Layer getTextLayer(int width, int height, String name) {
    String text = (StringUtils.isNotEmpty(name) ? name + "\n" : "")
        + width + " x " + height;
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

}
