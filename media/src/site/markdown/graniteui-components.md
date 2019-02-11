## Granite UI components


### Media Handler-aware File Upload

This is a customized File Upload component that allows to reference AEM assets or upload binary files as inline assets with enhanced support for the wcm.io Media Handler.

```json
"fileReference": {
  "sling:resourceType": "wcm-io/handler/media/components/granite/form/fileupload",
  "fieldLabel": "Asset reference",
  "rootPath": "/content/dam"
}
```

Enhancements over AEM version:

* A path field is additionally displayed which shows the references of the selected asset, and allows to browse it directly alternatively to drag & drop from the asset finder. The root path of this path field can be configured with the `rootPath` property.
* The referenced asset is checked automatically against the configured set of media formats. A warning is displayed in the edit dialog when the selected asset does not match with the media formats. The media formats are taken from the component definition, or can be set directly for the edit dialog using the properties `mediaFormats`, `mediaFormatsMandatory` and `mediaCropAuto`.
* The properties `name`, `fileNameParameter` and `fileReferenceParameter` are set automatically to their default values as configured in the Media Handler configuration. They only have to be set on the component if different values should be used for a component (e.g. when multiple asset references are stored for one component).
* When the property `fieldDescription` is not set, it is set automatically with information about the expected media formats
* The property `allowUpload` is set to `false` by default
* The property `mimeTypes` is pre-initialized with mimetypes for GIF, JPEG and PNG images


### Media Handler-aware Path Field

This is a customized Path Field component that allows to reference AEM assets with enhanced support for the wcm.io Media Handler.

```json
"fileReference": {
  "sling:resourceType": "wcm-io/handler/media/components/granite/form/fileupload",
  "fieldLabel": "Asset reference",
  "rootPath": "/content/dam"
}
```

Enhancements over AEM version:

* The referenced asset is checked automatically against the configured set of media formats. A warning is displayed in the edit dialog when the selected asset does not match with the media formats. The media formats are taken from the component definition, or can be set directly for the edit dialog using the properties `mediaFormats`, `mediaFormatsMandatory` and `mediaCropAuto`.
* The property `name` is set automatically to it's default value as configured in the Media Handler configuration. They only have to be set on the component if different values should be used for a component (e.g. when multiple asset references are stored for one component).
* The property `rootPath` defaults to `/content/dam`
* When the property `fieldDescription` is not set, it is set automatically with information about the expected media formats
