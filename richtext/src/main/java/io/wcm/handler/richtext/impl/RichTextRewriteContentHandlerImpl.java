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
package io.wcm.handler.richtext.impl;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.link.LinkNameConstants;
import io.wcm.handler.link.SyntheticLinkResource;
import io.wcm.handler.link.spi.LinkHandlerConfig;
import io.wcm.handler.link.spi.LinkType;
import io.wcm.handler.link.type.InternalLinkType;
import io.wcm.handler.link.type.MediaLinkType;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.richtext.util.RewriteContentHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.FileExtension;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Default implementation of {@link RichTextRewriteContentHandlerImpl}.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = RewriteContentHandler.class)
public final class RichTextRewriteContentHandlerImpl implements RewriteContentHandler {

  @Self
  private Adaptable adaptable;
  @SlingObject
  private ResourceResolver resourceResolver;
  @Self
  private LinkHandler linkHandler;
  @Self
  private LinkHandlerConfig linkHandlerConfig;
  @Self
  private MediaHandler mediaHandler;

  /**
   * List of all tag names that should not be rendered "self-closing" to avoid interpretation errors in browsers
   */
  private static final Set<String> NONSELFCLOSING_TAGS = ImmutableSet.of(
      "div",
      "span",
      "strong",
      "em",
      "b",
      "i",
      "ul",
      "ol",
      "li"
      );

  /**
   * Maps legacy metadata attribute names to JCR property names used by link handling
   */
  private static final Map<String, String> LINKATTRIBUTE_MAPPING = ImmutableMap.<String, String>builder()
      .put("linktype", LinkNameConstants.PN_LINK_TYPE)
      .put("linkanchorname", LinkNameConstants.PN_LINK_ANCHOR_NAME)
      .put("linkqueryparams", LinkNameConstants.PN_LINK_QUERY_PARAM)
      .put("linkwindowwidth", LinkNameConstants.PN_LINK_WINDOW_WIDTH)
      .put("linkwindowheight", LinkNameConstants.PN_LINK_WINDOW_HEIGHT)
      .put("linkmediadownload", LinkNameConstants.PN_LINK_MEDIA_DOWNLOAD)
      .put("linkwindowfeatures", LinkNameConstants.PN_LINK_WINDOW_FEATURES)
      .build();


  /**
   * Checks if the given element has to be rewritten.
   * Is called for every child single element of the parent given to rewriteContent method.
   * @param element Element to check
   * @return null if nothing is to do with this element.
   *         Return empty list to remove this element.
   *         Return list with other content to replace element with new content.
   */
  @Override
  public List<Content> rewriteElement(Element element) {

    // rewrite anchor elements
    if (StringUtils.equalsIgnoreCase(element.getName(), "a")) {
      return rewriteAnchor(element);
    }

    // rewrite image elements
    else if (StringUtils.equalsIgnoreCase(element.getName(), "img")) {
      return rewriteImage(element);
    }

    // detect BR elements and turn those into "self-closing" elements
    // since the otherwise generated <br> </br> structures are illegal and
    // are not handled correctly by Internet Explorers
    else if (StringUtils.equalsIgnoreCase(element.getName(), "br")) {
      if (element.getContent().size() > 0) {
        element.removeContent();
      }
      return null;
    }

    // detect empty elements and insert at least an empty string to avoid "self-closing" elements
    // that are not handled correctly by most browsers
    else if (NONSELFCLOSING_TAGS.contains(StringUtils.lowerCase(element.getName()))) {
      if (element.getContent().isEmpty()) {
        element.setText("");
      }
      return null;
    }

    return null;
  }

  /**
   * Checks if the given anchor element has to be rewritten.
   * @param element Element to check
   * @return null if nothing is to do with this element.
   *         Return empty list to remove this element.
   *         Return list with other content to replace element with new content.
   */
  protected List<Content> rewriteAnchor(Element element) {

    // detect empty anchor elements and insert at least an empty string to avoid "self-closing" elements
    // that are not handled correctly by most browsers
    if (element.getContent().isEmpty()) {
      element.setText("");
    }

    // resolve link metadata from DOM element
    Link link = getAnchorLink(element);

    // build anchor for link metadata
    Element anchorElement = buildAnchorElement(link, element);

    // Replace anchor tag or remove anchor tag if invalid - add any sub-content in every case
    List<Content> content = new ArrayList<Content>();
    if (anchorElement != null) {
      anchorElement.addContent(element.cloneContent());
      content.add(anchorElement);
    }
    else {
      content.addAll(element.getContent());
    }
    return content;
  }

