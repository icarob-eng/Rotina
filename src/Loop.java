import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Loop {
    // loop principal do aplicativo, responsável por consultar os horários e abrir as aulas na hora certa

    private static final int ANTECIP = 4, DELAY = 60;  // tempo de antecipação, em minutos e tempo de espera, em segundos

    public static void start(MainPanel panel){
        new Thread(() -> {
            String aulaAberta = "";  // string com a aula que foi está aberta

            while (true) {

                Horario horarioAtual = null;
                // para cada horário, se começou e não terminou, retorna o atual
                for (Horario h : panel.horarios)
                    if (passou(hNow(), minNow(), h.inicioH, h.inicioM)
                            && !passou(hNow(), minNow(), h.fimH, h.fimM)) {
                        horarioAtual = h;
                        break;
                    }

                if (horarioAtual != null) {
                    String aula = horarioAtual.getAula(diaSemana())[0];
                    String link = horarioAtual.getAula(diaSemana())[1];

                    if (! aulaAberta.equals(aula) && ! aula.equals("")) {
                        // se a aula atual não estiver aberta e não for vazia
                        aulaAberta = aula;  // atualiza a aula
                        openInBrowser(link, aula, panel);

                        /*
                         *
                         * Inserir funções extras aqui !!!
                         *
                        */
                    }
                } else { aulaAberta = "";}  // se o horário for nulo limpa a aula aberta

                try {Thread.sleep(DELAY * 1000L);} catch (InterruptedException ignored) {}
            }
        }).start();
        // loop principal para checar horários
    }

    public static int hNow() { return LocalTime.now().getHour();}

    public static int minNow() { return LocalTime.now().getMinute() - ANTECIP;}

    public static int diaSemana() { return LocalDate.now().getDayOfWeek().getValue() % 7;}

    public static boolean passou(int xh, int xm, int yh, int ym){
        // retorna true se o horário de 'xh:xm' for maior ou igual ao horário de 'yh:ym'
        return xh > yh  // se a hora é maior
                || xh == yh && xm > ym;  // se a hora é a mesma e os minutos não
    }

    public static void openInBrowser(String url, String aula, MainPanel panel){
        /*
         * Função de abrir navegador padrão, para múltiplos sistemas operacionais.
         * Adaptado da classe feita por Dave em https://stackoverflow.com/a/54869038.
         * A aula e o panel são necessários para ajudar nas mensagens de erro.
        */

        String myOS = System.getProperty("os.name").toLowerCase();

        try {
            URI uri = new URI(url);  // uri feito aqui para checar se o url é válido

            if(myOS.contains("win")) Desktop.getDesktop().browse(uri);
            else {
                Runtime runtime = Runtime.getRuntime();

                // Apples
                if(myOS.contains("mac")) runtime.exec("open " + url);
                else // Linux flavours
                    if(myOS.contains("nix") || myOS.contains("nux")) runtime.exec("xdg-open " + url);
                else JOptionPane.showMessageDialog(panel.frame,
                            "Sistema operacional não suportado ou não reconhecido." +
                                    "Fale com o desenvolvedor para mais informações.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch(IOException e) {
            JOptionPane.showMessageDialog(panel.frame,
                    "Algo de errado não está certo. Por favor mostre isto para o desenvolvedor: \n" + e,
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch(URISyntaxException e) {
            JOptionPane.showMessageDialog(panel.frame,
                    "O link \"" + url + "\" da aula \"" + aula + "\" aparentemente não é um link válido.",
                    "Link inválido", JOptionPane.ERROR_MESSAGE);
        }
    }
}
