package org.regpattern2vec;

import java.util.*;
import java.util.stream.Stream;
import java.lang.Iterable;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import javax.ws.rs.core.Context;

public class RegPattern2Vec {

    @Context
    public GraphDatabaseService gdbs;

    @Procedure(value = "regpattern2vec.RegPattern2Vec")
    @Description("Generated RegPattern2Vec")

    public void generateRegPattern2Vec(@Name("input") String input) {
        // Your procedure logic here

        // Using sets to avoid duplicates
        Set<String> labelsSet = new HashSet<>();
        Set<String> relTypesSet = new HashSet<>();

        try (Transaction tx = gdbs.beginTx()) {
            // Iterate over all nodes to collect labels
            for (Node node : gdbs.beginTx().getAllNodes()) {
                for (Label label : node.getLabels()) {
                    labelsSet.add(label.name());
                }
            }

            // Iterate over all relationships to collect relationship types
            for (Relationship rel : gdbs.beginTx().getAllRelationships()) {
                RelationshipType type = rel.getType();
                relTypesSet.add(type.name());
            }

            tx.commit();
        }

        // Convert sets to lists if needed
        List<String> labelsList = new ArrayList<>(labelsSet);
        List<String> relTypesList = new ArrayList<>(relTypesSet);

        // Now you have labelsList and relTypesList for further processing.
        System.out.println("Node Labels: " + labelsList);
        System.out.println("Relationship Types: " + relTypesList);
    }
    }