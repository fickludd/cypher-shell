package org.neo4j.shell.prettyprint;

import org.junit.Test;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ProfiledPlan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.StatementType;
import org.neo4j.shell.state.BoltResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public abstract class OutputFormatterTestBase {

    private final PrettyPrinter printer;

    OutputFormatterTestBase(PrettyPrinter printer) {
        this.printer = printer;
    }

    @Test
    public void prettyPrintPlanInformation() {
        // given
        ResultSummary resultSummary = mock(ResultSummary.class);
        ProfiledPlan plan = mock(ProfiledPlan.class);
        when(plan.dbHits()).thenReturn(1000L);
        when(plan.records()).thenReturn(20L);

        when(resultSummary.hasPlan()).thenReturn(true);
        when(resultSummary.hasProfile()).thenReturn(true);
        when(resultSummary.plan()).thenReturn(plan);
        when(resultSummary.profile()).thenReturn(plan);
        when(resultSummary.resultAvailableAfter(anyObject())).thenReturn(5L);
        when(resultSummary.resultConsumedAfter(anyObject())).thenReturn(7L);
        when(resultSummary.statementType()).thenReturn(StatementType.READ_ONLY);
        Map<String, Value> argumentMap = Values.parameters("Version", "3.1", "Planner", "COST", "Runtime", "INTERPRETED").asMap(v -> v);
        when(plan.arguments()).thenReturn(argumentMap);

        BoltResult result = mock(BoltResult.class);
        when(result.getRecords()).thenReturn(Collections.emptyList());
        when(result.getSummary()).thenReturn(resultSummary);

        // when
        String actual = printer.format(result);

        // then
        assertPrettyPrintPlanInformation(actual, argumentMap);
    }

    abstract void assertPrettyPrintPlanInformation(String actual, Map<String, Value> argumentMap);

    @Test
    public void basicTable() {
        // GIVEN
        StatementResult result = mockResult(asList("c1", "c2"), "a", 42);

        // WHEN
        String actual = formatResult(result);

        // THEN
        assertBasicTable(actual);
    }

    abstract void assertBasicTable( String actual );

    @Test
    public void twoRows() {
        // GIVEN
        StatementResult result = mockResult(asList("c1", "c2"), "a", 42, "b", 43);

        // WHEN
        String actual = formatResult(result);

        // THEN
        assertTwoRows(actual);
    }

    abstract void assertTwoRows( String actual );

    // HELPERS

    private String formatResult(StatementResult result) {
        // calling list() is what actually executes cypher on the server
        List<Record> list = result.list();
        return printer.format(new BoltResult(list, result));
    }

    private StatementResult mockResult(List<String> cols, Object... data) {
        StatementResult result = mock(StatementResult.class);
        Statement statement = mock(Statement.class);
        ResultSummary summary = mock(ResultSummary.class);
        when(summary.statement()).thenReturn(statement);
        when(result.keys()).thenReturn(cols);
        List<Record> records = new ArrayList<>();
        List<Object> input = asList(data);
        int width = cols.size();
        for (int row = 0; row < input.size() / width; row++) {
            records.add(record(cols, input.subList(row * width, (row + 1) * width)));
        }
        when(result.list()).thenReturn(records);
        when(result.consume()).thenReturn(summary);
        when(result.summary()).thenReturn(summary);
        return result;
    }

    private Record record(List<String> cols, List<Object> data) {
        assert cols.size() == data.size();
        Value[] values = data.stream()
                .map(Values::value)
                .toArray(Value[]::new);
        return new InternalRecord(cols, values);
    }
}
