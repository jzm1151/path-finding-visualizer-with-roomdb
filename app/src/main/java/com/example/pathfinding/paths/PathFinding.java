package com.example.pathfinding.paths;

import android.os.AsyncTask;

import java.util.List;

// Classes designed for path finding algorithms should extend this class.
public abstract class PathFinding extends AsyncTask<Integer, List<Node>, Node> {
    // Should return how far along in the process a path find algorithm is
    public abstract int getCount();
}