  /**
   * Extracts link metadata from the DOM elements attributes and resolves them to a {@link Link} object.
   * @param element DOM element
   * @return Link metadata
   */
  protected Link getAnchorLink(Element element) {
    SyntheticLinkResource resource = new SyntheticLinkResource(resourceResolver);
    ValueMap resourceProps = resource.getValueMap();

    // get link metadata from data element
    boolean foundMetadata = getAnchorMetadataFromData(resourceProps, element);
    if (!foundMetadata) {
      // support for legacy metadata stored in single "data" attribute
      foundMetadata = getAnchorLegacyMetadataFromSingleData(resourceProps, element);
      if (!foundMetadata) {
        // support for legacy metadata stored in rel attribute
        getAnchorLegacyMetadataFromRel(resourceProps, element);
      }
    }

    // build anchor via linkhandler
    return linkHandler.get(resource).build();
  }

  /**
   * Builds anchor element for given link metadata.
   * @param pLink Link metadata
   * @param element Original element
   * @return Anchor element or null if link is invalid
   */
  protected Element buildAnchorElement(Link pLink, Element element) {
    return pLink.getAnchor();
  }

  /**
   * Support data structures where link metadata is stored in mutliple HTML5 data-* attributes.
   * @param pResourceProps Valuemap to write link metadata to
   * @param element Link element
   * @return true if any metadata attribute was found
   */
  protected boolean getAnchorMetadataFromData(ValueMap pResourceProps, Element element) {
    boolean foundAny = false;

    List<Attribute> attributes = element.getAttributes();
    for (Attribute attribute : attributes) {
      if (DataPropertyUtil.isHtml5DataName(attribute.getName())) {
        String value = attribute.getValue();
        if (StringUtils.isNotEmpty(value)) {
          String property = DataPropertyUtil.toHeadlessCamelCaseName(attribute.getName());
          if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
            try {
              JSONArray jsonArray = new JSONArray(value);
              String[] values = new String[jsonArray.length()];
              for (int i = 0; i < jsonArray.length(); i++) {
                values[i] = jsonArray.optString(i);
              }
              pResourceProps.put(property, values);
            }
            catch (JSONException ex) {
              // ignore
            }
          }
          else {
            // decode if required
            value = decodeIfEncoded(value);
            pResourceProps.put(property, value);
          }
          foundAny = true;
        }
      }
    }

