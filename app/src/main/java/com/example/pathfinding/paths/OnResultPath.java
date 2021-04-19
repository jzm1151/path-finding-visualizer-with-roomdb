package com.example.pathfinding.paths;

import java.util.List;

// Callback objects used in classes that run path finding algorithms should implement this interface
public interface OnResultPath {
   // Reports which nodes have been added to the frontier or removed from the frontier
    void reportProgress(List<Node> update);

    // Called when a path finding algorithm is finished running, should return null if no path is found
    void pathFound(Node node);
}
