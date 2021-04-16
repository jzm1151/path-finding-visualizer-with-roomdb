package com.example.pathfinding.paths;

import android.os.AsyncTask;

import java.util.List;

public abstract class PathFinding extends AsyncTask<Integer, List<Node>, Node> {
    public abstract int getCount();
}
