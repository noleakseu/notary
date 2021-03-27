package notary;

/**
 * https://raw.githubusercontent.com/puppeteer/puppeteer/main/src/common/DeviceDescriptors.ts
 */
class Device {
    /**
     * Copyright 2017 Google Inc. All rights reserved.
     * <p>
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * <p>
     * http://www.apache.org/licenses/LICENSE-2.0
     * <p>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    public enum Type {
        iPhone(
                "iPhone 8",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
                375,
                667,
                2,
                true,
                true,
                false
        ),
        iPadPro(
                "iPad Pro landscape",
                "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",
                1366,
                1024,
                2,
                true,
                true,
                true
        ),
        Nexus(
                "Nexus 10",
                "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",
                800,
                1280,
                2,
                true,
                true,
                false
        ),
        Kindle(
                "Kindle Fire HDX landscape",
                "Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",
                1280,
                800,
                2,
                true,
                true,
                true
        );

        private final String name;
        private final String userAgent;
        private final int width;
        private final int height;
        private final int scaleFactor;
        private final boolean isMobile;
        private final boolean hasTouch;

        Type(String name, String userAgent, int width, int height, int scaleFactor, boolean isMobile, boolean hasTouch, boolean isLandscape) {
            this.name = name;
            this.userAgent = userAgent;
            this.width = width;
            this.height = height;
            this.scaleFactor = scaleFactor;
            this.isMobile = isMobile;
            this.hasTouch = hasTouch;
        }

        public String getName() {
            return this.name;
        }

        public String getUserAgent() {
            return this.userAgent;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public int getScaleFactor() {
            return this.scaleFactor;
        }

        public boolean isMobile() {
            return this.isMobile;
        }

        public boolean hasTouch() {
            return this.hasTouch;
        }
    }
}