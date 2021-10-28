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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.NONFIXED_RAW;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_STANDARD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.hamcrest.ResourceMatchers;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.InplaceEditingConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.wcmio.wcm.MockInstanceType;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class IPEConfigResourceProviderTest {

  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
  @Mock
  ComponentManager componentManager;
  @Mock
  MediaFormatHandler mediaFormatHandler;

  private Resource componentContentResource;

  @BeforeEach
  void setUp() {
    MockInstanceType.setAuthor(context);

    context.registerAdapter(ResourceResolver.class, ComponentManager.class, componentManager);
    context.registerAdapter(Resource.class, MediaFormatHandler.class, mediaFormatHandler);
    context.registerInjectActivateService(new IPEConfigResourceProvider());

    context.build().resource("/apps/app1/components/comp1")
        .resource("cq:editConfig/cq:inplaceEditing",
            "editorType", "image",
            "active", true)
        .resource("config/plugins/crop",
            "features", "*")
        .resource("aspectRatios/ratio1",
            "name", "Ratio 1",
            "ratio", "0.5")
        .commit();

    componentContentResource = context.create().resource("/content/myresource",
        "sling:resourceType", "/apps/app1/components/comp1");

    when(componentManager.getComponentOfResource(any(Resource.class))).thenAnswer(new Answer<Component>() {
      @Override
      @SuppressWarnings("null")
      @SuppressFBWarnings("STYLE")
      public Component answer(InvocationOnMock invocation) throws Throwable {
        Resource resource = invocation.getArgument(0);
        assertEquals(componentContentResource.getPath(), resource.getPath());

        Component component = mock(Component.class);
        EditConfig editConfig = mock(EditConfig.class);
        when(component.getEditConfig()).thenReturn(editConfig);

        Resource ipeConfigResource = context.resourceResolver().getResource(
            "/apps/app1/components/comp1/cq:editConfig/cq:inplaceEditing");
        InplaceEditingConfig ipeConfig = new InplaceEditingConfig(ipeConfigResource.adaptTo(Node.class));
        when(editConfig.getInplaceEditingConfig()).thenReturn(ipeConfig);
        return component;
      }
    });

    when(mediaFormatHandler.getMediaFormat(EDITORIAL_1COL.getName())).thenReturn(EDITORIAL_1COL);
    when(mediaFormatHandler.getMediaFormat(SHOWROOM_STANDARD.getName())).thenReturn(SHOWROOM_STANDARD);
    when(mediaFormatHandler.getMediaFormat(NONFIXED_RAW.getName())).thenReturn(NONFIXED_RAW);
  }
  @Test
  @SuppressWarnings("null")
  void testCustomIPEConfig() {
    String path = IPEConfigResourceProvider.buildPath(componentContentResource.getPath(),
        ImmutableSet.of(EDITORIAL_1COL.getName(), SHOWROOM_STANDARD.getName(), NONFIXED_RAW.getName()));

    Resource ipeConfig = context.resourceResolver().getResource(path);
    assertNotNull(ipeConfig);

    List<Resource> ipeConfigChildren = ImmutableList.copyOf(ipeConfig.listChildren());
    assertEquals(1, ipeConfigChildren.size());
    assertEquals("plugins", ipeConfigChildren.get(0).getName());

    Resource aspectRatios = ipeConfig.getChild("plugins/crop/aspectRatios");
    assertNotNull(aspectRatios);

    List<Resource> aspectRatiosChildren = ImmutableList.copyOf(aspectRatios.listChildren());
    assertEquals(3, aspectRatiosChildren.size());
    assertThat(aspectRatiosChildren.get(0), ResourceMatchers.nameAndProps(EDITORIAL_1COL.getName(),
        "name", EDITORIAL_1COL.getLabel() + " (215:102)", "ratio", 1d / EDITORIAL_1COL.getRatio()));
    assertThat(aspectRatiosChildren.get(1), ResourceMatchers.nameAndProps(NONFIXED_RAW.getName(),
        "name", NONFIXED_RAW.getLabel(), "ratio", 0d));
    assertThat(aspectRatiosChildren.get(2), ResourceMatchers.nameAndProps(SHOWROOM_STANDARD.getName(),
        "name", SHOWROOM_STANDARD.getLabel() + " (1055:500)", "ratio", 1d / SHOWROOM_STANDARD.getRatio()));
  }

}
