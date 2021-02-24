package science.aist.neo4j.service;

import science.aist.neo4j.annotation.Transaction;
import science.aist.neo4j.transaction.TransactionManager;

/**
 * <p>Dummy Service for Testing</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class DummyService {
    private TransactionManager txManager;

    @Transaction(mode = Transaction.Mode.WRITE, transactionManager = "txManager")
    public void runInTransaction() {
    }

    @Transaction(mode = Transaction.Mode.READ, transactionManager = "txManager")
    public Object runInTransaction2() {
        return "";
    }

    @Transaction(transactionManager = "txManager")
    public void runInTransaction3(int i) {
    }

    @Transaction(transactionManager = "txManager")
    public Object runInTransaction4(int i) {
        return "";
    }

    @Transaction(transactionManager = "txManager")
    public Object runInTransaction5(int i, Object o2, String s3, long l4, double d5) {
        return "";
    }

    @Transaction(transactionManager = "txManager")
    public Object runInTransaction6(int i, Object o2, String s3, long l4, double d5) {
        throw new IllegalStateException();
    }

    public void doNotTouchMe() {

    }

    /**
     * sets value of field {@link DummyService#txManager}
     *
     * @param txManager value of field txManager
     * @see DummyService#txManager
     */
    public void setTxManager(TransactionManager txManager) {
        this.txManager = txManager;
    }
}