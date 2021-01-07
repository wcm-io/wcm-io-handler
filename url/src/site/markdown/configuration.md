## URL Handler System Configuration

### Service user configuration

The URL handler requires a service user mapping for detecting client libraries located at `/apps` or `/libs` with "allowProxy" mode, to rewrite resource URLs pointing to them to `/etc.clientlibs`.

Create a principal-based service user mapping with an entry like this:

```
  org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-myapp-wcmio-handler-url
    user.mapping=["io.wcm.handler.url:clientlibs-service\=[sling-scripting]"]
```

The built-in principal `sling-scripting` has read access to `/apps` and `/libs`.

This configuration is required **on both author and publish instances**.
