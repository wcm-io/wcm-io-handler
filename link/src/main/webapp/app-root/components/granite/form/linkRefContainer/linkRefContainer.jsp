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
<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ComponentHelper.Options"%>
<%@page import="com.adobe.granite.ui.components.Tag"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="com.google.common.collect.ImmutableSet"%>
<%@page import="io.wcm.handler.link.LinkNameConstants"%>
<%@page import="io.wcm.handler.link.LinkComponentPropertyResolver"%>
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
   * Filter link types allowed in the dialog.
   * Only link types allowed in the link handler configuration are possible.
   * When setting this property not all configured link types are displayed, but only those also
   * contained in this list.
   */
  - linkTypes (String[])

  /**
   * Prefix for all property names in the link reference dialog.
   * Can be used to store the properties in another resource by setting e.g. to "./mySubNode/".
   */
  - namePrefix (String) = "./"

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

  /**
   * It is required to select any link target and a link title (if a link title field is shown).
   */
  - required (Boolean) = 'false'

  /**
   * It is required to select any link target.
   */
  - requiredLink (Boolean) = 'false'

  /**
   * If showLinkTitle is set to true, it is set to required.
   */
  - requiredTitle (Boolean) = 'false'

###--%><%

Tag tag = cmp.consumeTag();
Config cfg = cmp.getConfig();

boolean showLinkTitle = cfg.get("showLinkTitle", false);
String namePrefix = cfg.get("namePrefix", "./");
boolean requiredLink = cfg.get("requiredLink", cfg.get("required", false));
boolean requiredTitle = cfg.get("requiredTitle", cfg.get("required", false));

Resource contentResource = GraniteUi.getContentResourceOrParent(request);
LinkHandlerConfig linkHandlerConfig = null;
if (contentResource != null) {
  linkHandlerConfig = contentResource.adaptTo(LinkHandlerConfig.class);
}

// check if a link target URL fallback property is defined and has a value
String[] linkTargetUrlFallbackProperty = null;
String linkTargetUrlFallbackValue = null;
String linkTargetUrlFallbackTypeId = null;
if (contentResource != null && contentResource.getValueMap().get(LinkNameConstants.PN_LINK_TYPE, String.class) == null) {
  LinkComponentPropertyResolver propertyResolver = new LinkComponentPropertyResolver(contentResource);
  linkTargetUrlFallbackProperty = propertyResolver.getLinkTargetUrlFallbackProperty();
  if (linkTargetUrlFallbackProperty != null && linkTargetUrlFallbackProperty.length > 0) {
    for (String propertyName : linkTargetUrlFallbackProperty) {
      linkTargetUrlFallbackValue = contentResource.getValueMap().get(propertyName, String.class);
      if (StringUtils.isNotBlank(linkTargetUrlFallbackValue)) {
        break;
      }
    }
  }
  // detect link type
  if (StringUtils.isNotBlank(linkTargetUrlFallbackValue)) {
    for (Class<? extends LinkType> linkTypeClass : linkHandlerConfig.getLinkTypes()) {
      LinkType linkType = resource.adaptTo(linkTypeClass);
      if (linkType.accepts(linkTargetUrlFallbackValue)) {
        String linkTargetUrlExisting = contentResource.getValueMap().get(linkType.getPrimaryLinkRefProperty(), String.class);
        if (linkTargetUrlExisting == null) {
          linkTargetUrlFallbackTypeId = linkType.getId();
        }
        break;
      }
    }
  }
}


// this is required to ensure that multiple link containers in the same dialog do not interfere with each other
String showhideCssClass = "linkRefContainer-option-linktype-showhide-target";
String parentCssClass = "linkRefContainer-option-linktype-showhide-parent";

String[] allowedLinkTypes = cfg.get("linkTypes", new String[0]);
Map<String,LinkType> linkTypes = getLinkTypes(contentResource, linkHandlerConfig, allowedLinkTypes);

Resource container = GraniteUiSyntheticResource.wrapMerge(resource, ImmutableValueMap.builder()
    .put("granite:class", parentCssClass)
    .build());
Resource items = GraniteUiSyntheticResource.child(container, "items", JcrConstants.NT_UNSTRUCTURED);


// Link title
if (showLinkTitle) {
  GraniteUiSyntheticResource.child(items, "linkTitle", "granite/ui/components/coral/foundation/form/textfield",
      ImmutableValueMap.builder()
      .put("name", namePrefix + LinkNameConstants.PN_LINK_TITLE)
      .put("fieldLabel", "io.wcm.handler.link.components.granite.form.linkRefContainer.linkTitle.fieldLabel")
      .put("required", requiredTitle)
      .build());
}


// Link type
ImmutableValueMap.Builder linkTypeSelectProps = ImmutableValueMap.builder()
    .put("name", namePrefix + LinkNameConstants.PN_LINK_TYPE)
    .put("fieldLabel", "io.wcm.handler.link.components.granite.form.linkRefContainer.linkType.fieldLabel")
    .put("required", requiredLink)
    .put("granite:class", "wcmio-dialog-showhide");
if (linkTargetUrlFallbackTypeId != null) {
  linkTypeSelectProps.put("ignoreData", true);
}
Resource linkTypeSelect = GraniteUiSyntheticResource.child(items, "linkType", "granite/ui/components/coral/foundation/form/select",
    linkTypeSelectProps.build());
GraniteUiSyntheticResource.child(linkTypeSelect, "granite:data", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("wcmio-dialog-showhide-target", "." + showhideCssClass)
    .put("wcmio-dialog-showhide-parent", "." + parentCssClass)
    .build());
