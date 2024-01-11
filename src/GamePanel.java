import java.util.Arrays;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// panel displaying solely the game board's matrix of buttons
public class GamePanel extends JPanel implements ActionListener {
    // reference to the main app panel/wrapper panel
    private App app;
    // number of moves counter
    private int moves;

    private int[][] board;
    private final int[][] COMPLETED;

    // game tile background colors
    private static final Color CORRECT_COLOR = new Color(60, 255, 80);
    private static final Color ZERO_COLOR = new Color(28, 28, 32);
    private static final Color DEFAULT_COLOR = new Color(210, 144, 144);
    // game tile outline color
    private static final Color OUTLINE_COLOR = Color.BLACK;
    // font colors
    private static final Color FONT_COLOR = Color.WHITE;
    private static final Color WIN_COLOR = Color.YELLOW;
    // font used on the game tiles (buttons)
    protected static final Font BUTTON_FONT = new Font("Verdana", Font.BOLD, 30);

    public GamePanel(App app) {
        this.app = app;
        moves = 0;

        // 1D array listing all the numbers present in the game
        int[] allNumbers = generateNumbers(app.size);
        // a constant 2D array representing the game state when the game is solved
        // i.e. it is sorted (unshuffled)
        COMPLETED = chunk(allNumbers, app.size);
        // sets the starting game board
        // it takes the completed board matrix
        // and repeatedlyplays a series of random valid moves from there to shuffle it
        board = chunk(allNumbers, app.size);
        shuffle(200);

        setBackground(Color.BLACK);
        setLayout(new GridLayout(app.size, app.size));

        // setups the grid by adding all the buttons for the number matrix
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                add(createButton(i, j));
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        Object component = event.getSource();

        if (component instanceof JButton) {
            JButton button = (JButton) component;
            int num = Integer.parseInt(button.getText());

            // the click is only valid
            // if the clicked number is beside the blank tile
            if (getBlankNeighbors().contains(num)) {
                // gets the indices of the tile with a value of `num`
                Point pressedCoord = getTile(num);
                // gets the indices of the `blank` tile
                Point blankCoord = getTile(0);
                // swap the pressed button and the blank button
                swap(pressedCoord, blankCoord);

                // update move's counter and display
                moves++;
                app.movesLabel.setText("Moves: " + moves);
                app.movesLabel.setForeground(App.TEXT_COLOR);

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
                    app.movesLabel.setText("Congratulations! You've won in " + moves + " moves!");
                    app.movesLabel.setForeground(WIN_COLOR);
                }
            }
        }
    }

    // converts a pair of (row, col) indices to a single index
    // that will index the same element when the matrix is flattened
    private int to1DIdx(Point indices) {
        return indices.x * app.size + indices.y;
    }

    // creates a new `JButton` to be added to the `JPanel`
    // with attributes like color, label etc.
    private JButton createButton(int i, int j) {
        JButton button = new JButton();
        updateButton(button, i, j);

        button.setForeground(FONT_COLOR);
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
    // within the interval [i, size^2]
    //
    private static int[] generateNumbers(int size) {
        int count = size * size;
        int[] arr = new int[count];

        for (int i = 1; i < count; i++) {
            arr[i - 1] = i;
        }
        arr[count - 1] = 0;
        return arr;
    }

    // swaps the `clicked` tile and the `blank` tile
    //
    private void swap(Point pressedCoord, Point blankCoord) {
        int temp = board[pressedCoord.x][pressedCoord.y];

        board[pressedCoord.x][pressedCoord.y] = board[blankCoord.x][blankCoord.y];
        board[blankCoord.x][blankCoord.y] = temp;
    }

    // shuffles the completed array by simulating gameplay `count` number of moves
    // sets up the starting game board
    private void shuffle(int count) {
        Point blankPressed = getTile(0);

        for (int i = 0; i < count; i++) {
            // get all potential tiels we can move/swap/click
            ArrayList<Integer> neighbors = getBlankNeighbors();

            int idx = neighbors.indexOf(
                // value of the previous move
                board[blankPressed.x][blankPressed.y]
            );
            if (idx != -1) {
                // if the previous MOVE is in our available moves
                neighbors.remove(idx);
            }

            Point pressedCoord = getTile(
                neighbors.get(
                    (int) Math.random() * neighbors.size()
                )
            );
            blankPressed = getTile(0);
            swap(pressedCoord, blankPressed);
        }
    }

    // chunks a 1 dimensional array
    // into a 2 dimensional array with row length's of `size`
    // chunk([1, 2, 3, 4]), size=2 -> [[1, 2], [3, 4]]
    private static int[][] chunk(int[] arr, int size) {
        int[][] chunked = new int[size][];

        for (
            int i = 0, idx = 0;
            i < arr.length;
            i += size, idx++
        ) {
            int[] chunk = new int[size];

            for (int j = i; j < i + size; j++) {
                chunk[j - i] = arr[j];
            }
            chunked[idx] = chunk;
        }
        return chunked;
    }

    // returns the indices of the tile that has a label/value of `num`
    //
    private Point getTile(int num) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == num) {
                    return new Point(i, j);
                }
            }
        }
        // not found but this should not be reached
        // if `num` is in the interval: `[0, size^2]`
        return null;
    }

    // gets the numbers that are in the 4-neighborhood of the blank tile
    // effectively are all the tiles that are able to be selected/moved
    //
    // ensures that the neighbor's are within the grid bounds.
    private ArrayList<Integer> getBlankNeighbors() {
        Point empty = getTile(0);

        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        Point[] points = {
            new Point(empty.x - 1, empty.y),
            new Point(empty.x + 1, empty.y),
            new Point(empty.x, empty.y - 1),
            new Point(empty.x, empty.y + 1),
        };

        for (Point indices : points) {
            if (
                0 <= indices.x
                && indices.x < app.size
                && 0 <= indices.y
                && indices.y < app.size
            ) {
                neighbors.add(board[indices.x][indices.y]);
            }
        }
        return neighbors;
    }
}