## Granite UI components


### Site Root Path Field

This is a customized Path Field that always sets the root path to the the "site root" of the current site. That is usually the root page of the current site root. The site root is configured in the URL handler configuration, see [usage][usage].

```json
"field": {
  "sling:resourceType": "wcm-io/handler/url/components/granite/form/siteRootPathField",
  "name": "./field",
  "fieldLabel": "Internal Page"
}
```

This component extends the [wcm.io Granite UI components Path Field][wcmio-wcm-ui-granite-pathfield]. Enhancements over this version:

* Dynamically sets the `rootPath` to the current site root
* A fallback root path can be configured via a `fallbackRootPath` property - it is used when no site root path could be detected


### Site Root Path Browser

_Please note: It is recommended to use the Path Field instead, because the Path Browser is deprecated in AEM._

This is a customized Path Browser that always sets the root path to the the "site root" of the current site. That is usually the root page of the current site root. The site root is configured in the URL handler configuration, see [usage][usage].

```json
"field": {
  "sling:resourceType": "wcm-io/handler/url/components/granite/form/siteRootPathBrowser",
  "name": "./field",
  "fieldLabel": "Internal Page"
}
```

Enhancements over AEM version:

* Dynamically sets the `rootPath` to the current site root
* A fallback root path can be configured via a `fallbackRootPath` property - it is used when no site root path could be detected



[usage]: usage.html
[wcmio-wcm-ui-granite-pathfield]: https://wcm.io/wcm/ui/granite/components.html#Path_Field
