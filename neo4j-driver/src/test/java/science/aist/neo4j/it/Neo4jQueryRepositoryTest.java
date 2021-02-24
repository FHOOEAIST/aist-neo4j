package science.aist.neo4j.it;

import science.aist.neo4j.Neo4jQueryRepository;
import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

/**
 * <p>Tests for {@link Neo4jQueryRepository}</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public class Neo4jQueryRepositoryTest extends AbstractDbTest {

    @Autowired
    @Qualifier("bnalyticsRepository")
    private Neo4jQueryRepository repo;

    @Autowired
    private TransactionManager txManager;

    @Test
    public void testQueryInt() {
        // given
        int age = 42;

        // when
        int i = repo.query("CREATE (a: Person {age: $age}) RETURN a.age", Values.parameters("age", age), Integer.class);

        // then
        Assert.assertEquals(i, age);
    }

    @Test
    public void testQueryString() {
        // given
        String name = "Max Mustermann";

        // when
        String i = repo.query("CREATE (a: Person {name: $name}) RETURN a.name", Values.parameters("name", name), String.class);

        // then
        Assert.assertEquals(i, name);
    }

    @Test
    public void testQueryBoolean() {
        // given

        // when
        boolean res = repo.query("RETURN 1 = 1", null, Boolean.class);

        // then
        Assert.assertTrue(res);
    }

    @Test
    public void testQueryList() {
        // given

        // when
        List<?> l = repo.query("CREATE (a: Person {name: \"a\"}), (b : Person {name: \"b\"}) RETURN [a.name, b.name]", null, List.class);

        // then
        Assert.assertEquals(l.size(), 2);
    }

    @Test
    public void testQueryAllString() {
        // given

        // when
        Stream<String> l = repo.queryAll("CREATE (a: Person {name: \"a\"}), (b : Person {name: \"b\"}) \n" +
            "WITH [a.name, b.name] AS persons\n" +
            "UNWIND persons AS person\n" +
            "RETURN person", null, String.class);

        // then
        Assert.assertEquals(l.count(), 2L);
    }

    @Test
    public void testQueryIntWithRollback() {
        // given
        int age = 42;

        // when
        int id;
        try (Transaction tx = txManager.beginTransaction()) {
            id = repo.query("CREATE (a: Person {age: $age}) RETURN id(a)", Values.parameters("age", age), Integer.class);
            tx.rollback();
        }

        // then
        Integer i = repo.query("MATCH(x) WHERE id(x) = $id RETURN x.age", Values.parameters("id", id), Integer.class);
        Assert.assertNull(i);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testQueryIntWithReadTxWhenWrite() {
        // given
        int age = 42;

        // when
        txManager.runRead(tx -> repo.query("CREATE (a: Person {age: $age}) RETURN id(a)", Values.parameters("age", age), Integer.class));

        // then
    }

    @Test
    public void testQueryIntWithWriteTxWhenRead() {
        // given

        // when
        Object o = txManager.executeWrite(tx -> repo.query("MATCH(x) WHERE id(x) = $id RETURN x.age", Values.parameters("id", -1), Integer.class));

        // then
        Assert.assertNull(o);
    }

}
