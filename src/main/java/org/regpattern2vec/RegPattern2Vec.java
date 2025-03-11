package org.regpattern2vec;

import java.util.*;

import java.util.stream.Stream;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;

public class RegPattern2Vec {

    static {
        System.out.println("RegPattern2Vec plugin is loading...");
    }

    @Context
    public GraphDatabaseService gdbs;

    @Procedure(value = "embeddings1.regpattern2vec", mode = Mode.READ)
    @Description("Generates RegPattern2Vec")

    public Stream<Output> regpattern2vec(@Name("startNode") String startNode, @Name("regPattern") String regPattern, @Name("walkLength") Long walkLength, @Name("walkCount") Long walkCount) {
        // Your procedure logic here
        // Using sets to avoid duplicates
        Set<String> labelsSet = new HashSet<>();
        Set<String> relTypesSet = new HashSet<>();

        try (Transaction tx = gdbs.beginTx()) {
            // Iterate over all nodes to collect labels
            for (Node node : tx.getAllNodes()) {
                for (Label label : node.getLabels()) {
                    labelsSet.add(label.name());
                }
            }

            // Iterate over all relationships to collect relationship types
            for (Relationship rel : tx.getAllRelationships()) {
                RelationshipType type = rel.getType();
                relTypesSet.add(type.name());
            }

            tx.commit();
        }

        // Convert sets to lists if needed
        List<String> labelsList = new ArrayList<>(labelsSet);
        List<String> relTypesList = new ArrayList<>(relTypesSet);

        RegexToDfa rd = new RegexToDfa(relTypesList, startNode, regPattern);
        //SyntaxTree st = new SyntaxTree(regPattern,)

        // Now you have labelsList and relTypesList for further processing.
//        System.out.println("Node Labels: " + labelsList);
//        System.out.println("Relationship Types: " + relTypesList);
//
//        System.out.println("RegexToDfa: " + rd.finalRegex);



        return Stream.of(new Output("Labels list : "+labelsList+" Reltypes list : "+relTypesList+" RegexToDfa : "+rd.getAllTransitions().keySet()  ));
    }

    public static class Output {
        public String out;

        public Output(String out) {
            this.out = out;
        }
    }
}