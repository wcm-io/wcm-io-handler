## Granite UI components


### Internal Link Type Path Field

This is a customized Path Field that always sets the root path to the link root path as defined by the Link Handler configuration for internal links. By default, this is the site root path as defined by the URL Handler configuration.

```json
"field": {
  "sling:resourceType": "wcm-io/handler/link/components/granite/form/internalLinkPathField",
  "fieldLabel": "Internal page (same site)"
}
```

Enhancements over AEM version:

* Dynamically sets `rootPath` to the link root path as returned by the Link Handler configuration
* Dynamically sets `name` to the default property name for internal links


### Internal Link Cross Context Type Path Field

This is a customized Path Field that always sets the root path to the link root path as defined by the Link Handler configuration for internal cross-context links. By default, this is `/content`.

```json
"field": {
  "sling:resourceType": "wcm-io/handler/link/components/granite/form/internalCrossContextLinkPathField",
  "fieldLabel": "Internal Page (other site)"
}
```

Enhancements over AEM version:

* Dynamically sets `rootPath` to the link root path as returned by the Link Handler configuration
* Dynamically sets `name` to the default property name for internal cross-context links


### Media Link Type Path Field

This is a customized Path Field that always sets the root path to the link root path as defined by the Link Handler configuration for media links. By default, this is `/content/dam`.

```json
"field": {
  "sling:resourceType": "wcm-io/handler/link/components/granite/form/mediaLinkPathField",
  "fieldLabel": "Asset reference"
}
```

This component extends the "Media Handler-aware Path Field". Enhancements over this version:

* Dynamically sets `rootPath` to the link root path as returned by the Link Handler configuration
* Dynamically sets `name` to the default property name for media links
* Dynamically sets `mediaFormats` to a list of all media formats with "download" flag


### Link reference container

Component that defines the full set of fields for defining a link with it's properties to be included in a dialog tab. The link types are displayed dynamically as configured in the link handler configuration. It is possible to add your own fields.

```json
{
  "jcr:primaryType": "nt:unstructured",
  "sling:resourceType": "granite/ui/components/coral/foundation/fixedcolumns",
  "jcr:title": "Link",
  "margin": true,
  "cq:showOnCreate": false,
  "items": {
    "column": {
      "sling:resourceType": "granite/ui/components/coral/foundation/container",
      "items": {
        "linkRef": {
          "sling:resourceType": "wcm-io/handler/link/components/granite/form/linkRefContainer",
          "showLinkTitle": true
        }
      }
    }
  }
}
```
