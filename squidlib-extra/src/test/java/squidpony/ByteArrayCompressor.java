/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidmath.BlueNoise;

public class ByteArrayCompressor extends ApplicationAdapter {
    @Override
    public void create() {
        StringBuilder sb = new StringBuilder(250000);
        sb.append("public static final byte[][] ALT_NOISE = new byte[][]{\n");
        for (int i = 0; i < 64; i++) {
            byte[] noise = BlueNoise.ALT_NOISE[i];
            sb.append("decompress(").append('"').append(ByteStringEncoding.compress(noise).replaceAll("([\"\\\\])", "\\\\$1")).append('"').append("),\n");
        }
        sb.append("};");
        String s = sb.toString();

        System.out.println(s);
        Gdx.files.local("BlueNoiseData.txt").writeString(s, false);

        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ByteArrayCompressor(), config);
    }

}
