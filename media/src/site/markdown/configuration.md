## Media Handler System Configuration


### Service user configuration for Asset Rendition Metadata

The DAM source implementation for Media Handler requires a background service that detects additional metadata for each rendition that is added, modified or removed for a DAM asset (to calculate their width and height and store them in the repository). Another service user mapping is required that allows the dynamic media support to read configured image profiles.

This service needs a principal-based mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

_Media Handler 2.0 and up_

```
user.mapping=["io.wcm.handler.media:dam-rendition-metadata=[dam-writer-service]"]
```

_Media Handler 1.x_
```
user.mapping=["io.wcm.handler.media=[dam-writer-service]"]
```

The principal `dam-writer-service` that comes with AEM can be used for this.

This configuration is required **only on author instances**.


### Service user configuration for Dynamic Media support

Another service user mapping is required that allows the dynamic media support to read configured image profiles.

This service needs a principal-based mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping=["io.wcm.handler.media:dynamic-media-support=[configuration-reader-service]"]
```

The principal `configuration-reader-service` that comes with AEM can be used for this.

This configuration is required **for both author and publish instances**.


### Previews for inline images in Authoring

The "FileUpload" Granite UI component provides a preview of the selected image in the edit dialog. This does not work out of the box if an inline image was uploaded to the component's resource (instead of selecting a DAM asset). In this case the component has to provide a preview of the image via the `img` selected. The media handler provides a servlet for this, but it has to be configured for each resource type that requires it.

Example for the [wcm.io WCM Core Components][wcmio-wcm-core-components] Responsive Image component for the factory configuration `io.wcm.handler.media.impl.InlineImageAuthorPreviewServlet`:

```
sling.servlet.resourceTypes=["wcm-io/wcm/core/components/wcmio/responsiveimage/v1/responsiveimage"]
```

This configuration is required **only on author instances**.


### Workflow process for rendition metadata

There is also a workflow process implementation named "wcm.io Media Handler: Rendition metadata" that can be applied to any existing asset to generate the metadata (width/height information) for all assets that were present in the system before the background service was deployed. Create a custom workflow, add a "Process Step" and assign this process with "Handler Advance" flag.

However, this workflow process should not be part of the main "DAM Update Asset" workflow, as this would trigger the metadata generation twice for new assets.

See this [How-to article][workflow-how-to] for details.




[workflow-how-to]: https://wcm-io.atlassian.net/wiki/x/AQDrRw
[wcmio-wcm-core-components]: https://wcm.io/wcm/core-components/
