/*
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

///
/// A wrapper around a Java ReentrantLock.
///
opaque type ReentrantLock = ##java.util.concurrent.locks.ReentrantLock

namespace ReentrantLock {

    ///
    /// Creates an instance of ReentrantLock with the given fairness policy.
    /// `fair = False` is the default for Java.
    ///
    pub def new(fair: Bool): ReentrantLock & Impure =
        import new java.util.concurrent.locks.ReentrantLock(Bool) as reentrantLock;
        ReentrantLock(reentrantLock(fair))

    ///
    /// Returns true if this lock has fairness set true.
    ///
    pub def isFair(lock: ReentrantLock): Bool & Impure =
        import java.util.concurrent.locks.ReentrantLock.isFair();
        let ReentrantLock(l) = lock;
        isFair(l)

    ///
    /// Acquires the lock.
    ///
    pub def lock(lock: ReentrantLock): Unit & Impure =
        import java.util.concurrent.locks.ReentrantLock.lock() as lockIt;
        let ReentrantLock(l) = lock;
        lockIt(l)

    ///
    /// Acquires the lock unless the current thread is interrupted.
    ///
    pub def lockInterruptibly(lock: ReentrantLock): Result[Unit, ##java.lang.InterruptedException] & Impure =
        import java.util.concurrent.locks.ReentrantLock.lockInterruptibly();
        let ReentrantLock(l) = lock;
        try {
            Ok(lockInterruptibly(l))
        } catch {
            case e: ##java.lang.InterruptedException => Err(e)
        }

    ///
    /// Returns a Condition instance for use with this Lock instance.
    ///
    pub def newCondition(lock: ReentrantLock): Condition & Impure =
        import java.util.concurrent.locks.ReentrantLock.newCondition();
        let ReentrantLock(l) = lock;
        Condition(newCondition(l))

    ///
    /// Acquires the lock only if it is not held by another thread at the time of invocation.
    ///
    pub def tryLock(lock: ReentrantLock): Bool & Impure =
        import java.util.concurrent.locks.ReentrantLock.tryLock();
        let ReentrantLock(l) = lock;
        tryLock(l)

    ///
    /// Acquires the lock if it is not held by another thread within the given waiting time (nanos) and the current thread has not been interrupted.
    ///
    pub def tryLockNanos(lock: ReentrantLock, nanosTimeout: Int64): Result[Bool, ##java.lang.InterruptedException] & Impure =
        import java.util.concurrent.locks.ReentrantLock.tryLock(Int64, ##java.util.concurrent.TimeUnit);
        import get java.util.concurrent.TimeUnit:NANOSECONDS as nano;
        let ReentrantLock(l) = lock;
        try {
            Ok(tryLock(l, nanosTimeout, nano()))
        } catch {
            case e: ##java.lang.InterruptedException => Err(e)
        }

    ///
    /// Attempts to release this lock.
    ///
    pub def unlock(lock: ReentrantLock): Result[Unit, ##java.lang.IllegalMonitorStateException] & Impure =
        import java.util.concurrent.locks.ReentrantLock.unlock();
        let ReentrantLock(l) = lock;
        try {
            Ok(unlock(l))
        } catch {
            case e: ##java.lang.IllegalMonitorStateException => Err(e)
        }
}
