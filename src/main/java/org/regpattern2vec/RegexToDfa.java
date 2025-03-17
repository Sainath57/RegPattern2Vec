package org.regpattern2vec;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author @Sainath Talakanti
 */
public class RegexToDfa {

    private Set<Integer>[] followPos;
    public String finalRegex;
    private Nodes root;
    public Set<State> DStates;

    private  Set<String> input; //set of inputs is used in input regex
    public List<State> stateNames = new ArrayList<>();

//    private  List<String> TypeList = new ArrayList<>();
//    private  String regex;

    /**
     * a number is assigned to each input (even duplicate ones)
     * <p>
     * symbolNum is a hash map has a key which mentions the number and has
     * a value which mentions the corresponding character or sometimes a string
     * for characters is followed up by backslash like "\*"
     */
    private static HashMap<Integer, String> symbolNum;

//    public static void main(String[] args) {
//        List<String> relTypes = new ArrayList<>(Arrays.asList("ACTED_IN", "REVIEWED", "PRODUCED", "WROTE", "FOLLOWS", "DIRECTED"));
//        String fN = "Start";
//        String reg = "(ACTED_IN)*[REVIEWED]+((PRODUCED)|(WROTE)).(^FOLLOWS){2,}[DIRECTED]+";
//        new RegexToDfa(relTypes,fN,reg);
//    }
    public RegexToDfa() {
    }

    public RegexToDfa(List<String> TypeList, String regex) {

//        this.TypeList = TypeList;
//        this.regex = regex;

        System.out.println("Input Regex: " + regex);

//        Scanner in = new Scanner(System.in);
        //allocating
        DStates = new HashSet<>();
        input = new HashSet<String>();

        //Check if regex has incorrect relationShip types
        String splitter = "(?:\\{\\d+,\\})|(?:[^()\\[\\]\\*\\+\\.\\^]+)|(?:[()\\[\\]\\*\\+\\.\\^])";

        Pattern pattern = Pattern.compile(splitter);
        Matcher matcher = pattern.matcher(regex);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        //System.out.println("Tokens: " + tokens);

        for (String token : tokens) {
            if(!token.equals("(") && !token.equals(")") && !token.equals("{") && !token.equals("}")
                    && !token.equals("[") && !token.equals("]") && !token.equals("*") && !token.equals("+")
                        && !token.equals(".") && !token.equals("|") && !token.equals("^") && !token.matches("\\{\\d+,\\}")  && !TypeList.contains(token)) {
                System.out.println("Incorrect Relationship Type in Given Regular Expression");
                return;

            }
        }

//        String regex = getRegex(in);
//        getSymbols(regex);

        /// giving the regex to SyntaxTree class constructor and creating the
        /// syntax tree of the regular expression in it
        SyntaxTree st = new SyntaxTree(regex+".#", TypeList);
        finalRegex = st.getFinalRegex();
        //System.out.println("Syntax Tree Sample: " + st.getRoot().getLeft().getLeft().getLeft().getLeft().getLeft()
        //                                                .getLeft().getLeft().getLeft().getLeft().getLeft().getSymbol());
        System.out.println("Final Regex: " + finalRegex);
        System.out.println("Number of Leaves: "+ st.getFollowPos().length);
        getSymbols(finalRegex);
        System.out.println("Input:" + input);
        root = st.getRoot(); //root of the syntax tree
        followPos = st.getFollowPos(); //the followPos of the syntax tree

        /// creating the DFA using the syntax tree were created upside and
        /// returning the start state of the resulted DFA
        State q0 = createDFA();
        DfaTraversal dfat = new DfaTraversal(q0, input);

        System.out.println("All transitions: ");
        System.out.println("DStates: " + DStates.size());
        for(State state: DStates) {
            if(!DStates.isEmpty()) {
                System.out.println("State Name: " + state.getName());
                for (Map.Entry<String, State> entry : state.getAllMoves().entrySet()) {
                    if (!entry.getKey().equals("#")) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().getName());
                        //System.out.println(state.getAllMoves().entrySet());
                    }
                }
            }
        }

//        for(State state: stateNames){
//            System.out.println("State Name: "+state.getName());
//        }

        //System.out.println(getAllStateNames());
