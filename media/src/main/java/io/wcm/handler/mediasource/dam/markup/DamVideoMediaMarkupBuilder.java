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
package io.wcm.handler.mediasource.dam.markup;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaMetadata;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.url.UrlHandler;
import io.wcm.wcm.commons.contenttype.ContentType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.PrefixRenditionPicker;
import com.day.cq.dam.video.VideoConstants;
import com.day.cq.dam.video.VideoProfile;

/**
 * Default implementation of {@link MediaMarkupBuilder} for DAM video assets.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
public class DamVideoMediaMarkupBuilder implements MediaMarkupBuilder {

  private static final String H264_PROFILE = "hq";
  private static final String OGG_PROFILE = "firefoxhq";
  private static final String[] VIDEO_PROFILE_NAMES = new String[] {
    OGG_PROFILE, H264_PROFILE
  };

  private static final Logger log = LoggerFactory.getLogger(DamVideoMediaMarkupBuilder.class);

  @SlingObject
  private ResourceResolver resourceResolver;
  @Self
  private UrlHandler urlHandler;

  @Override
  public final boolean accepts(MediaMetadata mediaMetadata) {
    Asset asset = getDamAsset(mediaMetadata);
    if (asset != null) {
      return asset.getRendition(new PrefixRenditionPicker(VideoConstants.RENDITION_PREFIX)) != null;
    }
    else {
      return false;
    }
  }

  /**
   * Return video profile names stored below /etc/dam/video supported by this markup builder.
   * @return Video profile names
   */
  protected String[] getVideoProfileNames() {
    return VIDEO_PROFILE_NAMES;
  }

  /**
   * Return video profiles supported by this markup builder.
   * @return Video profiles
   */
  protected List<VideoProfile> getVideoProfiles() {
    List<VideoProfile> profiles = new ArrayList<VideoProfile>();
    String[] profileNames = getVideoProfileNames();
    if (profileNames != null) {
      for (String profileName : profileNames) {
        VideoProfile profile = VideoProfile.get(resourceResolver, profileName);
        if (profile != null) {
          profiles.add(profile);
        }
        else {
          log.warn("DAM video profile with name '{}' does not exist.", profileName);
        }
      }
    }
    return profiles;
  }

  /**
   * @param mediaMetadata Media metadata
   * @return DAM asset or null
   */
  protected Asset getDamAsset(MediaMetadata mediaMetadata) {
    return mediaMetadata.getMediaItem().adaptTo(Asset.class);
  }

  @Override
  public final HtmlElement<?> build(MediaMetadata mediaMetadata) {
    return getVideoPlayerElement(mediaMetadata);
  }

  /**
   * Build HTML5 video player element
   * @param mediaMetadata Media metadata
   * @return Media element
   */
  protected Video getVideoPlayerElement(MediaMetadata mediaMetadata) {
    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(mediaMetadata);

    Video video = new Video();
    video.setWidth((int)dimension.getWidth());
    video.setHeight((int)dimension.getHeight());
    video.setControls(true);

    // add video sources for each video profile
    addSources(video, mediaMetadata);

    // add flash player as fallback
    video.addContent(getFlashPlayerElement(mediaMetadata, dimension));

    return video;
  }

  /**
   * Add sources for HTML5 video player
   * @param video Video
   * @param mediaMetadata Media metadata
   */
  protected void addSources(Video video, MediaMetadata mediaMetadata) {
    Asset asset = getDamAsset(mediaMetadata);
    if (asset == null) {
      return;
    }

    for (VideoProfile profile : getVideoProfiles()) {
      com.day.cq.dam.api.Rendition rendition = profile.getRendition(asset);
      if (rendition != null) {
        video.createSource().setType(profile.getHtmlType())
        .setSrc(urlHandler.url(rendition.getPath()).externalizeResource().build());
      }
    }
  }

  /**
   * Build flash player element
   * @param mediaMetadata Media metadata
   * @param dimension Dimension
   * @return Media element
   */
  protected HtmlElement getFlashPlayerElement(MediaMetadata mediaMetadata, Dimension dimension) {
    Asset asset = getDamAsset(mediaMetadata);
    if (asset == null) {
      return null;
    }

    com.day.cq.dam.api.Rendition rendition = asset.getRendition(new PrefixRenditionPicker(VideoConstants.RENDITION_PREFIX + H264_PROFILE));
    if (rendition == null) {
      return null;
    }

    String playerUrl = urlHandler.url("/etc/clientlibs/foundation/video/swf/StrobeMediaPlayback.swf")
        .externalizeResource().build();

    // strobe specialty: path must be relative to swf file
    String renditionUrl = "../../../../.." + rendition.getPath();

    // manually apply jcr_content namespace mangling
    renditionUrl = StringUtils.replace(renditionUrl, JcrConstants.JCR_CONTENT, "_jcr_content");

    HtmlElement object = new HtmlElement("object");
    object.setAttribute("type", ContentType.SWF);
    object.setAttribute("data", playerUrl);
    object.setAttribute("width", Long.toString(dimension.getWidth()));
    object.setAttribute("height", Long.toString(dimension.getHeight()));

    // get flashvars
    Map<String, String> flashvars = new HashMap<String, String>();
    flashvars.put("src", renditionUrl);
    flashvars.putAll(getAdditionalFlashPlayerFlashVars(mediaMetadata, dimension));

    // get flash parameters
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("movie", playerUrl);
    parameters.put("flashvars", buildFlashVarsString(flashvars));
    parameters.putAll(getAdditionalFlashPlayerParameters(mediaMetadata, dimension));

    // set parameters on object element
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      HtmlElement param = object.create("param");
      param.setAttribute("name", entry.getKey());
      param.setAttribute("value", entry.getValue());
    }

    return object;
  }

  /**
   * Build flashvars string to be used on HTML object element for flash embedding.
   * @param flashVars flashvars map
   * @return flashvars string with proper encoding
   */
  protected String buildFlashVarsString(Map<String, String> flashVars) {
    try {
      StringBuilder flashvarsString = new StringBuilder();
      Iterator<Map.Entry<String, String>> flashvarsIterator = flashVars.entrySet().iterator();
      while (flashvarsIterator.hasNext()) {
        Map.Entry<String, String> entry = flashvarsIterator.next();
        flashvarsString.append(URLEncoder.encode(entry.getKey(), CharEncoding.UTF_8));
        flashvarsString.append("=");
        flashvarsString.append(URLEncoder.encode(entry.getValue(), CharEncoding.UTF_8));
        if (flashvarsIterator.hasNext()) {
          flashvarsString.append("&");
        }
      }
      return flashvarsString.toString();
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("Unsupported encoding.", ex);
    }
  }

  /**
   * Get additional parameters to be set as &lt;param&gt; elements on html object element for flash player.
   * @param mediaMetadata Media metadata
   * @param dimension Dimension
   * @return Set of key/value pairs
   */
  protected Map<String, String> getAdditionalFlashPlayerParameters(MediaMetadata mediaMetadata, Dimension dimension) {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("allowFullScreen", "true");
    parameters.put("wmode", "opaque");

    return parameters;
  }

  /**
   * Get additional parameters to be set as flashvars parameter on html object element for flash player.
   * @param mediaMetadata Media metadata
   * @param dimension Dimension
   * @return Set of key/value pairs
   */
  protected Map<String, String> getAdditionalFlashPlayerFlashVars(MediaMetadata mediaMetadata, Dimension dimension) {
    Map<String, String> flashvars = new HashMap<String, String>();

    flashvars.put("autoPlay", "false");
    flashvars.put("loop", "false");

    return flashvars;
  }

  @Override
  public final boolean isValidMedia(HtmlElement<?> element) {
    return (element instanceof Video);
  }

}
