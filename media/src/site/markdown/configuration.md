## Media Handler System Configuration


### Service user configuration

The DAM source implementation for Media Handler requires a background service that detects additional metadata for each rendition that is added, modified or removed for a DAM asset (to calculate their width and height and store them in the repository).

This service needs a service user mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping=["io.wcm.handler.media=wcmioDamSystemUser"]
```

The user should have `jcr:read` and `rep:write` privileges on `/content/dam`. This configuration and system user is only required on Author instances.

This configuration is required **only on author instances**.


### Workflow process for rendition metadata

There is also a workflow process implementation named "wcm.io Media Handler: Rendition metadata" that can be applied to any existing asset to generate the metadata (width/height information) for all assets that were present in the system before the background service was deployed. Create a custom workflow, add a "Process Step" and assign this process with "Handler Advance" flag.

However, this workflow process should not be part of the main "DAM Update Asset" workflow, as this would trigger the metadata generation twice for new assets.

See this [How-to article][https://wcm-io.atlassian.net/wiki/x/AQDrRw] for details.