//        String str = getStr(regex);
//        boolean acc = false;
//
//        //Check if regex has incorrect relationShip types
//        String splitter1 = "([^()\\{\\}\\[\\]\\*\\+\\.]+|[()\\{\\}\\[\\]\\*\\+\\.])";
//
//        Pattern pattern1 = Pattern.compile(regex);
//        Matcher matcher1 = pattern1.matcher(splitter);
//
//        List<String> tokens1 = new ArrayList<>();
//        while (matcher.find()) {
//            tokens1.add(matcher.group());
//        }
//
//        for (String token : tokens1) {
//            if (dfat.setCharacter(token)) {
//                acc = dfat.traverse();
//            } else {
//                System.out.println("WRONG CHARACTER!");
//                System.exit(0);
//            }
//        }
//        if (acc) {
//            System.out.println((char) 27 + "[32m" + "this string is acceptable by the regex!");
//        } else {
//            System.out.println((char) 27 + "[31m" + "this string is not acceptable by the regex!");
//        }
    }

    private  String getRegex(String str) {
        return str+"#";
    }

    private void getSymbols(String regex) {
        /**
         * op is a set of characters have operational meaning for example '*'
         * could be a closure operator
         */
        Set<String> op = new HashSet<>(Arrays.asList("(", ")", "*", "|", ".", "[", "]", "+", "^", "{", "}", ","));

        input = new HashSet<>();
        symbolNum = new HashMap<>();
        int num = 1;
        //Check if regex has incorrect relationShip types
        String splitter = "(?:\\{\\d+,\\})|(?:[^()\\[\\]\\*\\+\\.\\^]+)|(?:[()\\[\\]\\*\\+\\.\\^])";

        Pattern pattern = Pattern.compile(splitter);
        Matcher matcher = pattern.matcher(finalRegex);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        for (String token : tokens) {
            if (!op.contains(token)) {
                input.add(token);
                symbolNum.put(num++, token);
            }
        }
    }

    private State createDFA() {
        int id = 0;
        Set<Integer> firstpos_n0 = root.getFirstPos();
        State q0 = new State(id++);
        q0.addAllToName(firstpos_n0);
        if (q0.getName().contains(followPos.length)) {
            q0.setAccept();
        }
        DStates.clear();
        DStates.add(q0);
        //System.out.println("Q0: " + q0.getName());
        stateNames.add(q0);

        while (true) {
            boolean exit = true;
            State s = null;
            for (State state : DStates) {
                if (!state.getIsMarked()) {
                    exit = false;
                    s = state;
                }
            }
            if (exit) {
                break;
            }

            if (s.getIsMarked()) {
                continue;
            }
            s.setIsMarked(true); //mark the state
            Set<Integer> name = s.getName();
            for (String a : input) {
                Set<Integer> U = new HashSet<>();
                for (int p : name) {
                    if (symbolNum.get(p).equals(a)) {
                        U.addAll(followPos[p - 1]);
                    }
                }
                boolean flag = false;
                State tmp = null;
                for (State state : DStates) {
                    if (state.getName().equals(U)) {
                        tmp = state;
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    State q = new State(id++);
                    q.addAllToName(U);
                    if (U.contains(followPos.length)) {
                        q.setAccept();
                    }
                    DStates.add(q);
                    if(!q.getName().isEmpty()){stateNames.add(q);}
                    tmp = q;
                }
                s.addMove(a, tmp);
            }
        }

        return q0;
    }

    private String getStr(String str) {
        System.out.print("Enter a string: ");
        return "";
    }

    public HashMap<Set<Integer>,Map.Entry<String, State>> getAllTransitions() {

        HashMap<Set<Integer>,Map.Entry<String, State>> allTransitions = new HashMap<>();

        for(State state: DStates) {
            for(Map.Entry<String, State> entry: state.getAllMoves().entrySet()) {
                allTransitions.put(state.getName(),entry);
            }
        }
        return allTransitions;
    }

    public List<Set<Integer>> getAllStateNames() {
        List<Set<Integer>> allStateNames = new ArrayList<>();
        for(State state: stateNames){
            allStateNames.add(state.getName());
        }
        return allStateNames;
    }
}
