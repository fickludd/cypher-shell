package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.shell.prettyprint.ValueFormatter.NEWLINE;
import static org.neo4j.shell.prettyprint.ValueFormatter.repeat;

public class JsonOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        String[] columns = result.getKeys().toArray(new String[0]);
        sb.append("{").append(NEWLINE).append("  records: ");

        List<Record> records = result.getRecords();
        if (records.isEmpty()) {
            sb.append("[]").append(NEWLINE).append("}");
            return sb.toString();
        }

        sb.append("[");
        String comma = NEWLINE;
        for (Record record : records) {
            sb.append(comma);
            comma = ","+NEWLINE;
            formatRecord(sb, 2, columns, record);
        }
        sb.append(NEWLINE);

        sb.append("  ]").append(NEWLINE);
        sb.append("}");
        return sb.toString();
    }

    private void formatRecord(StringBuilder sb,
                              int indent,
                              String[] columns,
                              @Nonnull final Record record) {

        if (columns.length == 0) {
            sb.append("{}");
            return;
        }

        sb.append(repeat("  ", indent)).append("{");
        String comma = NEWLINE;
        for (int i = 0; i < columns.length; i++) {
            sb.append(comma);
            comma = ","+NEWLINE;
            sb.append(repeat("  ", indent+1))
                    .append(columns[i])
                    .append(": ")
                    .append(ValueFormatter.formatValue(record.get(i)));
        }
        sb.append(NEWLINE).append(repeat("  ", indent)).append("}");
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
