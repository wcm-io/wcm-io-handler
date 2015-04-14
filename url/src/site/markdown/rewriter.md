## Sling Rewriter Integration


### Introduction

Sling has the possibility to rewrite the output/generated markup of a page via a pipelining feature, See [Output Rewriting Pipelines][sling-rewriter] for details.

This feature is activated in AEM by default and is used for the AEM-builtin link checker and link rewriting feature.

The URL handler provides an a transformer implementation named `wcm-io-urlhandler-externalizer` that can be used to externalize resource URLs within the markup (e.g. links to stylesheets, java script files, static images and others). It is not intended to externalize links to content pages or DAM assets.


### Defining your own rewriter configuration

To enable the `wcm-io-urlhandler-externalizer` you have to define your own rewriter configuration. This configuration has to be stored in the repository at a path like `/apps/{appName}/config/rewriter/{configName}`. The `{appName}` part has to be a single node name, no hierarchy is allowed here.

Example:

```json
{
  "jcr:primaryType": "nt:unstructured",
  "enabled": true,
  "generatorType": "htmlparser",
  "transformerTypes": [
    "linkchecker",
    "mobile",
    "mobiledebug",
    "contentsync",
    "wcm-io-urlhandler-externalizer"
  ],
  "serializerType": "htmlwriter",
  "order": "1",
  "contentTypes": [
    "text/html"
  ],
  "transformer-wcm-io-urlhandler-externalizer": {
    "rewriteElements": [
      "img:src",
      "link:href",
      "script:src"
    ]
  }
}
```
Additional remarks:

* You can set define an additional property to apply the configuration only to certain resource types or resource super types. Example:

```json
"resourceTypes": [
  "/apps/myApp/base/components/global/page"
]
```

* You can remove some or all of the AEM-default transformers if you do not need them.
* If you want to rewrite uncommon tag names like `<use xlink:href="...">` you may have to update the configuration of the AEM-built-in `htmlparser` generator. Example:

```
"generator-htmlparser": {
  "includeTags": [
    "A",
    "/A",
    "IMG",
    "AREA",
    "FORM",
    "BASE",
    "LINK",
    "SCRIPT",
    "BODY",
    "/BODY",
    "USE",
    "/USE"
  ]
}
```


### Configuring the wcm-io-urlhandler-externalizer

You can control which element names and attribute names are rewritten via the `rewriteElements` attribute of the transformer configuration. This attribute contains an array with element name and attribute name separated by ":".

The default values are:

* `img:src`
* `link:href`
* `script:src`



[sling-rewriter]: https://sling.apache.org/documentation/bundles/output-rewriting-pipelines-org-apache-sling-rewriter.html
