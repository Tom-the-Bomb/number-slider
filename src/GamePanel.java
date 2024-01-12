import java.util.Arrays;

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
        int[] allNumbers = generateNumbers(app.size);

        // clone so we do not ruin the order when creating `COMPLETED`
        int[] clone = allNumbers.clone();
        shuffle(clone);
        // append the empty tile (which will always start at the end of the board)
        // (append after shuffle so it is guaranteed to stay there)
        int[] shuffledBoard = pushBack(clone, 0);

        while (!isSolvable(shuffledBoard)) {
            // keep reshuffling until the shuffled board is actually solvable
            shuffle(clone);
            shuffledBoard = pushBack(clone, 0);
        }
        // 2D array representing the current game state / board
        // generated from random shuffling and ensuring it is always solvable
        board = chunk(shuffledBoard, app.size);

        // a constant 2D array representing the game state when the game is solved
        // i.e. it is sorted (unshuffled)
        COMPLETED = chunk(
            pushBack(allNumbers, 0),
            app.size
        );

        setBackground(OUTLINE_COLOR);
        setLayout(new GridLayout(app.size, app.size));

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
            if (contains(getBlankNeighbors(), num)) {
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
                    app.movesLabel.setText("Congratulations! You've won in " + moves + " moves!");
                    app.movesLabel.setForeground(WIN_COLOR);
                }
            }
        }
    }

    // converts a pair of (row, col) indices to a single index
    // that will index the same element when the matrix is flattened
    //
    private int to1DIdx(Point indices) {
        return indices.x * app.size + indices.y;
    }

    // simple helper method to check if `target` in the array `arr`
    //
    private boolean contains(int[] arr, int target) {
        for (int element : arr) {
            if (element == target) {
                return true;
            }
        }
        return false;
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
    // within the interval [i, size^2]
    //
    private static int[] generateNumbers(int size) {
        int count = size * size;
        int[] arr = new int[count - 1];

        for (int i = 1; i < count; i++) {
            arr[i - 1] = i;
        }
        return arr;
    }

    // swaps the `clicked` tile and the `blank` tile
    //
    private void swap(Point pressedCoord, Point blankCoord) {
        int temp = board[pressedCoord.x][pressedCoord.y];

        board[pressedCoord.x][pressedCoord.y] = board[blankCoord.x][blankCoord.y];
        board[blankCoord.x][blankCoord.y] = temp;
    }

    // appends a single `element` to the back of an array: `arr`
    //
    private static int[] pushBack(int[] arr, int element) {
        int[] copy = new int[arr.length + 1];

        for (int i = 0; i < arr.length; i++) {
            copy[i] = arr[i];
        }
        copy[arr.length] = element;
        return copy;
    }

    // randomly shuffles a 1 dimensional array *in-place* using the
    // [Fish Yates Algorithm](https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle)
    //
    // generates random indices using `Math.random` that are in range of the array's length
    // and then swaps the array's elements one by one with the randomly generated index.
    //
    private static void shuffle(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int j = (int) (Math.random() * arr.length - 1) + 1;

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // checks whether or not the randomly jumbled array is solvable
    // if the number of `inversions` that has occured in the flattened board matrix is `even` it is solvable
    // otherwise (if it is `odd` it is not
    //
    // an inversion is when there is a tile (`a`) that is situated before another (`b`) but a > b
    // and therefore it is out of desired order, therefore it is inversed
    //
    // <https://math.stackexchange.com/questions/4064152/solvability-of-a-sliding-puzzle-of-size-nn>
    //
    private static boolean isSolvable(int[] flatBoard) {
        int nInversions = 0;

        // loop through/check all tiles
        for (int i = 0; i < flatBoard.length; i++) {
            // for each tile, also pair it up and loop through all tiles that are AFTER it
            for (int j = i + 1; j < flatBoard.length; j++) {
                // our first tile
                int first = flatBoard[i];
                // our second tile to compare to
                int after = flatBoard[j];
                if (
                    // check if `first` and `after` are not blank tiles
                    first != 0
                    && after != 0
                    // the first occuring tile is greater than the one after it
                    // hence it is an `inversion` (as it is in reverse/descending order)
                    && first > after
                ) {
                    nInversions++;
                }
            }
        }
        return nInversions % 2 == 0;
    }

    // chunks a 1 dimensional array
    // into a 2 dimensional array with row length's of `size`
    // `chunk([1, 2, 3, 4]), size=2` -> `[[1, 2], [3, 4]]`
    //
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
        // not found but this should not be reached
        // if `num` is in the interval: `[0, size^2]`
        return null;
    }

    // gets the numbers that are in the 4-neighborhood of the blank tile
    // effectively are all the tiles that are able to be selected/moved
    //
    // ensures that the neighbor's are within the grid bounds.
    private int[] getBlankNeighbors() {
        Point empty = getTile(0);

        int[] neighbors = new int[4];
        Point[] points = {
            new Point(empty.x - 1, empty.y),
            new Point(empty.x + 1, empty.y),
            new Point(empty.x, empty.y - 1),
            new Point(empty.x, empty.y + 1),
        };

        for (int i = 0; i < points.length; i++) {
            Point indices = points[i];

            if (
                0 <= indices.x
                && indices.x < app.size
                && 0 <= indices.y
                && indices.y < app.size
            ) {
                neighbors[i] = board[indices.x][indices.y];
            }
        }
        return neighbors;
    }
}