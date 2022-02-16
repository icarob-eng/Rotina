import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class MainPanel extends JPanel {

    // configs padrão
    private static final int ALTURA = 30, AFASTAMENTOX = 20, AFASTAMENTOY = 30, LARGURA_HORARIO = 120, LARGURA_DIAS = 120;
    private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK, 1);
    private static final String[] SEMANA = {"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"};

    // elementos de layout
    public final MainFrame frame;
    private JButton btnAddHorario;

    // variáveis
    volatile ArrayList<Horario> horarios = new ArrayList<>();
    JSONHandler jsonHandler;

    public MainPanel(MainFrame frame) {
        setLayout(null);
        this.frame = frame;

        JLabel txtLegenda = new JLabel("Clique nos botões para editar. " +
                "Se não adicionar nenhuma aula à um novo horário ele pode não ser salvo.");
        txtLegenda.setBounds(AFASTAMENTOX - 10, 0, 700, ALTURA);
        add(txtLegenda);

        JComboBox<String> aparencias = new JComboBox<>(frame.lookAndFeelOptions);
        // seletor de aparências
        aparencias.setSelectedIndex(frame.getLookAndFeel());
        aparencias.addActionListener(l -> {
            try{
                frame.selectLookAndFeel(aparencias.getSelectedIndex());
                JOptionPane.showMessageDialog(frame, "Reinicie a aplicação para ativar aparência.");
            } catch (UnsupportedLookAndFeelException e) {
                JOptionPane.showMessageDialog(frame,
                        "Error ao configurar aparência. Por favor escolha outra.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                        "Error ao salvar aparência.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        aparencias.setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * 7 - 100, 0, 100, ALTURA);
        add(aparencias);  // largura de 100

        JLabel txtLook = new JLabel("Aparência: ");
        txtLook.setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * 7 - 200, 0, 100, ALTURA);
        txtLook.setHorizontalAlignment(SwingConstants.LEFT);
        add(txtLook);  // largura de 100 + JComboBox

        // configura a legenda da coluna de horários
        JLabel txtHorarios = new JLabel("Horários");
        txtHorarios.setBounds(AFASTAMENTOX, AFASTAMENTOY, LARGURA_HORARIO, ALTURA);
        txtHorarios.setBorder(BORDER);
        txtHorarios.setHorizontalAlignment(SwingConstants.CENTER);
        add(txtHorarios);

        // configura as legendas das colunas de dias da semana
        for (int d = 0; d < 7; d++) {
            // para cada dia da semana, adiciona uma nova legenda
            JLabel dia = new JLabel(SEMANA[d]);
            dia.setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * d, AFASTAMENTOY, LARGURA_DIAS, ALTURA);
            dia.setBorder(BORDER);
            dia.setHorizontalAlignment(SwingConstants.CENTER);
            add(dia);
        }

        jsonHandler = new JSONHandler(this);
        jsonHandler.lerJSON();
        jsonHandler.salvarHorarios();

        redrawn();
        Loop.start(this);
    }

    private void addHorarioVazio() {
        // adiciona botão para adicionar horário

        // se o botão já existir, o remove primeiro
        if(btnAddHorario != null) remove(btnAddHorario);

        int y = AFASTAMENTOY + ALTURA * (1 + 2 * horarios.size());

        btnAddHorario = new JButton("+");
        btnAddHorario.setBounds(AFASTAMENTOX, y, LARGURA_HORARIO, ALTURA);
        btnAddHorario.setHorizontalAlignment(SwingConstants.CENTER);
        btnAddHorario.addActionListener(l -> novoHorarioDialog());
        btnAddHorario.setMargin(new Insets(0,0,0,0));
        add(btnAddHorario);

        // redimensiona o layout com os novos botões
        if (y + ALTURA + AFASTAMENTOY > frame.getHeight())
            frame.setSize(frame.getWidth(), frame.getHeight() + 2 * ALTURA);

        // repinta o painel
        revalidate();
        repaint();
    }

    public static int[] lerHora(String horario) throws InputMismatchException {
        /* Função que lê hora no formato 'horahmin'
         * Ex: 9h30.
         */
        try {
            String hora = horario.split("h")[0];
            String min = horario.split("h")[1];

            // permite formados como 02h02
            if (hora.charAt(0) == '0') hora = Character.toString(hora.charAt(1));
            if (min.charAt(0) == '0') min = Character.toString(min.charAt(1));

            return new int[]{Integer.parseInt(hora), Integer.parseInt(min)};
        } catch (Exception e){
            throw new InputMismatchException("Entrada invalida.");
        }
    }

    private void novoHorarioDialog(){
        // cria um diálogo perguntando o início e fim dos horários
        DialogPanel dialog = new DialogPanel("Horário inicial:", "Horário final:");
        int option = JOptionPane.showConfirmDialog(
                null, dialog,
                "Entre com os hórarios", JOptionPane.OK_CANCEL_OPTION);

        // quando o usuário confirma
        if (option == JOptionPane.OK_OPTION) try {
            // testa as inputs
            String inicio = dialog.getValues()[0];
            String fim = dialog.getValues()[1];

            // checa se o início é antes do fim
            if(Loop.passou(
                    lerHora(inicio)[0], lerHora(inicio)[1],
                    lerHora(fim)[0], lerHora(fim)[1]
            ))  throw new IllegalArgumentException("Início depois do fim");

            criarHorario(inicio, fim);
        } catch (InputMismatchException e) {
            // no caso de um erro, mostra essa mensagem
            JOptionPane.showMessageDialog(frame,
                    "O formato de hora deve ser 03h42 ou 0h42.",
                    "Entrada inválida", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            // no caso em que o início está depois do fim
            JOptionPane.showMessageDialog(frame,
                    "O início do horário está depois do fim. Vou lhe dar outro paradoxo: (clique em Ok)",
                    "Paradoxo", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(frame,
                    "O próximo alerta é falso.",
                    "Paradoxo", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(frame,
                    "O alerta anterior é verdadeiro.",
                    "Paradoxo", JOptionPane.ERROR_MESSAGE);
        }

    }

    public Horario criarHorario(String inicio, String fim) {
        // cria as variáveis e botões para criar horário
        ArrayList<JButton> btns = new ArrayList<>();  // lista com os botões do horário
        ArrayList<JLabel> labels = new ArrayList<>();  // lista com os botões do horário

        int y = AFASTAMENTOY + ALTURA * (1 + 2 * horarios.size());  // alinhamento horizontal da linha

        // cria botão de configurar horário
        JButton btnHorario = new JButton(inicio + " - " + fim);
        btnHorario.setBounds(AFASTAMENTOX, y, LARGURA_HORARIO, ALTURA);
        btnHorario.setHorizontalAlignment(SwingConstants.CENTER);
        btnHorario.setMargin(new Insets(0, 0, 0, 0));
        add(btnHorario);
        btns.add(btnHorario);

        // cria legenda de links, abaixo do botão de configurar horário
        JLabel labelHorario = new JLabel("Links:");
        labelHorario.setBounds(AFASTAMENTOX, y + ALTURA, LARGURA_HORARIO, ALTURA);
        labelHorario.setHorizontalAlignment(SwingConstants.CENTER);
        labelHorario.setVerticalAlignment(SwingConstants.TOP);
        add(labelHorario);
        labels.add(labelHorario);

        for (int d = 0; d < 7; d++) {
            // cria botões do horário para cada dia da semana
            JButton btnAula = new JButton("+");
            btnAula.setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * d, y, LARGURA_DIAS, ALTURA);
            btnAula.setHorizontalAlignment(SwingConstants.CENTER);
            btnAula.setMargin(new Insets(0, 0, 0, 0));
            add(btnAula);
            btns.add(btnAula);

            // cria legendas com os links de cada aula, abaixo dos botões de editar
            JLabel labelLink = new JLabel("---");
            labelLink.setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * d, y + ALTURA, LARGURA_DIAS, ALTURA);
            textFitter(labelLink);
            labelLink.setHorizontalAlignment(SwingConstants.CENTER);
            labelLink.setVerticalAlignment(SwingConstants.TOP);
            add(labelLink);
            labels.add(labelLink);
        }

        // cria e adiciona o horário à lista
        Horario horario = new Horario(lerHora(inicio)[0], lerHora(inicio)[1],
                lerHora(fim)[0], lerHora(fim)[1],
                this, btns, labels);
        horarios.add(horario);

        // botão de configuração de horário
        btnHorario.addActionListener(l -> {
            // dialogo perguntando novos horários
            DialogPanel dialog = new DialogPanel("Horário inicial:", "Horário final:", inicio, fim);
            int optionR = JOptionPane.showOptionDialog(this, dialog,
                    "Entre com os hoŕarios", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, new String[]{"Ok", "Apagar", "Cancelar"}, "Ok");

            // resposta Ok
            if (optionR == 0) try {
                // checa se o início é antes do fim
                String rInicio = dialog.getValues()[0];
                String rFim = dialog.getValues()[1];

                if(Loop.passou(
                        lerHora(rInicio)[0], lerHora(rInicio)[1],
                        lerHora(rFim)[0], lerHora(rFim)[1]
                ))  throw new IllegalArgumentException("Início depois do fim");

                // tenta atualizar, exceto se houver erro
                horario.updateHorario(rInicio, rFim);
                ordenarHorarios();
            } catch (InputMismatchException e) {
                // no caso de um erro, mostra essa mensagem
                JOptionPane.showMessageDialog(frame,
                        "O formato de hora deve ser 03h42 ou 0h42.",
                        "Entrada inválida", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                // no caso em que o início está depois do fim
                JOptionPane.showMessageDialog(frame,
                        "O início do horário está depois do fim.",
                        "Paradoxo", JOptionPane.ERROR_MESSAGE);
            } else if (optionR == 1) {
                // resposta Apagar
                horarios.remove(horario);
                horario.delete();
            }
        });


        // configura listeners dos botões das aulas
        for (int b = 1; b < horario.btns.size(); b++) {
            JButton btn = horario.btns.get(b);
            JLabel label = horario.labels.get(b);
            int dia = b - 1;

            btn.addActionListener(l -> {
                // pega os valores antigos pré selecionados
                String nome = horario.getAula(dia)[0];
                String link = horario.getAula(dia)[1];

                // configura dialogo de edição de aula
                DialogPanel dialogAula = new DialogPanel("Nome da aula (abreviado):", "Link a ser aberto:",
                        nome, link);

                int optionR = JOptionPane.showOptionDialog(this, dialogAula,
                        "Dados da aula", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, new String[]{"Ok", "Apagar", "Abrir", "Cancelar"}, "Ok");

                // recupera os valores selecionados
                String nomeR = dialogAula.getValues()[0];
                String linkR = dialogAula.getValues()[1];

                if (optionR == 0) {
                    // resposta Ok

                    // atualiza os valores
                    horario.setAula(dia, nomeR, linkR);
                    btn.setText(nomeR);
                    label.setText(linkR);
                    textFitter(label);

                } else if (optionR == 1) {
                    // resposta Apagar
                    horario.setAula(dia, "", "");
                    btn.setText("+");
                } else if (optionR == 2) {
                    Loop.openInBrowser(link, nome, this);
                }
            });
        }

        ordenarHorarios();

        return horario;
    }



    public void redrawn(){
        // função de redesenhar as linhas
        for (int h = 0; h < horarios.size(); h++) {
            int y = AFASTAMENTOY + ALTURA * (1 + 2 * h);
            Horario horario = horarios.get(h);
            horario.btns.get(0).setBounds(AFASTAMENTOX, y, LARGURA_HORARIO, ALTURA);

            for (int d = 1; d < horario.btns.size(); d++) {
                horario.btns.get(d).setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * (d - 1), y,
                        LARGURA_DIAS, ALTURA);
                horario.labels.get(d).setBounds(AFASTAMENTOX + LARGURA_HORARIO + LARGURA_DIAS * (d - 1), y + ALTURA,
                        LARGURA_DIAS, ALTURA);
                if (! horario.getAula(d - 1)[0].equals("")) {
                    horario.btns.get(d).setText(horario.getAula(d - 1)[0]);
                    horario.labels.get(d).setText(horario.getAula(d - 1)[1]);
                } else {
                    horario.btns.get(d).setText("+");
                    horario.labels.get(d).setText("---");
                }
                textFitter(horario.labels.get(d));
            }
        }
        addHorarioVazio();
    }

    public void ordenarHorarios(){
        // reordena todos os horários da classe em ordem crescente de conforme o horário de início
        horarios.sort(Comparator.comparingInt((Horario h) -> h.inicioH));
        redrawn();
    }

    public static void textFitter(JLabel label) {
        /*
         * Função para alterar o tamanho da fonte da label de modo que caiba o máximo de texto,
         * removendo textos desnecessários, e quebrando a linha
         * Adaptado deste método: https://stackoverflow.com/a/2715279
         * Não faz nada se o novo tamanho for maior que o anterior ou menor que 8.
         * Provavelmente não é a melhor implementação...
         */

        int oldFontSize = label.getFont().getSize();  // tamanho anterior da fonte
        Font labelFont = label.getFont();
        String labelText = label.getText();

        // remove "ruido" da label
        labelText = labelText.replaceAll("https?://", "");
        labelText = labelText.replaceAll("www\\.", "");

        // cirurgia de string para quebrar a linha do url entre o domínio e o caminho
        String newText = labelText;
        int splitAt = labelText.indexOf('/');
        if (splitAt > 0) newText = "<html>" + labelText.substring(0, splitAt) + "<br>"
                + labelText.substring(splitAt) + "</html>";
        label.setText(newText);

        int stringWidth = label.getFontMetrics(labelFont).stringWidth(labelText);
        int componentWidth = label.getWidth();

        // Find out how much the font can grow in width.
        double widthRatio = (double)componentWidth / (double)stringWidth;

        int newFontSize = (int)(labelFont.getSize() * widthRatio);

        // Seleciona um novo tamanho de fonte se for menor que o anterior se for maior que 8
        // TAMANHO MÍNIMO DE FONTE = 8
        int fontSizeToUse = Math.max(Math.min(newFontSize, oldFontSize), 8);

        // Set the label's font size to the newly determined size.
        label.setFont(new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse));

    }
}
