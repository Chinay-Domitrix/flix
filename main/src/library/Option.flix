/*
 * Copyright 2019 Liam Palmer, Magnus Madsen
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
/// The Option type.
///
/// An option represents an optional value.
/// The constructor `None` represents an absent value,
/// whereas the constructor `Some(v)` represents the value `v`.
///
enum Option[t] with Eq, Order, ToString {
    case None,
    case Some(t)
}

instance Hash[Option[a]] with Hash[a] {
    pub def hash(o: Option[a]): Int = match o {
        case Some(y) => 919 * Hash.hash(y)
        case None => 1046527
    }
}

instance Functor[Option] {
    pub def map(f: a -> b & ef, o: Option[a]): Option[b] & ef = Option.map(f, o)
}

instance Applicative[Option] {
    pub def point(x: a) : Option[a] = Option.point(x)

    pub def ap(f: Option[a -> b & e], x: Option[a]) : Option[b] & e = Option.ap(f, x)

    // Same as the default implementation using `map` and `ap` but with less indirection.
    override pub def liftA2(f: t1 -> t2 -> r & e, x1: Option[t1], x2: Option[t2]): Option[r] & e = Option.lift2(f, x1, x2)
    override pub def liftA3(f: t1 -> t2 -> t3 -> r & e, x1: Option[t1], x2: Option[t2], x3: Option[t3]): Option[r] & e = Option.lift3(f, x1, x2, x3)
    override pub def liftA4(f: t1 -> t2 -> t3 -> t4 -> r & e, x1: Option[t1], x2: Option[t2], x3: Option[t3], x4: Option[t4]): Option[r] & e = Option.lift4(f, x1, x2, x3, x4)
    override pub def liftA5(f: t1 -> t2 -> t3 -> t4 -> t5 -> r & e, x1: Option[t1], x2: Option[t2], x3: Option[t3], x4: Option[t4], x5: Option[t5]): Option[r] & e = Option.lift5(f, x1, x2, x3, x4, x5)
}

instance Monad[Option] {
    pub def flatMap(f: a -> Option[b] & ef, x : Option[a]) : Option[b] & ef = Option.flatMap(f, x)
}

instance Foldable[Option] {
    pub def foldLeft(f: (b, a) -> b & ef, s: b, o: Option[a]): b & ef = Option.foldLeft(f, s, o)
    pub def foldRight(f: (a, b) -> b & ef, s: b, o: Option[a]): b & ef = Option.foldRight(f, o, s)
}

namespace Option {

    ///
    /// Returns `true` iff `o` is `None`.
    ///
    @Time(1) @Space(1)
    pub def isEmpty(o: Option[a]): Bool = match o {
        case None => true
        case Some(_) => false
    }

    ///
    /// Returns `v` if `o` is `Some(v).` Otherwise returns `d`.
    ///
    @Time(1) @Space(1)
    pub def getWithDefault(o: Option[a], d: a): a = match o {
        case None => d
        case Some(v) => v
    }

    ///
    /// Returns `o1` if it is `Some(v)`. Otherwise returns `o2`.
    ///
    @Time(1) @Space(1)
    pub def withDefault(o1: Option[a], o2: Option[a]): Option[a] = match o1 {
        case None => o2
        case Some(_) => o1
    }

    ///
    /// Returns `Some(y)` if `o` is `Some(x)`. Otherwise returns `o`.
    ///
    @Time(1) @Space(1)
    pub def replace(x: a, y: a, o: Option[a]): Option[a] with Eq[a] = match o {
        case None => o
        case Some(v) => Some(if (v == x) y else v)
    }

    ///
    /// Returns `true` if `o` is `Some(v)` and the predicate `f(v)` evaluates to `true`. Otherwise returns `false`.
    ///
    /// The function `f` must be pure.
    ///
    @Time(time(f)) @Space(space(f))
    pub def exists(f: a -> Bool, o: Option[a]): Bool = match o {
        case None    => false
        case Some(v) => f(v)
    }

    ///
    /// Returns `true` if `o` is `Some(v)` and the predicate `f(v)` evaluates to `true` or if `o` is `None`.
    ///
    /// Otherwise returns `false`.
    ///
    /// The function `f` must be pure.
    ///
    @Time(time(f)) @Space(space(f))
    pub def forall(f: a -> Bool, o: Option[a]): Bool = match o {
        case None    => true
        case Some(v) => f(v)
    }

    ///
    /// Returns `o` if `o` is `Some(v)` and the predicate `f(v)` is true. Otherwise returns `None`.
    ///
    /// The function `f` must be pure.
    ///
    @Time(time(f)) @Space(space(f))
    pub def filter(f: a -> Bool, o: Option[a]): Option[a] = match o {
        case None => None
        case Some(v) => if (f(v)) o else None
    }

    ///
    /// Returns `Some(f(v))` if `o` is `Some(v)`. Otherwise returns `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def map(f: a -> b & ef, o: Option[a]): Option[b] & ef = match o {
        case None => None
        case Some(v) => Some(f(v))
    }

    ///
    /// Returns `Some(x)`.
    ///
    pub def point(x: a) : Option[a] = Some(x)

    ///
    /// If both arguments are `Some`, return a `Some` containing the result of applying the function inside
    /// `f` to the value inside `x`. Otherwise return `None`.
    ///
    pub def ap(f: Option[a -> b & e], x: Option[a]) : Option[b] & e =
        match f {
            case None => None
            case Some(g) => match x {
                case None => None
                case Some(y) => Some(g(y))
            }
        }

    ///
    /// Returns `f(v)` if `o` is `Some(v)`. Otherwise returns `None`.
    ///
    pub def flatMap(f: a -> Option[b] & ef, o: Option[a]): Option[b] & ef = match o {
        case None => None
        case Some(v) => f(v)
    }

    ///
    /// Returns `Some(f(v1, v2))` if `o1` is `Some(v1)` and `o2` is `Some(v2)`. Otherwise returns `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def map2(f: (a, b) -> c & ef, o1: Option[a], o2: Option[b]): Option[c] & ef = match (o1, o2) {
        case (None, _) => None
        case (_, None) => None
        case (Some(v1), Some(v2)) => Some(f(v1, v2))
    }

    ///
    /// Returns `f(v1, v2)` if `o1` is `Some(v1)` and `o2` is `Some(v2)`. Otherwise returns `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def flatMap2(f: (a, b) -> Option[c] & ef, o1: Option[a], o2: Option[b]): Option[c] & ef = match (o1, o2) {
        case (None, _) => None
        case (_, None) => None
        case (Some(v1), Some(v2)) => f(v1, v2)
    }

    ///
    /// Returns `1` if `o` is `Some(v)` and the predicate `f(v)` evaluates to `true`. Otherwise returns `0`.
    ///
    /// The function `f` must be pure.
    ///
    @Time(time(f)) @Space(space(f))
    pub def count(f: a -> Bool, o: Option[a]): Int32 = match o {
        case None => 0
        case Some(v) => if (f(v)) 1 else 0
    }

    ///
    /// Returns `o` if `o` is `Some(v)` and the predicate `f(v)` evaluates to `true`. Otherwise returns `None`.
    ///
    /// The function `f` must be pure.
    ///
    @Time(time(f)) @Space(space(f))
    pub def find(f: a -> Bool, o: Option[a]): Option[a] = match o {
        case None => None
        case Some(v) => if (f(v)) o else None
    }

    ///
    /// Returns `v` if `o` is `Some(v)`. Otherwise returns `None`.
    ///
    @Time(1) @Space(1)
    pub def flatten(o: Option[Option[a]]): Option[a] = match o {
        case None => None
        case Some(v) => v
    }

    ///
    /// Returns the result of applying `combine` to all the elements in `o`, using `empty` as the initial value.
    ///
    pub def fold(o: Option[a]): a with Monoid[a] = Foldable.fold(o)

    ///
    /// Returns `f(z, v)` if `o` is `Some(v)`. Otherwise returns `z`.
    ///
    pub def foldLeft(f: (b, a) -> b & ef, z: b, o: Option[a]): b & ef = match o {
        case None => z
        case Some(v) => f(z, v)
    }

    ///
    /// Returns `f(v, z)` if `o` is `Some(v)`. Otherwise returns `z`.
    ///
    pub def foldRight(f: (a, b) -> b & ef, o: Option[a], z: b): b & ef = match o {
        case None => z
        case Some(v) => f(v, z)
    }

    ///
    /// Returns `Some(v1 :: v2 :: ... :: vn)` if each of `xs_i` is `Some(v_i)`. Otherwise returns `None`.
    ///
    @Time(List.length(xs)) @Space(List.length(xs))
    pub def sequence(xs: List[Option[a]]): Option[List[a]] =
        sequenceHelper(xs, _ -> None, ys -> Some(ys))

    ///
    /// Helper function for `sequence`.
    ///
    def sequenceHelper(xs: List[Option[a]], fk: Unit -> Option[List[a]], sk: List[a] -> Option[List[a]]): Option[List[a]] = match xs {
        case Nil            => sk(Nil)
        case None :: _      => fk()
        case Some(y) :: rs  => sequenceHelper(rs, fk, ys -> sk(y :: ys))
    }

    ///
    /// Returns `Some(v1 :: v2 :: ... v :: vn)` if each of `f(xs_i)` is `Some(v_i)`. Otherwise returns `None`.
    ///
    pub def traverse(f: a -> Option[b] & ef, xs: List[a]): Option[List[b]] & ef =
        traverseHelper(f, xs, _ -> None, ys -> Some(ys))

    ///
    /// Helper function for `traverse`.
    ///
    def traverseHelper(f: a -> Option[b] & f, xs: List[a], fk: Unit -> Option[List[b]], sk: List[b] -> Option[List[b]]): Option[List[b]] & f = match xs {
        case Nil            => sk(Nil)
        case x :: rs        => match f(x) {
            case None    => fk()
            case Some(y) => traverseHelper(f, rs, fk, ys -> sk(y :: ys))
        }
    }

    ///
    /// Returns `Some()` if each of `f(xs_i)` is `Some(_)`. Otherwise returns `None`.
    ///
    /// This function is the "forgetful" version of `traverse`, use it when the you want the effect
    /// of applying `f` to each element but do not care about collecting the results.
    ///
    pub def traverseX(f: a -> Option[b] & ef, xs: List[a]): Option[Unit] & ef = match xs {
        case Nil            => Some()
        case x :: rs        => match f(x) {
            case None    => None
            case Some(_) => traverseX(f, rs)
        }
    }

    ///
    /// Returns the result of applying `f` to a start value `s` and the elements in `xs`
    /// going from left to right.
    ///
    /// If at any step applying `f` fails (i.e. it produces a `None` value) the traversal
    /// of `xs` is short-circuited and `None` is returned.
    ///
    /// If `f` is successfully applied to all the elements in `xs` the result is of the form:
    /// `Some(f(...f(f(s, x1), x2)..., xn))`.
    ///
    pub def foldLeftM(f: (b, a) -> Option[b] & f, s: b, xs: List[a]): Option[b] & f = match xs {
        case Nil => Some(s)
        case x :: rs => match f(s, x) {
            case Some(s1) => foldLeftM(f, s1, rs)
            case None => None
        }
    }

    ///
    /// Returns the result of applying `f` to a start value `s` and the elements in `xs`
    /// going from right to left.
    ///
    /// If at any step applying `f` fails (i.e. it produces a `None` value) the traversal
    /// of `xs` is short-circuited and `None` is returned.
    ///
    /// If `f` is successfully applied to al elements in `xs` the result is of the form:
    /// `Some(f(x1, ...f(xn-1, f(xn, s))...))`.
    ///
    pub def foldRightM(f: (a, b) -> Option[b] & f, s: b, xs: List[a]): Option[b] & f =
        foldRightMHelper(f, s, xs, _ -> None as & f, s1 -> Some(s1) as & f)

    ///
    /// Helper function for `foldRightM`.
    ///
    def foldRightMHelper(f: (a, b) -> Option[b] & f, s: b, xs: List[a], fk: Unit -> Option[b] & f, sk: b -> Option[b] & f): Option[b] & f = match xs {
        case Nil => sk(s)
        case x :: rs =>
            foldRightMHelper(f, s, rs, fk, s1 -> match f(x, s1) {
                    case Some(s2) => sk(s2)
                    case None => fk()
                })
    }

    ///
    /// Returns a one-element list of the value `v` if `o` is `Some(v)`. Otherwise returns the empty list.
    ///
    @Time(1) @Space(1)
    pub def toList(o: Option[a]): List[a] = match o {
        case None => Nil
        case Some(v) => v :: Nil
    }

    ///
    /// Returns a one-element set of the value `v` if `o` is `Some(v)`. Otherwise returns the empty set.
    ///
    @Time(1) @Space(1)
    pub def toSet(o: Option[a]): Set[a] with Order[a] = match o {
        case None       => Set.empty()
        case Some(v)    => Set.singleton(v)
    }

    ///
    /// Returns a singleton map with the mapping `k -> v` if `o` is `Some((k, v))`. Otherwise returns the empty map.
    ///
    @Time(1) @Space(1)
    pub def toMap(o: Option[(k, v)]): Map[k, v] with Order[k] = match o {
        case None           => Map.empty()
        case Some((k, v))   => Map.singleton(k, v)
    }

    ///
    /// Returns the Option value `Ok(v)` if `o` is `Some(v)`. Otherwise returns `Err(e)`.
    ///
    @Time(1) @Space(1)
    pub def toOk(o: Option[t], e: e): Result[t, e] = match o {
        case None       => Err(e)
        case Some(a)    => Ok(a)
    }

    ///
    /// Returns the Option value `Err(e)` if `o` is `Some(e)`. Otherwise returns `Ok(d)`.
    ///
    @Time(1) @Space(1)
    pub def toErr(o: Option[e], d: t): Result[t, e] = match o {
        case None       => Ok(d)
        case Some(e)    => Err(e)
    }

    ///
    /// Returns the Validation value `Success(v)` if `o` is `Some(v)`. Otherwise lifts `e` into Validation's `Failure`.
    ///
    @Time(1) @Space(1)
    pub def toSuccess(o: Option[t], e: e): Validation[t, e] = match o {
        case None       => Failure(Nel(e, Nil))
        case Some(a)    => Success(a)
    }

    ///
    /// Returns `e` into Validation's `Failure` if `o` is `Some(e)`. Otherwise returns `Success(d)`.
    ///
    @Time(1) @Space(1)
    pub def toFailure(o: Option[e], d: t): Validation[t, e] = match o {
        case None       => Success(d)
        case Some(e)    => Failure(Nel(e, Nil))
    }

    ///
    /// Returns `Some((v1, v2))` if `o1` is `Some(v1)` and `o2` is `Some(v2)`. Otherwise returns `None`.
    ///
    @Time(1) @Space(1)
    pub def zip(o1: Option[a], o2: Option[b]): Option[(a, b)] = match (o1, o2) {
        case (None, _) => None
        case (_, None) => None
        case (Some(v1), Some(v2)) => Some((v1, v2))
    }

    ///
    /// Returns `(Some(v1), Some(v2))` if `o` is `Some((v1, v2))`. Otherwise returns `(None, None)`.
    ///
    @Time(1) @Space(1)
    pub def unzip(o: Option[(a, b)]): (Option[a], Option[b]) = match o {
        case None => (None, None)
        case Some((v1, v2)) => (Some(v1), Some(v2))
    }

    ///
    /// Applies `f` to `v` if `o` is `Some(v)`. Otherwise does nothing.
    ///
    @Time(time(f)) @Space(space(f))
    pub def foreach(f: a -> Unit & ef, o: Option[a]): Unit & ef = match o {
        case None    => ()
        case Some(v) => f(v)
    }

    ///
    /// Applies the binary function `f` to the values in `o1` and `o2`.
    ///
    /// Returns `None` if either `o1` or `o2` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift2(f: (t1, t2) -> u & f, o1: Option[t1], o2: Option[t2]): Option[u] & f =
        ap(map(f, o1), o2)

    ///
    /// Applies the ternary function `f` to the values in `o1`, `o2` and `o3`.
    ///
    /// Returns `None` if any of `o1`, `o2` and `o3` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift3(f: (t1, t2, t3) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3]): Option[u] & f =
        ap(lift2(f, o1, o2), o3)

    ///
    /// Applies the 4-ary function `f` to the values in `o1`, `o2`, `o3` and `o4`.
    ///
    /// Returns `None` if any of `o1`, `o2`, `o3` and `o4` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift4(f: (t1, t2, t3, t4) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4]): Option[u] & f =
        ap(lift3(f, o1, o2, o3), o4)

    ///
    /// Applies the 5-ary function `f` to the values in `o1`, `o2`, ... `o5`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o5` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift5(f: (t1, t2, t3, t4, t5) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5]): Option[u] & f =
        ap(lift4(f, o1, o2, o3, o4), o5)

    ///
    /// Applies the 6-ary function `f` to the values in `o1`, `o2`, ... `o6`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o6` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift6(f: (t1, t2, t3, t4, t5, t6) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5], o6: Option[t6]): Option[u] & f =
        ap(lift5(f, o1, o2, o3, o4, o5), o6)

    ///
    /// Applies the 7-ary function `f` to the values in `o1`, `o2`, ... `o7`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o7` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift7(f: (t1, t2, t3, t4, t5, t6, t7) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5], o6: Option[t6], o7: Option[t7]): Option[u] & f=
        ap(lift6(f, o1, o2, o3, o4, o5, o6), o7)

    ///
    /// Applies the 8-ary function `f` to the values in `o1`, `o2`, ... `o8`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o8` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift8(f: (t1, t2, t3, t4, t5, t6, t7, t8) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5], o6: Option[t6], o7: Option[t7], o8: Option[t8]): Option[u] & f =
        ap(lift7(f, o1, o2, o3, o4, o5, o6, o7), o8)

    ///
    /// Applies the 9-ary function `f` to the values in `o1`, `o2`, ... `o9`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o9` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift9(f: (t1, t2, t3, t4, t5, t6, t7, t8, t9) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5], o6: Option[t6], o7: Option[t7], o8: Option[t8], o9: Option[t9]): Option[u] & f =
        ap(lift8(f, o1, o2, o3, o4, o5, o6, o7, o8), o9)

    ///
    /// Applies the 10-ary function `f` to the values in `o1`, `o2`, ... `o10`.
    ///
    /// Returns `None` if any of `o1`, `o2`, ... `o10` are `None`.
    ///
    @Time(time(f)) @Space(space(f))
    pub def lift10(f: (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) -> u & f, o1: Option[t1], o2: Option[t2], o3: Option[t3], o4: Option[t4], o5: Option[t5], o6: Option[t6], o7: Option[t7], o8: Option[t8], o9: Option[t9], o10: Option[t10]): Option[u] & f =
        ap(lift9(f, o1, o2, o3, o4, o5, o6, o7, o8, o9), o10)

    ///
    /// Returns an iterator over `o` with 1 element or an empty iterator if `o` is `None`.
    ///
    pub def toIterator(o: Option[a]): Iterator[a] & Impure = match o {
        case None    => Iterator.empty()
        case Some(x) => Iterator.singleton(x)
    }
}
