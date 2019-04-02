package org.neo4j.shell.prettyprint;

import org.hamcrest.CoreMatchers;
import org.neo4j.driver.v1.Value;
import org.neo4j.shell.cli.Format;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class TableOutputFormatterTest extends OutputFormatterTestBase {

    public TableOutputFormatterTest() {
        super(new PrettyPrinter(Format.VERBOSE));
    }

    @Override
    void assertPrettyPrintPlanInformation(String actual, Map<String, Value> argumentMap) {

        // then
        argumentMap.forEach((k, v) -> {
            assertThat(actual, CoreMatchers.containsString("| " + k));
            assertThat(actual, CoreMatchers.containsString("| " + v.toString()));
        });
    }

    @Override
    void assertBasicTable(String actual) {

        // then
        assertThat(actual, containsString("| c1  | c2 |"));
        assertThat(actual, containsString("| \"a\" | 42 |"));
    }

    @Override
    void assertTwoRows(String actual) {

        // then
        assertThat(actual, containsString("| \"a\" | 42 |"));
        assertThat(actual, containsString("| \"b\" | 43 |"));
    }
}
