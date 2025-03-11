package org.regpattern2vec;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import java.util.*;

public class Walks {

    private Path path;
    List<String> relTypes = new ArrayList<>(Arrays.asList("ACTED_IN", "REVIEWED", "PRODUCED", "WROTE", "FOLLOWS", "DIRECTED"));
    String fN = "Start";
    String reg = "(ACTED_IN)*[REVIEWED]+((PRODUCED)|(WROTE)).(^FOLLOWS){2,}[DIRECTED]+";
    public RegexToDfa regexToDfa = new RegexToDfa(relTypes,fN,reg);
    public final List<State> allStates = regexToDfa.stateNames;
    public int noOfStates;
    public int walkLength;

    public Walks(String startNode, int noOfWalks, int walkLength) {

        this.noOfStates = noOfWalks;
        this.walkLength = walkLength;
        //allStates = regexToDfa.getAllStateNames()

        System.out.println("All State Names: "+regexToDfa.getAllStateNames());
        System.out.println(allStates.size());
        transformStateNames();
        System.out.println("All State Names: "+getAllStateNames());

//        System.out.println("All transitions: ");
//        System.out.println("DStates: " + allStates.size());
//        for(State state: allStates) {
//            if(!allStates.isEmpty()) {
//                System.out.println("State Name: " + state.getName());
//                for (Map.Entry<String, State> entry : state.getAllMoves().entrySet()) {
//                    if (!entry.getKey().equals("#")) {
//                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().getName());
//                        //System.out.println(state.getAllMoves().entrySet());
//                    }
//                }
//            }
//        }
    }

    public static void main(String[] args) {
        new Walks("startNode", 10, 10);
    }

    public void transformStateNames() {
        int counter = 0;
        for (State state : allStates) {
            state.setTransformedName(++counter);
        }
    }

    public List<Integer> getAllStateNames() {
        List<Integer> allStateNames = new ArrayList<>();
        for(State state: allStates){
            allStateNames.add(state.getTransformedName());
        }
        return allStateNames;
    }

    public void walkProbability(Node currentNode){



    }

}
