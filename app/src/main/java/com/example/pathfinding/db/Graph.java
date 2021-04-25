package com.example.pathfinding.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "graphs")
public class Graph {
    public Graph(int id, String graphName, String graph) {
        this.id = id;
        this.graphName = graphName;
        this.graph = graph;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "row_id")
    public int id;

    @ColumnInfo(name = "graph_name")
    public String graphName;

    @ColumnInfo(name = "graph")
    public String graph;
}
