package science.aist.neo4j.repository;

import science.aist.neo4j.it.dummy.A;
import science.aist.neo4j.it.dummy.B;
import science.aist.neo4j.it.dynamic.domain.C;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>Test class for {@link ReflectiveNeo4JNodeRepositoryImpl}</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
public class ReflectiveNeo4JNodeRepositoryImplTest {
    ReflectiveNeo4JNodeRepositoryImpl<A> aRepository;
    ReflectiveNeo4JNodeRepositoryImpl<B> bRepository;
    ReflectiveNeo4JNodeRepositoryImpl<C> cRepository;
    Map<Class, AbstractNeo4JRepository> repositoyMap;

    @BeforeMethod
    public void before() {

        aRepository = new ReflectiveNeo4JNodeRepositoryImpl<>(null, A.class);
        bRepository = new ReflectiveNeo4JNodeRepositoryImpl<>(null, B.class);
        cRepository = new ReflectiveNeo4JNodeRepositoryImpl<>(null, C.class);

        repositoyMap = new HashMap<>();
        repositoyMap.put(A.class, aRepository);
        repositoyMap.put(B.class, bRepository);
        repositoyMap.put(C.class, cRepository);

        aRepository.setRepositories(repositoyMap);
        bRepository.setRepositories(repositoyMap);
        cRepository.setRepositories(repositoyMap);
    }

    /**
     * Tests query method which calls "queryTyped" for retrieving result
     */
    @Test
    public void testQueryWithExecute() {
        // given
        AbstractNeo4JNodeRepositoyImpl<A> spy = Mockito.spy(aRepository);
        Mockito.doReturn(new A()).when(spy).queryTyped(Mockito.any(), Mockito.any());
        repositoyMap.put(A.class, spy);

        // when
        spy.query("test", null, A.class);

        // then
        Mockito.verify(spy, Mockito.times(1)).queryNormalOrTyped("test", null, A.class, repositoyMap);
        Mockito.verify(spy, Mockito.times(1)).query("test", null, A.class);
        Mockito.verify(spy, Mockito.times(1)).queryTyped("test", null);
        Mockito.verifyNoMoreInteractions(spy);
    }

    /**
     * Tests query method which calls "queryHelp" for retrieving result
     */
    @Test
    public void testQueryWithQueryHelp() {
        // given
        AbstractNeo4JNodeRepositoyImpl<A> spy = Mockito.spy(aRepository);
        Mockito.doReturn(new Object()).when(spy).queryHelp(Mockito.any(), Mockito.any(), Mockito.any());

        // when
        spy.query("test", null, Object.class);

        // then
        Mockito.verify(spy, Mockito.times(1)).queryNormalOrTyped("test", null, Object.class, repositoyMap);
        Mockito.verify(spy, Mockito.times(1)).query("test", null, Object.class);
        Mockito.verify(spy, Mockito.times(1)).queryHelp("test", null, Object.class);
        Mockito.verifyNoMoreInteractions(spy);
    }

    /**
     * Tests query method which calls "queryAllTyped" for retrieving result
     */
    @Test
    public void testQueryAllWithExecuteAll() {
        // given
        AbstractNeo4JNodeRepositoyImpl<A> spy = Mockito.spy(aRepository);
        Mockito.doReturn(Arrays.asList(new A())).when(spy).queryAllTyped(Mockito.any(), Mockito.any());
        repositoyMap.put(A.class, spy);

        // when
        spy.queryAll("test", null, A.class);

        // then
        Mockito.verify(spy, Mockito.times(1)).queryAllNormalOrTyped("test", null, A.class, repositoyMap);
        Mockito.verify(spy, Mockito.times(1)).queryAll("test", null, A.class);
        Mockito.verify(spy, Mockito.times(1)).queryAllTyped("test", null);
        Mockito.verifyNoMoreInteractions(spy);
    }

    /**
     * Tests query method which calls "queryAllHelp" for retrieving result
     */
    @Test
    public void testQueryAllWithQueryHelpAll() {
        // given
        AbstractNeo4JNodeRepositoyImpl<A> spy = Mockito.spy(aRepository);
        Mockito.doReturn(Stream.of(new Object())).when(spy).queryAllHelp(Mockito.any(), Mockito.any(), Mockito.any());

        // when
        spy.queryAll("test", null, Object.class);

        // then
        Mockito.verify(spy, Mockito.times(1)).queryAllNormalOrTyped("test", null, Object.class, repositoyMap);
        Mockito.verify(spy, Mockito.times(1)).queryAll("test", null, Object.class);
        Mockito.verify(spy, Mockito.times(1)).queryAllHelp("test", null, Object.class);
        Mockito.verifyNoMoreInteractions(spy);
    }
}