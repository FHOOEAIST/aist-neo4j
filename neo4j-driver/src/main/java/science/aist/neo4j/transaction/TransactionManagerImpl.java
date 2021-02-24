package science.aist.neo4j.transaction;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.springframework.beans.factory.annotation.Required;

/**
 * <p>Transaction manager, that will return an inner transaction if there is already an active transaciton
 * Once the most outer transaction is closed, a new transaction can be created.</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public class TransactionManagerImpl implements TransactionManager {
    /**
     * Reference to the neo4j driver
     */
    private Driver driver;

    /**
     * The current active transaction
     */
    private OuterTransaction currentActiveTransaction = null;

    /**
     * the current active access mode
     */
    private AccessMode currentAccessMode = null;

    @Override
    public Transaction beginTransaction() {
        return beginTransaction(AccessMode.WRITE);
    }

    @Override
    public Transaction beginTransaction(AccessMode accessMode) {
        if (currentActiveTransaction == null) {
            currentAccessMode = accessMode;
            return currentActiveTransaction = new OuterTransaction(driver.session(SessionConfig.builder().withDefaultAccessMode(accessMode).build()).beginTransaction());
        }
        if (currentAccessMode == AccessMode.READ && accessMode == AccessMode.WRITE) {
            throw new IllegalStateException("Cannot open a inner write transaction, when the outer transaction is readonly");
        }
        return new InnerTransaction(currentActiveTransaction);
    }

    @Override
    public void closeSession() {
        driver.session().close();
    }

    /**
     * sets value of field {@link TransactionManagerImpl#driver}
     *
     * @param driver value of field driver
     * @see TransactionManagerImpl#driver
     */
    @Required
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    private class OuterTransaction extends AbstractTransaction {

        public OuterTransaction(Transaction decoratedElement) {
            super(decoratedElement);
            logger.debug("Outer transaction ({}) started", getTransactionId());
        }

        @Override
        public void commit() {
            logger.debug("Outer transaction ({}) success", getTransactionId());
            getDecoratedElement().commit();
        }

        @Override
        public void close() {
            currentActiveTransaction = null;
            logger.debug("Outer transaction ({}) close", getTransactionId());
            getDecoratedElement().close();
        }

    }

    private class InnerTransaction extends AbstractTransaction {
        private OuterTransaction outerTransaction;

        public InnerTransaction(OuterTransaction decoratedElement) {
            super(decoratedElement);
            this.outerTransaction = decoratedElement;
            logger.debug("Inner transaction ({}) of outer transaction ({}) started", getTransactionId(), outerTransaction.getTransactionId());
        }

        @Override
        public void commit() {
            logger.debug("Inner transaction ({}) of outer transaction ({}) ignoring success", getTransactionId(), outerTransaction.getTransactionId());
        }

        @Override
        public void close() {
            logger.debug("Inner transaction ({}) of outer transaction ({}) ignoring close", getTransactionId(), outerTransaction.getTransactionId());
        }

    }
}
