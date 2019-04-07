## Media Handler Component Properties

It is possible to set "component properties" in the component definition (resource type node) of a component. These properties act as default values for the media handling process and do not need to be repeated in the Java code or dialog definitions when defined on component level. They are also used for customizing the cropping ratios in the in-place image editor.

Available component properties:

|Property name                  |Description
|-------------------------------|---------------------------------------------------------------------
| `wcmio:mediaFormats`          | List of media formats accepted by this component.
| `wcmio:mediaFormatsMandatory` | Resolving of all media formats is mandatory.
| `wcmio:mediaCropAuto`         | Enable "auto-cropping" mode for this component by setting to true.

These component properties are also inherited from components linked with the `sling:resourceSuperType' property.