    return foundAny;
  }

  /**
   * Support legacy data structures where link metadata is stored as JSON fragement in single HTML5 data attribute.
   * @param pResourceProps Valuemap to write link metadata to
   * @param element Link element
   */
  protected boolean getAnchorLegacyMetadataFromSingleData(ValueMap pResourceProps, Element element) {
    boolean foundAny = false;

    JSONObject metadata = null;
    Attribute dataAttribute = element.getAttribute("data");
    if (dataAttribute != null) {
      String metadataString = dataAttribute.getValue();
      if (StringUtils.isNotEmpty(metadataString)) {
        try {
          metadata = new JSONObject(metadataString);
        }
        catch (JSONException ex) {
          RichTextHandlerImpl.log.debug("Invalid link metadata: " + metadataString, ex);
        }
      }
    }
    if (metadata != null) {
      JSONArray names = metadata.names();
      for (int i = 0; i < names.length(); i++) {
        String name = names.optString(i);
        pResourceProps.put(name, metadata.opt(name));
        foundAny = true;
      }
    }

    return foundAny;
  }

  /**
   * Support legacy data structures where link metadata is stored as JSON fragement in rel attribute.
   * @param pResourceProps Valuemap to write link metadata to
   * @param element Link element
   */
  protected void getAnchorLegacyMetadataFromRel(ValueMap pResourceProps, Element element) {
    // Check href attribute - do not change elements with no href or links to anchor names
    String href = element.getAttributeValue("href");
    String linkWindowTarget = element.getAttributeValue("target");
    if (href == null || href.startsWith("#")) {
      return;
    }

    // get link metadata from rel element
    JSONObject metadata = null;
    String metadataString = element.getAttributeValue("rel");
    if (StringUtils.isNotEmpty(metadataString)) {
      try {
        metadata = new JSONObject(metadataString);
      }
      catch (JSONException ex) {
        RichTextHandlerImpl.log.debug("Invalid link metadata: " + metadataString, ex);
      }
    }
    if (metadata == null) {
      metadata = new JSONObject();
    }

    // transform link metadata to virtual JCR resource with JCR properties
    JSONArray metadataPropertyNames = metadata.names();
    if (metadataPropertyNames != null) {
      for (int i = 0; i < metadataPropertyNames.length(); i++) {
        String metadataPropertyName = metadataPropertyNames.optString(i);
        String jcrPropertyName = mapMetadataPropertyName(metadataPropertyName);

        // check if value is array
        JSONArray valueArray = metadata.optJSONArray(metadataPropertyName);
        if (valueArray != null) {
          // store array values
          List<String> values = new ArrayList<String>();
          for (int j = 0; j < valueArray.length(); j++) {
            values.add(valueArray.optString(j));
          }
          pResourceProps.put(jcrPropertyName, values.toArray(new String[values.size()]));
        }
        else {
          // store simple value
          Object value = metadata.opt(metadataPropertyName);
          if (value != null) {
            pResourceProps.put(jcrPropertyName, value);
          }
        }
      }
    }

    // detect link type
    LinkType linkType = null;
    String linkTypeString = pResourceProps.get(LinkNameConstants.PN_LINK_TYPE, String.class);
    for (Class<? extends LinkType> candidateClass : linkHandlerConfig.getLinkTypes()) {
      LinkType candidate = AdaptTo.notNull(adaptable, candidateClass);
      if (StringUtils.isNotEmpty(linkTypeString)) {
        if (StringUtils.equals(linkTypeString, candidate.getId())) {
          linkType = candidate;
          break;
        }
      }
      else if (candidate.accepts(href)) {
        linkType = candidate;
        break;
      }
    }
    if (linkType == null) {
      // skip further processing if link type was not detected
      return;
    }

    // workaround: strip off ".html" extension if it was added automatically by the RTE
    if (linkType instanceof InternalLinkType || linkType instanceof MediaLinkType) {
      String htmlSuffix = "." + FileExtension.HTML;
      if (StringUtils.endsWith(href, htmlSuffix)) {
        href = StringUtils.substringBeforeLast(href, htmlSuffix);
      }
    }

    // store link reference (property depending on link type)
    pResourceProps.put(linkType.getPrimaryLinkRefProperty(), href);
    pResourceProps.put(LinkNameConstants.PN_LINK_WINDOW_TARGET, linkWindowTarget);

  }

  /**
   * Checks if the given image element has to be rewritten.
   * @param element Element to check
   * @return null if nothing is to do with this element.
   *         Return empty list to remove this element.
   *         Return list with other content to replace element with new content.
   */
  protected List<Content> rewriteImage(Element element) {

    // resolve media metadata from DOM element
    Media media = getImageMedia(element);

    // build image for media metadata
    Element imageElement = buildImageElement(media, element);

    // return modified element
    List<Content> content = new ArrayList<Content>();
    if (imageElement != null) {
      content.add(imageElement);
    }
    return content;
  }

  /**
   * Extracts media metadata from the DOM element attributes and resolves them to a {@link Media} object.
   * @param element DOM element
   * @return Media metadata
   */
  protected Media getImageMedia(Element element) {
    String ref = element.getAttributeValue("src");
    if (StringUtils.isNotEmpty(ref)) {
      ref = unexternalizeImageRef(ref);
    }
    return mediaHandler.get(ref).build();
  }

  /**
   * Builds image element for given media metadata.
   * @param pMedia Media metadata
   * @param element Original element
   * @return Image element or null if media reference is invalid
   */
  protected Element buildImageElement(Media pMedia, Element element) {
    if (pMedia.isValid()) {
      element.setAttribute("src", pMedia.getUrl());
    }
    return element;
  }

  /**
   * Converts the RTE externalized form of media reference to internal form.
   * @param ref Externalize media reference
   * @return Internal media reference
   */
  private String unexternalizeImageRef(String ref) {
    String unexternalizedRef = ref;

    if (StringUtils.isNotEmpty(unexternalizedRef)) {

      // decode if required
      unexternalizedRef = decodeIfEncoded(unexternalizedRef);

      // TODO: implementation has to be aligned with MediaSource implementations!
      // remove default servlet extension that is needed for inline images in RTE
      unexternalizedRef = StringUtils.removeEnd(unexternalizedRef, "/" + JcrConstants.JCR_CONTENT + ".default");
      unexternalizedRef = StringUtils.removeEnd(unexternalizedRef, "/_jcr_content.default");
    }

    return unexternalizedRef;
  }

  /**
   * URL-decode value if required.
   * @param value Probably encoded value.
   * @return Decoded value
   */
  private String decodeIfEncoded(String value) {
    if (StringUtils.contains(value, "%")) {
      try {
        return URLDecoder.decode(value, CharEncoding.UTF_8);
      }
      catch (UnsupportedEncodingException ex) {
        throw new RuntimeException(ex);
      }
    }
    return value;
  }

  /**
   * Maps a metadata property name to a JCR property name.
   * By default they should be identical, but legacy data can contain old variants of property names (e.g. lowercase).
   * @param property Metadata property name
   * @return JCR property name
   */
  protected String mapMetadataPropertyName(String property) {
    String translation = LINKATTRIBUTE_MAPPING.get(property);
    if (StringUtils.isNotEmpty(translation)) {
      return translation;
    }
    else {
      return property;
    }
  }

  @Override
  public List<Content> rewriteText(Text text) {
    // nothing to do with text element
    return null;
  }

}
