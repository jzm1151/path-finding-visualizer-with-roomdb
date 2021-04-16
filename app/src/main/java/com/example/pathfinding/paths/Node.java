package com.example.pathfinding.paths;

import java.io.Serializable;

public class Node implements Serializable, Comparable<Node> {
    private static final long serialVersionUID = 1234L;
    private int first;
    private int second;
    private Node next;
    private int pathCost;
    private int goalFirst;
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

    public int getEstimateToGoal() {
        return Math.abs(goalFirst - first) + Math.abs(goalSecond - second);
    }

    public int pathAndEstimate() {
        return getEstimateToGoal() + getPathCost();
    }

    @Override
    public int compareTo(Node o) {
        return pathAndEstimate() - o.pathAndEstimate();
    }
}
