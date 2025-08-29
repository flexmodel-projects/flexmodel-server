package tech.wetech.flexmodel.application;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatApplicationService {

    public void sendMessage(String message) {
        System.out.println("Sending message: " + message);
    }

}