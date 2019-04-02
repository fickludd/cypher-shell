package org.neo4j.shell.prettyprint;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.neo4j.driver.internal.*;
import org.neo4j.driver.internal.value.*;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ProfiledPlan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.StatementType;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.state.BoltResult;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.shell.prettyprint.ValueFormatter.formatValue;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class ValueFormatterTest {

    @Test
    public void formatPoint() {
        Value point2d = new PointValue(new InternalPoint2D(4326, 42.78, 56.7));
        Value point3d = new PointValue(new InternalPoint3D(4326, 1.7, 26.79, 34.23));

        assertThat(formatValue(point2d), equalTo("point({srid:4326, x:42.78, y:56.7})") );
        assertThat(formatValue(point3d), equalTo("point({srid:4326, x:1.7, y:26.79, z:34.23})") );
    }

    @Test
    public void formatDuration() {
        Value duration = new DurationValue(new InternalIsoDuration(1, 2, 3, 4));

        assertThat(formatValue(duration), equalTo("P1M2DT3.000000004S") );
    }

    @Test
    public void formatDurationWithNoTrailingZeroes() {
        Value duration = new DurationValue(new InternalIsoDuration(1, 2, 3, 0));

        assertThat(formatValue(duration), equalTo("P1M2DT3S"));
    }

    @Test
    public void formatNode() {
        List<String> labels = asList("label1", "label2");
        Map<String, Value> propertiesAsMap = new HashMap<>();
        propertiesAsMap.put("prop1", Values.value("prop1_value"));
        propertiesAsMap.put("prop2", Values.value("prop2_value"));
        Value node = new NodeValue(new InternalNode(1, labels, propertiesAsMap));

        assertThat(formatValue(node), equalTo("(:label1:label2 {prop2: \"prop2_value\", prop1: \"prop1_value\"})"));
    }

    @Test
    public void formatRelationships() {
        Map<String, Value> propertiesAsMap = new HashMap<>();
        propertiesAsMap.put("prop1", Values.value("prop1_value"));
        propertiesAsMap.put("prop2", Values.value("prop2_value"));
        RelationshipValue relationship =
                new RelationshipValue(new InternalRelationship(1, 1, 2, "RELATIONSHIP_TYPE", propertiesAsMap));

        assertThat(formatValue(relationship), equalTo("[:RELATIONSHIP_TYPE {prop2: \"prop2_value\", prop1: \"prop1_value\"}]"));
    }

    @Test
    public void formatPath() {
        // given
        Node n1 = mock(Node.class);
        when(n1.id()).thenReturn(1L);
        List<String> labels = asList("L1");
        when(n1.labels()).thenReturn(labels);
        when(n1.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Relationship r1 = mock(Relationship.class);
        when(r1.startNodeId()).thenReturn(2L);
        when(r1.type()).thenReturn("R1");
        when(r1.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Node n2 = mock(Node.class);
        when(n2.id()).thenReturn(2L);
        when(n2.labels()).thenReturn(asList("L2"));
        when(n2.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Relationship r2 = mock(Relationship.class);
        when(r2.startNodeId()).thenReturn(2L);
        when(r2.type()).thenReturn("R2");
        when(r2.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Node n3 = mock(Node.class);
        when(n3.id()).thenReturn(3L);
        when(n3.labels()).thenReturn(asList("L3"));
        when(n3.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Path.Segment s1 = mock(Path.Segment.class);
        when(s1.relationship()).thenReturn(r1);
        when(s1.start()).thenReturn(n1);
        when(s1.end()).thenReturn(n2);

        Path.Segment s2 = mock(Path.Segment.class);
        when(s2.relationship()).thenReturn(r2);
        when(s2.start()).thenReturn(n2);
        when(s2.end()).thenReturn(n3);

        List<Path.Segment> segments = asList(s1, s2);
        List<Node> nodes = asList(n1, n2);
        List<Relationship> relationships = asList(r1);
        InternalPath internalPath = new InternalPath(segments, nodes, relationships);
        Value path = new PathValue(internalPath);

        assertThat(formatValue(path), equalTo("(:L1)<-[:R1]-(:L2)-[:R2]->(:L3)"));
    }

    @Test
    public void formatRelationshipWithEscapingForSpecialCharacters() {
        Map<String, Value> propertiesAsMap = new HashMap<>();
        propertiesAsMap.put("prop1", Values.value("prop1, value"));
        propertiesAsMap.put("prop2", Values.value(1));
        Value relationship = new RelationshipValue(new InternalRelationship(1, 1, 2, "RELATIONSHIP,TYPE", propertiesAsMap));

        // then
        assertThat(formatValue(relationship),
                equalTo("[:`RELATIONSHIP,TYPE` {prop2: 1, prop1: \"prop1, value\"}]"));
    }

    @Test
    public void formatNodeWithEscapingForSpecialCharacters() {
        List<String> labels = asList("label `1", "label2");
        Map<String, Value> nodeProperties = new HashMap<>();
        nodeProperties.put("prop1", Values.value("prop1:value"));
        String doubleQuotes = "\"\"";
        nodeProperties.put("1prop1", Values.value(doubleQuotes));
        nodeProperties.put("ä", Values.value("not-escaped"));
        Value node = new NodeValue(new InternalNode(1, labels, nodeProperties));

        // then
        assertThat(formatValue(node),
                equalTo("(:`label ``1`:label2 {`1prop1`: \"\\\"\\\"\", " +
                        "prop1: \"prop1:value\", ä: \"not-escaped\"})"));
    }

    @Test
    public void formatCollections() {
        Value list = Values.value(12, 13);
        Value map = Values.value(singletonMap("a", 42));
        Value listMap = Values.value(singletonMap("a", asList(14, 15)));

        assertThat(formatValue(list), equalTo("[12, 13]"));
        assertThat(formatValue(map), equalTo("{a: 42}"));
        assertThat(formatValue(listMap), equalTo("{a: [14, 15]}"));
    }

    @Test
    public void formatEntities() {
        Map<String, Value> properties = singletonMap("name", Values.value("Mark"));
        Map<String, Value> relProperties = singletonMap("since", Values.value(2016));
        InternalNode node = new InternalNode(12, asList("Person"), properties);
        InternalRelationship relationship = new InternalRelationship(24, 12, 12, "TEST", relProperties);
        InternalPath path = new InternalPath(node, relationship, node);

        assertThat(formatValue(node.asValue()), equalTo("(:Person {name: \"Mark\"})"));
        assertThat(formatValue(relationship.asValue()), equalTo("[:TEST {since: 2016}]"));
        assertThat(formatValue(path.asValue()), equalTo("(:Person {name: \"Mark\"})-[:TEST {since: 2016}]->(:Person {name: \"Mark\"})"));
    }
}
