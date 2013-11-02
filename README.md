UrlShortener
============

Fast URL Shortener - Pure Java SE Web Application with embedded web server.

Use java -server -jar UrlShortener.jar to run.

Try it on-line at [http://go2.pt/](http://go2.pt/).

Get the latest stable version at [r9](https://github.com/vilaca/UrlShortener/releases/tag/r9).


How to build your own Url Shortener
===================================

At the source/main/config/ folder you can find the application.properties file with the default configuration.

To customize the install to your liking edit the application.properties file or create a new file at the 'base directory' of your application with new values.

| Field | Default value | Meaning |
|-------|---------------|---------|
| server.port | 80 | Port the server will be listening at. |
| server.ip | 0.0.0.0 | Your server IP. |
| server.backlog | 100 | Backlog for waiting connections. |
| server.accessLog | access_log | File where the access log is written. 
| server.version | software version | Version of the server software being run. |
| google-site-verification | googlee8c2b6528722a6b6.html | Key used by Google Webmaster tools to confirm site ownership. |
| server.redirect | 301 | HTTP response code for Shortened Urls. Valid values are either 301 or 302. |


Dependencies
============

Apache Commons Validator 1.4.0 - http://commons.apache.org/proper/commons-validator/

Log4j2 - http://logging.apache.org/log4j/2.x/


License
=======

GNU Affero General Public License.

