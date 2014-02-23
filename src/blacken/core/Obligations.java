/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class Obligations {
    private static final Logger LOGGER = LoggerFactory.getLogger(Obligations.class);
    private static String getResourceContents(String name) {
        // LOGGER.debug("Found resource {}", Obligations.class.getResource(name));
        InputStream stream = Obligations.class.getResourceAsStream(name);
        byte[] bytebuf = new byte[4096];
        int cnt;
        StringBuilder buf = new StringBuilder();
        try {
            while ((cnt = stream.read(bytebuf)) != -1) {
                buf.append(new String(bytebuf, 0, cnt, Charset.forName("UTF-8")));
            }
        } catch (NullPointerException | IOException ex) {
            buf.append("Failed to load ");
            buf.append(name);
            buf.append(" file\n");
        }
        return buf.toString();
    }
    public static String getBlackenLicense() {
        return getResourceContents("LICENSE.txt");
    }
    public static String getBlackenNotice() {
        return getResourceContents("NOTICE.txt");
    }
    public static String getFontName() {
        return "DejaVu";
    }
    public static String getFontLicense() {
        return getResourceContents("/fonts/LICENSE-dejavu.txt");
    }
}
