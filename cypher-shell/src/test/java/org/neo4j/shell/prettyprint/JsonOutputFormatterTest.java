package org.neo4j.shell.prettyprint;

import org.hamcrest.CoreMatchers;
import org.neo4j.driver.v1.Value;
import org.neo4j.shell.cli.Format;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class JsonOutputFormatterTest extends OutputFormatterTestBase {

    public JsonOutputFormatterTest() {
        super(new PrettyPrinter(Format.JSON));
    }

    @Override
    void assertPrettyPrintPlanInformation(String actual, Map<String, Value> argumentMap) {

        // then
        argumentMap.forEach((k, v) -> {
            assertThat(actual, containsString(k + ": " + v.toString()));
        });
    }

    @Override
    void assertBasicTable(String actual) {

        // then
        assertThat(actual, containsString("{ c1: \"a\", c2: 42 }"));
    }

    @Override
    void assertTwoRows(String actual) {

        // then
        assertThat(actual, containsString("{ c1: \"a\", c2: 42 }, { c1: \"b\", c2: 43 }"));
    }

}
