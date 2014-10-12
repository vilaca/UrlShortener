UrlShortener
============

URL Shortener - Pure Java SE Web Application with embedded Jetty 9 Http server.

Use java -server -jar UrlShortener.jar to run.

Try it on-line at [http://go2.pt/](http://go2.pt/) or download the [latest version jar](http://vilaca.eu/Download/UrlShortener.jar).


Features
========

- Single Jar Application;
- No Application server, Servelet Container or External Database dependencies;
- Refuses abusive Phishing Urls;
- Easily customized with PHP-like smart tags;
- Apache style logging for compatibility with existing tools;
- Integration with Google webmaster tools and PhishTank API;
- Configurable status redirects;
- Light footprint, can run on very constrained systems;
- Included Eclipse Java project.


Get the latest version
======================

The current stable version is [v0.1.0](https://github.com/vilaca/UrlShortener/releases/tag/v0.1.0).


How to customize the instalation
===================================

At the source/main/config/ folder you can find the application.properties file with the default configuration.

To customize the install to your liking edit the application.properties file or create a new file at the 'base directory' of your application with new values.


Server configuration
--------------------

The server configuration can be edited without having to change the source code by editing the following properties in the application.properties file.


| Field | Default value | Meaning |
|-------|:-------------:|---------|
| server.port | 80 | Port the server will be listening at. |
| server.ip | 0.0.0.0 | Your server IP. |
| server.backlog | 100 | Backlog for waiting connections. |
| server.accessLog | access_log | File where the access log is written. 
| server.version | go2.pt | Version of the server software being run. |
| google-site-verification | | Key used by Google Webmaster tools to confirm site ownership. |
| server.redirect | 301 | HTTP response code for Shortened Urls. Valid values are either 301 or 302. |
| enforce-domain | | Must be your site domain plus a '/' |


Content and layout
------------------

If you deploy your own Url Shortener you'll want to edit those values for sure.

They control what is displayed in the Url Shortener html.


| Field | Default value | Meaning |
|-------|:-------------:|---------|
| web.title | URL Shortener | HTML page title tag content. Displayed on the browser tab. |
| web.header | go2.pt | Site logo. |
| web.footer | Download this site source code... | Optional footer text |


Further customizations
----------------------

The following files can be edited for a deeper customization:

| File | Type | Contents |
|------|:----:|----------|
| src / main / resources / index.html | HTML | Application main page. |
| src / main / resources / screen.css | CSS | Application style and layout. |
| src / main / resources / 404.html | HTML | Error page for invalid links. |
| src / main / resources / ajax.js | JS | Client side ajax script. |
| src / main / resources / robots.txt | Text | Robots file. |


Dependencies
============

Apache Commons Validator 1.4.0 - http://commons.apache.org/proper/commons-validator/

Apache Http-Components - http://hc.apache.org/

Log4j2 - http://logging.apache.org/log4j/2.x/

Jetty 9 - http://www.eclipse.org/jetty/

License
=======

GNU Affero General Public License.

