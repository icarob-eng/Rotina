import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class MainFrame extends JFrame {
    private int lookAndFeel;
    private final File file = new File("theme_config.txt");
    public final String[] lookAndFeelOptions = new String[]{"Padrão", "Do sistema", "Light", "Dark", "Intellij", "Darcula"};

    public MainFrame() {
        super("Abertura automática de aulas");

        setIconImage(new ImageIcon("rotinas_icon.png").getImage());

        // configurações de look and feel
        int selection = 0;
        try {
            if(! file.createNewFile()) try (Scanner scanner = new Scanner(file)){
                if (scanner.hasNextLine()) {
                    selection = Integer.parseInt(scanner.nextLine());
                }
            }  // se o arquivo existe, tenta criar um scanner e lê-lo
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error ao recuperar aparência configurada. Selecionado a padrão.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        try {
            selectLookAndFeel(selection);
        } catch (UnsupportedLookAndFeelException | IOException ignored) {}

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(true);
        setSize(1000, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        MainPanel panel = new MainPanel(this);
        add(panel);
    }


    public int getLookAndFeel() {
        // pega o nome do look and feel usado
        return lookAndFeel;
    }

    public void selectLookAndFeel(int lookAndFeelSelection) throws UnsupportedLookAndFeelException,
            InvalidParameterException, IOException {
        // configura o look and feel
        lookAndFeel = lookAndFeelSelection;
        switch (lookAndFeelSelection) {
            case 0:  // look and feel padrão
                break;
            case 1:
                try {
                    UIManager.setLookAndFeel(new GTKLookAndFeel());  // tenta GTK, senão, tenta windows
                } catch (UnsupportedLookAndFeelException e) {
                    UIManager.setLookAndFeel(new WindowsLookAndFeel());
                }
                break;
            case 2:
                UIManager.setLookAndFeel(new FlatLightLaf());
                break;
            case 3:
                UIManager.setLookAndFeel(new FlatDarkLaf());
                break;
            case 4:
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                break;
            case 5:
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                break;
            default:
                throw new InvalidParameterException("Look and feel não reconhecido");
        }

        try (FileWriter writer = new FileWriter(file)){
            writer.write(Integer.toString(lookAndFeel));
        }
    }
}

