# Notary
NoLeaks Notary is a tool that takes digitally signed snapshots of web-pages for court proceedings.

## Key features
- Integrated Chromium browser and WebDriver ver.89.
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
  data controller behind an HTTP request. NoLeaks Notary considers two requests belong to the same party if:
  <pre>
  - both domain names equal, or
  - a sub-domain points to the domain's TLS certificate, or
  - a sub-domain points to the domain's IP address.
  </pre>
  </details>

## Snapshot includes
- Web-page inspections:
  - Traffic
  - Resources
  - Visibility
  - Modality
  - Cookies
  - ETags
  - TLS Certificate
- Inspection artifacts such as screenshots and HTTP archives.
- Machine-readable Metadata in JSON format.
- Machine-readable JSON Schema of Metadata.

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
