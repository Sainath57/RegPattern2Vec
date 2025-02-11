/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.regpattern2vec;

import java.util.*;

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
    private Stack<Node> stackNode = new Stack<>();
    private Stack<Character> operator = new Stack<Character>();

    // Set of inputs
    private Set<Character> input = new HashSet<Character>();
    private ArrayList<Character> op = new ArrayList<>();

    private ArrayList<Character> nodeOrRelationTypes = new ArrayList<>();

    // Generates tree using the regular expression and returns it's root
    public Node generateTree(String regular1) {

        Collections.addAll(nodeOrRelationTypes,'a','b','c','d');

        Character[] ops = {'*', '|', '.'};
        op.addAll(Arrays.asList(ops));

        // Only inputs available
        Character[] ch = new Character[26 + 26];
        for (int i = 65; i <= 90; i++) {
            ch[i - 65] = (char) i;
            ch[i - 65 + 26] = (char) (i + 32);
        }
        //Character[] integer = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        Character[] others = {'#', ' ', '(', ')','[', ']','{', '}', '^', ','};
        input.addAll(Arrays.asList(ch));
        //input.addAll(Arrays.asList(integer));
        input.addAll(Arrays.asList(others));

        // Generate regular expression with the concatenation
        String regular2 = AddConcat(regular1);
        //System.out.println("After concatenation: " + regular2);

        String regular3 = plusToStart(regular2);
        //System.out.println("After plusToStar: " + regular3);

        String regular4 = complementReplacement(regular3);
        //System.out.println("After complementReplacement: " + regular4);

        regular = flowerBracketReplacement(regular4);
        System.out.println("After flowerBracketReplacement: " + regular);
        
        // Cleaning stacks
        stackNode.clear();
        operator.clear();


        for (int i = 0; i < regular.length(); i++) {

            if (isInputCharacter(regular.charAt(i))) {
                pushStack(Character.toString(regular.charAt(i)));
            } else if (operator.isEmpty()) {
                operator.push(regular.charAt(i));

            } else if (regular.charAt(i) == '(' || regular.charAt(i) == '[') {
                operator.push(regular.charAt(i));

            } else if (regular.charAt(i) == ')' || regular.charAt(i) == ']') {
                while (operator.getLast() != '(' && operator.getLast() != '[') {
                    doOperation();
                }

                // Pop the '(' left parenthesis
                operator.pop();

            } else {
                while (!operator.isEmpty()
                        && Priority(regular.charAt(i), operator.getLast())) {
                    doOperation();
                }
                operator.push(regular.charAt(i));
            }
        }

        // Clean the remaining elements in the stack
        while (!operator.isEmpty()) {
            doOperation();
        }

        // Get the complete Tree
        return stackNode.pop();
    }

    private boolean Priority(char first, Character second) {
        if (first == second) {
            return true;
        }
        if (first == '*') {
            return false;
        }
        if (second == '*') {
            return true;
        }
        if (first == '.') {
            return false;
        }
        if (second == '.') {
            return true;
        }
        return first != '|';
    }

    // Do the desired operation based on the top of stackNode
    private void doOperation() {
        if (!this.operator.isEmpty()) {
            char charAt = operator.pop();

                switch (charAt) {
                    case ('|'):
                        union();
                        break;

                    case ('.'):
                        concatenation();
                        break;

                    case ('*'):
                        star();
                        break;

                    default:
                        System.out.println(">>" + charAt);
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
            if (isInputCharacter(regular.charAt(i)) && isInputCharacter(regular.charAt(i + 1))) {
                newRegular.append(regular.charAt(i)).append(".");
            }
            else if (isInputCharacter(regular.charAt(i)) && regular.charAt(i + 1) == '(') {
                newRegular.append(regular.charAt(i)).append(".");

            }
            else if ((regular.charAt(i) == ']' || regular.charAt(i) == ')') && isInputCharacter(regular.charAt(i + 1))) {
                newRegular.append(regular.charAt(i)).append(".");

            }
            else if ((regular.charAt(i) == '*' && isInputCharacter(regular.charAt(i + 1))) || (regular.charAt(i) == '+' && isInputCharacter(regular.charAt(i + 1)))) {
                newRegular.append(regular.charAt(i)).append(".");

            }
            else if ((regular.charAt(i) == '*' && (regular.charAt(i + 1) == '(' || regular.charAt(i + 1) == '[')) || (regular.charAt(i) == '+' && (regular.charAt(i + 1) == '(' || regular.charAt(i + 1) == '['))) {
                newRegular.append(regular.charAt(i)).append(".");

            }
            else if ((regular.charAt(i) == ')' && regular.charAt(i + 1) == '(') || (regular.charAt(i) == ']' && regular.charAt(i + 1) == '[') || (regular.charAt(i) == '}' && (regular.charAt(i + 1) == '[' || regular.charAt(i + 1) == '('))) {
                newRegular.append(regular.charAt(i)).append(".");

            }
            else {
                newRegular.append(regular.charAt(i));
            }
        }
        newRegular.append(regular.charAt(regular.length() - 1));
        return newRegular.toString();
    }

    private String plusToStart(String regular) {
        String newRegular = regular;
        String temp;
        for(int i = 0; i<regular.length(); i++) {
            if(regular.charAt(i) == '+') {
                if(regular.charAt(i-1) == ')' || regular.charAt(i-1) == ']') {
                    for(int j = i-2; i>0; i--) {
                        if(regular.charAt(j) == '(' || regular.charAt(j) == '[') {
                            temp = regular.substring(j,i);
                            newRegular = regular.replace(temp + '+', temp + '.' + temp + '*');
                        }
                    }
                }
                if(isInputCharacter(regular.charAt(i-1))) {
                    temp = String.valueOf(regular.charAt(i-1));
                    newRegular = regular.replace(temp + '+', temp + '.' + temp + '*');
                }
            }
        }
        return newRegular;
    }

    private String complementReplacement(String regular) {
        String newRegular = regular;

        for(int i = 0; i<newRegular.length(); i++) {
            if(newRegular.charAt(i) == '^') {
                if((newRegular.charAt(i-1) == '(' || newRegular.charAt(i-1) == '[') && (newRegular.charAt(i+2) == ')' || newRegular.charAt(i+2) == ']')) {
                    String temp;
                    char tempStr;
                    ArrayList<Character> tempList = new ArrayList<>(nodeOrRelationTypes);
                    tempStr = newRegular.charAt(i+1);
                    tempList.remove(Character.valueOf(tempStr));
                    temp = String.valueOf(tempList.getFirst());
                    for(int j = 1; j<tempList.size()-1; j++) {
                        temp = temp + "|" + tempList.get(j);
                    }
                    temp = temp + "|" + tempList.getLast();
                    // Replace the character at position i+1 only:
                    newRegular = newRegular.substring(0, i+1) + temp + newRegular.substring(i+2);
                }
            }
        }

        newRegular = newRegular.replaceAll("\\^", "");

        return newRegular;
    }

    private String flowerBracketReplacement(String regular) {
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
                        if (newRegular.charAt(j) == '(' || newRegular.charAt(j) == '[') {
                            temp1 = newRegular.substring(j, i);
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
    private boolean isInputCharacter(char charAt) {

        if (op.contains(charAt)) {
            return false;
        }
        for (Character c : input) {
            if (c == charAt && charAt != '(' && charAt != ')' && charAt != '[' && charAt != ']' && charAt != '{' && charAt != '}' && charAt != '^' && charAt != ',') {
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