package physics;

import javax.swing.JFrame;

public class Frame extends JFrame {
    Frame() {
        this.add(new Panel());
        this.setTitle("Physics sim");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack(); // fits the frame around the components
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