Resource linkTypeItems = GraniteUiSyntheticResource.child(linkTypeSelect, "items", JcrConstants.NT_UNSTRUCTURED);
for (LinkType linkType : linkTypes.values()) {
  boolean selected = StringUtils.equals(linkType.getId(), linkTargetUrlFallbackTypeId);
  GraniteUiSyntheticResource.child(linkTypeItems, linkType.getId(), JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("value", linkType.getId())
      .put("text", "io.wcm.handler.link.components.granite.form.linkRefContainer." + linkType.getId() + ".type")
      .put("selected", selected)
      .build());
}


// Render edit components for each link type (each within a well)
for (LinkType linkType : linkTypes.values()) {
  String linkTypeEditComponentResourceType = linkType.getEditComponentResourceType();
  if (linkTypeEditComponentResourceType == null) {
    continue;
  }

  Resource linkTypeWell = GraniteUiSyntheticResource.child(items, "linkTypeWell_" + linkType.getId(), "granite/ui/components/coral/foundation/well",
      ImmutableValueMap.builder()
      .put("granite:class", "hide " + showhideCssClass + " foundation-layout-util-vmargin")
      .build());
  GraniteUiSyntheticResource.child(linkTypeWell, "granite:data", JcrConstants.NT_UNSTRUCTURED,
      ImmutableValueMap.builder()
      .put("showhidetargetvalue", linkType.getId())
      .build());
  Resource linkTypeWellItems = GraniteUiSyntheticResource.child(linkTypeWell, "items", JcrConstants.NT_UNSTRUCTURED);

  ImmutableValueMap.Builder linkTypeEditComponentProps = ImmutableValueMap.builder()
      .put("namePrefix", namePrefix)
      .put("required", requiredLink)
      .put("showhideCssClass", showhideCssClass);
  if (StringUtils.equals(linkTargetUrlFallbackTypeId, linkType.getId())) {
    linkTypeEditComponentProps.put("linkTargetUrlFallbackValue", linkTargetUrlFallbackValue);
  }
  Resource linkTypeEditComponent = GraniteUiSyntheticResource.child(linkTypeWellItems, "linkTypeEdit_" + linkType.getId(),
      linkTypeEditComponentResourceType, linkTypeEditComponentProps.build());

  // backward compatibility: handle special properties for defining additional link fields
  if (StringUtils.equalsAny(linkType.getId(),
      InternalLinkType.ID, InternalCrossContextLinkType.ID, ExternalLinkType.ID, MediaLinkType.ID)) {
    addAdditionalComponents(linkTypeEditComponent, linkType.getId() + "LinkFields", cfg.getChild(linkType.getId() + "LinkFields"));
  }
}


// Link window target
Resource linkWindowTarget = GraniteUiSyntheticResource.child(items, "linkWindowTarget", "granite/ui/components/coral/foundation/form/select",
    ImmutableValueMap.builder()
    .put("name", namePrefix + LinkNameConstants.PN_LINK_WINDOW_TARGET)
    .put("fieldLabel", "io.wcm.handler.link.components.granite.form.linkRefContainer.linkWindowTarget.fieldLabel")
    .build());
Resource linkWindowTargetItems = GraniteUiSyntheticResource.child(linkWindowTarget, "items", JcrConstants.NT_UNSTRUCTURED);
GraniteUiSyntheticResource.child(linkWindowTargetItems, "_self", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("value", "_self")
    .put("text", "io.wcm.handler.link.components.granite.form.linkRefContainer.linkWindowTarget._self")
    .put("selected", true)
    .build());
GraniteUiSyntheticResource.child(linkWindowTargetItems, "_blank", JcrConstants.NT_UNSTRUCTURED,
    ImmutableValueMap.builder()
    .put("value", "_blank")
    .put("text", "io.wcm.handler.link.components.granite.form.linkRefContainer.linkWindowTarget._blank")
    .build());

insertAdditionalComponents(items, cfg.getChild("allLinkTypeFields"));


// clear link target URL fallback property on saving
if (linkTargetUrlFallbackProperty != null) {
  for (String propertyName : linkTargetUrlFallbackProperty) {
    GraniteUiSyntheticResource.child(items, "linkTargetUrlFallbackProperty", "granite/ui/components/coral/foundation/form/hidden",
        ImmutableValueMap.builder()
        .put("name", "./" + propertyName + "@Delete")
        .build());
  }
}


cmp.include(container, "granite/ui/components/coral/foundation/container", new Options().tag(tag));
%><%!

private final Logger log = LoggerFactory.getLogger(getClass());

private Map<String,LinkType> getLinkTypes(Resource resource, LinkHandlerConfig linkHandlerConfig,
    String[] allowedLinkTypes) {
  Set<String> allowedLinkTypeSet = ImmutableSet.copyOf(allowedLinkTypes);
  Map<String,LinkType> linkTypes = new LinkedHashMap<>();
  if (resource != null) {
    for (Class<? extends LinkType> linkTypeClass : linkHandlerConfig.getLinkTypes()) {
      LinkType linkType = resource.adaptTo(linkTypeClass);
      if (allowedLinkTypeSet.isEmpty() || allowedLinkTypeSet.contains(linkType.getId())) {
        linkTypes.put(linkType.getId(), linkType);
      }
    }
  }
  else {
    log.warn("Unable to get link types for link reference container - content resource not detected.");
  }
  return linkTypes;
}

private void addAdditionalComponents(Resource target, String propertyName, Resource source) {
  if (source == null) {
    return;
  }
  Resource targetChild = GraniteUiSyntheticResource.child(target, propertyName, JcrConstants.NT_UNSTRUCTURED);
  for (Resource sourceChild : source.getChildren()) {
    GraniteUiSyntheticResource.copySubtree(targetChild, sourceChild);
  }
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
