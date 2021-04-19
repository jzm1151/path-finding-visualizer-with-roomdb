package com.example.pathfinding.paths;

import android.util.Pair;

import com.example.pathfinding.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

// A class that allows the A* algorithm to be run on a background thread
public class AStarSearch extends PathFinding {
    // Expanded list and also will show which node are blocked
    private String[][] visitedList;

    // Used to determine how far along the process is
    private int count;

    private Pair<Integer, Integer> start;
    private Pair<Integer, Integer> goal;
    private OnResultPath listener;

    public AStarSearch(String[][] visitedList, int startFirst, int startSecond, int goalFirst, int goalSecond, OnResultPath listener) {
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
        Queue<Node> frontier = null;
        List<Node> update;
        Node currNode;
        Node temp;
        int currFirst;
        int currSecond;

        // Used to tell how far along in the process the path finding algorithm should be
        int speedUntilReach = integers.length == 0 ? 0 : integers[0];

        frontier = new PriorityQueue<>();
        frontier.add(new Node(start.first, start.second, null, 0, goal.first, goal.second));
        updateCount();

        while (!frontier.isEmpty()) {
            // Checking if the task has been cancelled
            if (isCancelled())
                return null;

            // Getting and removing the first node in the queue
            currNode = frontier.poll();

            updateCount();

            // The position of currNode in the graph
            currFirst = currNode.getFirst();
            currSecond = currNode.getSecond();

            // Making sure not to try and expand a node twice
            if (!visitedList[currFirst][currSecond].equals(MainActivity.visitedOrEmptyListNodeKey)) {
                // currNode to the expanded list
                visitedList[currFirst][currSecond] = MainActivity.visitedOrEmptyListNodeKey;

                // Making sure update has a size of 0 and that nowhere else has a reference to the ArrayList
                // being modified
                update = new ArrayList<>();

                // Goal node found
                if (currFirst == goal.first && currSecond == goal.second) {
                    return currNode;
                }

                // If currNode is not the goal node then it is added to the front of the list so it can be set to a visited list node
                update.add(currNode);

                // Checking if above is a valid space
                if (currFirst - 1 >= 0 && !visitedList[currFirst - 1][currSecond].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                        !visitedList[currFirst - 1][currSecond].equals(MainActivity.blockedNodeKey)) {
                    temp = new Node(currFirst - 1, currSecond, currNode, currNode.getPathCost() + 1, goal.first, goal.second);
                    frontier.offer(temp);
                    update.add(temp);
                    updateCount();
                }

                // Checking if below is a valid space
                if (currFirst + 1 < visitedList.length && !visitedList[currFirst + 1][currSecond].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                        !visitedList[currFirst + 1][currSecond].equals(MainActivity.blockedNodeKey)) {
                    temp = new Node(currFirst + 1, currSecond, currNode, currNode.getPathCost() + 1, goal.first, goal.second);
                    frontier.offer(temp);
                    update.add(temp);
                    updateCount();
                }

                // Checking if to the left is a valid space
                if (currSecond - 1 >= 0 && !visitedList[currFirst][currSecond - 1].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                        !visitedList[currFirst][currSecond - 1].equals(MainActivity.blockedNodeKey)) {
                    temp = new Node(currFirst, currSecond - 1, currNode, currNode.getPathCost() + 1, goal.first, goal.second);
                    frontier.offer(temp);
                    update.add(temp);
                    updateCount();
                }

                // Checking if to the right is a valid space
                if (currSecond + 1 < visitedList.length && !visitedList[currFirst][currSecond + 1].equals(MainActivity.visitedOrEmptyListNodeKey) &&
                        !visitedList[currFirst][currSecond + 1].equals(MainActivity.blockedNodeKey)) {
                    temp = new Node(currFirst, currSecond + 1, currNode, currNode.getPathCost() + 1, goal.first, goal.second);
                    frontier.offer(temp);
                    update.add(temp);
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

    @Override
    public synchronized int getCount() {
        return count;
    }

    private synchronized void updateCount() {
        count += 1;
    }
}


