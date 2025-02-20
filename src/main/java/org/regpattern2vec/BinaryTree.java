/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.regpattern2vec;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author @ALIREZA_KAY
 */
class BinaryTree {
    
    /*
        ***
            (a|b)*a => creating binary syntax tree:
                                .
                               / \
                              *   a
                             /
                            |
                           / \
                          a   b
        ***
    */

    private String regular;

    private int leafNodeID = 0;
    
    // Stacks for symbol nodes and operators
    private final Stack<Node> stackNode = new Stack<>();
    private final Stack<String> operatorStack = new Stack<String>();

    // Set of inputs
    private final Set<String> input = new HashSet<String>();
    private ArrayList<String> op = new ArrayList<>();

    private List<String> relationshipTypeList = new ArrayList<>();
    private  String firstNodeName;

    // Generates tree using the regular expression and returns it's root
    public Node generateTree(String regular1, List<String> relationshipTypeList, String firstNodeName) {

        System.out.println("Entering BinaryTree");

        this.relationshipTypeList = relationshipTypeList;
        this.firstNodeName = firstNodeName;

        op.addAll(Arrays.asList("*", "|", "."));

        // Only inputs available
//        Character[] ch = new Character[26 + 26];
//        for (int i = 65; i <= 90; i++) {
//            ch[i - 65] = (char) i;
//            ch[i - 65 + 26] = (char) (i + 32);
//        }
        //Character[] integer = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String[] others = {"#", " ", "(", ")","[", "]","{", "}", "^", ","};
//        input.addAll(Arrays.asList(ch));
        //input.addAll(Arrays.asList(integer));

        input.addAll(relationshipTypeList);
        input.addAll(Arrays.asList(others));

        // Generate regular expression with the concatenation
        String regular2 = AddConcat(regular1);
        //System.out.println("After concatenation: " + regular2);

        String regular3 = plusToStart(regular2);
        System.out.println("After plusToStar: " + regular3);

        String regular4 = complementReplacement(regular3);
        System.out.println("After complementReplacement: " + regular4);

        regular = flowerBracketReplacement(regular4);
        System.out.println("After flowerBracketReplacement: " + regular);
        
        // Cleaning stacks
        stackNode.clear();
        operatorStack.clear();

        //Check if regex has incorrect relationShip types
        String splitter = "([^()\\[\\]\\*\\.]+|[()\\[\\]\\*\\.])";

        Pattern pattern = Pattern.compile(splitter);
        Matcher matcher = pattern.matcher(regular);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        System.out.println("tokens : " + tokens);

        for (String token : tokens) {

            if (isRelationshipType(token)) {
                pushStack(token);
            } else if (operatorStack.isEmpty()) {
                operatorStack.push(token);

            } else if (Objects.equals(token, "(") || Objects.equals(token, "[")) {
                operatorStack.push(token);

            } else if (Objects.equals(token, ")") || Objects.equals(token, "]")) {
                while (!operatorStack.get(operatorStack.size()-1).equals("(") && !operatorStack.get(operatorStack.size()-1).equals("[")) {
                    doOperation();
                }

                // Pop the '(' left parenthesis
                operatorStack.pop();

            } else {
                while (!operatorStack.isEmpty()
                        && Priority(token, operatorStack.get(operatorStack.size()-1))) {
                    doOperation();
                }
                operatorStack.push(token);
            }
        }

        // Clean the remaining elements in the stack
        while (!operatorStack.isEmpty()) {
            doOperation();
        }

        // Get the complete Tree
        return stackNode.pop();
    }

    private boolean Priority(String first, String second) {
        if (Objects.equals(first, second)) {
            return true;
        }
        if (Objects.equals(first, "*")) {
            return false;
        }
        if (Objects.equals(second, "*")) {
            return true;
        }
        if (Objects.equals(first, ".")) {
            return false;
        }
        if (Objects.equals(second, ".")) {
            return true;
        }
        return !Objects.equals(first, "|");
    }

    // Do the desired operation based on the top of stackNode
    private void doOperation() {
        if (!this.operatorStack.isEmpty()) {
            String stringAt = operatorStack.pop();

                switch (stringAt) {
                    case ("|"):
                        union();
                        break;

                    case ("."):
                        concatenation();
                        break;

                    case ("*"):
                        star();
                        break;

                    default:
                        System.out.println(">>" + stringAt);
                        System.out.println("Unknown Symbol !");
                        System.exit(1);
                        break;
                }
        }
    }

