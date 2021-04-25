package com.example.pathfinding.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GraphDAO {
    @Query("SELECT * FROM graphs ORDER BY graph_name COLLATE NOCASE, row_id")
    LiveData<List<Graph>> getAll();

    @Query("SELECT * FROM graphs WHERE row_id = :graphId")
    Graph getById(int graphId);

    @Query("SELECT * FROM graphs WHERE graph_name = :graphName")
    Graph getByName(String graphName);

    @Insert
    void insert(Graph... graphs);

    @Update
    void update(Graph... graphs);

    @Delete
    void delete(Graph... graphs);

    @Query("DELETE FROM graphs WHERE row_id = :graphId")
    void delete(int graphId);
}
