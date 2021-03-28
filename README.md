# Notary
NoLeaks Notary is a tool that takes digitally signed snapshots of web-pages for court proceedings.

## Key features
- Integrated Chromium browser and WebDriver ver.89.
  <details>
  NoLeaks Notary employs standard Google's Chromium in fullscreen mode that simulates realistic browsing sessions.
  Each session observed by the proxy which records traffic between the browser and Internet.
  In contrast to Puppeteer, PuppeteerHar, ChromeHarCapturer and other tools that control the browser over high-level API, 
  NoLeaks Notary inspects all traffic, including "invisible" (favicons and requests to Google services), providing consistent digital evidence.
  </details>
- Digital signatures according to [NIST](https://csrc.nist.gov/Projects/Hash-Functions/NIST-Policy-on-Hash-Functions).
  <details>
  NoLeaks Notary signs all files included in the snapshot with SHA3-256 Secure Hash.
  Digital signatures can be independently verified by <a href="https://docs.oracle.com/en/java/javase/11/tools/jarsigner.html">Oracle's jarsigner</a> tool:
  <pre>
  $ jarsigner -verify snapshot.zip
  </pre>
  </details>
- Trusted timestamp by GlobalSign.
  <details>
  NoLeaks Notary protects integrity of the snapshot by using an independent Time Stamp Authority.
  The timestamp can be verified by <a href="https://docs.oracle.com/en/java/javase/11/tools/jarsigner.html">Oracle's jarsigner</a> tool:
  <pre>
  $ jarsigner -verify snapshot.zip
  </pre>
  </details>
- Trusted domain names by Cloudflare.
  <details>
  NoLeaks Notary performs encrypted Domain Name System resolution by using DNS-over-HTTPS protocol. 
  This protocol prevents manipulation of data or misconfiguration of the resolver on client side.
  </details>
- Trusted clock source by Cloudflare.
  <details>
  NoLeaks Notary performs timestamping by using Network Time Protocol that prevents 
  misconfiguration of clock on client side.
  </details>
- Strict third-party matching.
  <details>
  Distinction of the remote parties is a common problem for client-side traffic analysers.
  Nowadays webmasters widely use cloud infrastructure that may associate
  one domain name with many IP addresses, pointing each address to many geographically
  distributed processing facilities. There is no way to unambiguously identify respective 
  data controller behind HTTP request. NoLeaks Notary considers two requests belong to the same party if:
  <pre>
  - both domain names equal, or
  - a sub-domain points to the domain's TLS certificate, or
  - a sub-domain points to the domain's IP address.
  </pre>
  </details>

## Snapshot includes
- Web-page inspections:
  - Traffic
    <details><summary>example</summary>
    <pre>
    {
      "artifacts": [
        {
          "file": "TrafficInspection.First.har",
          "type": "application/json"
        },
        {
          "file": "TrafficInspection.Returning.har",
          "type": "application/json"
        },
        {
          "file": "TrafficInspection.Incognito.har",
          "type": "application/json"
        }
      ],
      "inspection": "TrafficInspection"
    }
    </pre>
    </details>
  - Resources
    <details><summary>example</summary>
    <pre>
    {
      "firstParty": [
        {
          "url": "http://test.noleaks.eu/favicon.ico",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://test.noleaks.eu/",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://tracker.noleaks.eu/last.php",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://tracker.noleaks.eu/redirect.php",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://tracker.noleaks.eu/etag.php",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://test.noleaks.eu/bootstrap.min.css",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://tracker.noleaks.eu/redirect.php?id=04488d0a4a73c69168ecd4efd0123723",
          "ip": "51.15.79.110"
        },
        {
          "url": "http://tracker.noleaks.eu/cookie.php",
          "ip": "51.15.79.110"
        }
      ],
      "thirdParty": [
        {
          "url": "https://lh4.googleusercontent.com/proxy/kFIJNnm2DMbS3B5LXaIdm2JKI6twGWwmzQbcJCfqTfuaH_ULD50v1Z3BGPEF32xTPRvgGLx492zcy_kcatCde2wmz-9ZYFqifbJRMl2DzyE=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        },
        {
          "url": "https://www.google.com/async/ddljson?async=ntp:2",
          "ip": "172.217.168.36"
        },
        {
          "url": "https://lh6.googleusercontent.com/proxy/fUx750lchxFJb3f37v_-4iJPzcTKtJbd5LDRO7S9Xy7nkPzh7HFU61tN36j4Diaa9Yk3K7kWshRwmqcrulnhbeJrRpIn79PjHN-N=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        },
        {
          "url": "https://lh6.googleusercontent.com/proxy/KyyCsF6dIQ783r3Znmvdo76QY2RgzcR5t4rnA5kKjsmrlpsb_pWGndQkyuAI4mv68X_9ZX2Edd-0FP4iQZRFm8UAW3oDX8Coqk3C85UNAX3H4Eh_5wGyDB0SY6HOQjOXVQ=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        },
        {
          "url": "https://accounts.google.com/ListAccounts?gpsia=1&source=ChromiumBrowser&json=standard",
          "ip": "172.217.168.45"
        },
        {
          "url": "https://lh6.googleusercontent.com/proxy/4IP40Q18w6aDF4oS4WRnUj0MlCCKPK-vLHqSd4r-RfS6JxgblG5WJuRYpkJkoTzLMS0qv3Sxhf9wdaKkn3vHnyy6oe7Ah5y0=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        },
        {
          "url": "https://www.google.com/async/newtab_promos",
          "ip": "172.217.168.36"
        },
        {
          "url": "https://www.google.com/async/newtab_ogb?hl=en-US&async=fixed:0",
          "ip": "172.217.168.36"
        },
        {
          "url": "https://www.google.com/async/newtab_shopping_tasks?hl=en-US",
          "ip": "172.217.168.36"
        },
        {
          "url": "https://lh5.googleusercontent.com/proxy/xvtq6_782kBajCBr0GISHpujOb51XLKUeEOJ2lLPKh12-xNBTCtsoHT14NQcaH9l4JhatcXEMBkqgUeCWhb3XhdLnD1BiNzQ_LVydwg=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        },
        {
          "url": "https://lh3.googleusercontent.com/proxy/d_4gDNBtm9Ddv8zqqm0MVY93_j-_e5M-bGgH-bSAfIR65FYGacJTemvNp9fDT0eiIbi3bzrf7HMMsupe2QIIfm5H7BMHY3AI5rkYUpx-lQ=w170-h85-p-k-no-nd-mv",
          "ip": "172.217.168.33"
        }
      ],
      "inspection": "ResourcesInspection"
    }
    </pre>
    </details>
  - Visibility
    <details><summary>example</summary>
    <pre>
    {
      "artifacts": [
        {
          "file": "VisibilityInspection.First.png",
          "type": "image/png"
        }
      ],
      "inspection": "VisibilityInspection"
    }
    </pre>
    </details>
  - Modality
    <details><summary>example</summary>
    <pre>
    {
      "artifacts": [
        {
          "file": "ModalityInspection.First.png",
          "type": "image/png",
          "scrollable": true
        }
      ],
      "inspection": "ModalityInspection"
    }
    </pre>
    </details>
  - Cookies
    <details><summary>example</summary>
    <pre>
    {
      "firstParty": [
        {
          "name": "id",
          "value": "12cc65518a2e4407850524313073d095",
          "domain": "tracker.noleaks.eu",
          "expiryDate": "2021-03-27T17:33:19.000+0000",
          "persistent": true
        }
      ],
      "thirdParty": [
        {
          "name": "NID",
          "value": "212=SahyPZvqTJgP1xsKeWHZeXYjda00eLcnZ9C6Hr_KXNPxc2TvikzkdqBTBXFFhPevMYvxXFx8PE_-orYF7KUkvCwtWbff1wwoKjaOifsTgx4_nnbrRHqU6i3pHr38y1BOR1byQ2iqQq0US1-o4NlYSLnirb0AWXnVWIBd1J5gLZA",
          "domain": "google.com",
          "expiryDate": "2021-09-26T17:29:19.000+0000",
          "persistent": true
        }
      ],
      "inspection": "CookieInspection"
    }
    </pre>
    </details>
  - ETags
    <details><summary>example</summary>
    <pre>
    {
      "firstParty": [
        {
          "resource": "http://test.noleaks.eu/",
          "value": "\"5f2d7121-129e\"",
          "contentHash": "d408ffc7b78ffa38979e5f49dd9959829ad7fa82"
        },
        {
          "resource": "http://tracker.noleaks.eu/etag.php",
          "value": "\"81cd800152b9f116be80662854c8f317\"",
          "contentHash": "356a192b7913b04c54574d18c28d46e6395428ab"
        },
        {
          "resource": "http://test.noleaks.eu/bootstrap.min.css",
          "value": "\"5f2d7124-2565e\"",
          "contentHash": "3ae9bb0e7929489abd23736ae892939c8fe98645"
        },
        {
          "resource": "http://tracker.noleaks.eu/etag.php",
          "value": "\"81cd800152b9f116be80662854c8f317\"",
          "contentHash": "da4b9237bacccdf19c0760cab7aec4a8359010b0"
        },
        {
          "resource": "http://tracker.noleaks.eu/etag.php",
          "value": "\"e38bc4b87928928f35c33b6f597632e6\"",
          "contentHash": "356a192b7913b04c54574d18c28d46e6395428ab"
        }
      ],
      "thirdParty": [
        {
          "resource": "https://update.googleapis.com/service/update2/json?cup2key=10:2543876387&cup2hreq=fb9d37372d7660ee67cd796ec151dc5237cc3454bbc4e6dc9e6e559ddd722457",
          "value": "W/\"304502204b49e5631e118b0265ec76f70b97da96ec31365e8da806238a44c120baee2583022100c519db08f6749ee565a626a527d9cd036ace148abfeea69a49a019a8398c094e:fb9d37372d7660ee67cd796ec151dc5237cc3454bbc4e6dc9e6e559ddd722457\"",
          "contentHash": "ea08fb9072349eec8ee8d4f88440a67b5abc32f0"
        },
        {
          "resource": "http://edgedl.gvt1.com/edgedl/chromewebstore/L2Nocm9tZV9leHRlbnNpb24vYmxvYnMvYjFkQUFWdmlaXy12MHFUTGhWQUViMUVlUQ/0.57.44.2492_hnimpnehoodheedghdeeijklkeaacbdc.crx",
          "value": "\"2e2fe7\"",
          "contentHash": "86b1b058e1e7d2f1f35e830db446b59e15670e5e"
        }
      ],
      "inspection": "EtagInspection"
    }
    </pre>
    </details>
  - TLS Certificate
    <details><summary>example</summary>
    <pre>
    {
      "valid": true,
      "path": [
        {
          "type": "X.509",
          "valid": true,
          "issuer": "CN=R3, O=Let's Encrypt, C=US",
          "serial": 416448724033966231022324633582038357897465,
          "expiryDate": "2021-06-17T06:04:43.000+0000"
        },
        {
          "type": "X.509",
          "valid": true,
          "issuer": "CN=DST Root CA X3, O=Digital Signature Trust Co.",
          "serial": 85078157426496920958827089468591623647,
          "expiryDate": "2021-09-29T19:21:40.000+0000"
        }
      ],
      "inspection": "TlsCertificateInspection"
    }
    </pre>
    </details>
- Inspection artifacts such as screenshots and HTTP archives.
  <details>
  HTTP Archive format (HAR) contains detailed information about each HTTP transaction. Recorded HAR files can be
  independently verified by <a href="https://toolbox.googleapps.com/apps/har_analyzer/">Google's HAR Analyzer</a> tool.
  </details>
- Machine-readable Metadata in JSON format.
  <details>
  Metadata refers to electronic information about other electronic data, which may reveal the identification, 
  origin or history of the digital evidence, as well as relevant dates and times. NoLeaks Notary gathers effective 
  Chrome options, properties of the simulated device and system properties, including OS, network configuration and 
  execution environment.
  </details>
- Machine-readable JSON Schema of Metadata.
  <details>
  NoLeaks Notary annotates gathered Metadata according to
  <a href="https://json-schema.org">JSON Schema standard</a> that helps document data for further analysis 
  and ensures its quality.
  </details>

## Requirements
- JDK 11+
- Linux 64-bit
- X.509 Certificate

## Usage
Download the latest version from the [Releases](https://github.com/noleakseu/notary/releases/).
```
$ jarsigner -verify notary.jar
jar verified.

$ java -jar notary.jar -h
usage: notary [-h] -k KEYSTORE -a ALIAS -s STOREPASS [-d {iPhone,iPadPro,Nexus,Kindle}] [-l {en}] [-u URL]
Notary 1.7.2-beta by NoLeaks
named arguments:
  -h, --help             show this help message and exit
  -k KEYSTORE, --keystore KEYSTORE
                         Specify P12 keystore
  -a ALIAS, --alias ALIAS
                         Specify keystore alias
  -s STOREPASS, --storepass STOREPASS
                         Specify keystore password
  -d {iPhone,iPadPro,Nexus,Kindle}, --device {iPhone,iPadPro,Nexus,Kindle}
                         Specify device (CLI mode only) (default: Kindle)
  -l {en}, --language {en}
                         Specify language (CLI mode only) (default: en)
  -u URL, --url URL      Specify URL (CLI mode only) (default: )
```
Create your own X.509 Certificate for snapshots signing.
```
$ keytool -genkeypair -alias snapshot -keyalg RSA -keysize 2048 -dname "CN=selfsigned" -validity 7 -storetype PKCS12 -keystore snapshot.p12 -storepass snapshot
```
Take a snapshot in CLI:
```
$ java -jar notary.jar --keystore snapshot.p12 --storepass snapshot --alias snapshot --url http://test.noleaks.eu
$ jarsigner -verbose -verify snapshot.zip
jar verified.
```
Take a snapshot via API:
```
$ java -jar notary.jar --keystore snapshot.p12 --storepass snapshot --alias snapshot
# from another console
$ wget 'http://127.0.0.1:8000/?url=http%3A%2F%2Ftest.noleaks.eu&device=Kindle&language=en' -O snapshot.zip
$ jarsigner -verbose -verify snapshot.zip
jar verified.
```

# Roadmap
1. Human-readable Evidence Summary in
    - Bulgarian
    - Croatian
    - Czech
    - Danish
    - Dutch
    - English
    - Estonian
    - Finnish
    - French
    - German
    - Greek
    - Hungarian
    - Irish
    - Italian
    - Latvian
    - Lithuanian
    - Maltese
    - Polish
    - Portuguese
    - Romanian
    - Slovak
    - Slovenian
    - Spanish
    - Swedish
2. Deploy as a service
3. Replace NTP with NTS
