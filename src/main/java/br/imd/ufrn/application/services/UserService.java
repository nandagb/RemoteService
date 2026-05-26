package br.imd.ufrn.application.services;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Post;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.application.models.User;

@RemoteService("/users")
public class UserService {
    // Requisição: POST /users/create
    @Post("/create")
    public User send(@Body User user) {
        try {
            String name = user.getName();
            String number = user.getNumber();
            
            System.out.println("Executando o send dentro do UserService");

            System.out.println("UserService: nome=" + name + " numero=" + number);
            
            // Aqui: persistência
            user.setId(1);
            return user;
        } catch (Exception e) {
            System.out.println("UserService error: " + e.getMessage());
            return null;
        }
    }
}
