import java.util.Arrays;
import java.util.ArrayList;

import java.time.Instant;
import java.time.Duration;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Panel displaying solely the game board's matrix of buttons
//
public class GamePanel extends JPanel implements ActionListener {
    // reference to the main app panel/wrapper panel
    private App app;
    // number of moves counter
    private int moves;

    private int[][] board;
    private final int[][] COMPLETED;

    // an instant representing the time when the first tile was clicked (first move)
    private Instant startTime;
    // a duration representing the difference between `startTime` and the instant when the user has completed the puzzle
    private Duration timeTaken;

    // game tile background colors
    private static final Color CORRECT_COLOR = new Color(60, 255, 80);
    private static final Color ZERO_COLOR = new Color(28, 28, 32);
    private static final Color DEFAULT_COLOR = new Color(210, 144, 144);
    // game tile outline color
    private static final Color OUTLINE_COLOR = Color.BLACK;
    // font colors
    protected static final Color TEXT_COLOR = Color.WHITE;
    private static final Color WIN_COLOR = Color.YELLOW;
    // font used on the game tiles (buttons)
    protected static final Font BUTTON_FONT = new Font("Verdana", Font.BOLD, 30);

    public GamePanel(App app) {
        this.app = app;
        moves = 0;

        // 1D array listing all the numbers present in the game
        int[] allNumbers = generateNumbers(app.rows, app.cols);

        // a constant 2D array representing the game state when the game is solved
        // i.e. it is sorted (unshuffled)
        COMPLETED = chunk(allNumbers);

        // 2D array representing the current game state / board
        // generated from shuffling the end-goal sorted array and playng valid moves randomly backwards.
        //
        // re-evaluates the chunking as `shuffle` modifies in-place so we do not want to end up modifying `COMPLETED` too
        board = chunk(allNumbers);
        // shuffles a random amount of times/moves between `[500, 700]`
        shuffle((int) Math.pow(app.rows * app.cols, 2));

        setBackground(OUTLINE_COLOR);
        // sets the layout of a panel to be a grid layout
        // increases responsiveness, (row x cols sized grid)
        setLayout(new GridLayout(app.rows, app.cols));

        // setups the grid by adding all the buttons for the number matrix
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                add(createButton(i, j));
            }
        }
    }

    // handles move when the buttons are clicked
    //
    public void actionPerformed(ActionEvent event) {
        Object component = event.getSource();

        if (component instanceof JButton) {
            JButton button = (JButton) component;
            int num = Integer.parseInt(button.getText());

            // the click is only valid
            // if the clicked number is beside the blank tile
            if (getBlankNeighbors().contains(num)) {
                if (moves == 0) {
                    startTime = Instant.now();
                }

                // gets the indices of the tile with a value of `num`
                Point pressedCoord = getTile(num);
                // gets the indices of the `blank` tile
                Point blankCoord = getTile(0);
                // swap the pressed button and the blank button
                swap(pressedCoord, blankCoord);

                // update move's counter and display
                moves++;
                app.movesLabel.setText("Moves: " + moves);
                app.movesLabel.setForeground(App.LABEL_COLOR);

                // updates the properties of the button that was clicked
                updateButton(
                    (JButton) getComponents()[to1DIdx(pressedCoord)],
                    pressedCoord.x, pressedCoord.y
                );
                // updates the properties of the blank button that was swapped
                updateButton(
                    (JButton) getComponents()[to1DIdx(blankCoord)],
                    blankCoord.x, blankCoord.y
                );

                // the user's board equals the sorted/target end board `COMPLETED`
                // meaning the user has finished the puzzle / has won
                if (Arrays.deepEquals(board, COMPLETED)) {
                    // calculates time taken for the user to solve the current puzzle
                    //
                    // <https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances>
                    //
                    timeTaken = Duration.between(startTime, Instant.now());

                    app.movesLabel.setText(String.format(
                        "Congratulations! You've won in %d moves, taking %s",
                        moves,
                        humanizeDuration(timeTaken)
                    ));
                    app.movesLabel.setForeground(WIN_COLOR);
                }
            }
        }
    }

    // formats a `Duration` properly, displaying it in a non redundant (zero values ommited) manner
    // and in a human-readable format
    //
    private static String humanizeDuration(Duration duration) {
        String formatted = "";

        long days = duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        if (days > 0) {
            formatted += String.format(" %dd", days);
        }
        if (hours > 0) {
            formatted += String.format(" %dh", hours);
        }
        if (minutes > 0) {
            formatted += String.format(" %dm", minutes);
        }
        if (seconds > 0) {
            formatted += String.format(" %ds", seconds);
        }
        return "[" + formatted.strip() + "]";
    }

    // converts a pair of (row, col) indices to a single index
    // that will index the same element when the matrix is flattened
    //
    private int to1DIdx(Point indices) {
        return indices.x * app.cols + indices.y;
    }

    // creates a new `JButton` to be added to the `JPanel`
    // with required attributes like color, label etc.
    //
    private JButton createButton(int i, int j) {
        JButton button = new JButton();
        updateButton(button, i, j);

        button.setForeground(TEXT_COLOR);
        button.setFont(BUTTON_FONT);
        button.setBorder(
            BorderFactory.createLineBorder(OUTLINE_COLOR, 4)
        );
        button.addActionListener(this);
        button.addComponentListener(new ButtonListener());
        return button;
    }

    // update's the `button`'s properties that are changed every moved
    // this includes:
    //     - the color: (green if the tile is in the right spot)
    //     - the label: (numbers have been swapped)
    //     - whether or not the button is disabled (only if the tile is blank)
    //
    // `i` and `j` respectively are the row and column indices of the tile/button
    //
    private void updateButton(JButton button, int i, int j) {
        int num = board[i][j];

        Color color = num == COMPLETED[i][j]
            ? CORRECT_COLOR
            : num == 0
            ? ZERO_COLOR
            : DEFAULT_COLOR;

        button.setText(
            num == 0 ? "" : String.valueOf(num)
        );
        button.setBackground(color);
        button.setEnabled(num != 0);
    }

    // generates the array of numbers that will be used
    // within the interval [1, rows * cols) (+ a `0` at the end)
    //
    private static int[] generateNumbers(int rows, int cols) {
        int count = rows * cols;
        int[] arr = new int[count];

        for (int i = 1; i < count; i++) {
            arr[i - 1] = i;
        }
        arr[arr.length - 1] = 0;
        return arr;
    }

    // swaps the `clicked` tile and the `blank` tile
    //
    private void swap(Point pressedCoord, Point blankCoord) {
        int temp = board[pressedCoord.x][pressedCoord.y];

        board[pressedCoord.x][pressedCoord.y] = board[blankCoord.x][blankCoord.y];
        board[blankCoord.x][blankCoord.y] = temp;
    }

    // method to aid in generate the starting grid configuration
    // by randomly performing valid moves starting with the solved grid configuration
    //
    // performs `count` number of moves backwrads to generate a random configuration
    //
    private void shuffle(int count) {
        Point blank = getTile(0);

        for (int i = 0; i < count; i++) {
            ArrayList<Integer> neighbors = getBlankNeighbors();
            int idxToRemove = neighbors.indexOf(
                board[blank.x][blank.y]
            );

            if (idxToRemove != -1) {
                neighbors.remove(idxToRemove);
            }

            int move = neighbors.get(
                (int) (Math.random() * neighbors.size())
            );
            blank = getTile(0);
            swap(getTile(move), blank);
        }
    }

    // chunks a 1 dimensional array
    // into a 2 dimensional array with row length's of `size`
    // `chunk([1, 2, 3, 4, 5, 6], size=2)` -> `[[1, 2], [3, 4], [5, 6]]`
    //
    private int[][] chunk(int[] arr) {
        int[][] chunked = new int[app.rows][app.cols];

        for (
            int i = 0, idx = 0;
            i < arr.length;
            i += app.cols, idx++
        ) {
            int[] chunk = new int[app.cols];

            for (int j = i; j < i + app.cols; j++) {
                chunk[j - i] = arr[j];
            }
            chunked[idx] = chunk;
        }
        return chunked;
    }

    // returns the indices as `Point(row, col)`
    // of the tile that has a label/value of `num`
    //
    private Point getTile(int num) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == num) {
                    return new Point(i, j);
                }
            }
        }
        // returns `null` when not not found
        //
        // but this should never be reached
        // if `num` is in the interval: `[0, rows * cols]`
        return null;
    }

    // gets the numbers that are in the 4-neighborhood of the blank tile
    // effectively are all the tiles that are able to be selected/moved
    //
    // ensures that the neighbor's are within the grid bounds.
    //
    // uses an `ArrayList<Integer>` instead of `int[]` as there may not always be 4 neighbors,
    // as some may be out of bounds
    //
    private ArrayList<Integer> getBlankNeighbors() {
        Point empty = getTile(0);

        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        Point[] points = {
            new Point(empty.x - 1, empty.y),
            new Point(empty.x + 1, empty.y),
            new Point(empty.x, empty.y - 1),
            new Point(empty.x, empty.y + 1),
        };

        for (Point indices: points) {
            if (
                0 <= indices.x
                && indices.x < board.length
                && 0 <= indices.y
                && indices.y < board[0].length
            ) {
                neighbors.add(board[indices.x][indices.y]);
            }
        }
        return neighbors;
    }
}