    // Do the star operation
    private void star() {
        // Retrieve top Node from Stack
        Node node = stackNode.pop();

        Node root = new Node("*");
        root.setLeft(node);
        root.setRight(null);
        node.setParent(root);

        // Put node back in the stackNode
        stackNode.push(root);
    }

    // Do the concatenation operation
    private void concatenation() {
        // retrieve node 1 and 2 from stackNode
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node root = new Node(".");
        root.setLeft(node1);
        root.setRight(node2);
        node1.setParent(root);
        node2.setParent(root);

        // Put node back to stackNode
        stackNode.push(root);
    }

    // Makes union of sub Node 1 with sub Node 2
    private void union() {
        // Load two Node in stack into variables
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node root = new Node("|");
        root.setLeft(node1);
        root.setRight(node2);
        node1.setParent(root);
        node2.setParent(root);

        // Put Node back to stack
        stackNode.push(root);
    }

    // Push input symbol into stackNode
    private void pushStack(String symbol) {
        Node node = new LeafNode(symbol, ++leafNodeID);
        node.setLeft(null);
        node.setRight(null);

        // Put Node back to stackNode
        stackNode.push(node);
    }

    // add "." when is concatenation between to symbols that: "." -> "&"
    // concatenates to each other
    private String AddConcat(String regular) {

        System.out.println("Entering AddConcat");

        StringBuilder newRegular = new StringBuilder();

        for (int i = 0; i < regular.length() - 1; i++) {
            /*
             *#  consider a , b are characters in the Σ
             *#  and the set: {'(', ')', '*', '+', '.', '|'} are the operators
             *#  then, if '.' is the concat symbol, we have to concatenate such expressions:
             *#  a . b
             *#  a . (
             *#  ) . a
             *#  * . a
             *#  * . (
             *#  ) . (
             */
//            if (isInputCharacter(regular.charAt(i)) && isInputCharacter(regular.charAt(i + 1))) {
//                newRegular.append(regular.charAt(i)).append(".");
//            }
//            else if (isInputCharacter(regular.charAt(i)) && regular.charAt(i + 1) == '(') {
//                newRegular.append(regular.charAt(i)).append(".");
//
//            }
            if ((regular.charAt(i) == ']' || regular.charAt(i) == ')') && regular.charAt(i+1) != '*'
                    && regular.charAt(i+1) != '+' && regular.charAt(i+1) != '|' && regular.charAt(i+1) != '.'
                    && regular.charAt(i+1) != ')' && regular.charAt(i+1) != ']' && regular.charAt(i+1) != '{') {
                newRegular.append(regular.charAt(i)).append(".");

            }
//            else if ((regular.charAt(i) == '*' && isInputCharacter(regular.charAt(i + 1))) || (regular.charAt(i) == '+' && isInputCharacter(regular.charAt(i + 1)))) {
//                newRegular.append(regular.charAt(i)).append(".");
//
//            }
            else if ((regular.charAt(i) == '*' || regular.charAt(i) == '+' || regular.charAt(i) == ']' || regular.charAt(i) == ')' || regular.charAt(i) == '}') && (regular.charAt(i + 1) == '(' || regular.charAt(i + 1) == '[')) {
                newRegular.append(regular.charAt(i)).append(".");

            }
//            else if ((regular.charAt(i) == ']' || regular.charAt(i) == ')' || regular.charAt(i) == '}') && (regular.charAt(i + 1) == '(' || regular.charAt(i+1) == '[')) {
//                newRegular.append(regular.charAt(i)).append(".");
//
//            }
            else {
                newRegular.append(regular.charAt(i));
            }
        }
        newRegular.append(regular.charAt(regular.length() - 1));
        return newRegular.toString();
    }

    private String plusToStart(String regular) {

        System.out.println("Entering plusToStart");

        String newRegular = regular;
        for(int i = 0; i<regular.length(); i++) {
            if(regular.charAt(i) == '+') {
                if(regular.charAt(i-1) == ')' || regular.charAt(i-1) == ']') {
                    for(int j = i-2; j>0; j--) {
                        String temp;
                        if(regular.charAt(j) == '(' || regular.charAt(j) == '[') {
                            temp = regular.substring(j,i);
                            newRegular = newRegular.replace(temp + '+', temp + '.' + temp + '*');
                        }
                    }
                }
                else {
                    for(int j = i-2; j>0; j--) {
                        String temp;
                        if(regular.charAt(j) == '.') {
                            temp = regular.substring(j,i);
                            if(isRelationshipType(temp)){
                                newRegular = newRegular.replace(temp + '+', temp + '.' + temp + '*');}
                            else
                                System.out.println("Incorrect Regular Expression ! Error at Plus to Start Conversion");
                        }
                    }
                }
            }
        }
        return newRegular;
    }

