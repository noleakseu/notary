# Notary
NoLeaks Notary is a tool that takes digitally signed snapshots of web-pages for court proceedings.

## Key features
- Integrated Chromium browser and WebDriver ver.89.
- Trusted domain names (DNS-over-HTTPS).
- SHA3-256 signatures.
- Strict third-party matching.

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
Notary 1.7.1-beta by NoLeaks
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
$ keytool -genkeypair -alias notary -keyalg RSA -keysize 2048 -dname "CN=selfsigned" -validity 7 -storetype PKCS12 -keystore notary.p12 -storepass notary
```
Take a snapshot in CLI:
```
$ java -jar notary.jar --keystore notary.p12 --storepass notary --alias notary --url http://test.noleaks.eu
$ jarsigner -verify snapshot.zip
jar verified.
```
Launch Notary as a service:
```
$ java -jar notary.jar --keystore notary.p12 --storepass notary --alias notary
# from another console
$ wget 'http://127.0.0.1:8000/?url=http%3A%2F%2Ftest.noleaks.eu&device=Kindle&language=en' -O snapshot.zip
$ jarsigner -verify snapshot.zip
jar verified.
```

# Roadmap
1. Trusted timestamps
2. Human-readable Evidence Summary in
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
3. Deploy as a service
