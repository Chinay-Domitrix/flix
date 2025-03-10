/*
 * Copyright 2021 Nina Andrup Pedersen
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

namespace TestFile {

    use File.{exists, isDirectory, isRegularFile, isReadable, isSymbolicLink,
        isWritable, isExecutable, readLines, readLinesWith, read, readWith,
        readLinesIter, readLinesIterWith};

    /////////////////////////////////////////////////////////////////////////////
    // exists                                                                  //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testExists01(): Bool & Impure =
        exists("./README.md") == Ok(true)

    @test
    def testExists02(): Bool & Impure =
        exists("./fakeREADME.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // isDirectory                                                             //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsDirectory01(): Bool & Impure =
        isDirectory("./main/test/ca/uwaterloo/flix/library") == Ok(true)

    @test
    def testIsDirectory02(): Bool & Impure =
        isDirectory("./README.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // isRegularFile                                                           //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsRegularFile01(): Bool & Impure =
        isRegularFile("./main/test/ca/uwaterloo/flix/library") == Ok(false)

    @test
    def testIsRegularFile02(): Bool & Impure =
        isRegularFile("./README.md") == Ok(true)

    /////////////////////////////////////////////////////////////////////////////
    // isReadable                                                              //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsReadable01(): Bool & Impure =
        isReadable("./README.md") == Ok(true)

    @test
    def testIsReadable02(): Bool & Impure =
        isReadable("./fakeREADME.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // isSymbolicLink                                                          //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsSymbolicLink(): Bool & Impure =
        isSymbolicLink("./README.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // isWritable                                                              //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsWritable01(): Bool & Impure =
        isWritable("./README.md") == Ok(true)

    @test
    def testIsWritable02(): Bool & Impure =
        isWritable("./fakeREADME.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // isExecutable                                                            //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testIsExecutable(): Bool & Impure =
        isExecutable("./fakeREADME.md") == Ok(false)

    /////////////////////////////////////////////////////////////////////////////
    // readLines                                                               //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testReadLines01(): Bool & Impure =
        Result.isOk(readLines("./LICENSE.md"))

    /////////////////////////////////////////////////////////////////////////////
    // readLinesWith                                                           //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testReadLinesWith01(): Bool & Impure =
        Result.isOk(readLinesWith({charSet = "UTF8"}, "./LICENSE.md"))

    /////////////////////////////////////////////////////////////////////////////
    // read                                                                    //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testRead01(): Bool & Impure =
        Result.isOk(read("./LICENSE.md"))

    /////////////////////////////////////////////////////////////////////////////
    // readWith                                                                //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testReadWith01(): Bool & Impure =
        Result.isOk(readWith({offset = 2i64, count = 2, charSet = "UTF8"}, "./LICENSE.md"))

    @test
    def testReadWith02(): Bool & Impure =
        readWith({offset = 2i64, count = 2, charSet = "US-ASCII"}, "./LICENSE.md") == Ok("ac")

    /////////////////////////////////////////////////////////////////////////////
    // readLinesIter                                                           //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testReadLinesIter01(): Bool & Impure =
        match readLinesIter("./LICENSE.md") {
            case Iterator(_, next) => Result.isOk(next())
        }

    @test
    def testReadLinesIter02(): Bool & Impure =
        match readLinesIter("./LICENSE.md") {
            case Iterator(done, _) => done() == false
        }

    /////////////////////////////////////////////////////////////////////////////
    // readLinesIterWith                                                       //
    /////////////////////////////////////////////////////////////////////////////

    @test
    def testReadLinesIterWithWith01(): Bool & Impure =
        match readLinesIterWith({charSet = "utf8"}, "./LICENSE.md") {
            case Iterator(_, next) => Result.isOk(next())
        }

}
