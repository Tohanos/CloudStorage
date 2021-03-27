package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientParameterDialog {

    enum KeyPressed {
        OK_PRESSED,
        CANCEL_PRESSED
    }

    private static ClientParameterDialog instance;
    private static StringBuilder answer;
    private static KeyPressed keyPressed;

    public static ClientParameterDialog getInstance() {
        if (instance == null) {
            instance = new ClientParameterDialog();
        }
        answer = new StringBuilder("");
        return instance;
    }

    public static String getResult() {
        return answer.toString();
    }

    public static KeyPressed getKeyPressed() {
        return keyPressed;
    }

    public void parameterEntry(String textToShow) {

        JFrame dialogWindowFrame = new JFrame("Enter something");
        dialogWindowFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogWindowFrame.setSize(300, 150);
        dialogWindowFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
        dialogWindowFrame.setResizable(false);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JLabel text = new JLabel(textToShow);

        JPanel textPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton, FlowLayout.LEFT);
        buttonPanel.add(cancelButton, FlowLayout.LEFT);

        JTextField entryField = new JTextField();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(text);
        textPanel.add(entryField);

        dialogWindowFrame.getContentPane().add(textPanel, BorderLayout.NORTH);
        dialogWindowFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        dialogWindowFrame.setVisible(true);
        okButton.addActionListener(a -> {
            answer.append(entryField.getText());
            keyPressed = KeyPressed.OK_PRESSED;
            dialogWindowFrame.dispose();
        });

        cancelButton.addActionListener(a -> {
            keyPressed = KeyPressed.CANCEL_PRESSED;
            dialogWindowFrame.dispose();
        });

    }

    public static void main(String[] args) throws InterruptedException {
        ClientParameterDialog cpd = ClientParameterDialog.getInstance();
        cpd.parameterEntry("Enter ggegrgg");
        boolean result = true;
        while (result) {
            result = (getKeyPressed() == KeyPressed.OK_PRESSED ||
                    getKeyPressed() == KeyPressed.CANCEL_PRESSED);

        }
        System.out.println(getResult());
    }
}
