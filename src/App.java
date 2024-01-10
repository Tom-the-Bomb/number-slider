import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;

public class App extends JPanel implements ActionListener {
    private GamePanel gamePanel;

    private JLabel movesLabel;
    private JButton restart;
    private JLabel sizeLabel;
    private JTextField sizeInput;

    private int size = 4;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;

    private static final Color TITLE_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(170, 255, 200);
    private static final Color BG_COLOR = new Color(40, 50, 60);

    private final Font TITLE_FONT;
    private final Font CODE_FONT;

    public App() throws FontFormatException, IOException {
        setBackground(BG_COLOR);

        TITLE_FONT = getFont("/fonts/BrownieStencil-vmrPE.ttf", 50);
        CODE_FONT = getFont("/fonts/FiraCodeNerdFont-Bold.ttf", 30);

        movesLabel = new JLabel("Moves: 0");
        movesLabel.setFont(CODE_FONT);
        movesLabel.setForeground(TEXT_COLOR);
        gamePanel = new GamePanel(size, movesLabel, BG_COLOR);

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

        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 50, 0, 50);
        // outer container around the game tiles
        // adds an even 100px margin around the entire game
        // csizeLabelizes the game on the frame
        //
        // <https://stackoverflow.com/questions/30611975/giving-a-jpanel-a-percentage-based-width>
        //
        // Uses `GridBagLayout` to do so.
        add(gamePanel, constraints);

        JPanel controls = new JPanel();
        controls.setBackground(BG_COLOR);

        restart = new JButton("Restart");
        restart.setForeground(Color.WHITE);
        restart.setBackground(Color.RED);

        sizeInput = new JTextField(5);

        sizeLabel = new JLabel("    Grid Size:");
        sizeLabel.setForeground(Color.WHITE);

        for (JComponent component : new JComponent[] {
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
                restart.addActionListener(this);
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

    public void actionPerformed(ActionEvent event) {
        Object component = event.getSource();

        if (component == restart) {
            remove(gamePanel);

            try {
                size = Integer.parseInt(
                    sizeInput.getText()
                );
            } catch (NumberFormatException err) {}

            movesLabel.setText("Moves: 0");
            gamePanel = new GamePanel(size, movesLabel, BG_COLOR);

            GridBagConstraints constraints = getDefaultConstraints();
            constraints.gridy = 2;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = new Insets(0, 50, 0, 50);

            add(gamePanel, constraints);
            revalidate();
            repaint();
        }
    }

    public static void main(String[] args) throws FontFormatException, IOException {
        JFrame frame = new JFrame("Number Slider");
        App app = new App();

        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(app);
        frame.setVisible(true);
    }
}