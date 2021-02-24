package science.aist.neo4j.transaction;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.summary.ResultSummary;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper Class for Neo4J 4 that copies all Results, to prevent them becoming unavailable after the transaction is closed.
 *
 * This class is only needed when you load Results that should become available OUTSIDE of a transaction.
 * Preferrably you should use the science.aist.neo4j.annotation.Transaction annotation.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ResultClone implements Result {

    /**
     * Keys of result
     */
    private List<String> keys;

    /**
     * Records in result
     */
    private List<Record> records = new LinkedList<>();

    /**
     * Iterator on records
     */
    private Iterator<Record> iterator;

    /**
     * Peekerator on records to enable the "peek" functionality of Result
     */
    private Iterator<Record> peekerator;

    public <T> ResultClone(Result apply) {
        keys = apply.keys();
        while (apply.hasNext()) {
            records.add(apply.next());
        }

        iterator = records.iterator();
        peekerator = records.iterator();
    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Record next() {
        peekerator = iterator;
        return iterator.next();
    }

    @Override
    public Record single() throws NoSuchRecordException {
        if (records.isEmpty()) {
            throw new NoSuchRecordException("no record");
        }
        return records.get(0);
    }

    @Override
    public Record peek() {
        return peekerator.next();
    }

    @Override
    public Stream<Record> stream() {
        return records.stream();
    }

    @Override
    public List<Record> list() {
        return records;
    }

    @Override
    public <T> List<T> list(Function<Record, T> function) {
        return records.stream().map(function::apply).collect(Collectors.toList());
    }

    @Override
    public ResultSummary consume() {
        throw new UnsupportedOperationException("consume not implemented for ResultClone");
    }
}
