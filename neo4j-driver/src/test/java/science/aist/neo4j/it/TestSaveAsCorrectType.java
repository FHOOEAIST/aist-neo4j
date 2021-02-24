package science.aist.neo4j.it;

import science.aist.neo4j.it.dummy.A;
import science.aist.neo4j.it.dummy.AA;
import science.aist.neo4j.it.dummy.B;
import science.aist.neo4j.it.dummy.C;
import science.aist.neo4j.namespace.NamespaceAwareReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.repository.AbstractNeo4JNodeRepositoyImpl;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.neo4j.transaction.TransactionManagerImpl;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Praschl
 * @since 1.0
 */
public class TestSaveAsCorrectType extends AbstractDbTest {

    public class ARepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<A> {
        public ARepo(TransactionManager manager, Class<A> aClass) {
            super(manager, aClass);
        }
    }

    public class BRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<B> {
        public BRepo(TransactionManager manager, Class<B> aClass) {
            super(manager, aClass);
        }
    }

    public class CRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<C> {
        public CRepo(TransactionManager manager, Class<C> aClass) {
            super(manager, aClass);
        }
    }

    @Autowired
    Driver driver;

    @Test
    public void test() {
        // given
        TransactionManagerImpl txMgr = new TransactionManagerImpl();
        txMgr.setDriver(driver);

        ARepo aRepo = new ARepo(txMgr, A.class);
        BRepo bRepo = new BRepo(txMgr,B.class);
        CRepo cRepo = new CRepo(txMgr,C.class);

        Map<Class, AbstractNeo4JNodeRepositoyImpl> repos = new HashMap<>();
        repos.put(A.class, aRepo);
        repos.put(B.class, bRepo);
        repos.put(C.class, cRepo);

        repos.values().forEach(repo -> ((ReflectiveNeo4JNodeRepositoryImpl) repo).setRepositories(repos));

        B b = new B();
        C c = new C();
        c.x = 1;
        c.y = 3;

        C cS = new C();
        cS.x = 98;
        cS.y = 99;

        AA a = new AA();

        a.elements.add(b);
        a.elements.add(c);
        a.singleElement = cS;
        a.b = c;
        a.bees = new ArrayList<>();
        a.bees.add(b);

        // when
        a = aRepo.save(a);
        Assert.assertNotNull(a.id);
        A aLoad = aRepo.findById(a.id);

        C cSLoad = ClassInformation.castToClass(aLoad.singleElement, C.class);

        AA aaLoad = ClassInformation.castToClass(aLoad, AA.class);

        // then
        Assert.assertNotNull(aLoad);
        Assert.assertNotNull(aLoad.elements);
        Assert.assertEquals(aLoad.elements.size(), 2);
        Assert.assertNotNull(aLoad.singleElement);
        Assert.assertNotNull(cSLoad);
        Assert.assertEquals(cSLoad.x, 98);
        Assert.assertEquals(cSLoad.y, 99);
        Assert.assertNotNull(aaLoad.b);
        Assert.assertNotNull(aaLoad.bees);
    }

}
