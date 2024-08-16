package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    static Map<String, String> logins = new ConcurrentHashMap<>();
    static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Scanner console = new Scanner(System.in);
        Integer portServer = null;
        AtomicBoolean flag = new AtomicBoolean(true);

        try {
            if (!Files.exists(Paths.get("login.txt"))) {
                Files.createFile(Paths.get("login.txt"));
                System.out.println("Файл успешно создан.");
            }
            List<String> lines = Files.readAllLines(Paths.get("login.txt"));
            for (String line : lines) {
                String[] words = line.split(" ");
                logins.put(words[0], words[1]);
            }
        } catch (IOException e) {
            System.err.println("Произошла ошибка при чтении файла: " + e.getMessage());
        }

        while (true) {
            try {
                System.out.println("Введите порт для запуска чата, либо exit для выхода из приложения");
                String inter = console.nextLine();
                if (inter.equals("exit")) return;
                portServer = Integer.parseInt(inter);
                break;
            } catch (Exception e) {
                System.out.println("Ошибка ввода!!!");
            }
        }

        try (ServerSocket server = new ServerSocket(portServer)) {
            System.out.println("Сервер запущен");

            new Thread(() -> {
                while (true) {
                    String exit = console.nextLine();
                    if (exit.equals("exit")) {
                        flag.set(false);
                        for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
                            ClientHandler clientI = client.getValue();
                            if (clientI.clientLogin != null) {
                                clientI.sendMessage("Exit", "Сервер завершил работу", null);
                            }
                        }
                        System.out.println("Сервер завершил работу");
                        try {
                            server.close();
                        } catch (IOException e) {
                            System.err.println("Ошибка при закрытии серверного сокета: " + e.getMessage());
                        }
                        break;
                    }
                }
            }).start();

            while (flag.get()) {
                try {
                    System.out.println("Ждем клиентского подключения");
                    Socket client = server.accept();
                    ClientHandler clientHandler = new ClientHandler(client, clients);
                    new Thread(clientHandler).start();
                    System.out.println("Подключился новый пользователь");
                } catch (SocketException e) {
                    if (!flag.get()) {
                        break;
                    } else {
                        System.err.println("Ошибка при принятии клиентского подключения: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка во время работы сервера: " + e.getMessage());
        }
        System.exit(0);
    }

    private static class ClientHandler implements Runnable {
        private final Socket client;
        private final Scanner read;
        private final PrintWriter write;
        private String clientLogin;

        public ClientHandler(Socket client, Map<String, ClientHandler> clients) throws IOException {
            this.client = client;
            this.read = new Scanner(client.getInputStream());
            this.write = new PrintWriter(client.getOutputStream(), true);
        }

        @Override
        public void run() {
            while (true) {
                String messageFromClient = read.nextLine();
                Message message = getMessage(messageFromClient);

                if (message.getType().equals("RegLogin")) {
                    char firstChar = message.getMessage().charAt(0);
                    if (logins.containsKey(message.getMessage())) {
                        sendMessage("Error", "Логин уже существует", null);
                    } else if (message.getMessage().length() < 3 || message.getMessage().length() > 16 || message.getMessage().contains(" ") || firstChar == '@' || message.getMessage().isBlank()) {
                        sendMessage("Error", "Неверный формат логина", null);
                    } else {
                        logins.put(message.getMessage(), "");
                        clientLogin = message.getMessage();
                        sendMessage("OK", "Логин установлен", null);
                    }
                } else if (message.getType().equals("RegPass")) {
                    if (message.getMessage().isBlank() || message.getMessage().contains(" ")) {
                        sendMessage("Error", "Неверный формат пароля", null);
                    } else {
                        logins.put(clientLogin, message.getMessage());
                        try (FileWriter writer = new FileWriter("login.txt")) {
                            for (Map.Entry<String, String> entry : logins.entrySet()) {
                                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                            }
                            System.out.println("Файл с логинами успешно перезаписан");
                        } catch (IOException e) {
                            System.err.println("Ошибка записи в файл: " + e.getMessage());
                        }
                        sendMessage("OK", "Логин установлен", null);
                        break;
                    }
                } else if (message.getType().equals("Auth")) {
                    String[] words = message.getMessage().split(" ");
                    if(words.length < 2) {
                        sendMessage("Error", "Неверная пара логин - пароль", null);
                    } else if (clients.containsKey(words[0])) {
                        sendMessage("Error", "Пользователь с таким логином уже в чате", null);
                    } else if (!words[1].equals(logins.get(words[0]))) {
                        sendMessage("Error", "Неверная пара логин - пароль", null);
                    } else {
                        clientLogin = words[0];
                        sendMessage("OK", "Вы успешно подключены к чату", null);
                        break;
                    }
                }
            }
            clients.put(clientLogin, this);
            System.out.println("Новый пользователь зарегистрирован под логином " + clientLogin);
            System.out.println("Всего подключенных пользователей " + clients.size());

            while (true) {
                String messageFromClient = read.nextLine();
                final String type;
                final Message message;
                try {
                    message = objectMapper.reader().readValue(messageFromClient, Message.class);
                    type = message.getType();
                } catch (IOException e) {
                    System.err.println("Не удалось прочитать сообщение от клиента [" + clientLogin + "]: " + e.getMessage());
                    sendMessage("Error", "Не удалось прочитать сообщение: " + e.getMessage(), clientLogin);
                    continue;
                }

                if (type.equals("Message")) {
                    ClientHandler clientTo = clients.get(message.getRecipient());
                    if (clientTo == null) {
                        sendMessage("Error", "Клиент с логином [" + message.getRecipient() + "] не найден", clientLogin);
                        continue;
                    }
                    clientTo.sendMessage("Message", message.getMessage(), clientLogin);
                } else if (type.equals("MessageAll")) {
                    for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
                        ClientHandler clientI = client.getValue();
                        if (clientI.clientLogin != null && clientI.clientLogin != clientLogin) {
                            clientI.sendMessage("MessageAll", message.getMessage(), clientLogin);
                        }
                    }
                } else if (type.equals("Exit")) {
                    sendMessage("Exit", "Вы вышли из чата", clientLogin);
                    for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
                        ClientHandler clientI = client.getValue();
                        if (clientI.clientLogin != null && clientI.clientLogin != clientLogin) {
                            clientI.sendMessage("Exit", "Клиент с логином " + clientLogin + " вышел из чата", clientLogin);
                        }
                    }
                    System.out.println("Клиент с логином " + clientLogin + " вышел из чата");
                    break;
                } else {
                    break;
                }
            }
            doClose();
        }

        private void doClose() {
            try {
                read.close();
                write.close();
                client.close();
                clients.remove(clientLogin);
            } catch (IOException e) {
                System.err.println("Ошибка во время отключения клиента: " + e.getMessage());
            }
        }

        public void sendMessage(String type, String text, String login) {
            Message message = new Message();
            message.setType(type);
            message.setMessage(text);
            message.setRecipient(login);
            try {
                String sendMessage = objectMapper.writeValueAsString(message);
                write.println(sendMessage);
            } catch (JsonProcessingException e) {
                System.err.println("Ошибка создания json: " + e.getMessage());
            }
        }

        public Message getMessage(String text) {
            Message message = null;
            try {
                message = objectMapper.reader().readValue(text, Message.class);
                return message;
            } catch (IOException e) {
                return message;
            }
        }
    }
}