## RichText Handler RTE link plugin

*Please note:* The RTE link plugin is only supported in AEM 6.3 and higher.

The wcm.io RichText Handler includes a plugin for the [AEM Rich Text Editor (RTE)][aem-rte] that provides all the features from the [wcm.io Link Handler][link-handler] standard link dialog. It shows a select field to pick the link type and the associated fields. The list of link types that is shown depended on the project's link handler configuration. It is possible to add your own fields.

![RTE link plugin](images/rte-link-plugin.png)


### Enable the RTE link plugin

To enable plugin you have to disable the AEM built-in link (`links`) plugin and instead enable the wcm.io RichText Handler link plugin (`wcmio-links`):

```json
// Configure rich text plugins
"rtePlugins": {
  "links": {
    "features": "-"
  },
  "wcmio-links": {
    "features": [
      "modifylink",
      "unlink"
    ]
  },
  // ...
}
```

Within the UI section RTE configuration where the RTE toolbars are defined you have to replace the references to the `links` plugin with `wcmio-links`:

```json
"uiSettings": {
  "cui": {
    "inline": {
      "toolbar": [
        // ...
        "wcmio-links#modifylink",
        "wcmio-links#unlink",
        // ...
        ]
      }
    },
    "fullscreen": {
      "toolbar": [
        // ...
        "wcmio-links#modifylink",
        "wcmio-links#unlink",
        // ...
      ],
    }
  }
}
```

For a complete example see [richTextConfig.json][rte-full-example].



### Customize the RTE link plugin dialog

If you want to customize the link plugin dialog you have to create your own custom RTE link plugin that inherits from the wcm.io RichText Handler link plugin. Then you can override some of the methods that control which fields are displayed for each link type or all link types.

Example for a custom `linkPlugin.js`:

```js
/**
 * Extends wcm.io Richtext Handler RTE Link Plugin with project-specific functionality.
 */
;(function($) {

  myproject.rte.plugins.LinkPlugin = new Class({

    extend: wcmio.handler.richtext.rte.plugins.LinkPlugin,
    
    getDialogClass: function() {
      return myproject.rte.plugins.LinkDialog;
    },
    
    initializeUI: function (tbGenerator) {
      // register icon for this plugins toolbar actions
      tbGenerator.registerIcon("myproject.links#modifylink", "link");
      tbGenerator.registerIcon("myproject.links#unlink", "linkOff");
      // call the "super" method
      this.inherited(arguments);
    }

  });

})(window.jQuery);

// register plugin
CUI.rte.plugins.PluginRegistry.register("myproject.links", myproject.rte.plugins.LinkPlugin);
```

Example for a custom `linkDialog.js`:

```js
/**
 * Custom link dialog that provides all field supported by the link handler.
 */
;(function($) {
  
  myproject.rte.plugins.LinkDialog = new Class({

    extend: wcmio.handler.richtext.rte.plugins.LinkDialog,

    /**
     * Add fields visible for all link types.
     */
    addAllLinkTypeFields: function(frag, config) {
      
      // call the "super" method
      this.inherited(arguments);
      
      frag.appendChild(this.createColumnItem({
        name: "linkNoFollow",
        fn: this.createCheckbox,
        label: "No Follow"
      }));
    }

  });

})(window.jQuery);
````

For more details have a look into the [sources of the wcm.io RichText Handler link plugin][rte-link-plugin-sources].




[aem-rte]: https://helpx.adobe.com/experience-manager/6-4/sites/administering/using/rich-text-editor.html
[link-handler]: ../link/
[rte-full-example]: https://github.com/wcm-io/wcm-io-samples/blob/develop/bundles/core/src/main/webapp/app-root/components/global/include/richTextConfig.json
[rte-link-plugin-sources]: https://github.com/wcm-io/wcm-io-handler/tree/develop/richtext/src/main/webapp/app-root/clientlibs/rte.plugins/js
