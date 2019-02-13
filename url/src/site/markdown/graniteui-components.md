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

Enhancements over AEM version:

* Dynamically sets the `rootPath` to the current site root


### Site Root Path Browser

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



[usage]: usage.html
