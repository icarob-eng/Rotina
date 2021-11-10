import javax.swing.*;
import java.util.ArrayList;
import java.util.InputMismatchException;

public class Horario {
    int inicioH, inicioM, fimH, fimM;
    private final MainPanel panel;
    final ArrayList<JButton> btns;  // lista com os botões de cada dia no horário 0 = horário
    final ArrayList<JLabel> labels;  // lista com os botões de cada dia no horário 0 = horário
    private volatile ArrayList<String>[] aulas; // lista com dados das aulas {{Aula, Link}, {Aula, Link}}
    // a lista é volátil por conta do outro loop

    public Horario(int inicioHora, int inicioMin,
                   int fimHora, int fimMin,
                   MainPanel mainPanel, ArrayList<JButton> btns, ArrayList<JLabel> labels) {
        inicioH = inicioHora;
        inicioM = inicioMin;
        fimH = fimHora;
        fimM = fimMin;

        panel = mainPanel;
        this.btns = btns;  // lista com os botões do horário
        this.labels = labels;  // lista com as legendas dos links

        aulas = new ArrayList[] {
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        };

        panel.jsonHandler.salvarHorarios(); // já atualiza o Json
    }

    public String[] getAula(int dia) {
        // retorna o nome e o link da aula, se existir
        try {
            return new String[]{aulas[dia].get(0), aulas[dia].get(1)};
        } catch (IndexOutOfBoundsException e) {
            return new String[]{"", ""};
        }
    }

    public void saveLessSetAula(int dia, String nome, String link) {
        // configura a aula, iniciando nova config se não houver, sem salvar no JSON
        try {
            aulas[dia].set(0, nome);
            aulas[dia].set(1, link);
        } catch (IndexOutOfBoundsException e) {
            aulas[dia].add(nome);
            aulas[dia].add(link);
        }
    }

    public void setAula(int dia, String nome, String link) {
        // configura a aula, iniciando nova config se não houver, salvando no JSON
        saveLessSetAula(dia, nome, link);
        panel.jsonHandler.salvarHorarios();
    }

    public void updateHorario(String inicio, String fim) throws InputMismatchException {
        // atualiza os tempos do horário
        inicioH = MainPanel.lerHora(inicio)[0];
        inicioM = MainPanel.lerHora(inicio)[1];
        fimH = MainPanel.lerHora(fim)[0];
        fimM = MainPanel.lerHora(fim)[1];

        btns.get(0).setText(inicio + " - " + fim);
        panel.jsonHandler.salvarHorarios();
    }

    public void delete() {
        // remove a linha do horário
        for (JButton btn : btns) panel.remove(btn);
        panel.horarios.remove(this);
        panel.redrawn();
        panel.jsonHandler.salvarHorarios();
    }
}
