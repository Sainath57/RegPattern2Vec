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

    public static class NodeRecord {
        public final Node node;

        public NodeRecord(Node node) {
            this.node = node;
        }
    }

    @Context
    public GraphDatabaseService gdbs;


    static List<List<Double>> embeddings = new ArrayList<>();

    @Procedure(value = "embeddings1.regpattern2vec", mode = Mode.READ)
    @Description("Generates RegPattern2Vec")

    public Stream</*List<List<Double>>*/NodeRecord> regpattern2vec(@Name("regPattern") String regPattern, @Name("walkLength") Long walkLength, @Name("walkCount") Long walkCount) {
        // Your procedure logic here
        // Using sets to avoid duplicates

        List<NodeRecord> regularExpressionRandomWalks = new ArrayList<>();

        Set<String> labelsSet = new HashSet<>();
        Set<String> relTypesSet = new HashSet<>();

        try (Transaction tx = gdbs.beginTx()) {
            // Iterate over all nodes to collect labels
//            for (Node node : tx.getAllNodes()) {
//                for (Label label : node.getLabels()) {
//                    labelsSet.add(label.name());
//                }
//            }

            // Iterate over all relationships to collect relationship types
            for (Relationship rel : tx.getAllRelationships()) {
                RelationshipType type = rel.getType();
                relTypesSet.add(type.name());
            }

            //tx.commit();
            //}

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


            //try (Transaction tx = gdbs.beginTx()) {
                for (int i = 1; i <= walkCount; i++) {
                    for (Node node : tx.getAllNodes()){

                        regularExpressionRandomWalks = RegularExpressionRandomWalks(tx,gdbs,rd.DStates, node, walkLength);
                        //HeterogenousSkipGram(embeddings,regularExpressionRandomWalks, walkLength);

                    }
                }
                tx.commit();
        }

        catch(Exception e) {
            e.printStackTrace();
        }


        //return Stream.of(embeddings);
        return regularExpressionRandomWalks.stream();
    }

    private List<NodeRecord> RegularExpressionRandomWalks(Transaction tx, GraphDatabaseService gd, List<State> transitions, Node node, Long walkLength){

        List<NodeRecord> regularExpressionRandomWalks = new ArrayList<>(List.of(new NodeRecord(node)));
        State currentState = transitions.stream().filter(state -> state.isFirstState).findFirst().orElse(null);
        if (currentState == null) {
            throw new IllegalStateException("No starting state found in the transitions.");
        }

        NodeRecord currentNode = regularExpressionRandomWalks.get(regularExpressionRandomWalks.size() - 1);

        for(int i = 1; i <= walkLength-1; i++) {

            Iterable<Relationship> allNeighbourRelationships = currentNode.node.getRelationships();
            boolean transitionMade = false;

            for (Relationship rel: allNeighbourRelationships) {

                String relType = rel.getType().name();
                Map<String, State> currentStateTransitions = currentState.getAllMoves();

                if (currentStateTransitions.containsKey(relType)) {
                    currentNode = new NodeRecord(rel.getOtherNode(currentNode.node));
                    regularExpressionRandomWalks.add(currentNode);
                    currentState = currentState.getNextStateBySymbol(relType);
                    transitionMade = true;
                    break;
                }
            }

                if (!transitionMade) {
                    break;
                }

                //Logic
//                if (nodeSelectionProbability(gd, transitions, currentNode)) {
//
//                    regularExpressionRandomWalks.add(currentNode);
//
//                }
        }

        return regularExpressionRandomWalks;

    }

    private void HeterogenousSkipGram(List<List<Double>> embeddings, List<Node> regularExpressionRandomWalks, Long walkLength) {

        for(int i = 1; i < walkLength; i++) {

            //String node = regularExpressionRandomWalks.get(i);

        }

    }

    private void sendValuesIntoFile(String fileName) {

    }

    private boolean nodeSelectionProbability(GraphDatabaseService gd, List<State> transitions, Node node) {

        return true;
    }

}