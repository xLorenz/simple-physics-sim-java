package physics;

import javax.swing.JFrame;

public class Frame extends JFrame {
    Frame() {
        SimCanvas canvas = new SimCanvas();
        this.add(canvas);
        this.setTitle("Physics sim");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack(); // fits the frame around the components
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        canvas.createBufferStrategy(3);
        canvas.requestFocusInWindow();

        new Thread(canvas).start(); // start sim thread
    }
}