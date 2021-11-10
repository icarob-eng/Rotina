import javax.swing.*;
import java.awt.*;

public class DialogPanel extends JPanel {
    private final JTextField x, y;

    public DialogPanel(String label1, String label2, String value1, String value2){
        // configurações de diálogo para entrar com dados do horário e da aula
        setLayout(new GridLayout(4, 1));   // faz com que o layout seja vertical

        x = new JTextField(value1);
        y = new JTextField(value2);
        add(new JLabel(label1));
        add(x);
        add(new JLabel(label2));
        add(y);
    }

    public DialogPanel(String label1, String label2) {
        this(label1, label2, "", "");
    }

    public String[] getValues(){
        // pega os valores e limpa os dados
        return new String[] {x.getText(), y.getText()};
    }
}
