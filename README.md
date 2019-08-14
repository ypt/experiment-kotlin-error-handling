# Why?
TODO: flesh this section out. For now, [read this](https://phauer.com/2019/sealed-classes-exceptions-kotlin/).

# What?
TODO: flesh this section out. For now, see `main.kt` for example usage as well as implementation.

# Why not X, Y, Z?
Q: Why not use the Kotlin Result type?

A: For now, the Kotlin Result type [is not allowed as a direct result type of Kotlin functions](https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/result.md#limitations).

Q: Why not use [kittinunf.result](https://github.com/kittinunf/Result)?

A: The kittinunf.result library great, and was definitely an inspiration for this implementation.
   One main difference is that kittinunf.result seeks to wrap _Exception_ types inside the Failure,
   while this seeks to allow _any user defined type_ inside wrapped inside the Failure type. This
   allows us to use a sealed class to enumerate a finite set of failure scenarios, which can then
   be more precisely used for local handling of the failures. This is especially powerful when
   combined with a `when` assignment, which enables the compiler to check that we are accounting
   for all the scenarios exhaustively

Q: Why not use [Arrow](https://arrow-kt.io/)?

A: If you need something more fully featured, you should definitely consider Arrow! Arrow's 
   [Either](https://arrow-kt.io/docs/arrow/core/either/) data type is essentially the same concept 
   as the `Outcome` type demoed in this repo - though Arrow is much more fully featured, and using 
   more idiomatic functional programming terminology: 

   - `Either`:`Outcome`
   - `Left`:`Failure`
   - `Right`:`Success`

# Demo
Kotlin needs to be installed
```
$ brew update
$ brew install kotlin
```

Build & run
```
$ kotlinc main.kt -include-runtime -d program.jar && java -jar program.jar
```

# Inspiration
- https://phauer.com/2019/sealed-classes-exceptions-kotlin/
- https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/result.md#error-handling-style-and-exceptions
- https://github.com/kittinunf/Result
- https://fsharpforfunandprofit.com/posts/recipe-part2/

# TODO
- Set this up as a proper library