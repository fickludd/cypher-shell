package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.summary.Plan;
import org.neo4j.driver.v1.summary.ProfiledPlan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ResultSummaries {

    private ResultSummaries() {
        // static methods only
    }

    private static List<String> INFO = asList("Version", "Planner", "Runtime");

    @Nonnull
    static Map<String, Value> info(@Nonnull ResultSummary summary) {
        Map<String, Value> result = new LinkedHashMap<>();
        if (!summary.hasPlan()) return result;

        Plan plan = summary.plan();
        result.put("Plan", Values.value(summary.hasProfile() ? "PROFILE" : "EXPLAIN"));
        result.put("Statement", Values.value(summary.statementType().name()));
        Map<String, Value> arguments = plan.arguments();
        Value defaultValue = Values.value("");

        for (String key : INFO) {
            Value value = arguments.getOrDefault(key, arguments.getOrDefault(key.toLowerCase(), defaultValue));
            result.put(key, value);
        }
        result.put("Time", Values.value(summary.resultAvailableAfter(MILLISECONDS)+summary.resultConsumedAfter(MILLISECONDS)));
        if ( summary.hasProfile() ) result.put( "DbHits", Values.value( collectHits( summary.profile() ) ) );
        if (summary.hasProfile()) result.put("Rows", Values.value( summary.profile().records() ));
        return result;
    }

    private static long collectHits(@Nonnull ProfiledPlan operator) {
        long hits = operator.dbHits();
        hits = operator.children().stream().map( ResultSummaries::collectHits ).reduce(hits, (acc, subHits) -> acc + subHits );
        return hits;
    }

}
