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
import static io.wcm.handler.media.testcontext.DummyMediaFormats.SHOWROOM_STANDARD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.hamcrest.ResourceMatchers;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
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

import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.wcm.commons.util.RunMode;

@ExtendWith(MockitoExtension.class)
public class IPEConfigResourceProviderTest {

  @Rule
  public AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
  @Mock
  public ComponentManager componentManager;
  @Mock
  public MediaFormatHandler mediaFormatHandler;

  private Resource componentContentResource;

  @BeforeEach
  public void setUp() {
    context.runMode(RunMode.AUTHOR);

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
  }

  @Test
  public void testCustomIPEConfig() {
    String path = IPEConfigResourceProvider.buildPath(componentContentResource.getPath(),
        ImmutableSet.of(EDITORIAL_1COL.getName(), SHOWROOM_STANDARD.getName()));

    Resource ipeConfig = context.resourceResolver().getResource(path);
    assertNotNull(ipeConfig);

    List<Resource> ipeConfigChildren = ImmutableList.copyOf(ipeConfig.listChildren());
    assertEquals(1, ipeConfigChildren.size());
    assertEquals("plugins", ipeConfigChildren.get(0).getName());

    Resource aspectRatios = ipeConfig.getChild("plugins/crop/aspectRatios");
    assertNotNull(aspectRatios);

    List<Resource> aspectRatiosChildren = ImmutableList.copyOf(aspectRatios.listChildren());
    assertEquals(2, aspectRatiosChildren.size());
    assertThat(aspectRatiosChildren.get(0), ResourceMatchers.nameAndProps(EDITORIAL_1COL.getName(),
        "name", EDITORIAL_1COL.getLabel() + " (215:102)", "ratio", 1 / EDITORIAL_1COL.getRatio()));
    assertThat(aspectRatiosChildren.get(1), ResourceMatchers.nameAndProps(SHOWROOM_STANDARD.getName(),
        "name", SHOWROOM_STANDARD.getLabel() + " (1055:500)", "ratio", 1 / SHOWROOM_STANDARD.getRatio()));
  }

}
