## Media Handler System Configuration

### Service user configuration

The DAM source implementation for Media Handler required a background services that detects additional metadata for each rendition that is added, modified or removed for a DAM asset (to calculate their width and height and store them in the repository).

This service needs a service user mapping for the factory configuration `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended` with an entry like this:

```
user.mapping="[io.wcm.handler.media=damuser]"
```

You should assign a user that has read/write permissions on `/content/dam`.
