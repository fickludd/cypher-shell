package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;

public interface OutputFormatter {

    @Nonnull String format(@Nonnull BoltResult result);

    @Nonnull default String formatPlan(@Nonnull ResultSummary summary) {
        return "";
    }
    @Nonnull default String formatInfo(@Nonnull ResultSummary summary) {
        return "";
    }
    @Nonnull default String formatFooter(@Nonnull BoltResult result) {
        return "";
    }
}
