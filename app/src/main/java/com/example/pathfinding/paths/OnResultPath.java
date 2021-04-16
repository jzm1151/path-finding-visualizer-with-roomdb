package com.example.pathfinding.paths;

import java.util.List;

public interface OnResultPath {
    public void reportProgress(List<Node> update);
    public void pathFound(Node node);
}
