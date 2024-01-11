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

        // controls sub-panel in the last row of the app
        JPanel controls = new JPanel(
            new GridLayout(1, 3, 10, 0)
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
            help,
            restart,
            sizeLabel,
            sizeInput,
        }) {
            component.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            );
            component.setFont(
                CODE_FONT.deriveFont(Font.PLAIN, 20)
            );

            if (component instanceof JButton) {
                ((JButton) component).addActionListener(this);
            }
            controls.add(component);
        }

        constraints.gridy = 3;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.NONE;

        add(controls, constraints);
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
            revalidate();
            repaint();
        } else if (component == help) {
            remove(gamePanel);


        }
    }

    public static void main(String[] args) throws FontFormatException, IOException {
        JFrame frame = new JFrame("Number Slider");
        App app = new App(4);

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(app);
        frame.setVisible(true);
    }
}