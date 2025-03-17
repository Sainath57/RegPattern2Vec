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


    List<List<Double>> embeddings = new ArrayList<>();

    @Procedure(value = "embeddings1.regpattern2vec", mode = Mode.READ)
    @Description("Generates RegPattern2Vec")

    public Stream<List<List<Double>>> regpattern2vec(@Name("regPattern") String regPattern, @Name("walkLength") Long walkLength, @Name("walkCount") Long walkCount) {
        // Your procedure logic here
        // Using sets to avoid duplicates

        List<String> regularExpressionRandomWalks = new ArrayList<>();

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

        RegexToDfa rd = new RegexToDfa(relTypesList, regPattern);
        //SyntaxTree st = new SyntaxTree(regPattern,)

        // Now you have labelsList and relTypesList for further processing.
//        System.out.println("Node Labels: " + labelsList);
//        System.out.println("Relationship Types: " + relTypesList);
//
//        System.out.println("RegexToDfa: " + rd.finalRegex);


        try (Transaction tx = gdbs.beginTx()) {
            for (int i = 1; i <= walkCount; i++) {
                for (Node node : tx.getAllNodes()){

                    regularExpressionRandomWalks = RegularExpressionRandomWalks(gdbs,rd, node, walkLength);
                    HeterogenousSkipGram(embeddings,regularExpressionRandomWalks, walkLength);

                }
            }
        }


        return Stream.of(embeddings);
    }

    private List<String> RegularExpressionRandomWalks(GraphDatabaseService gd, RegexToDfa rd, Node node, Long walkLength){

        List<String> regularExpressionRandomWalks = new ArrayList<>(Integer.parseInt(node.toString()));
        Iterable<Relationship> allNeighbourRelationships = node.getRelationships();

        for(int i = 1; i <= walkLength-1; i++){

            Node u = null;

            //Logic
            if(nodeSelectionProbability(gd,rd,u)) {

                regularExpressionRandomWalks.add(u.toString());

            }
        }

        return regularExpressionRandomWalks;

    }

    private void HeterogenousSkipGram(List<List<Double>> embeddings, List<String> regularExpressionRandomWalks, Long walkLength) {

        for(int i = 1; i < walkLength; i++) {

            String node = regularExpressionRandomWalks.get(i);

        }

    }

    private void sendValuesIntoFile(String fileName) {

    }

    private boolean nodeSelectionProbability(GraphDatabaseService gd, RegexToDfa rd, Node node) {

        return true;
    }

}