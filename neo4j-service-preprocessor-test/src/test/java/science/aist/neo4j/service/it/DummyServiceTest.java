package science.aist.neo4j.service.it;

import science.aist.neo4j.service.DummyService;
import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import science.aist.neo4j.Neo4jServiceTransactionPreprocessorMojo;

import java.util.Map;

/**
 * <p>Tests {@link Neo4jServiceTransactionPreprocessorMojo}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class DummyServiceTest {

    // Helper classes, which track the transaction management.
    class TestTransaction implements Transaction {

        boolean isRunning;
        boolean isSuccess = false;
        boolean isFailure = false;
        boolean isClosed = false;

        AccessMode accessMode;

        TestTransaction(AccessMode accessMode) {
            isRunning = true;
            this.accessMode = accessMode;
        }

        @Override
        public void commit() {
            isRunning = false;
            isSuccess = true;
        }

        @Override
        public void rollback() {
            isRunning = false;
            isFailure = true;
        }

        @Override
        public boolean isOpen() {
            return isRunning;
        }

        @Override
        public void close() {
            isRunning = false;
            isClosed = true;
        }

        @Override
        public Result run(String statementTemplate, Value parameters) {
            return null;
        }

        @Override
        public Result run(String statementTemplate, Map<String, Object> statementParameters) {
            return null;
        }

        @Override
        public Result run(String statementTemplate, Record statementParameters) {
            return null;
        }

        @Override
        public Result run(String statementTemplate) {
            return null;
        }


        @Override
        public Result run(Query statement) {
            return null;
        }

    }

    class TestTransactionManager implements TransactionManager {

        TestTransaction txCurrent;

        @Override
        public Transaction beginTransaction() {
            return beginTransaction(AccessMode.WRITE);
        }

        @Override
        public Transaction beginTransaction(AccessMode accessMode) {
            txCurrent = new TestTransaction(accessMode);
            return txCurrent;
        }

        @Override
        public void closeSession() {
        }
    }

    @Test
    public void testDummyServiceCompilingDoNotTouchMe() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        ds.doNotTouchMe();

        // then
        Assert.assertNull(ttm.txCurrent);
    }

    @Test
    public void testDummyServiceCompilingRunInTransaction() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        ds.runInTransaction();

        // then
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.WRITE);
        Assert.assertFalse(ttm.txCurrent.isFailure);
        Assert.assertTrue(ttm.txCurrent.isSuccess);
    }


    @Test
    public void testDummyServiceCompilingRunInTransaction2() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        Object o = ds.runInTransaction2();

        // then
        Assert.assertEquals(o, "");
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.READ);
        Assert.assertFalse(ttm.txCurrent.isFailure);
        Assert.assertTrue(ttm.txCurrent.isSuccess);
    }

    @Test
    public void testDummyServiceCompilingRunInTransaction3() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        ds.runInTransaction3(5);

        // then
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.WRITE);
        Assert.assertFalse(ttm.txCurrent.isFailure);
        Assert.assertTrue(ttm.txCurrent.isSuccess);
    }

    @Test
    public void testDummyServiceCompilingRunInTransaction4() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        Object o = ds.runInTransaction4(5);

        // then
        Assert.assertEquals(o, "");
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.WRITE);
        Assert.assertFalse(ttm.txCurrent.isFailure);
        Assert.assertTrue(ttm.txCurrent.isSuccess);
    }

    @Test
    public void testDummyServiceCompilingRunInTransaction5() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        Object o = ds.runInTransaction5(5, "", "", 3L, 4d);

        // then
        Assert.assertEquals(o, "");
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.WRITE);
        Assert.assertFalse(ttm.txCurrent.isFailure);
        Assert.assertTrue(ttm.txCurrent.isSuccess);
    }

    @Test
    public void testDummyServiceCompilingRunInTransaction6() {
        // given
        TestTransactionManager ttm = new TestTransactionManager();
        DummyService ds = new DummyService();
        ds.setTxManager(ttm);

        // when
        Object o = null;
        boolean iseE = false;
        try {
            o = ds.runInTransaction6(5, "", "", 3L, 4d);
        } catch (IllegalStateException ise) {
            iseE = true;
        }

        // then
        Assert.assertNull(o);
        Assert.assertTrue(iseE);
        Assert.assertTrue(ttm.txCurrent.isClosed);
        Assert.assertFalse(ttm.txCurrent.isRunning);
        Assert.assertEquals(ttm.txCurrent.accessMode, AccessMode.WRITE);
        Assert.assertTrue(ttm.txCurrent.isFailure);
        Assert.assertFalse(ttm.txCurrent.isSuccess);
    }

}
