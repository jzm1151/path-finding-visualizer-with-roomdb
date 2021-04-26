package com.example.pathfinding.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class GraphViewModel extends AndroidViewModel {
    private LiveData<List<Graph>> graphs;

    public GraphViewModel(@NonNull Application application) {
        super(application);
        graphs = DatabaseOfGraphs.getDatabase(getApplication()).graphDAO().getAll();
    }

    public LiveData<List<Graph>> getAll() {
        return graphs;
    }
}
