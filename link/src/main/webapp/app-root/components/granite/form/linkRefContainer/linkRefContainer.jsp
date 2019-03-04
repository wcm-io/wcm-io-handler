<%--
  #%L
  wcm.io
  %%
  Copyright (C) 2019 wcm.io
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@page import="java.util.Map"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ComponentHelper.Options"%>
<%@page import="com.adobe.granite.ui.components.Tag"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="io.wcm.handler.link.type.InternalLinkType"%>
<%@page import="io.wcm.handler.link.type.InternalCrossContextLinkType"%>
<%@page import="io.wcm.handler.link.type.ExternalLinkType"%>
<%@page import="io.wcm.handler.link.type.MediaLinkType"%>
<%@page import="io.wcm.handler.link.spi.LinkType"%>
<%@page import="io.wcm.handler.link.spi.LinkHandlerConfig"%>
<%@page import="io.wcm.sling.commons.resource.ImmutableValueMap"%>
<%@page import="io.wcm.sling.commons.util.Escape"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@page import="io.wcm.wcm.ui.granite.util.GraniteUi"%>
<%@include file="../../global/global.jsp" %><%--###

wcm.io Link reference container
===============================

Component that defines the full set of fields for defining a link with it's properties to be included in a dialog tab.
The link types are displayed dynamically as configured in the link handler configuration.
It is possible to add your own fields.

Properties:

.. gnd:gnd::

  /**
   * Show text field with link title.
   */
  - showLinkTitle (boolean) = false

  /**
   * Prefix for all link reference properties. Can be set to another prefix if multiple links
   * should be stored in a single resource.
   */
  - propertyPrefix (String) = "link"
  
  /**
   * Additional Granite UI components to be displayed for "internal" link type.
   */
  - internalLinkFields (Resources)

  /**
   * Additional Granite UI components to be displayed for "internalCrossContext" link type.
   */
  - internalCrossContextLinkFields (Resources)

  /**
   * Additional Granite UI components to be displayed for "external" link type.
   */
  - externalLinkFields (Resources)

  /**
   * Additional Granite UI components to be displayed for "media" link type.
   */
  - mediaLinkFields (Resources)

  /**
   * Additional Granite UI components to be displayed for all link types.
   */
  - allLinkTypeFields (Resources)

###--%><%

Tag tag = cmp.consumeTag();
Config cfg = cmp.getConfig();

boolean showLinkTitle = cfg.get("showLinkTitle", false);
String propertyPrefix = cfg.get("propertyPrefix", "link");

// this is required to ensure that multiple link contains in the same dialog do not interfer each other
String showhideCssClass = "option-linktype-showhide-target-" + Escape.validName(propertyPrefix);

Map<String,LinkType> linkTypes = getLinkTypes(GraniteUi.getContentResourceOrParent(request));

Resource container = GraniteUiSyntheticResource.wrap(resource);
Resource items = GraniteUiSyntheticResource.child(container, "items", JcrConstants.NT_UNSTRUCTURED);


// Link title
if (showLinkTitle) {
  GraniteUiSyntheticResource.child(items, "linkTitle", "granite/ui/components/coral/foundation/form/textfield",
      ImmutableValueMap.builder()
      .put("name", "./" + propertyPrefix + "Title")
      .put("fieldLabel", "Link title")
      .build());
}


// Link type
Resource linkTypeSelect = GraniteUiSyntheticResource.child(items, "linkType", "granite/ui/components/coral/foundation/form/select",
    ImmutableValueMap.builder()
    .put("name", "./" + propertyPrefix + "Type")
    .put("fieldLabel", "Link type")
    .put("granite:class", "cq-dialog-dropdown-showhide")
    .build());
GraniteUiSyntheticResource.child(linkTypeSelect, "granite:data", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("cq-dialog-dropdown-showhide-target", "." + showhideCssClass)
    .build());
Resource linkTypeItems = GraniteUiSyntheticResource.child(linkTypeSelect, "items", JcrConstants.NT_UNSTRUCTURED);
for (LinkType linkType : linkTypes.values()) {
  GraniteUiSyntheticResource.child(linkTypeItems, linkType.getId(), JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("value", linkType.getId())
      .put("text", linkType.getLabel())
      .build());
}


// --- Internal Link Well ---
if (linkTypes.containsKey(InternalLinkType.ID)) {
  Resource internalWell = GraniteUiSyntheticResource.child(items, "internalWell", "granite/ui/components/coral/foundation/well",
      ImmutableValueMap.builder()
      .put("granite:class", "hide " + showhideCssClass + " foundation-layout-util-vmargin")
      .build());
  GraniteUiSyntheticResource.child(internalWell, "granite:data", JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("showhidetargetvalue", InternalLinkType.ID)
      .build());
  Resource internalWellItems = GraniteUiSyntheticResource.child(internalWell, "items", JcrConstants.NT_UNSTRUCTURED);
  
  GraniteUiSyntheticResource.child(internalWellItems, "linkContentRef", "wcm-io/handler/link/components/granite/form/internalLinkPathField",
      ImmutableValueMap.builder()
      .put("name", "./" + propertyPrefix + "ContentRef")
      .put("fieldLabel", "Internal page")
      .put("fieldDescription", "Link to target page in CMS (same site)")
      .build());
  
  insertAdditionalComponents(internalWellItems, cfg.getChild("internalLinkFields"));
}

