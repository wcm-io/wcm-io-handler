## URL Handler System Configuration

### Service user configuration

The URL handler requires a service user mapping for detecting client libraries located at `/apps` or `/libs` with "allowProxy" mode, to rewrite resource URLs pointing to them to `/etc.clientlibs`.

Create a principal-based service user mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping=["io.wcm.handler.url:clientlibs-service\=[sling-scripting]"]
```

The principal `sling-scripting` that comes with AEM can be used for this.

This configuration is required **on both author and publish instances**.
