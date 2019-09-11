import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then the method start() far below.
 * - The method updateWorld() is called periodically by a Java timer.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    Random rand = new Random();

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;

        // TODO Update logical state of world

        updateEachActor(world, threshold);
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (square)
        int nLocations = 90000;

        // TODO Create and populate world

        world = generateWorld(nLocations, dist);

        // Should be last
        fixScreenSize(nLocations);
    }


    //---------------- Methods ----------------------------

    void updateEachActor(Actor[][] world, double threshold) {

        for (int row = 0; row < world.length; row++) {
            for (int i = 0; i < world[row].length; i++) {

                double sameDist = getSameNeighboursFor(world, row, i);

                if (sameDist < threshold) {
                    // Unsatisfied, move Actor

                    int[] emptyCoordinates = getEmptyPlace(world);

                    int emptyRow = emptyCoordinates[0];
                    int emptyIndex = emptyCoordinates[1];

                    // Swap places on Actors..

                    world[emptyRow][emptyIndex] = world[row][i];
                    world[row][i] = Actor.NONE;

                }

            }
        }

    }

    double getSameNeighboursFor(Actor[][] world, int row, int index) {

        int sameNeighbours = 0;
        int notSameNeighbours = 0;

        for (int rowMod = -1; rowMod <= 1; rowMod++) {
            for (int iMod = -1; iMod <= 1; iMod++) {

                int exactRow = row + rowMod;
                int exactIndex = index + iMod;

                if (!(exactRow == row && exactIndex == index)) {

                    if (isValidLocation(world.length, exactRow, exactIndex)) {

                        if (world[exactRow][exactIndex] != Actor.NONE) {

                            if (world[exactRow][exactIndex] == world[row][index]) {

                                sameNeighbours++;
                            } else {

                                notSameNeighbours++;
                            }

                        }
                    }

                }

            }
        }

        if (sameNeighbours + notSameNeighbours == 0) {

            return 0;
        }

        return sameNeighbours / (sameNeighbours + notSameNeighbours);
    }

    int[] getEmptyPlace(Actor[][] world) {

        int[] coordinates = new int[2];

        while (true) {

            int randomRow = rand.nextInt(world.length);
            int randomIndex = rand.nextInt(world.length);

            if (world[randomRow][randomIndex] == Actor.NONE) {

                coordinates[0] = randomRow;
                coordinates[1] = randomIndex;
                break;
            }

        }

        return coordinates;
    }

    Actor[][] generateWorld(int nLocs, double[] dist) {

        int dimension = (int) Math.sqrt(nLocs);

        double nRedDist = dist[0] * nLocs;
        double nBlueDist = dist[1] * nLocs;
        double nNoneDist = dist[2] * nLocs;

        Actor[][] world = new Actor[dimension][dimension];

        for (int row = 0; row < world.length; row++) {
            for (int i = 0; i < world[row].length; i++) {

                while (true) {

                    int nr = rand.nextInt(3);

                    if (nr == 0 && nRedDist != 0) {

                        world[row][i] = Actor.RED;
                        nRedDist--;
                        break;
                    } else if (nr == 1 && nBlueDist != 0) {

                        world[row][i] = Actor.BLUE;
                        nBlueDist--;
                        break;
                    } else if (nr == 2 && nNoneDist != 0) {

                        world[row][i] = Actor.NONE;
                        nNoneDist--;
                        break;
                    }

                }

            }

        }

        return world;
    }

    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size &&
                0 <= col && col < size;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing

        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));
        out.println(isValidLocation(size, 2, 2));

        // TODO More tests

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // ###########  NOTHING to do below this row, it's JavaFX stuff  ###########

    double width = 800;   // Size for window
    double height = 800;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Segregation Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
