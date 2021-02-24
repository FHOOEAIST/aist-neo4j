package science.aist.neo4j.transaction;

import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>Transaction class, which decorates a element of {@link org.neo4j.driver.Transaction} and delegates
 * the methods calls to this transaction</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public abstract class AbstractTransaction implements Transaction {
    /**
     * Next unique transaction id for logging
     */
    private static int nextTransactionId = 0;

    /**
     * Logger
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * id of the current transaction
     */
    private final int transactionId;

    /**
     * The decorated neo4j transaction
     */
    private Transaction decoratedElement;

    /**
     * @param decoratedElement the decorated neo4j transaction
     */
    public AbstractTransaction(Transaction decoratedElement) {
        transactionId = nextTransactionId++;
        this.decoratedElement = decoratedElement;
    }

    /**
     * gets value of field {@link AbstractTransaction#decoratedElement}
     *
     * @return value of field decoratedElement
     * @see AbstractTransaction#decoratedElement
     */
    protected Transaction getDecoratedElement() {
        return decoratedElement;
    }

    /**
     * gets value of field {@link AbstractTransaction#transactionId}
     *
     * @return value of field transactionId
     * @see AbstractTransaction#transactionId
     */
    public int getTransactionId() {
        return transactionId;
    }

    @Override
    public void commit() {
        decoratedElement.commit();
    }

    @Override
    public void rollback() {
        decoratedElement.rollback();
    }

    @Override
    public void close() {
        decoratedElement.close();
    }

    @Override
    public Result run(String statementTemplate, Value parameters) {
        return decoratedElement.run(statementTemplate, parameters);
    }

    @Override
    public Result run(String statementTemplate, Map<String, Object> statementParameters) {
        return decoratedElement.run(statementTemplate, statementParameters);
    }

    @Override
    public Result run(String statementTemplate, Record statementParameters) {
        return decoratedElement.run(statementTemplate, statementParameters);
    }

    @Override
    public Result run(String statementTemplate) {
        return decoratedElement.run(statementTemplate);
    }

    @Override
    public Result run(Query statement) {
        return decoratedElement.run(statement);
    }

    @Override
    public boolean isOpen() {
        return decoratedElement.isOpen();
    }

}
