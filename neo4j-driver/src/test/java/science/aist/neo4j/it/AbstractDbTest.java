package science.aist.neo4j.it;

import science.aist.neo4j.it.nodes.NodiestNode;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
@ContextConfiguration(locations = {"classpath*:testRepositoryConfig.xml"})
public abstract class AbstractDbTest extends AbstractTestNGSpringContextTests {

    // doesn't really matter we just want any repository
    @Autowired
    @Qualifier("nodiestNodeRepository")
    ReflectiveNeo4JNodeRepositoryImpl<NodiestNode> repository;


    @BeforeClass
    public void before() {
//        Logger.getInstance().error("BC: " + this.getClass().getName());
        repository.queryTyped("MATCH (n) detach delete n", null);
    }

    @AfterClass
    public void after() {
//        Logger.getInstance().error("AC: " + this.getClass().getName());
        repository.queryTyped("MATCH (n) detach delete n", null);
    }

    // NOTE: this is debug code. Feel free to un-comment if you think the testNG order is wrong, but please don't check it into the repository
//    @AfterMethod
//    public void methafter(Method method) {
//        Logger.getInstance().error("AFTER: " + method.getDeclaringClass().getName() + "." + method.getName());
//    }
//
//    @BeforeMethod
//    public void methbefore(Method method) {
//        Logger.getInstance().error("BEFORE: " + method.getDeclaringClass().getName() + "." + method.getName());
//    }

}
