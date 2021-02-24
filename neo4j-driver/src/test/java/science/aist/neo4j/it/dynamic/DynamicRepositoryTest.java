package science.aist.neo4j.it.dynamic;

import science.aist.neo4j.dynamic.DynamicRepository;
import science.aist.neo4j.it.dynamic.domain.*;
import science.aist.neo4j.namespace.NamespaceAwareReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.repository.AbstractNeo4JNodeRepositoyImpl;
import science.aist.neo4j.repository.AbstractNeo4JRepository;
import science.aist.neo4j.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

/**
 * <p>Test {@link DynamicRepository}</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@ContextConfiguration(locations = {"classpath*:testRepositoryConfig.xml"})
public class DynamicRepositoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private TransactionManager txManager;

    public static class ARepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<A> {
        ARepo(TransactionManager manager) {
            super(manager, A.class);
        }
    }

    public static class BRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<B> {
        BRepo(TransactionManager manager) {
            super(manager, B.class);
        }
    }

    public static class CRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<C> {
        CRepo(TransactionManager manager) {
            super(manager, C.class);
        }
    }

    public static class DRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<D> {
        DRepo(TransactionManager manager) {
            super(manager, D.class);
        }
    }

    public static class ERepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<E> {
        ERepo(TransactionManager manager) {
            super(manager, E.class);
        }
    }

    public static class FRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<F> {
        FRepo(TransactionManager manager) {
            super(manager, F.class);
        }
    }

    public static class GRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<G> {
        GRepo(TransactionManager manager) {
            super(manager, G.class);
        }
    }

    public static class HRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<H> {
        HRepo(TransactionManager manager) {
            super(manager, H.class);
        }
    }

    public static class IRepo extends NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<I> {
        IRepo(TransactionManager manager) {
            super(manager, I.class);
        }
    }

    @Autowired
    private TransactionManager transactionManager;

    private DynamicRepository dynamicRepository;
    private ARepo aRepo;
    private BRepo bRepo;
    private CRepo cRepo;
    private DRepo dRepo;
    private ERepo eRepo;
    private FRepo fRepo;
    private GRepo gRepo;
    private HRepo hRepo;
    private IRepo iRepo;

    @BeforeMethod
    public void initRepos() {
        aRepo = new ARepo(txManager);
        bRepo = new BRepo(txManager);
        cRepo = new CRepo(txManager);
        dRepo = new DRepo(txManager);
        eRepo = new ERepo(txManager);
        fRepo = new FRepo(txManager);

        gRepo = new GRepo(txManager);
        hRepo = new HRepo(txManager);
        iRepo = new IRepo(txManager);

        Map<Class, AbstractNeo4JRepository> repositoryMap = new HashMap<>();
        repositoryMap.put(A.class, aRepo);
        repositoryMap.put(B.class, bRepo);
        repositoryMap.put(C.class, cRepo);
        repositoryMap.put(D.class, dRepo);
        repositoryMap.put(E.class, eRepo);
        repositoryMap.put(F.class, fRepo);
        repositoryMap.put(G.class, gRepo);
        repositoryMap.put(H.class, hRepo);
        repositoryMap.put(I.class, iRepo);
        aRepo.setRepositories(repositoryMap);
        bRepo.setRepositories(repositoryMap);
        cRepo.setRepositories(repositoryMap);
        dRepo.setRepositories(repositoryMap);
        eRepo.setRepositories(repositoryMap);
        fRepo.setRepositories(repositoryMap);
        gRepo.setRepositories(repositoryMap);
        hRepo.setRepositories(repositoryMap);
        iRepo.setRepositories(repositoryMap);

        Map<Class, AbstractNeo4JNodeRepositoyImpl> nodeRepositories = new HashMap<>();
        nodeRepositories.put(A.class, aRepo);
        nodeRepositories.put(B.class, bRepo);
        nodeRepositories.put(C.class, cRepo);
        nodeRepositories.put(D.class, dRepo);
        nodeRepositories.put(E.class, eRepo);
        nodeRepositories.put(F.class, fRepo);
        nodeRepositories.put(G.class, gRepo);
        nodeRepositories.put(H.class, hRepo);
        nodeRepositories.put(I.class, iRepo);

        dynamicRepository = new DynamicRepository(nodeRepositories, transactionManager);
    }

    @BeforeMethod
    public void setUp() {
        fRepo.queryAllTyped("MATCH (n) WHERE any(l IN labels(n) WHERE l=~'SomeCreateNameSpace.*') detach delete n;", null);
    }

    @Test
    public void testSave() {
        // given
        C c2 = new C(99, "a C to rule them all", "c2");
        C c3 = new C(33, "cKing", "c2");

        B b2 = new B(42, "Stringify");

        E e = new E(123);
        D d2 = new D(73, "asdfadfaa", e);

        A a = new A();
        a.setBs(Arrays.asList(c2, c3, b2, d2));

        // when
        dynamicRepository.save(a);

        // then
        Assert.assertNotNull(a.getId());
        Assert.assertEquals(aRepo.findAllAsStream().count(), 1L);
        // Can' test here if the saved a equals the original a, as the driver can only load the static type of the class.
        //  Assert.assertTrue(aRepo.findAllAsStream().anyMatch(x -> x.equals(a)));
        Optional<A> a1 = aRepo.findAllAsStream().findAny();
        Assert.assertTrue(a1.isPresent());
        Assert.assertEquals(a1.get().getBs().size(), 4);
        Assert.assertEquals(bRepo.findAllAsStream().count(), 4L);
        Assert.assertTrue(bRepo.findAllAsStream().anyMatch(x -> x.equals(b2)));
        Assert.assertEquals(cRepo.findAllAsStream().count(), 2L);
        Assert.assertTrue(cRepo.findAllAsStream().anyMatch(x -> x.equals(c2)));
        Assert.assertTrue(cRepo.findAllAsStream().anyMatch(x -> x.equals(c3)));
        Assert.assertEquals(dRepo.findAllAsStream().count(), 1L);
        Assert.assertTrue(dRepo.findAllAsStream().anyMatch(x -> x.equals(d2)));
        Assert.assertEquals(eRepo.findAllAsStream().count(), 1L);
        Assert.assertTrue(eRepo.findAllAsStream().anyMatch(x -> x.equals(e)));
    }

    @Test
    public void testLoad() {
        // given
        C c2 = new C(99, "a C to rule them all", "c2");
        C c3 = new C(33, "cKing", "c2");

        B b2 = new B(42, "Stringify");

        E e = new E(123);
        D d2 = new D(73, "asdfadfaa", e);

        A a = new A();
        a.setBs(Arrays.asList(c2, c3, b2, d2));

        A a2 = new A();
        a2.setBs(Arrays.asList(b2, d2));

        F f = new F();
        f.setAs(Arrays.asList(a, a2));

        dynamicRepository.save(f);


        // when
        F load = dynamicRepository.load(f.getId(), F.class);

        // then
        List<A> as = load.getAs();
        Assert.assertEquals(as.size(), 2);
        for (A a1 : as) {
            List<B> bs = a1.getBs();
            Assert.assertTrue(bs.size() == 4 || bs.size() == 2);
        }
    }

    @Test
    public void testLoad2() {
        // given
        G g = new G();

        H h1 = new H();
        H h2 = new H();
        H h3 = new H();
        H h4 = new H();
        H h5 = new H();

        List<H> hs1 = Arrays.asList(h1, h2, h3, h4, h5);
        g.setHs(hs1);
        hs1.forEach(h -> h.setG(g));

        I i = new I();
        i.setG(g);
        dynamicRepository.save(i);

        // when
        I load = dynamicRepository.load(i.getId(), I.class);

        // then
        G loadedG = load.getG();
        Assert.assertNotNull(loadedG);
        List<H> hs = loadedG.getHs();
        Assert.assertEquals(hs.size(), hs1.size());
        for (H h : hs) {
            Assert.assertEquals(h.getG(), loadedG);
        }
    }

    @Test
    public void testLoad3() {
        // given
        C c2 = new C(99, "a C to rule them all", "c2");
        C c3 = new C(33, "cKing", "c2");

        B b2 = new B(42, "Stringify");

        E e = new E(123);
        D d2 = new D(73, "asdfadfaa", e);

        A a = new A();
        a.setBs(Arrays.asList(c2, c3, b2, d2));

        A a2 = new A();
        a2.setBs(Arrays.asList(b2, d2));

        F f = new F();
        f.setAs(Arrays.asList(a, a2));

        dynamicRepository.save(f);


        // when
        F load = dynamicRepository.load(f.getId(), F.class, B.class);

        // then
        List<A> as = load.getAs();
        Assert.assertEquals(as.size(), 2);
        for (A a1 : as) {
            List<B> bs = a1.getBs();
            Assert.assertTrue(bs.isEmpty());
        }
    }

    @Test
    public void testLoad4() {
        // given
        C c2 = new C(99, "a C to rule them all", "c2");
        C c3 = new C(33, "cKing", "c2");

        B b2 = new B(42, "Stringify");

        E e = new E(123);
        D d2 = new D(73, "asdfadfaa", e);

        A a = new A();
        a.setBs(Arrays.asList(c2, c3, b2, d2));

        A a2 = new A();
        a2.setBs(Arrays.asList(b2, d2));

        F f = new F();
        f.setAs(Arrays.asList(a, a2));

        dynamicRepository.save(f);

        // when
        F load = dynamicRepository.load(f.getId(), F.class, Arrays.asList(A.class, B.class), true);

        // then
        List<A> as = load.getAs();
        Assert.assertEquals(as.size(), 2);
        for (A a1 : as) {
            List<B> bs = a1.getBs();
            Assert.assertFalse(bs.isEmpty());
        }
    }

    @Test
    public void testLoad5() {
        // given
        C c1 = new C(45, "test", "c2");
        C c2 = new C(99, "a C to rule them all", "c2");
        C c3 = new C(33, "cKing", "c2");
        C c4 = new C(67, "hallo", "c2");

        A a = new A();
        a.setBs(Arrays.asList(c1, c2, c3, c4));

        dynamicRepository.save(a);

        Map<Class<?>, Class<?>> mapping = new HashMap<>();
        mapping.put(B.class, C.class);

        // when
        A load = dynamicRepository.load(a.getId(), A.class, Arrays.asList(A.class, B.class), true, mapping);

        // then
        Assert.assertNotNull(load);
        List<B> bs = load.getBs();
        Assert.assertEquals(bs.size(), 4);
        for (B b : bs) {
            Assert.assertTrue(b instanceof C);
            C c = (C) b;
            Assert.assertEquals(c.getcString(), "c2");
        }
    }

    @Test
    public void testLoad6() {
        // given
        B c1 = new B(45, "test");
        B c2 = new B(99, "a C to rule them all");
        C c3 = new C(33, "cKing", "c2");
        C c4 = new C(67, "hallo", "c2");

        A a = new A();
        a.setBs(Arrays.asList(c1, c2, c3, c4));

        dynamicRepository.save(a);

        Map<Class<?>, Class<?>> mapping = new HashMap<>();
        mapping.put(B.class, C.class);

        // when
        A load = dynamicRepository.load(a.getId(), A.class, null, false, mapping);

        // then
        Assert.assertNotNull(load);
        List<B> bs = load.getBs();
        Assert.assertEquals(bs.size(), 4);
        int nrcs = 0;
        int nrbs = 0;
        System.out.println(bs);
        for (B b : bs) {
            System.out.println(b.getClass());
            if (b instanceof C) {
                nrcs++;
            } else {
                nrbs++;
            }
        }

        Assert.assertEquals(nrcs, 2);
        Assert.assertEquals(nrbs, 2);
    }
}