// --- Internal Cross-Context Link Well ---
if (linkTypes.containsKey(InternalCrossContextLinkType.ID)) {
  Resource internalCrossContextWell = GraniteUiSyntheticResource.child(items, "internalCrossContextWell", "granite/ui/components/coral/foundation/well",
      ImmutableValueMap.builder()
      .put("granite:class", "hide " + showhideCssClass + " foundation-layout-util-vmargin")
      .build());
  GraniteUiSyntheticResource.child(internalCrossContextWell, "granite:data", JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("showhidetargetvalue", InternalCrossContextLinkType.ID)
      .build());
  Resource internalCrossContextWellItems = GraniteUiSyntheticResource.child(internalCrossContextWell, "items", JcrConstants.NT_UNSTRUCTURED);
  
  GraniteUiSyntheticResource.child(internalCrossContextWellItems, "linkContentRef", "wcm-io/handler/link/components/granite/form/internalCrossContextLinkPathField",
      ImmutableValueMap.builder()
      .put("name", "./" + propertyPrefix + "CrossContextContentRef")
      .put("fieldLabel", "Internal page (other site)")
      .put("fieldDescription", "Link to target page in CMS (other site)")
      .build());

  insertAdditionalComponents(internalCrossContextWellItems, cfg.getChild("internalCrossContextLinkFields"));
}

// --- External Link Well ---
if (linkTypes.containsKey(ExternalLinkType.ID)) {
  Resource externalWell = GraniteUiSyntheticResource.child(items, "externalWell", "granite/ui/components/coral/foundation/well",
      ImmutableValueMap.builder()
      .put("granite:class", "hide " + showhideCssClass + " foundation-layout-util-vmargin")
      .build());
  GraniteUiSyntheticResource.child(externalWell, "granite:data", JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("showhidetargetvalue", ExternalLinkType.ID)
      .build());
  Resource externalWellItems = GraniteUiSyntheticResource.child(externalWell, "items", JcrConstants.NT_UNSTRUCTURED);
  
  GraniteUiSyntheticResource.child(externalWellItems, "linkExternalRef", "granite/ui/components/coral/foundation/form/textfield",
      ImmutableValueMap.builder()
      .put("name", "./" + propertyPrefix + "ExternalRef")
      .put("fieldLabel", "URL")
      .put("fieldDescription", "Link to external destination")
      .build());

  insertAdditionalComponents(externalWellItems, cfg.getChild("externalLinkFields"));
}

// --- Media Link Well ---
if (linkTypes.containsKey(MediaLinkType.ID)) {
  Resource mediaWell = GraniteUiSyntheticResource.child(items, "mediaWell", "granite/ui/components/coral/foundation/well",
      ImmutableValueMap.builder()
      .put("granite:class", "hide " + showhideCssClass + " foundation-layout-util-vmargin")
      .build());
  GraniteUiSyntheticResource.child(mediaWell, "granite:data", JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("showhidetargetvalue", MediaLinkType.ID)
      .build());
  Resource mediaWellItems = GraniteUiSyntheticResource.child(mediaWell, "items", JcrConstants.NT_UNSTRUCTURED);
  
  GraniteUiSyntheticResource.child(mediaWellItems, "linkMediaRef", "wcm-io/handler/link/components/granite/form/mediaLinkPathField",
      ImmutableValueMap.builder()
      .put("propertyPrefix", "./" + propertyPrefix + "Media")
      .put("fieldLabel", "Asset reference")
      .build());
  
  GraniteUiSyntheticResource.child(mediaWellItems, "linkMediaDownload", "wcm-io/wcm/ui/granite/components/form/checkbox",
      ImmutableValueMap.builder()
      .put("name", "./" + propertyPrefix + "MediaDownload")
      .put("text", "Download")
      .put("fieldDescription", "Open DAM asset item with download dialog")
      .build());

  insertAdditionalComponents(mediaWellItems, cfg.getChild("mediaLinkFields"));
}

// Link window target
Resource linkWindowTarget = GraniteUiSyntheticResource.child(items, "linkWindowTarget", "granite/ui/components/coral/foundation/form/select",
    ImmutableValueMap.builder()
    .put("name", "./" + propertyPrefix + "WindowTarget")
    .put("fieldLabel", "Window target")
    .build());
Resource linkWindowTargetItems = GraniteUiSyntheticResource.child(linkWindowTarget, "items", JcrConstants.NT_UNSTRUCTURED);
GraniteUiSyntheticResource.child(linkWindowTargetItems, "_self", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("value", "_self")
    .put("text", "Same window")
    .put("selected", true)
    .build());
GraniteUiSyntheticResource.child(linkWindowTargetItems, "_blank", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("value", "_blank")
    .put("text", "New window")
    .build());

insertAdditionalComponents(items, cfg.getChild("allLinkTypeFields"));


cmp.include(container, "granite/ui/components/coral/foundation/container", new Options().tag(tag));
%><%!

private final Logger log = LoggerFactory.getLogger(getClass());

private Map<String,LinkType> getLinkTypes(Resource resource) {
  Map<String,LinkType> linkTypes = new LinkedHashMap<>();
  if (resource != null) {
    LinkHandlerConfig linkHandlerConfig = resource.adaptTo(LinkHandlerConfig.class);
    for (Class<? extends LinkType> linkTypeClass : linkHandlerConfig.getLinkTypes()) {
      LinkType linkType = resource.adaptTo(linkTypeClass);
      linkTypes.put(linkType.getId(), linkType);
    }
  }
  else {
    log.warn("Unable to get link types for link reference container - content resource not detected.");
  }
  return linkTypes;
}

private void insertAdditionalComponents(Resource target, Resource source) {
  if (source == null) {
    return;
  }
  for (Resource sourceChild : source.getChildren()) {
    GraniteUiSyntheticResource.copySubtree(target, sourceChild);
  }
}

%>