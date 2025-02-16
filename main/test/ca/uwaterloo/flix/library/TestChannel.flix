/*
 * Copyright 2019 Magnus Madsen
 * Copyright 2021 Jonathan Lindegaard Starup
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

@test
def testNew01(): Channel.Mpmc & Impure = Channel.new(0)

@test
def testNew02(): Channel.Mpmc & Impure = Channel.new(32)

@test
def testGetPut01(): Bool & Impure =
    let c = Channel.new(1);
    Channel.put(BoxedBool(true), c);
    match Channel.get(c) {
        case BoxedBool(b) => b
        case _ => false
    }

@test
def testGetPut02(): Bool & Impure =
    let c = Channel.new(1);
    Channel.put(BoxedInt32(123), c);
    match Channel.get(c) {
        case BoxedInt32(i) => i == 123
        case _ => bug!("Impossible")
    }

@test
def testGetPut03(): Bool & Impure =
    let c = Channel.new(1);
    Channel.put(BoxedValue("Hello World!" as ##java.lang.Object), c);
    match Channel.get(c) {
        case BoxedValue(s) => (s as String) == "Hello World!"
        case _ => bug!("Impossible")
    }

@test
def testSelect01(): Bool & Impure =
    let c = Channel.new(1);
    Channel.put(BoxedInt32(2), c);
    match Channel.select([c], false) {
        case Some(0, BoxedInt32(i)) => i == 2
        case _ => bug!("impossible")
    }

@test
def testSelect02(): Bool & Impure =
    let c = Channel.new(1);
    match Channel.select([c], true) {
        case Some(0, BoxedInt32(_)) => bug!("The channel should be empty")
        case None => true
        case _ => bug!("impossible")
    }

@test
def testSelect03(): Bool & Impure =
    let cString = Channel.new(10);
    let cInt = Channel.new(0);

    Channel.put(BoxedValue("hey" as ##java.lang.Object), cString);

    match Channel.select([cInt, cString], false) {
        case Some(0, BoxedInt32(_)) => bug!("The channel should be empty")
        case Some(1, BoxedValue(o)) => let s = o as String; s == "hey"
        case _ => bug!("impossible")
    }
