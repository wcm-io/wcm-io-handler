/**
 * Extends /libs/cq/gui/components/authoring/rte/coralui3/js/RTE.CQLinkPlugin.js
 * with a link dialog similar to wcm.io Link Handler link dialog.
 */
;(function($) {
  
  wcmio.handler.richtext.rte.plugins.LinkPlugin = new Class({

    extend: CUI.rte.plugins.CQLinkPlugin,

    notifyPluginConfig: function(config) {

      // dynamically detect root paths
      var currentPagePath = Granite.author.ContentFrame.getContentPath();
      var siteRootPath = this._getAbsoluteParent(currentPagePath, 4);
      var tenantRootPath = this._getAbsoluteParent(currentPagePath, 2);
      
      config.linkDialogConfig = config.linkDialogConfig || {};
      config.linkDialogConfig.dialogProperties = config.linkDialogConfig.dialogProperties || {};
      config.linkDialogConfig.dialogProperties.rootPath = siteRootPath;

      config.linkDialogConfig.dialogProperties.linkTypes = config.linkDialogConfig.dialogProperties.linkTypes || {
        "internal": {
          value: "internal",
          text: "Internal (same site)"
        },
        "internalCrossContext": {
          value: "internalCrossContext",
          text: "Internal (other site)"
        },
        "external": {
          value: "external",
          text: "External"
        },
        "media": {
          value: "media",
          text: "Asset"
        }
      };
      
      config.linkDialogConfig.dialogProperties.targetItems = config.linkDialogConfig.dialogProperties.targetItems || {
        "_self": {
          value: "_self",
          text: "Same window"
        },
        "_blank": {
          value: "_blank",
          text: "New window"
        }
      };
      
      // call the "super" method
      this.inherited(arguments);
    },
    
    getDialogClass: function() {
      return wcmio.handler.richtext.rte.plugins.LinkDialog;
    },
    
    initializeUI: function (tbGenerator) {
      // register icon for this plugins toolbar actions
      tbGenerator.registerIcon("wcmio.handler.richtext.links#modifylink", "link");
      tbGenerator.registerIcon("wcmio.handler.richtext.links#unlink", "linkOff");
      // call the "super" method
      this.inherited(arguments);
    },
    
    _getAbsoluteParent: function (path, level) {
      var idx = 0;
      var length = path.length;
      while (level >= 0 && idx < length) {
        idx = path.indexOf("/", idx + 1);
        if (idx < 0) {
          idx = length;
        }
        level--;
      }
      return level >= 0 ? "" : path.substring(0, idx);
    }

  });

})(window.jQuery);

// register plugin
CUI.rte.plugins.PluginRegistry.register("wcmio.handler.richtext.links", wcmio.handler.richtext.rte.plugins.LinkPlugin);
