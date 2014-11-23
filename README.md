[![Build Status](https://travis-ci.org/vilaca/UrlShortener.svg?branch=dev-abuse)](https://travis-ci.org/vilaca/UrlShortener)


UrlShortener
============

URL Shortener - Pure Java SE Web Application with embedded Jetty 9 Http server.

Use java -server -jar UrlShortener.jar to run.

Try it on-line at [http://go2.pt/](http://go2.pt/) or download the [latest version jar](http://vilaca.eu/Download/UrlShortener.jar).


Features
========

- Single Jar Application;
- No Application server, Servelet Container or External Database dependencies;
- Refuses abusive Phishing and Malware Urls;
- Actively scans short Urls to identify possible threats;
- Apache style logging for compatibility with existing tools;
- Integration with Google Webmaster tools, Safebrowsing API and PhishTank API;
- Configurable status redirects;
- Light footprint, can run on very constrained systems.


Get the latest version
======================

The stable version used in production at http://go2.pt is [v0.1.0](https://github.com/vilaca/UrlShortener/releases/tag/v0.1.0).


Server configuration
--------------------

| Field | Default value | Meaning |
|-------|:-------------:|---------|
| server.port | 80 | Port the server will be listening at. |
| server.ip | 0.0.0.0 | Your server IP. |
| server.accessLog | access_log | Access log filename. |
| server.version |  | Version of the server software being run. |
| server.redirect | 301 | HTTP response code for Shortened Urls. Valid values are either 301 or 302. |
| server.cache | 24 | Amount of time (in hours) static pages should be cached. |
| server.domain | | Domain name where site is hosted. |
| database.folder |  | Where to place Url Database files. |
| google-site-verification | | Key used by Google Webmaster tools to confirm site ownership. |
| safe-lookup-api-key | | API key to integrate with Google safe browsing lookup API. |
| phishtank-api-key | | API key to integrate with Phishtank API. |
| watchdog.wait | 5 | Time in seconds Url Watchdog is called after application starts. |
| watchdog.interval | 16 | Time in minute Url Watchdog sleeps. |

Dependencies
============

Apache Commons Validator 1.4.0 - http://commons.apache.org/proper/commons-validator/

Log4j2 - http://logging.apache.org/log4j/2.x/

Jetty 9 - http://www.eclipse.org/jetty/

License
=======

GNU Affero General Public License.

