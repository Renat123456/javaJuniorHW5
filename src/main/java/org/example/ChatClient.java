package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        System.out.println("Введите хост для подключения к чату");
        String hostServer = console.nextLine();
        Integer parsePortServer = null;
        AtomicBoolean flag = new AtomicBoolean(true);
        while (true) {
            try {
                System.out.println("Введите порт для подключения к чату");
                String portServer = console.nextLine();
                parsePortServer = Integer.parseInt(portServer);
                break;
            } catch (Exception e) {
                System.out.println("Неверный формат порта");
            }
        }

        try (Socket server = new Socket(hostServer, parsePortServer)) {
            System.out.println("Вы успешно подключились к серверу");
            try (
                    PrintWriter write = new PrintWriter(server.getOutputStream(), true);
                    Scanner read = new Scanner(server.getInputStream())
            ) {
                System.out.println("Введите 1 для ввода логина и пароля, если Вы еще не зарегистрированы то введите 2");
                String clientLogin = console.nextLine();
                if(clientLogin.equals("2")){
                    while (true){
                        System.out.println("Введите логин для общения");
                        String string = console.nextLine();
                        String message = sendMessage("RegLogin", string, null);
                        write.println(message);
                        Message messageFromServer = messageToClass(read.nextLine());
                        if (messageFromServer.getType().equals("OK")){
                            break;
                        } else {
                            System.out.println(messageFromServer.getMessage());
                        }
                    }
                    while (true){
                        System.out.println("Введите пароль для входа");
                        String string = console.nextLine();
                        String message = sendMessage("RegPass", string, null);
                        write.println(message);
                        Message messageFromServer = messageToClass(read.nextLine());
                        if (messageFromServer.getType().equals("OK")){
                            break;
                        } else {
                            System.out.println(messageFromServer.getMessage());
                        }
                    }
                }else {
                    while (true){
                        System.out.println("Введите логин и через пробел пароль для подключения к чату");
                        clientLogin = console.nextLine();
                        String message = sendMessage("Auth", clientLogin, null);
                        write.println(message);
                        Message message2 = messageToClass(read.nextLine());
                        if (message2.getType().equals("Error")) {
                            System.out.println(message2.getMessage());
                        } else {
                            break;
                        }
                    }
                }

                System.out.println("Вы успешно подключились к чату. Для отправки сообщения всем участникам просто введите сообщение. Для отправки личного сообщения введите @ и логин адресата без пробела, а затем пробел и само сообщение. Для выхода из приложения введите exit");

                new Thread(() -> {
                    while (flag.get()) {
                        Message messageFromServer = messageToClass(read.nextLine());
                        if(messageFromServer.getType().equals("Error")){
                            System.out.println("Ошибка: " + messageFromServer.getMessage());
                        }
                        if(messageFromServer.getType().equals("OK")){
                            System.out.println("Успех: " + messageFromServer.getMessage());
                        }
                        if(messageFromServer.getType().equals("Message")){
                            System.out.println("!!! " + messageFromServer.getRecipient() + ": " + messageFromServer.getMessage());
                        }
                        if(messageFromServer.getType().equals("MessageAll")){
                            System.out.println("--> " + messageFromServer.getRecipient() + ": " + messageFromServer.getMessage());
                        }
                        if(messageFromServer.getType().equals("Exit")){
                            System.out.println(messageFromServer.getMessage());
                            break;
                        }
                    }
                }).start();

                while (true) {
                    String string = console.nextLine();
                    String firstWord = string.split(" ")[0];
                    char firstChar = firstWord.charAt(0);
                    if (firstChar == '@') {
                        Message request = new Message();
                        int spaceIndex = string.indexOf(" ");
                        String remainingStr = string.substring(spaceIndex + 1);
                        request.setType("Message");
                        request.setMessage(remainingStr);
                        request.setRecipient(firstWord.substring(1));
                        String sendMsgRequest = objectMapper.writeValueAsString(request);
                        write.println(sendMsgRequest);
                    } else if(firstWord.equals("exit")){
                        Message request = new Message();
                        request.setType("Exit");
                        String sendMsgRequest = objectMapper.writeValueAsString(request);
                        write.println(sendMsgRequest);
                        flag.set(false);
                        break;
                    } else {
                        Message request = new Message();
                        request.setType("MessageAll");
                        request.setMessage(string);
                        request.setRecipient("all");
                        String sendMsgRequest = objectMapper.writeValueAsString(request);
                        write.println(sendMsgRequest);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка во время подключения к серверу: " + e.getMessage());
        }
        System.out.println("Вы отключились от сервера");
    }

    private static Message messageToClass(String text) {
        Message message = null;
        try {
            message = objectMapper.reader().readValue(text, Message.class);
            return message;
        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON: " + e.getMessage());
            return message;
        }
    }

    public static String sendMessage(String type, String text, String login) {
        Message message = new Message();
        message.setType(type);
        message.setMessage(text);
        message.setRecipient(login);
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
