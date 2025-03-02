///
/// Ref[t] is a type alias for a scoped ref with a global lifetime.
///
type alias Ref[t] = ScopedRef[t, false]

///
/// The Reified Flix Bools.
///
pub enum ReifiedBool with Eq, Order, ToString {
    case ErasedBool,
    case ReifiedTrue,
    case ReifiedFalse
}

///
/// The Reified Flix Types.
///
pub enum ReifiedType with Eq, Order, ToString {
    case ErasedType,
    case ReifiedBool,
    case ReifiedChar,
    case ReifiedFloat32,
    case ReifiedFloat64,
    case ReifiedInt8,
    case ReifiedInt16,
    case ReifiedInt32,
    case ReifiedInt64,
    case ReifiedArray(ReifiedType)
}

///
/// The identity function.
///
pub def identity(x: a): a = x

///
/// Returns the constant function with return value `y`.
/// That is, returns `f` such that `f(x) = y` for all inputs `x`.
///
pub def constant(y: a): b -> a = _ -> y

///
/// Returns the function `f` with input arguments swapped.
/// That is, given the function `f: (a, b) -> c`, returns a function `(b, a) -> c`
///
pub def flip(f: (a, b) -> c & ef): (b, a) -> c & ef = (x, y) -> f(y, x)

///
/// `on(f, g, x, y) = f(g(x), g(y))`
///
/// Partially applying this function in the form ``f `on` g`` lifts the binary function `f` to work on
/// inputs transformed by `g`.
///
pub def on(f: (b, b) -> c & ef1, g: a -> b & ef2, x: a, y: a): c & (ef1 and ef2) = f(g(x), g(y))

///
/// Returns the result of currying the function `f`.
/// That is, given the function `f: (a, b) -> c`, returns a function `a -> b -> c`
///
pub def curry(f: (a, b) -> c & ef): a -> (b -> c & ef) = x -> y -> f(x, y)

///
/// Returns the result of uncurrying the function `f`.
/// That is, given the function `f: a -> b -> c`, returns a function `(a, b) -> c`
///
pub def uncurry(f: a -> (b -> c & ef1) & ef2): ((a, b)) -> c & (ef1 and ef2) = match (x, y) -> f(x, y)

///
/// Returns the first component of `t`.
///
pub def fst(p: (a, b)): a =
    let (x, _) = p;
        x

///
/// Returns the second component of `t`.
///
pub def snd(p: (a, b)): b =
    let (_, y) = p;
        y

///
/// Returns the pair `p` with the components swapped.
/// That is, returns `(y, x)` if `p = (x, y)`.
///
pub def swap(p: (a, b)): (b, a) =
    let (x, y) = p;
        (y, x)

///
/// Forwards function composition. Applies the function on the left first.
///
/// Given the functions `f: a -> b` and `g: b -> c` returns a function `a -> c`
///
pub def >>(f: a -> b & ef1, g: b -> c & ef2): a -> c & (ef1 and ef2) = x -> g(f(x))

///
/// Pipes the given value `x` into the function `f`.
///
/// Given a value `x` and a function `f` returns `f(x)`.
///
pub def |>(x: a, f: a -> b & ef): b & ef = f(x)

///
/// Pipes the given pair `x` into the function `f`.
///
/// Given a pair `x` and a function `f` returns `f(x)`.
///
pub def ||>(x: (a, b), f: ((a, b)) -> c & ef): c & ef = f(x)

///
/// Converts `x` to a string and prints it to standard out.
///
pub def print(x: a): Unit & Impure with ToString[a] =
    import get java.lang.System:out as getOut;
    import java.io.PrintStream.print(String);
    x |> ToString.toString |> print(getOut())

///
/// Converts `x` to a string and prints it to standard out followed by a new line.
///
pub def println(x: a): Unit & Impure with ToString[a] =
    import get java.lang.System:out as getOut;
    import java.io.PrintStream.println(String);
    x |> ToString.toString |> println(getOut())

///
/// Returns `true` if `x` implies `y` logically holds.
///
pub def ==>(x: Bool, y: Bool): Bool = not x or y

///
/// Returns true if `x` implies `y` and vise versa.
///
pub def <==>(x: Bool, y: Bool): Bool = x == y

///
/// Crashes the current process with the given message `m`.
///
pub def bug!(m: String): a =
    let f = () -> {
        import get java.lang.System:err as getErr;
        import java.io.PrintStream.println(String);
        import java.lang.String.repeat(Int32);
        let prt = println(getErr());
        prt(repeat("*", 80));
        prt("**") ;
        prt("**  BUG: ${m}") ;
        prt("**") ;
        prt(repeat("*", 80));
        prt("")
    };
    let _ = f() as & Pure;
    ?panic

///
/// Asserts that this expression is unreachable. Crashes if not.
///
pub def unreachable!(): a = bug!("Reached unreachable expression.")

///
/// Meta-programming facility to express the time complexity of a function `f`.
///
pub def time(_: a -> Int32): Int32 = 1

///
/// Meta-programming facility to express the space complexity of a function `f`.
///
pub def space(_: a -> Int32): Int32 = 1
