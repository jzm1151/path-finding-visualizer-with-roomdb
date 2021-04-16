package com.example.pathfinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pathfinding.paths.AStarSearch;
import com.example.pathfinding.paths.BreathFirstSearch;
import com.example.pathfinding.paths.DepthFirstSearch;
import com.example.pathfinding.paths.Node;
import com.example.pathfinding.paths.OnResultPath;
import com.example.pathfinding.paths.PathFinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnResultPath {
    // Width and height of the graph
    private final int n = 10;

    // Backgrounds for the TextViews in the graph
    private GradientDrawable blankNode;
    private GradientDrawable startNode;
    private GradientDrawable goalNode;
    private GradientDrawable blockedNode;
    private GradientDrawable frontierNode;
    private GradientDrawable visitedOrEmptyListNode;
    private GradientDrawable pathNode;

    private Map<String, GradientDrawable> backgroundMap;

    // Keys for backGroundMap
    public static final String blankNodeKey = "BLANK_NODE";
    public static final String startNodeKey = "START_NODE";
    public static final String goalNodeKey = "GOAL_NODE";
    public static final String blockedNodeKey = "BLOCKED_NODE";
    public static final String frontierNodeKey = "FRONTIER_NODE";
    public static final String visitedOrEmptyListNodeKey = "VISITED_NODE";
    public static final String pathNodeKey = "PATH_NODE";

    private String[][] persistGraph;
    private TextView[][] graph;

    // To keep track of where the start and goal nodes are
    private Pair<Integer, Integer> startNodePosition;
    private Pair<Integer, Integer> goalNodePosition;

    // Used to persist startNodePosition and goalNodePosition
    private static final String startNodePositionKey = "START_NODE_POSITION_KEY";
    private static final String goalNodePositionKey = "GOAL_NODE_POSITION_KEY";

    private final String persistGraphKey = "PERSIST_GRAPH";

    // The background of the View that is being dragged
    private Drawable dragging;

    // RadioButtons that will be disabled when a path finding algorithm is running
    private RadioButton aStar;
    private RadioButton breathFirst;
    private RadioButton depthFirst;

    //Determines if a path finding algorithm is running, the value will be true if one is running
    private boolean pathRunning;

    // Used to persist pathRunning
    private static final String pathRunningKey = "PATH_RUNNING_KEY";

    // Used to persist how far along in the process a path finding algorithm is
    private int progress;

    // Used to persist progress
    private static final String progressKey = "PROGRESS_KEY";

    private PathFinding pathThread;

    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout table = findViewById(R.id.tableLayoutGraph);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializing RadioButton instance variables
        aStar = findViewById(R.id.radioButtonAStar);
        breathFirst = findViewById(R.id.radioButtonBreathFirstSearch);
        depthFirst = findViewById(R.id.radioButtonDepthFirstSearch);

        // Initializing the backgrounds
        initializeBackgrounds();

        // Initializing backgroundMap
        backgroundMap = new HashMap<>();
        backgroundMap.put(blankNodeKey, blankNode);
        backgroundMap.put(startNodeKey, startNode);
        backgroundMap.put(goalNodeKey, goalNode);
        backgroundMap.put(blockedNodeKey, blockedNode);
        backgroundMap.put(frontierNodeKey, frontierNode);
        backgroundMap.put(visitedOrEmptyListNodeKey, visitedOrEmptyListNode);
        backgroundMap.put(pathNodeKey, pathNode);

        // Initializing progress and pathRunning and restoring the state of the RadioButtons
        if (savedInstanceState != null) {
            progress = savedInstanceState.getInt(progressKey);
            pathRunning = savedInstanceState.getBoolean(pathRunningKey);

            // If there is a path finding algorithm running a the RadioButtons are disabled again
            if (pathRunning)
                disableRadioButtons();
        }
        else {
            progress = 0;
            pathRunning = false;
        }

        // Initializing persistGraph
        if (savedInstanceState != null) {
            pathRunning = savedInstanceState.getBoolean(pathRunningKey);
            persistGraph = (String[][]) savedInstanceState.getSerializable(persistGraphKey);
        }
        else {
            persistGraph = new String[n][n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    persistGraph[i][j] = blankNodeKey;
                }
            }
        }

        // Initializing graph
        graph = new TextView[n][n];
        for (int i = 0; i < n; i ++) {
            TableRow row = new TableRow(this);

            for (int j = 0; j < n; j++) {
                TextView v = new TextView(this);
                v.setTag(new Pair<>(i, j));
                v.setGravity(Gravity.CENTER);
                v.setBackground(backgroundMap.get(persistGraph[i][j]));
                v.setOnLongClickListener(new NodeOnLongClickListener());
                v.setOnDragListener(new NodeOnDragListener());
                graph[i][j] = v;
                row.addView(v);
            }

            table.addView(row);
        }

        if (savedInstanceState == null) {
            startNodePosition = new Pair<>(0, 0);
            goalNodePosition = new Pair<>(n-1, n-1);
        }
        else {
            int[] tempPositionArr = savedInstanceState.getIntArray(startNodePositionKey);
            startNodePosition = new Pair<>(tempPositionArr[0], tempPositionArr[1]);

            tempPositionArr = savedInstanceState.getIntArray(goalNodePositionKey);
            goalNodePosition = new Pair<>(tempPositionArr[0], tempPositionArr[1]);
        }

        // Placing the start node and the goal node
        TextView start = graph[startNodePosition.first][startNodePosition.second];
        TextView goal = graph[goalNodePosition.first][goalNodePosition.second];
        start.setBackground(startNode);
        goal.setBackground(goalNode);

        // Allowing the brush to be drug
        ImageView brush = findViewById(R.id.imageViewBarrierDrag);
        brush.setOnLongClickListener(v -> {

            // Nothing should be moved when a path finding algorithm is running
            if (pathRunning)
                return false;

            // Setting everything except start, goal and blocked nodes to blank nodes
            cleanGraph();

            v.startDragAndDrop(null,
                    new View.DragShadowBuilder(v),
                    null,
                    0
            );

            dragging = blockedNode;

            return true;
        });

        // Allowing the eraser to be drug
        ImageView eraser = findViewById(R.id.imageViewEraseDrag);
        eraser.setOnLongClickListener(v -> {

            // Nothing should be moved when a path finding algorithm is running
            if (pathRunning)
                return false;

            // Setting everything except start, goal and blocked node to blank nodes
            cleanGraph();

            v.startDragAndDrop(null,
                    new View.DragShadowBuilder(v),
                    null,
                    0);

            dragging = blankNode;

            return true;
        });
    }

    // Disabling the RadioButtons so they cannot be clicked while a path finding algorithm is running
    private void disableRadioButtons() {
        aStar.setEnabled(false);
        breathFirst.setEnabled(false);
        depthFirst.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.path_finding_activity, menu);

        // If a path finding algorithm is running setting the run icon to the stop icon
        if (pathRunning)
            menu.findItem(R.id.menu_run).setIcon(R.drawable.ic_baseline_stop_24);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_run && !pathRunning) {
            disableRadioButtons();

            pathRunning = true;

            // changing the icon to the stop button
            item.setIcon(R.drawable.ic_baseline_stop_24);

            cleanGraph();

            // Choose the algorithm based on the Radio Button that is checked
            if (aStar.isChecked())
                aStar();
            else if (breathFirst.isChecked())
                breathFirst();
            else
                depthFirst();

            return true;
        }
        else if (id == R.id.menu_run) { // stopping the algorithm
            pathThread.cancel(false);
            transitionNonRunningState();
        }

        return super.onOptionsItemSelected(item);
    }

    private void aStar() {
        // Making all unnecessary nodes blank nodes
        cleanGraph();

        // Making sure persistGraph is up to date
        updatePersistGraph();

        pathThread = new AStarSearch(persistGraph,
                startNodePosition.first,
                startNodePosition.second,
                goalNodePosition.first,
                goalNodePosition.second,
                this
        );

        // progress with let the path finding algorithm know how far along it should be
        pathThread.execute(progress);
    }

    private void breathFirst() {
        // Making all unnecessary nodes blank nodes
        cleanGraph();

        // Making sure persistGraph is up to date
        updatePersistGraph();

        pathThread = new BreathFirstSearch(persistGraph,
                startNodePosition.first,
                startNodePosition.second,
                goalNodePosition.first,
                goalNodePosition.second,
                this
        );

        // progress with let the path finding algorithm know how far along it should be
        pathThread.execute(progress);
    }

    private void depthFirst() {
        // Making all unnecessary nodes blank nodes
        cleanGraph();

        // Making sure persistGraph is up to date
        updatePersistGraph();

        pathThread = new DepthFirstSearch(persistGraph,
                startNodePosition.first,
                startNodePosition.second,
                goalNodePosition.first,
                goalNodePosition.second,
                this
        );

        // progress with let the path finding algorithm know how far along it should be
        pathThread.execute(progress);
    }

    // This method sets all instance variable back to the state they should be in when there is
    // no path finding algorithm running
    private void transitionNonRunningState() {
        // No longer trying to find a path
        pathRunning = false;
        progress = 0;
        aStar.setEnabled(true);
        breathFirst.setEnabled(true);
        depthFirst.setEnabled(true);

        // onCreateOptionsMenu() is not always already called when transitionNonRunningState()
        // is called
        if (optionsMenu != null)
            optionsMenu.findItem(R.id.menu_run).setIcon(R.drawable.ic_menu_run);
    }

    // This method removes everything from the graph except the start node, goal node and blocked nodes
    private void cleanGraph() {
        Drawable background = null;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                background = graph[i][j].getBackground();

                if (background != startNode && background != goalNode && background != blockedNode) {
                    graph[i][j].setBackground(blankNode);
                }
            }
        }
    }

    // This method makes the state of the persistGraph match the state of the graph
    private void updatePersistGraph() {
        Drawable background;

        for (int i = 0; i < n; i ++) {
            for (int j = 0; j < n; j++) {
                background = graph[i][j].getBackground();

                if (background == blankNode)
                    persistGraph[i][j] = blankNodeKey;
                else if (background == startNode)
                    persistGraph[i][j] = startNodeKey;
                else if (background == goalNode)
                    persistGraph[i][j] = goalNodeKey;
                else if (background == blockedNode)
                    persistGraph[i][j] = blockedNodeKey;
                else if (background == frontierNode)
                    persistGraph[i][j] = frontierNodeKey;
                else if (background == visitedOrEmptyListNode)
                    persistGraph[i][j] = visitedOrEmptyListNodeKey;
                else
                    persistGraph[i][j] = pathNodeKey;
            }
        }
    }

    // This method removes everything from the graph except the start node and the goal node
    // This method is called by the reset button
    public void resetGraph(View view) {
        // Do nothing if a pathfinding algorithm is running
        if (pathRunning)
            return;

        // Resetting all the nodes in the graph to blank nodes
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                graph[i][j].setBackground(blankNode);
            }
        }

        // Resetting the start and goal node
        graph[startNodePosition.first][startNodePosition.second].setBackground(startNode);
        graph[goalNodePosition.first][goalNodePosition.second].setBackground(goalNode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(pathRunningKey, pathRunning);
        outState.putInt(progressKey, progress);

        //Persisting the start and goal node positions
        outState.putIntArray(startNodePositionKey, new int[]{startNodePosition.first, startNodePosition.second});
        outState.putIntArray(goalNodePositionKey, new int[]{goalNodePosition.first, goalNodePosition.second});

        // Making the state of persistGraph matches the state of graph
        updatePersistGraph();
        outState.putSerializable(persistGraphKey, persistGraph);

        super.onSaveInstanceState(outState);
    }

    public void forTesting(View view) {
        // nothing to test now
    }


    // Updates the graph to the point where the path finding algorithm is. The first node is the List
    // update is always going to be turned red because by design it is the node that was just removed
    // the frontier
    @Override
    public void reportProgress(List<Node> update) {
        Node curr = update.get(0);
        TextView temp = graph[curr.getFirst()][curr.getSecond()];

        // Getting the TextView that is the start node
        TextView start = graph[startNodePosition.first][startNodePosition.second];

        // Getting the TextView that is the goal node
        TextView goal = graph[goalNodePosition.first][goalNodePosition.second];

        // The first node in the list is always the one that is no longer in the frontier
        // If the first one is not a start node or goal node then the background is changed to
        // visitedOrEmptyListNode
        if (temp != start && temp != goal)
            temp.setBackground(visitedOrEmptyListNode);

        for (int i = 1; i < update.size(); i++) {
            curr = update.get(i);
            temp = graph[curr.getFirst()][curr.getSecond()];
            if (temp != start && temp != goal)
                temp.setBackground(frontierNode);
        }
    }


    // This method is called when a path has been found or if no path has been found and the process
    // is finished.
    @Override
    public void pathFound(Node node) {
        if (node == null) {
            Toast.makeText(this, "Path not found", Toast.LENGTH_LONG).show();
        }
        else {
            Node temp = node;
            TextView tempTextView;
            TextView start = graph[startNodePosition.first][startNodePosition.second];
            TextView goal = graph[goalNodePosition.first][goalNodePosition.second];

            while (temp != null) {
                tempTextView = graph[temp.getFirst()][temp.getSecond()];

                if (tempTextView  != start && tempTextView  != goal)
                    graph[temp.getFirst()][temp.getSecond()].setBackground(pathNode);
                temp = temp.getNext();
            }
        }

        // Because the path finding process is over
        transitionNonRunningState();
    }

    // The listener that allows the node in the graph to be drug if they are the start or goal node
    // it does not allow the nodes to be drug if a path finding algorithm is running
    private class NodeOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            // The only nodes that should be able to be dragged are the start and goal nodes
            if (v.getBackground() != startNode && v.getBackground() != goalNode)
                return false;

            // If there is currently a path finding algorithm running nothing should be move
            if (pathRunning)
                return false;

            // Setting everything to blank node except for start, goal and blocked nodes
            cleanGraph();

            // When the View is dropped this will be used to determine the background
            // of the view in the graph it was dropped on
            dragging = v.getBackground();

            v.startDragAndDrop(null,  // the data to be dragged (no data)
                    new View.DragShadowBuilder(v),  // the drag shadow builder
                    null,      // no need to use local data
                    0          // flags (not currently used, set to 0)
            );

            return true;
        }
    }

    // The listener for the nodes in the graph that allows blocked nodes to be erased by the eraser
    // and set by the brush. Also, this listener allows the start and goal nodes to be set into a new position
    private class NodeOnDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            Drawable backgroundNode = v.getBackground();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (backgroundNode == startNode || backgroundNode == goalNode)
                        return false;
                    else if (backgroundNode == blockedNode && dragging != blankNode)
                        return false;
                    else
                        return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (dragging == blankNode)
                        v.setBackground(dragging);
                    else if (dragging == blockedNode)
                        v.setBackground(dragging);

                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    // Nothing needs to be done for ACTION_DRAG_LOCATION
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // Nothing for now
                    return true;

                case DragEvent.ACTION_DROP:
                    if (dragging == startNode) {
                        v.setBackground(dragging);
                        graph[startNodePosition.first][startNodePosition.second].setBackground(blankNode);
                        startNodePosition = (Pair<Integer, Integer>) v.getTag();
                    }
                    else if (dragging == goalNode) {
                        v.setBackground(dragging);
                        graph[goalNodePosition.first][goalNodePosition.second].setBackground(blankNode);
                        goalNodePosition = (Pair<Integer, Integer>) v.getTag();
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    // Nothing needs to be done for ACTION_DRAG_ENDED
                    return true;
                default:
                    Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }

    private void initializeBackgrounds() {
        // Initializing blankNode
        blankNode = new GradientDrawable();
        blankNode.setShape(GradientDrawable.RECTANGLE);
        blankNode.setColor(Color.WHITE);
        blankNode.setStroke(2, Color.BLACK);

        // Initializing startNode
        startNode = new GradientDrawable();
        startNode.setShape(GradientDrawable.RECTANGLE);
        startNode.setColor(Color.BLACK);
        startNode.setStroke(2, Color.BLACK);

        // Initializing goalNode
        goalNode = new GradientDrawable();
        goalNode.setShape(GradientDrawable.RECTANGLE);
        goalNode.setColor(Color.BLACK);
        goalNode.setStroke(2, Color.BLACK);

        // Initializing blockedNode
        blockedNode = new GradientDrawable();
        blockedNode.setShape(GradientDrawable.RECTANGLE);
        blockedNode.setColor(Color.GRAY);
        blockedNode.setStroke(2, Color.BLACK);

        // Initializing frontierNode
        frontierNode = new GradientDrawable();
        frontierNode.setShape(GradientDrawable.RECTANGLE);
        frontierNode.setColor(Color.YELLOW);
        frontierNode.setStroke(2, Color.BLACK);

        // Initializing visitedOrEmptyListNode
        visitedOrEmptyListNode = new GradientDrawable();
        visitedOrEmptyListNode.setShape(GradientDrawable.RECTANGLE);
        visitedOrEmptyListNode.setColor(Color.RED);
        visitedOrEmptyListNode.setStroke(2, Color.BLACK);

        // Initializing pathNode
        pathNode = new GradientDrawable();
        pathNode.setShape(GradientDrawable.RECTANGLE);
        pathNode.setColor(Color.CYAN);
        pathNode.setStroke(2, Color.BLACK);
    }

    @Override
    protected void onPause() {
        // Making sure that if there is a path finding algorithm running it stops
        if (pathRunning) {
            progress = pathThread.getCount();
            pathThread.cancel(false);
            Log.d("onPause", "progress:"+progress);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        // If a path finding algorithm was running before onPause() was called it is restarted here
        if (pathRunning) {
            if (aStar.isChecked())
                aStar();
            else if (breathFirst.isChecked())
                breathFirst();
            else
                depthFirst();
        }

        super.onResume();
    }
}