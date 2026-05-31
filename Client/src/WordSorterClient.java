import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class WordSorterClient extends JFrame {
    private static final String SERVER_HOST = "ТВОЁ-ИМЯ-СЕРВИСА.onrender.com";

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton sortButton;
    private JLabel statusLabel;

    public WordSorterClient() {
        setTitle("Сортировщик слов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        inputArea = new JTextArea(5, 40);
        outputArea = new JTextArea(10, 40);
        sortButton = new JButton("Сортировать слова");
        statusLabel = new JLabel("Статус: Готов к работе");

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Введете текст:"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JLabel("Отсортированные уникальные слова:"), BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.add(sortButton);
        buttonPanel.add(buttonContainer, BorderLayout.CENTER);
        buttonPanel.add(statusLabel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        sortButton.addActionListener(e -> sortWords());
    }

    private void sortWords() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите текст!");
            return;
        }

        sortButton.setEnabled(false);
        statusLabel.setText("Статус: Подключение к серверу...");

        try {
            URL url = new URL("https://" + SERVER_HOST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = text.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim()).append("\n");
                }
                outputArea.setText(response.toString());
                statusLabel.setText("Статус: Успешно");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Ошибка при подключении к серверу: " + ex.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Статус: Ошибка подключения");
        } finally {
            sortButton.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WordSorterClient().setVisible(true);
        });
    }
}
