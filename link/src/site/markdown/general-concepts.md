## Link Handler general concepts

The wcm.io Link Handler makes use of a set of basic concepts that helps to enable a flexible and powerful handling of links in your AEM application.

This page describes these basic concepts, the [usage][usage] page shows examples how to use them.


### The aspect of link handling

A lot of components allow editors to define links to other targets, e.g. other AEM content pages, external links or downloadable files stored in AEM Assets. Defining link targets and checking if the link targets are valid requires boilerplate code that should not repeated in all components again and again.

The wcm.io Link Handler provides this link-related functionality as an "aspect" that can be added to any AEM component. Most parts of this aspect functionality can be configured in a central place in the AEM project so in each AEM component only very little link handling code is required. If the predefined Sling Models are used no Java code is required at all.


### Link types

The approach of both the [AEM Core WCM Components][aem-core-wcm-components] and the classic AEM foundation components for defining link is it to have a single text field that can be either an link to another content page or and external URL, and than "guessing" by looking at the string what might be meant.

The wcm.io Link Handler introduces a concept of **link type** that is stored in a separate property to define which type of target is linked upon. This makes the validation and the GUI to pick the link targets easier. The following link types are supported out-of-the-box:

* **Internal**: Link to another content page from _the same site_. If the content page is from another site the link is rewritten to the current site, and is invalid if no matching page exists in the current site.
* **Internal Cross-Context**: Link to another content page from _another site_. No rewriting of the content path to the current site takes place. The link is externalized with the absolute URL of the other site as it may use a different domain name than the current site (this requires proper site configuration of the [URL Handler][url-handler]).
* **External**: Link to an external URL. No validation of this URL takes place, only that the URL is semantically correct. Examples for external URLs: `http://www.mydomain.com`, `https://www.mydomain.com/path1/page1.html`, `mailto:firstname.lastname@mycompany.com`
* **Media**: Link to AEM asset that can be downloaded by the user. Uses a "download" media format defined for the [Media Handler][media-handler] to restrict asset links to a set of allowed file extensions.

It can be configured for each project which of these link types are actually used.

### Redirect templates

AEM by default provides a "Redirect" property in the "Advanced" tab of the page properties dialog. If this is set, the page redirects the user to a different URL or page while loading. This concept works, but has the drawback that it's not clear for the editor if a page is a content page or only used to redirect to another page.

Therefore the wcm.io Link Handler introduces dedicated redirect templates that are only used to declare redirects and visible as such by a special icon and template in the Authoring environment.

When an internal link is defined that points to a redirect page (or when a redirect page is found e.g. by listing child pages in a navigation component) the link handler automatically uses the target defined in the redirect page when building (and validating) the link.

Usually a project defines it's own redirect template by extending the wcm.io Redirect template.


### Touch UI support

The wcm.io Link Handler provides a set of Granite UI components to define links in the Touch UI dialogs in a consistent way in all components of the project.


### Inheriting URL parameters

By default the AEM-built in URL parameters `wcmmode` and `debugClientLibs` are "inherited" to all links in a page, when they are currently used to render the page. This makes it easy especially on the authoring instance to preview a page in "view as published" mode and then navigate the site using the links in the page without leaving this mode.

It's possible to add additional project-specific parameters to this list of inherited URL parameters.


### Highly customizable

The Link Handler is highly customizable to adapt it to the needs of your project:

* Define custom link types
* Customize the markup that is used to build the anchor tags, e.g. defining additional attributes for SEO or web tracking
* Define valid linkable internal pages and redirect pages
* Define root path for selecting files from AEM assets
* Hook in custom logic into the link handling processing by using pre- and postprocessors.


[usage]: usage.html
[url-handler]: ../url/
[media-handler]: ../media/
[aem-core-wcm-components]: https://github.com/adobe/aem-core-wcm-components
