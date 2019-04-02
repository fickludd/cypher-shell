package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static org.neo4j.shell.prettyprint.ValueFormatter.COMMA_SEPARATOR;
import static org.neo4j.shell.prettyprint.ValueFormatter.NEWLINE;

public class PlainOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        List<Record> records = result.getRecords();
        if (!records.isEmpty()) {
            sb.append(records.get(0).keys()
                    .stream()
                    .collect(Collectors.joining(COMMA_SEPARATOR)));

            sb.append("\n");
            sb.append(records.stream()
                    .map(this::formatRecord)
                    .collect(Collectors.joining("\n")));
        }
        return sb.toString();
    }

    @Nonnull
    private String formatRecord(@Nonnull final Record record) {
        return record.values().stream()
                .map(ValueFormatter::formatValue)
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    @Nonnull
    @Override
    public String formatInfo(@Nonnull ResultSummary summary) {
        if (!summary.hasPlan()) {
            return "";
        }
        Map<String, Value> info = ResultSummaries.info(summary);
        return info.entrySet().stream()
                .map( e -> String.format("%s: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(NEWLINE));
    }
}
