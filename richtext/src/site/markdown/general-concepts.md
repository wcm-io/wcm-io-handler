## RichText Handler General concepts

The wcm.io RichText Handler is responsible for rendering formatted rich text and helps processing links and images in it.

This page describes these basic concepts, the [usage][usage] page shows examples how to use them.


### Rich text processing with links and images

By default, AEM directly outputs the HTML markup created by the rich text editor when rendering a page. Links that are contained are probably rewritten by the AEM Link Rewriter.

The RichText handler provides a processing of the HTML markup before it is rendered into the page, supporting processing of included links and images by [Link Handler][link-handler] and [Media Handler][media-handler].

It's also possible to transform plain text to rich text (replacing line breaks with `<br>` elements).


### Defining links in rich text for Link Handler

The link handler expects additional properties to be set for each link (e.g. link type, different properties for link targets).

The RichText handler provides a custom RTE link plugin that is integrated into the AEM rich text editor and allows to define links in the same ways with the same properties as it is done in the component edit dialogs. The additional metadata that is stored by this plugin in the markup and the repository is rewritten to standard HTML markup when rendering the rich text.


### Highly customizable

The RichText Handler is highly customizable to adapt it to the needs of your project:

* Hook in custom "Rewrite Content Handlers" that transform certain HTML elements or attributes in the markup based on the project's needs


[usage]: usage.html
[link-handler]: ../link/
[media-handler]: ../media/
