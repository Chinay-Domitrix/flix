# Style Guide

## Flix and Scala

- Every file must start with a copyright header.
- Prefer functional to imperative programming.
  - Use of local mutability is okay.
- If a function or method can be private, make it private.
- Don't use fancy features unless necessary and reasonable.

## Flix-specific

- Variable names are typical one letter; `o` for Option, `l` for `List`.
- Type variable names are typically `a`, `b`, `c`.
- Effect variables are called `ef` or `ef1`, `ef2`...
- Type class instances declarations should appear just below a type declaration.
  - Instances should appear in the order: Eq, Order, ToString.
- Avoids casts.
  - If necessary, effect casts are OK.
  - Only in extreme cases are type casts OK.
- Indentation is 4 spaces.
- Pattern matches should align `=>`.
- Avoid unnecessary lambdas. 
  - e.g. prefer `List.map(String.toLowerCase)` over `List.map(s -> String.toLowerCase(s)`.
- Prefer string interpolation to constructing strings with concatenation.
- Keep the simplest cases in a pattern matches first.
  - e.g. keep the base case(s) before the inductive case(s).
- Doc comments should use triple slashes ///.

## Scala-specific

- No shadowed variables.
- No unused local variables.
- Common methods are `visitExp`, `visitExps`, `visitPat`, etc.
- When using monads like `Validation` use `mapN` over `for ... yield` whenever possible
- Write single-variable `mapN` cases open to additional variables with `mapN ... { case ... => ... }`
- Prefer to name expressions just `exp1`, `exp2`, `exp3`.
  - Names such as `beginExp` etc. quickly get outdated.
- Never use toString for anything other than debugging.
- Leave the code in better state than you found it in.
- Avoid inheritance. Prefer algebraic data types and functions on them.
- Think towards self-hosting: Don't use features that cannot easily be ported to Flix.

## JVM Bytecode Generation Policy

- Prefer public fields over private fields with getter/setter.
- Prefer direct field initialization over construction arguments.
- Ensure classes are final.

If a PR discovers a new style principle, feel free to add it to this file as part of the same PR.
