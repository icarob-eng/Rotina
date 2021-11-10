import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class JSONHandler {
    private final File file = new File("dados.json");
    private MainPanel panel;

    /*
     * organizador de JSON
     * Estrutura adotada:
     * {"2h2 - 3h3": {  // dados do horário na chave do dicionário
     *     2: ["Aula", "Link"],  // a chave deste dicionário é o dia da semana, a lista diz a aula e o link
     *     4: ["Aula", "Link"]}
     * }
     */

    public JSONHandler(MainPanel panel){
        // checa se o 'dados.json' já existe, senão o cria
        try {
            file.createNewFile();  // retorna se o arquivo foi criado
            this.panel = panel;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel.frame,
                    "Algo de errado não está certo. Por favor mostre isto para o desenvolvedor: \n" + e,
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void salvarHorarios(){
        // salva todos os horários do main panel em formato json
        JSONObject jHorarios = new JSONObject();
        for (Horario h : panel.horarios) {
            String i = h.inicioM + "", f = h.fimM + "";
            if (h.inicioM < 10) i = "0" + h.inicioM;
            if (h.fimM < 10) f = "0" + h.fimM;
            // esse rolê aí em cima serve para ficar 3h03 ao invés de 3h3 :)
            String tempo = h.inicioH + "h" + i + " - " + h.fimH + "h" + f;
            //  reformata a string para servir de tag no dicionário
            JSONObject jAulas = new JSONObject();
            for (Integer d = 0; d < 7; d++)
                jAulas.put(d.toString(), h.getAula(d));  // adiciona as aulas de cada dia no dicionário

            jHorarios.put(tempo, jAulas);
        }

        // salva os dados
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jHorarios.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel.frame,
                    "Algo de errado não está certo. Tivemos problemas ao salvar os dados. " +
                            "Por favor mostre isto para o desenvolvedor: \n" + e,
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void lerJSON(){
        // lê valores do arquivo e retorna horários modificados
        // não retorna objetos, pois a criação do horário se dá no próprio panel
        try (Scanner scanner = new Scanner(file)) {
            String string = scanner.nextLine();

            JSONObject jHorario = new JSONObject(string);

            for (String key : jHorario.keySet()) {
                Horario horario = panel.criarHorario(key.split(" - ")[0], key.split(" - ")[1]);

                JSONObject jAulas = jHorario.getJSONObject(key);
                for (String strDia : jAulas.keySet()) {
                    // recupera o dia, o nome e o link de cada aula de cada horário
                    int dia = Integer.parseInt(strDia);
                    String nome = jAulas.getJSONArray(strDia).getString(0);
                    String link = jAulas.getJSONArray(strDia).getString(1);
                    horario.saveLessSetAula(dia, nome, link);
                }
            }
        } catch (NoSuchElementException ignored) {  // ignora o caso de não ter elementos
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(panel.frame,
                    "Algo de errado não está certo. Tivemos problemas ao ler os dados. " +
                            "Por favor mostre isto para o desenvolvedor: \n" + e,
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
