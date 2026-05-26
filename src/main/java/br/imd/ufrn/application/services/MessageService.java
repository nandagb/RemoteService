package br.imd.ufrn.application.services;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Post;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.application.models.Message;

@RemoteService("/messages")
public class MessageService {
    // Requisição: POST /messages/send
    @Post("/send")
    public Message send(@Body Message message) {
        try {
            int senderId = message.getSenderId();
            int receiverId = message.getReceiverId();
            String content = message.getContent();
            
            System.out.println("Executando o send dentro do MessageService");

            System.out.println("MessageService: de=" + senderId + " para=" + receiverId + " conteudo=" + content);

            // Aqui: persistência
            message.setId(1);
            return message;
        } catch (Exception e) {
            System.out.println("MessageService error: " + e.getMessage());
            return null;
        }
    }
}
