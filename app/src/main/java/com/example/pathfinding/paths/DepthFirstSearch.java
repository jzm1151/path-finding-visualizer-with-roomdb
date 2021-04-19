package com.example.pathfinding.paths;

import android.util.Pair;

import com.example.pathfinding.MainActivity;

import java.util.ArrayList;
import java.util.List;

// A class that allows the Depth First Search algorithm to be run on a background thread
public class DepthFirstSearch extends PathFinding {
    // Visited list and also will show which node are blocked
    private String[][] visitedList;

    // Used to tell how many nodes overall have been added to and removed from the frontier
    private int count;

    private Pair<Integer, Integer> start;
    private Pair<Integer, Integer> goal;
    private OnResultPath listener;

    public DepthFirstSearch(String[][] visitedList, int startFirst, int startSecond, int goalFirst, int goalSecond, OnResultPath listener) {
        start = new Pair<>(startFirst, startSecond);
        goal = new Pair<>(goalFirst, goalSecond);

        // Setting the callback object
        this.listener = listener;

        count = 0;

        // Making sure there is no reference to the parameter visitedList
        int visitedListLength = visitedList.length;

        this.visitedList = new String[visitedListLength][visitedListLength];
        for (int i = 0; i < visitedListLength; i++) {
            for (int j = 0; j < visitedListLength; j++) {
                this.visitedList[i][j] = visitedList[i][j];
            }
        }
    }

    @Override
    protected Node doInBackground(Integer... integers) {
        List<Node> frontier;
        List<Node> update;
        Node currNode;
        int currFirst;
        int currSecond;

        // Used to tell how far along in the process the path finding algorithm should be
        int speedUntilReach = integers.length == 0 ? 0 : integers[0];

        frontier = new ArrayList<>();
        frontier.add(new Node(start.first, start.second)); // Adding the start node to the frontier
        visitedList[start.first][start.second] = MainActivity.visitedOrEmptyListNodeKey;
        updateCount();

        while (!frontier.isEmpty()) {
            // Checking if the task has been cancelled
            if (isCancelled()) {
                return null;
            }

            // Getting from the top of the "stack"
           currNode = frontier.get(frontier.size()-1);

            // Removing the top of the "stack"
           frontier.remove(frontier.size()-1);

           // Updating count because a node was removed from the frontier
           updateCount();

           // Making sure update has a size of 0 and that nowhere else has a reference to the ArrayList
           // being modified
           update = new ArrayList<>();

            // The position of currNode in the graph
           currFirst = currNode.getFirst();
           currSecond = currNode.getSecond();

           // Goal node found
           if (currFirst == goal.first && currSecond == goal.second) {
               return currNode;
           }

            // If currNode is not the goal node then it is added to the front of the list so it can be set to a visited list node
            update.add(currNode);

            // Checking if above is a valid space
           if (currFirst-1 >= 0 && !visitedList[currFirst-1][currSecond].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                   !visitedList[currFirst-1][currSecond].equals(MainActivity.blockedNodeKey))
           {
               frontier.add(new Node(currFirst-1, currSecond, currNode));
               update.add(frontier.get(frontier.size()-1));
               visitedList[currFirst-1][currSecond] = MainActivity.visitedOrEmptyListNodeKey;
               updateCount();
           }

           // Checking if below is a valid space
           if (currFirst+1 < visitedList.length && !visitedList[currFirst+1][currSecond].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                   !visitedList[currFirst+1][currSecond].equals(MainActivity.blockedNodeKey))
           {
                frontier.add(new Node(currFirst+1, currSecond, currNode));
                update.add(frontier.get(frontier.size()-1));
                visitedList[currFirst+1][currSecond] = MainActivity.visitedOrEmptyListNodeKey;
               updateCount();
           }

           // Checking if to the left is a valid space
           if (currSecond-1 >= 0 && !visitedList[currFirst][currSecond-1].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                   !visitedList[currFirst][currSecond-1].equals(MainActivity.blockedNodeKey))
           {
               frontier.add(new Node(currFirst, currSecond-1, currNode));
               update.add(frontier.get(frontier.size()-1));
               visitedList[currFirst][currSecond-1] = MainActivity.visitedOrEmptyListNodeKey;
               updateCount();
           }

           // Checking if to the right is a valid space
           if (currSecond+1 < visitedList.length && !visitedList[currFirst][currSecond+1].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                   !visitedList[currFirst][currSecond+1].equals(MainActivity.blockedNodeKey))
           {
               frontier.add(new Node(currFirst, currSecond+1, currNode));
               update.add(frontier.get(frontier.size()-1));
               visitedList[currFirst][currSecond+1] = MainActivity.visitedOrEmptyListNodeKey;
               updateCount();
           }

           publishProgress(update);

           // Giving time for the animations to be seen
           // Will not sleep until desired progress is made
            if (getCount() > speedUntilReach) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // No path was found
        return null;
    }

    @Override
    protected void onProgressUpdate(List<Node>... values) {
        listener.reportProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Node node) {
        listener.pathFound(node);
    }

    public synchronized int getCount() {
        return count;
    }

    private synchronized void updateCount() {
        count += 1;
    }
}
