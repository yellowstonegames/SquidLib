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

package squidpony.squidmath;

/**
 * An empty marker interface to indicate that an implementor has known or intentional issues with a key property of its
 * functionality. This is almost always combined with another interface, as in {@link FlawedRandomness}, which uses this
 * to indicate that implementations are not as "fair" as other {@link RandomnessSource} implementations, and usually
 * have severe statistical defects. Typically, you would use a flawed implementation to compare with a non-flawed one,
 * or because the flaws have aesthetic merit from their statistical biases.
 * <br>
 * Created by Tommy Ettinger on 4/14/2020.
 */
public interface IFlawed {
}
