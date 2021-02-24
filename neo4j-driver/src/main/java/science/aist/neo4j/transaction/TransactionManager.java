package science.aist.neo4j.transaction;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Interface for transaction management</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public interface TransactionManager {
    /**
     * Begins a new Transaction with default access mode. If a transaction is already running returns a inner transaction
     * as a decorate for the other transaction
     *
     * @return returns the transaction
     */
    Transaction beginTransaction();

    /**
     * Same as {@link TransactionManager#beginTransaction()} but with manual access mode
     *
     * @param accessMode the access mode
     * @return the transaction
     */
    Transaction beginTransaction(AccessMode accessMode);

    /**
     * Runs a code block in a transaction
     *
     * @param consumer   the code block to be executed
     * @param accessMode the access mode
     */
    default void run(Consumer<Transaction> consumer, AccessMode accessMode) {
        try (Transaction transaction = beginTransaction(accessMode)) {
            consumer.accept(transaction);
            transaction.commit();
        }
    }

    /**
     * Closes the current session (on start of a new transaction a new session will be created automatially).
     * As a session is a "logical container for a causally chained series of transactions" closing a session is
     * entirely up to the developer.
     */
    void closeSession();

    /**
     * Executes a code block with a return value in a transaction
     *
     * @param function   the function that is executed in the transaction
     * @param accessMode the access mode
     * @param <T>        the type of the return value
     * @return the result of the function
     */
    default <T> T execute(Function<Transaction, T> function, AccessMode accessMode) {
        try (Transaction transaction = beginTransaction(accessMode)) {
            T apply = function.apply(transaction);

            if (apply instanceof Result) {
                // copy over results that otherwise become unavailable after the transaction closes
                apply = (T) new ResultClone((Result)apply);
            }
            transaction.commit();
            return apply;
        }
    }

    /**
     * {@link TransactionManager#run(Consumer, AccessMode)} with access mode {@link AccessMode#READ}
     *
     * @param consumer the code block to be executed
     * @see TransactionManager#run(Consumer, AccessMode)
     */
    default void runRead(Consumer<Transaction> consumer) {
        run(consumer, AccessMode.READ);
    }


    /**
     * {@link TransactionManager#run(Consumer, AccessMode)} with access mode {@link AccessMode#WRITE}
     *
     * @param consumer the code block to be executed
     * @see TransactionManager#run(Consumer, AccessMode)
     */
    default void runWrite(Consumer<Transaction> consumer) {
        run(consumer, AccessMode.WRITE);
    }

    /**
     * {@link TransactionManager#execute(Function, AccessMode)} with access mode {@link AccessMode#READ}
     *
     * @param function the function that is executed in the transaction
     * @param <T>      the type of the return value
     * @return the result of the function
     */
    default <T> T executeRead(Function<Transaction, T> function) {
        return execute(function, AccessMode.READ);
    }

    /**
     * {@link TransactionManager#execute(Function, AccessMode)} with access mode {@link AccessMode#WRITE}
     *
     * @param function the function that is executed in the transaction
     * @param <T>      the type of the return value
     * @return the result of the function
     */
    default <T> T executeWrite(Function<Transaction, T> function) {
        return execute(function, AccessMode.WRITE);

    }
}
