package com.example.pathfinding.paths;

import java.io.Serializable;

// Node was only designed with breath first search, depth first search and A*, with the condition that
// the graph used in the path finding algorithms must be a 2-d array
public class Node implements Serializable, Comparable<Node> {
    private static final long serialVersionUID = 1234L;

    // row the node is in
    private int first;

    // column the node is in
    private int second;

    // holds the node that was expanded to get to this node
    private Node next;

    // total path cost to get to this node
    private int pathCost;

    // row the goal node is in
    private int goalFirst;

    // column the goal node is in
    private int goalSecond;

    public Node(int first, int second) {
        this.first = first;
        this.second = second;
        next = null;
        pathCost = 0;
        goalFirst = 0;
        goalSecond = 0;
    }

    public Node(int first, int second, Node next) {
        this.first = first;
        this.second = second;
        this.next = next;
        pathCost = 0;
        goalFirst = 0;
        goalSecond = 0;
    }

    public Node(int first, int second, Node next, int pathCost, int goalFirst, int goalSecond) {
        this.first = first;
        this.second = second;
        this.next = next;
        this.pathCost = pathCost;
        this.goalFirst = goalFirst;
        this.goalSecond = goalSecond;
    }

    public Node getNext() {return next;}

    public int getFirst() {return first;}

    public int getSecond() {return second;}

    public int getPathCost() {
        return pathCost;
    }

    // Gets the estimated cost to the goal node
    public int getEstimateToGoal() {
        return Math.abs(goalFirst - first) + Math.abs(goalSecond - second);
    }

    // Gets estimated cost to the goal node plus the path cost
    public int pathAndEstimate() {
        return getEstimateToGoal() + getPathCost();
    }

    // Only designed for A*
    @Override
    public int compareTo(Node o) {
        return pathAndEstimate() - o.pathAndEstimate();
    }
}
