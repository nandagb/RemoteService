package br.imd.ufrn.application.gateway;

import br.imd.ufrn.Middleware;
import br.imd.ufrn.application.interceptors.LoggingInterceptor;

public class APIGatewayApplication {
    public static void main( String[] args ) {
        if (args.length == 0) {
            System.out.println("Erro! Nenhum argumento fornecido");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String protocol = args[1];

        MiddlewareConfig.setProtocol(protocol);

        Middleware middleware = new Middleware();

        middleware.registerInterceptor(new LoggingInterceptor());
        //classe com anotações de messages
        middleware.register(MessagesGatewayController.class);
        //classe com anotações de user
        middleware.register(UsersGatewayController.class);
        middleware.start(port, protocol);
    }
}
