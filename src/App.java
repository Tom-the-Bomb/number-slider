import java.util.Arrays;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class App extends JPanel implements ActionListener {
    // the size of a singular dimension
    private int size;
    private int moves;
    private JFrame frame;

    private int[] allNumbers;
    private int[][] board;
    private int[][] completed;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int BUTTON_SIZE = 75;

    private static final Color GREEN = Color.GREEN;
    private static final Color GRAY = Color.GRAY;

    public App(int size, JFrame frame) {
        this.size = size;
        this.moves = 0;
        this.frame = frame;

        allNumbers = generateNumbers(size);

        board = chunk(
            pushBack(
                shuffle(allNumbers.clone()),
                0
            ),
            size
        );
        completed = chunk(
            pushBack(allNumbers, 0),
            size
        );

        renderButtons();
    }

    private void renderButtons() {
        for (
            int i = 0, y = 100;
            i < board.length;
            i++, y += BUTTON_SIZE
        ) {
            for (
                int j = 0, x = 100;
                j < board[i].length;
                j++, x += BUTTON_SIZE
            ) {
                int num = board[i][j];

                Color color = num == completed[i][j]
                    ? GREEN
                    : GRAY;
                JButton button = new JButton(
                    String.valueOf(num)
                );
                button.setBackground(color);
                button.setLocation(x, y);
                button.setSize(BUTTON_SIZE, BUTTON_SIZE);
                button.addActionListener(this);

                frame.add(button);
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        JButton src = (JButton) event.getSource();

        int[] neighbors = getBlankNeighbors();
        int num = Integer.parseInt(src.getText());

        if (contains(neighbors, num)) {
            Pair pressedCoord = getTile(num);
            Pair blankCoord = getTile(0);

            int temp = board[pressedCoord.x][pressedCoord.y];
            board[pressedCoord.x][pressedCoord.y] = board[blankCoord.x][blankCoord.y];
            board[blankCoord.x][blankCoord.y] = temp;

            System.out.println(Arrays.deepToString(board));

            moves++;
            removeAll();
            revalidate();
            repaint();
            renderButtons();
            revalidate();
            repaint();


            // the user's board equals the sorted/target end board `completed`
            // meaning the user has completed the puzzle / has won
            if (Arrays.deepEquals(board, completed)) {
                System.out.println("You've won!");
            }
        }
    }

    private static boolean contains(int[] arr, int target) {
        for (int element : arr) {
            if (element == target) {
                return true;
            }
        }
        return false;
    }

    // appends a single `element` to the back of an array: `arr`
    private static int[] pushBack(int[] arr, int element) {
        int[] copy = new int[arr.length + 1];

        for (int i = 0; i < arr.length; i++) {
            copy[i] = arr[i];
        }
        copy[arr.length] = element;
        return copy;
    }

    // generates the array of numbers that will be used
    // from [i, size^2]
    private static int[] generateNumbers(int size) {
        int count = size * size;
        int[] arr = new int[count - 1];

        for (int i = 1; i < count; i++) {
            arr[i - 1] = i;
        }
        return arr;
    }

    // randomly shuffles a 1 dimensional array using the
    // [Fish Yates Algorithm](https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle)
    //
    // generates random indices using `Math.random` that are in range of the array's length
    // and then swaps the array's elements one by one with the randomly generated index.
    private static int[] shuffle(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int j = (int) (Math.random() * arr.length - 1) + 1;

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    // chunks a 1 dimensional array
    // into a 2 dimensional array with row length's of `size`
    // chunk([1, 2, 3, 4]), size=2 -> [[1, 2], [3, 4]]
    private static int[][] chunk(int[] arr, int size) {
        int[][] chunked = new int[size][];

        for (
            int i = 0, idx = 0;
            i < arr.length;
            i += size, idx += 1
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
    private Pair getTile(int num) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == num) {
                    return new Pair(i, j);
                }
            }
        }
        // not found but this should not be reached
        // if `num` is in `[0, size * size]`
        return null;
    }

    // gets the numbers that are in the 4-neighborhood of the blank tile
    // effectively are all the tiles that are able to be selected/moved
    private int[] getBlankNeighbors() {
        Pair empty = getTile(0);

        int[] neighbors = new int[4];
        Pair[] pairs = empty.getNeighbors();

        for (int i = 0; i < pairs.length; i++) {
            Pair pair = pairs[i];

            if (
                0 <= pair.x
                && pair.x < size
                && 0 <= pair.y
                && pair.y < size
            ) {
                neighbors[i] = board[pair.x][pair.y];
            }
        }
        return neighbors;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Number Slider");
        App app = new App(4, frame);

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(app);
        frame.setLayout(new GridLayout(0, 4));
        frame.setVisible(true);
    }
}
