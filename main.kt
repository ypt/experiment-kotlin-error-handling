fun main(args: Array<String>) {
    // ========================================
    // Handling operations results using `when`
    // ========================================

    // Here's an example of a Success Outcome
    //
    // When using a `when` assignment, all possible cases need to be accounted for.
    // Otherwise we get a compiler warning like the below:
    //
    //  error: 'when' expression must be exhaustive, add necessary 'is Error' branch or 'else' branch instead
    val result = when (operation("SUCCESS", true)) {
        is Outcome.Success -> "SUCCESS!"
        is Outcome.Failure -> "ERROR!" // Try commenting out this line
    }
    println("RESULT 1: $result")

    val result2 = when (operation("ERROR", false)) {
        is Outcome.Success -> "SUCCESS!"
        is Outcome.Failure -> "ERROR!"
    }
    println("RESULT 2: $result2")

    // And if we want to examine the Outcome in a finer grained way, we can do that, too
    //
    // When using a `when` assignment, all possible error cases need to accounted for here too.
    // Otherwise we get a compiler warning like the below:
    //
    //  error: 'when' expression must be exhaustive, add necessary 'is ErrorB' branch or 'else' branch instead
    val result3 = when (val outcome = operation("ERROR", false)) {
        is Outcome.Success -> "SUCCESS!"
        is Outcome.Failure -> when (outcome.cause) {
            is MyOperationErrors.ErrorA -> "ERROR A"
            is MyOperationErrors.ErrorB -> "ERROR B" // Try commenting out this line
        }
    }
    println("RESULT 3: $result3")

    // ================================
    // Chaining Outcome transformations
    // ================================
    val outcome = operation("SUCCESS", true)

    // `map` will only operate on a `Success` Outcome, it is skipped if it is handed an `Error` Outcome
    val mappedOutcome = outcome
            .map { "MAP SUCCESS OUTCOME ($it)" }
            .map { "MAP SUCCESS OUTCOME AGAIN ($it)" }
    println("MAPPED OUTCOME 1: $mappedOutcome")

    val outcome2 = operation("THIS OPERATION FAILED!", false)
    val mappedOutcome2 = outcome2
            .map { "MAP SUCCESS OUTCOME ($it)" }
            .map { "MAP SUCCESS OUTCOME AGAIN ($it)" }
    println("MAPPED OUTCOME 2: $mappedOutcome2")

    // `flatMap` can be used if the transformation operation itself returns an `Outcome`
    // Here, we execute another operation based on the previous results
    val mappedOutcome3 = mappedOutcome.flatMap { operation2(it, true) }
    println("MAPPED OUTCOME 3: $mappedOutcome3")

    // `mapError` will only operate on an `Error` Outcome, it is skipped if it is handed a `Success` Outcome
    val mappedOutcome4 = mappedOutcome2.mapError { MyOperationErrors.ErrorB("MAP ERROR SHOULD NOT HAPPEN BECAUSE OUTCOME IS SUCCESS ($it)") }
    println("MAPPED OUTCOME 4: $mappedOutcome4")

    // Note, all of the above transformations can be chained together
    // The below is equivalent
    val mappedOutcome5 = outcome
            .map { "MAP SUCCESS OUTCOME ($it)" }
            .map { "MAP SUCCESS OUTCOME AGAIN ($it)" }
            .flatMap { operation2(it, true) }
            .mapError { MyOperationErrors.ErrorB("MAP ERROR SHOULD NOT HAPPEN BECAUSE OUTCOME IS SUCCESS ($it)") }
    println("MAPPED OUTCOME 5: $mappedOutcome5")

    // Once we've finished with our chain of Outcome transformations, we can
    // extract the actual value contained inside the Outcome
    //
    // Similar to the other times we use a `when` assignment, the cases handled
    // must be exhaustive, otherwise the compiler will tell us if we missed a
    // case
    val result4 = when (mappedOutcome3) {
        is Outcome.Success -> mappedOutcome3.value.toString()
        is Outcome.Failure -> mappedOutcome3.cause.message.toString() // Try commenting out this line
    }
    println("RESULT 4: $result4")
}

// ============================
// Example operations and types
// ============================

// Optionally, an interface can be used to specify what error implementations _must_ provide
// if some standardization is useful
interface ErrorInterface {
    val message: String
}

// A sealed class (or enum) can be used to enumerate what kinds of errors are possible.
// When used with a `when` statement, the compiler will check for exhaustiveness
sealed class MyOperationErrors : ErrorInterface {
    data class ErrorA(override val message: String) : MyOperationErrors()
    data class ErrorB(override val message: String) : MyOperationErrors()
}

fun operation(value: String, isSuccess: Boolean): Outcome<String, MyOperationErrors> {
    if (isSuccess) {
        return Outcome.Success(value)
    } else {
        return Outcome.Failure(MyOperationErrors.ErrorA(value))
    }
}

// Below is another operation and error with a different Outcome interface
sealed class MyOperationErrors2 : ErrorInterface {
    data class ErrorA2(override val message: String) : MyOperationErrors2()
    data class ErrorB2(override val message: String) : MyOperationErrors2()
}

fun operation2(value: String, isSuccess: Boolean): Outcome<List<String>, MyOperationErrors2> {
    if (isSuccess) {
        return Outcome.Success(listOf(value))
    } else {
        return Outcome.Failure(MyOperationErrors2.ErrorA2(value))
    }
}

// =======
// Outcome
// =======

sealed class Outcome<out V : Any, out E : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T, Nothing>()
    data class Failure<out E : Any>(val cause: E) : Outcome<Nothing, E>()
}

inline fun <V : Any, U : Any, E : Any> Outcome<V, E>.map(transform: (V) -> U): Outcome<U, E> {
    return when (this) {
        is Outcome.Success -> Outcome.Success(transform(value))
        is Outcome.Failure -> this
    }
}

inline fun <V : Any, U : Any, E : Any> Outcome<V, E>.flatMap(transform: (V) -> Outcome<U, E>): Outcome<U, E> {
    return when (this) {
        is Outcome.Success -> transform(value)
        is Outcome.Failure -> this
    }
}

inline fun <V : Any, U : Any, E : Any> Outcome<V, E>.mapError(transform: (E) -> U): Outcome<V, U> {
    return when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.Failure(transform(cause))
    }
}

inline fun <V : Any, U : Any, E : Any> Outcome<V, E>.flatMapError(transform: (E) -> Outcome<V, U>): Outcome<V, U> {
    return when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> transform(cause)
    }
}
