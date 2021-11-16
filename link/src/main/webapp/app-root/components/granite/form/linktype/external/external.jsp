<%--
  #%L
  wcm.io
  %%
  Copyright (C) 2021 wcm.io
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
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.adobe.granite.ui.components.Config"%>
<%@page import="com.adobe.granite.ui.components.ComponentHelper.Options"%>
<%@page import="com.adobe.granite.ui.components.Tag"%>
<%@page import="com.day.cq.commons.jcr.JcrConstants"%>
<%@page import="io.wcm.handler.link.LinkNameConstants"%>
<%@page import="io.wcm.handler.link.type.ExternalLinkType"%>
<%@page import="io.wcm.sling.commons.resource.ImmutableValueMap"%>
<%@page import="io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource"%>
<%@include file="../../../global/global.jsp" %><%--###

Dialog properties for "external" link type.

###--%><%

Tag tag = cmp.consumeTag();
Config cfg = cmp.getConfig();

String namePrefix = cfg.get("namePrefix", "./");
boolean required = cfg.get("required", false);
String linkTargetUrlFallbackValue = cfg.get("linkTargetUrlFallbackValue", String.class);

Resource container = GraniteUiSyntheticResource.wrap(resource);
Resource items = GraniteUiSyntheticResource.child(container, "items", JcrConstants.NT_UNSTRUCTURED);

ImmutableValueMap.Builder linkExternalRefProps = ImmutableValueMap.builder()
    .put("name", namePrefix + LinkNameConstants.PN_LINK_EXTERNAL_REF)
    .put("fieldLabel", "io.wcm.handler.link.components.granite.form.linkRefContainer.external.linkExternalRef.fieldLabel")
    .put("fieldDescription", "io.wcm.handler.link.components.granite.form.linkRefContainer.external.linkExternalRef.fieldDescription")
    .put("required", required)
    .put("validation", new String[] { "wcmio.handler.link.url" });
if (StringUtils.isNotBlank(linkTargetUrlFallbackValue)) {
  linkExternalRefProps
      .put("value", linkTargetUrlFallbackValue)
      .put("ignoreData", true);
}
GraniteUiSyntheticResource.child(items, LinkNameConstants.PN_LINK_EXTERNAL_REF, "granite/ui/components/coral/foundation/form/textfield",
    linkExternalRefProps.build());

insertAdditionalComponents(items, cfg.getChild("externalLinkFields"));

cmp.include(container, "granite/ui/components/coral/foundation/container", new Options().tag(tag));

%><%!
private void insertAdditionalComponents(Resource target, Resource source) {
  if (source == null) {
    return;
  }
  for (Resource sourceChild : source.getChildren()) {
    GraniteUiSyntheticResource.copySubtree(target, sourceChild);
  }
}
%>
