/*
 * Copyright (c) 2022  Eben Howard, Tommy Ettinger, and contributors
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
 *
 *
 */

package squidpony.squidmath;

/**
 * Simply groups the two interfaces {@link IRNG} and {@link StatefulRandomness} so some implementations of IRNG can have
 * their states read from and written to.
 * <br>
 * Created by Tommy Ettinger on 11/25/2018.
 */
public interface IStatefulRNG extends IRNG, StatefulRandomness {
}
