package com.example.pathfinding.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.pathfinding.MainActivity;

@Database(entities = {Graph.class}, version = 1, exportSchema = false)
public abstract class DatabaseOfGraphs extends RoomDatabase {
    public interface GraphListener {
        void onGraphReturned(Graph graph);
    }

    public abstract GraphDAO graphDAO();

    private static DatabaseOfGraphs INSTANCE;

    public static DatabaseOfGraphs getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseOfGraphs.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DatabaseOfGraphs.class, "database_of_graphs")
                            .addCallback(createDatabaseOfGraphsCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    private static RoomDatabase.Callback createDatabaseOfGraphsCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            createGraphTable();
        }
    };

    private static void createGraphTable() {
        // Something to be checked for correctness
        String firstGraph = "1";
        for (int i = 0; i < MainActivity.n * MainActivity.n - 2; i ++) {
            firstGraph += "0";
        }
        firstGraph += "2";

        insert(new Graph(0, "initial", firstGraph));
    }

    public static String turnGraphToString(String[][] graph) {
        String stringGraph = "";

        for (int i = 0; i < MainActivity.n; i++) {
            for (int j = 0; j < MainActivity.n; j++) {
                if (graph[i][j].equals(MainActivity.startNodeKey)) stringGraph += "1";
                else if (graph[i][j].equals(MainActivity.goalNodeKey)) stringGraph += "2";
                else if (graph[i][j].equals(MainActivity.blockedNodeKey)) stringGraph += "3";
                else stringGraph += "0";
            }
        }

        return stringGraph;
    }

    public static void getGraph(int id, GraphListener listener) {
        new AsyncTask<Integer, Void, Graph>() {
            protected Graph doInBackground(Integer... ids) {
                return INSTANCE.graphDAO().getById(ids[0]);
            }

            protected void onPostExecute(Graph graph) {
                super.onPostExecute(graph);
                listener.onGraphReturned(graph);
            }
        }.execute(id);
    }

    // testing
    public static void getGraphByName(String graphName, GraphListener listener) {
        new AsyncTask<String, Void, Graph>() {
            protected Graph doInBackground(String... names) {
                return INSTANCE.graphDAO().getByName(names[0]);
            }

            protected void onPostExecute(Graph graph) {
                super.onPostExecute(graph);
                listener.onGraphReturned(graph);
            }
        }.execute(graphName);
    }

    public static void insert(Graph graph) {
        new AsyncTask<Graph, Void, Void> () {
            protected Void doInBackground(Graph... graphs) {
                INSTANCE.graphDAO().insert(graphs);
                return null;
            }
        }.execute(graph);
    }

    public static void delete(int graphId) {
        new AsyncTask<Integer, Void, Void> () {
            protected Void doInBackground(Integer... ids) {
                INSTANCE.graphDAO().delete(ids[0]);
                return null;
            }
        }.execute(graphId);
    }

    public static void update(Graph graph) {
        new AsyncTask<Graph, Void, Void> () {
            protected Void doInBackground(Graph... graphs) {
                INSTANCE.graphDAO().update(graphs);
                return null;
            }
        }.execute(graph);
    }
}
