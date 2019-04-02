package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.neo4j.shell.prettyprint.ValueFormatter.NEWLINE;
import static org.neo4j.shell.prettyprint.ValueFormatter.formatValue;

public class TableOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        List<Value> data = result.getRecords().stream()
                .map(r -> Values.value(r.<Value>asMap(v -> v)))
                .collect(Collectors.toList());
        return formatValues(data, result.getKeys());
    }

    @Nonnull
    String formatValues(@Nonnull List<Value> data, List<String> columns) {
        if (columns.isEmpty()) {
            return "";
        }
        if (data.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Map<String, Integer> columnSizes = calculateColumnSizes(columns, data);
        String headerLine = padColumnHeading(columnSizes);
        int lineWidth = headerLine.length() - 2;
        String dashes = "+" + ValueFormatter.repeat('-', lineWidth) + "+";
        addHeader(sb, headerLine, dashes);

        data.forEach(record -> sb.append(padColumnHeading(columnSizes, record)).append(NEWLINE));

        sb.append(dashes).append(NEWLINE);
        return sb.toString();
    }

    private void addHeader(StringBuilder sb, String headerLine, String dashes) {
        sb.append(dashes).append(NEWLINE);
        sb.append(headerLine).append(NEWLINE);
        sb.append(dashes).append(NEWLINE);
    }

    @Nonnull
    public String formatFooter(@Nonnull BoltResult result) {
        int rows = result.getRecords().size();
        ResultSummary summary = result.getSummary();
        return String.format("%d row%s available after %d ms, " +
                        "consumed after another %d ms", rows, rows != 1 ? "s" : "",
                summary.resultAvailableAfter(MILLISECONDS),
                summary.resultConsumedAfter(MILLISECONDS));
    }

    @Nonnull
    private String padColumnHeading(@Nonnull Map<String, Integer> columnSizes, @Nonnull Value m) {
        StringBuilder sb = new StringBuilder("|");
        columnSizes.entrySet().forEach(entry -> {
            sb.append(" ");
            String txt = formatValue(m.get(entry.getKey()));
            String value = ValueFormatter.rightPad(txt, entry.getValue());
            sb.append(value);
            sb.append(" |");
        });
        return sb.toString();
    }

    @Nonnull
    private String padColumnHeading(@Nonnull Map<String, Integer> columnSizes) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columnSizes.keySet()) {
            sb.append(" ");
            sb.append(ValueFormatter.rightPad(column, columnSizes.get(column)));
            sb.append(" |");
        }
        return sb.toString();
    }

    @Nonnull
    private Map<String, Integer> calculateColumnSizes(@Nonnull List<String> columns, @Nonnull List<Value> data) {
        Map<String, Integer> columnSizesMap = mapColumnsToLength(columns);
        for (Map.Entry<String, Integer> entry : columnSizesMap.entrySet()) {
            String key = entry.getKey();
            Integer maxRecordSize = data.stream().map(record ->
                    formatValue(record.get(key)).length()).max(Integer::compareTo).get();
            if (entry.getValue() < maxRecordSize) {
                columnSizesMap.put(key, maxRecordSize);
            }
        }
        return columnSizesMap;
    }

    private Map<String, Integer> mapColumnsToLength(@Nonnull List<String> columns) {
        Map<String, Integer> columnSizes = new LinkedHashMap<>();
        for (String column : columns) {
            columnSizes.put(column, column.length());
        }
        return columnSizes;
    }

    @Override
    @Nonnull
    public String formatInfo(@Nonnull ResultSummary summary) {
        Map<String, Value> info = ResultSummaries.info(summary);
        List<Value> data = Collections.singletonList(Values.value(info));
        List<String> columns = new ArrayList<>(info.keySet());
        return formatValues(data, columns);
    }

    @Override
    @Nonnull
    public String formatPlan(@Nullable ResultSummary summary) {
        if (summary == null || !summary.hasPlan()) return "";
        return new TablePlanFormatter().formatPlan(summary.plan());
    }
}
