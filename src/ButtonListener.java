import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;

import javax.swing.JButton;

public class ButtonListener implements ComponentListener {
    // callback every time a component
    // in this case: we want to resize the button's `font` every time the component itself is resized
    //
    public void componentResized(ComponentEvent event) {
        Object component = event.getSource();

        // check's if the component is a `JButton`
        if (component instanceof JButton) {
            JButton button = (JButton) component;

            float fontSize = (float) (button.getWidth());
            Font font = GamePanel.BUTTON_FONT
                .deriveFont(fontSize);
            // Get's the pixel width of the rendered text using the given font:
            // <https://stackoverflow.com/questions/258486/calculate-the-display-width-of-a-string-in-java>
            //
            Rectangle2D textDims = font.getStringBounds(
                button.getText(),
                new FontRenderContext(new AffineTransform(), true, true)
            );

            // brute's force the ideal font-size for the text to fit within the button
            // keeps attempting while the current text's `width` and `height` are respectively
            // still greater than 80% (margin space) of the button's `width` and `height`
            //
            // decrements font size by `1` each time.
            while (
                (float) textDims.getWidth() > button.getWidth() * 0.8
                || (float) textDims.getHeight() > button.getHeight() * 0.8
            ) {
                font = font.deriveFont(fontSize);
                textDims = font.getStringBounds(
                    button.getText(),
                    new FontRenderContext(new AffineTransform(), true, true)
                );

                fontSize -= 1;
            }
            button.setFont(font);
        }
    }

    public void componentMoved(ComponentEvent event) {}

    public void componentHidden(ComponentEvent event) {}

    public void componentShown(ComponentEvent event) {}
}