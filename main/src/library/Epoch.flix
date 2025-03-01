/*
 * Copyright 2021 Jakob Schneider Villumsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

namespace Epoch {

    ///
    /// Returns the number of nano seconds since the Unix epoch.
    ///
    pub def nanoseconds(): Int64 & Impure =
        import java.lang.System:nanoTime();
        nanoTime()

    ///
    /// Returns the number of micro seconds since the Unix epoch.
    ///
    pub def microseconds(): Int64 & Impure = nanoseconds() / 1000i64

    ///
    /// Returns the number of milli seconds since the Unix epoch.
    ///
    pub def milliseconds(): Int64 & Impure = microseconds() / 1000i64

    ///
    /// Returns the number of seconds since the Unix epoch.
    ///
    pub def seconds(): Int64 & Impure = milliseconds() / 1000i64

}