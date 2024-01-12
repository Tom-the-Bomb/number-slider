import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;

public class App extends JPanel implements ActionListener {
    private GamePanel gamePanel;
    protected int size;

    protected JLabel movesLabel;
    private JButton help;
    private JButton restart;
    private JLabel sizeLabel;
    private JTextField sizeInput;

    // help screen components
    private JButton back;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;

    // text colors
    private static final Color TITLE_COLOR = new Color(255, 255, 255);
    protected static final Color LABEL_COLOR = new Color(170, 255, 200);
    protected static final Color BG_COLOR = new Color(60, 60, 70);
    // buttons colors
    private static final Color SECONDARY_BTN_COLOR = new Color(90, 90, 100);
    private static final Color DANGER_BTN_COLOR = Color.RED;

    private final Font TITLE_FONT;
    private final Font CODE_FONT;

    public App(int defaultSize) throws FontFormatException, IOException {
        setBackground(BG_COLOR);

        TITLE_FONT = getFont("/fonts/BrownieStencil-vmrPE.ttf", 50);
        CODE_FONT = getFont("/fonts/FiraCodeNerdFont-Bold.ttf", 30);

        size = defaultSize;
        movesLabel = new JLabel("Moves: 0");
        movesLabel.setFont(CODE_FONT);
        movesLabel.setForeground(LABEL_COLOR);
        gamePanel = new GamePanel(this);

        setupComponents();

        back = new JButton("Back");
        back.setForeground(GamePanel.TEXT_COLOR);
        back.setBackground(SECONDARY_BTN_COLOR);
        setupComponentProperties(back);
    }

    private void setupComponents() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints constraints = getDefaultConstraints();

        JLabel title = new JLabel("Number Slider");
        title.setFont(TITLE_FONT);
        title.setForeground(TITLE_COLOR);

        constraints.gridy = 0;
        add(title, constraints);

        constraints.gridy = 1;
        add(movesLabel, constraints);

        addGamePanel();

        // the `controls` sub-panel in the last row of the app
        JPanel controls = new JPanel();
        controls.setLayout(
            new GridLayout(0, 3, 10, 10)
        );
        controls.setBackground(BG_COLOR);

        help = new JButton("Help");
        help.setForeground(GamePanel.TEXT_COLOR);
        help.setBackground(SECONDARY_BTN_COLOR);

        restart = new JButton("Restart");
        restart.setForeground(GamePanel.TEXT_COLOR);
        restart.setBackground(DANGER_BTN_COLOR);

        sizeInput = new JTextField(5);

        sizeLabel = new JLabel("    Grid Size:");
        sizeLabel.setForeground(GamePanel.TEXT_COLOR);

        for (JComponent component : new JComponent[] {
            restart,
            sizeLabel,
            sizeInput,
            help,
        }) {
            setupComponentProperties(component);
            controls.add(component);
        }

        constraints.gridy = 3;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.NONE;

        add(controls, constraints);
    }

    // setups some default (repetitive) component properties
    // such as font, color, listener etc.
    //
    private void setupComponentProperties(JComponent component) {
        component.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        component.setFont(
            CODE_FONT.deriveFont(20f)
        );

        if (component instanceof JButton) {
            ((JButton) component).addActionListener(this);
        }
    }

    // loads in a custom font file (.ttf) as a `Font`:
    //
    // <https://stackoverflow.com/questions/71125231/how-to-set-the-size-of-a-font-from-a-file-in-swing>
    //
    private Font getFont(String path, int size) throws FontFormatException, IOException {
        return Font.createFont(
            Font.TRUETYPE_FONT,
            this.getClass().getResourceAsStream(path)
        ).deriveFont((float) size);
    }

    // returns an instance of `GridBagConstraints`
    // with default values for the properties that we desire for our panel
    //
    private GridBagConstraints getDefaultConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        return constraints;
    }

    // `app` is the outer container around the game tiles panel `gamePanel`
    // adds `gamePanel` to `app` with proper positioning
    //
    // adds an even 100px margin around the entire game
    // centerizes the game on the frame
    //
    // <https://stackoverflow.com/questions/30611975/giving-a-jpanel-a-percentage-based-width>
    //
    // `app` uses `GridBagLayout` to handle all this well.
    private void addGamePanel() {
        GridBagConstraints constraints = getDefaultConstraints();
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 50, 0, 50);

        add(gamePanel, constraints);
    }

    public void actionPerformed(ActionEvent event) {
        Object component = event.getSource();

        if (component == restart) {
            try {
                String val = sizeInput.getText();
                if (!val.isEmpty()) {
                    int newSize = Integer.parseInt(val);

                    if (newSize < 2 || newSize > 20) {
                        // Shows a message box like javascript's `alert(...)` in the web:
                        //
                        // <https://stackoverflow.com/questions/7080205/popup-message-boxes>
                        //
                        JOptionPane.showInternalMessageDialog(
                            null, "Grid size must be between 2 and 20."
                        );
                        return;
                    }

                    size = newSize;
                }
            } catch (NumberFormatException err) {
                JOptionPane.showInternalMessageDialog(
                    null, "Invalid numerical value"
                );
                return;
            }
            remove(gamePanel);

            movesLabel.setText("Moves: 0");
            gamePanel = new GamePanel(this);
            addGamePanel();

        } else if (component == help) {
            removeAll();

            JLabel title = new JLabel("How to Play");
            title.setFont(TITLE_FONT);
            title.setForeground(GamePanel.TEXT_COLOR);
            title.setHorizontalAlignment(JLabel.CENTER);

            GridBagConstraints constraints = getDefaultConstraints();

            constraints.gridy = 0;
            add(title, constraints);

            JTextPane description = new JTextPane();
            description.setContentType("text/html");
            description.setEditable(false);
            description.setText(
                String.format(
                    """
                    <html>
                        <div style="
                            color: rgb(%d, %d, %d);
                            font-size: 20pt;
                            font-weight: 200;
                        ">
                            <pre>
                    The goal of the game is to sort all the tiles in ascending order.

                    The desired endgame layout is to have the top-left corner be a <b>[1]</b>
                    and in <b>ascending</b> order all the way to the bottom-left which should be the <b>[largest number]</b>

                    The dark gray tile represents the <b>empty</b> tile that it's neighbors can move to.
                    Therefore, only the direct <b>neighbors</b> of that tile can be <b>clicked</b>
                    and said tile will get <b>swapped</b> with the blank tile when <b>clicked</b>
                            </pre>
                            <hr>
                            <pre>
                    Click <b>[Restart]</b> to generate a random fresh board (and to update grid size value)

                    Enter <b>[Grid Size]</b> (number from 1 to 20) to change the dimensions of the grid
                    (By default the grid size is 4)
                            </pre>
                        </div>
                    </html>
                    """,
                    LABEL_COLOR.getRed(),
                    LABEL_COLOR.getGreen(),
                    LABEL_COLOR.getRed()
                )
            );
            description.setOpaque(false);

            constraints.gridy = 1;
            constraints.fill = GridBagConstraints.NONE;
            add(description, constraints);

            constraints.gridy = 2;
            add(back, constraints);

        } else if (component == back) {
            removeAll();
            setupComponents();

        } else {
            return;
        }

        revalidate();
        repaint();
    }

    public static void main(String[] args) throws FontFormatException, IOException {
        JFrame frame = new JFrame("Number Slider");

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(
            new JScrollPane(
                new App(4),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            )
        );
        frame.setVisible(true);
    }
}