    private String complementReplacement(String regular) {

        System.out.println("Entering complementReplacement");

        String newRegular = regular;

        for(int i = 0; i<newRegular.length(); i++) {
                if (newRegular.charAt(i) == '^' && (newRegular.charAt(i - 1) == '(' || newRegular.charAt(i - 1) == '[')) {
                    for (int j = i + 1; j < regular.length(); j++) {
                        String temp;
                        List<String> tempList = new ArrayList<>(relationshipTypeList);
                        if (regular.charAt(j) == ')' || regular.charAt(j) == ']') {
                            temp = regular.substring(i + 1, j);
                            System.out.println("temp: " + temp);
                            String tempStr;
                            tempStr = temp;
                            tempList.remove(tempStr);
                            System.out.println("tempList: " + tempList);
                            temp = "(" + tempList.get(0) + ")";
                            for (int k = 1; k < tempList.size() - 1; k++) {
                                temp = temp + "|" + "(" + tempList.get(k) + ")";
                                System.out.println("Compliment temp1: " + temp);
                            }
                            temp = temp + "|" + "(" + tempList.get(tempList.size() - 1) + ")";
                            System.out.println("Compliment temp0: " + temp);
                            // Replace the character at position i+1 only:
                            newRegular = newRegular.substring(0, i) + temp + newRegular.substring(j);
                            break;
                        }
                    }
                }
        }

        newRegular = newRegular.replaceAll("\\^", "");

        return newRegular;
    }

    private String flowerBracketReplacement(String regular) {

        System.out.println("Entering flowerBracketReplacement");

        //ab*a(^b){2,}(a|b)(^a)d*
        String newRegular = regular;

        for(int i = 0; i<newRegular.length(); i++) {
            int a,b;
            String temp1 = "";
            String temp2 = "";
            String temp3 = "";
            if (newRegular.charAt(i) == '{') {
                if (newRegular.charAt(i-1) == ')' || newRegular.charAt(i-1) == ']') {
                    int j = i-1;
                    while(j>0){
                        if (newRegular.charAt(j) == '.') {
                            temp1 = newRegular.substring(j+1, i);
                            break;
                        }
                        j--;
                    }
                    System.out.println("temp1 = " + temp1);
                    if (newRegular.charAt(i + 3) == '}') {
                        a = Integer.parseInt(String.valueOf(newRegular.charAt(i+1)));
                        temp2 = newRegular.substring(i, i+4);
                        System.out.println("temp2 = " + temp2);
                        temp3 = temp1;
                        while(a>1) {
                            temp3 = temp3 + ".";
                            temp3 = temp3 + temp1;
                            System.out.println(a + ") temp3 = " + temp3);
                            a--;
                        }
                        temp3 = "." + temp3 + "*";
                        newRegular = newRegular.substring(0, i) + temp3 + newRegular.substring(i+4);
                    }
                    System.out.println("temp1 = " + temp1);

                    if (newRegular.charAt(i + 4) == '}') {
                        a = newRegular.charAt(i + 1);
                        b = newRegular.charAt(i + 3);

                        newRegular = newRegular.substring(0, i+1) + temp3 + newRegular.substring(i+2);
                    }
                }
            }
        }

        return newRegular;
    }

    // Return true if is part of the automata Language else is false
    private boolean isRelationshipType(String stringAt) {

        if (op.contains(stringAt)) {
            return false;
        }
        for (String str : input) {
            if (str.equals(stringAt) && !stringAt.equals("(") && !stringAt.equals(")") && !stringAt.equals("[") && !stringAt.equals("]") && !stringAt.equals("{") && !stringAt.equals("}") && !stringAt.equals("^") && !stringAt.equals(",")) {
                return true;
            }
        }
        return false;
    }
    
    /* This method is here just to test buildTree() */
    public void printInorder(Node node) {
        if (node == null) {
            return;
        }

        /* first recur on left child */
        printInorder(node.getLeft());

        /* then print the data of node */
        System.out.print(node.getSymbol() + " ");

        /* now recur on right child */
        printInorder(node.getRight());
    }
    
    public int getNumberOfLeafs(){
        return leafNodeID;
    }

    public String getRegular() {
        return regular;
    }
}