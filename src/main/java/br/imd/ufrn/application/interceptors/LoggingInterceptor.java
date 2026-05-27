package br.imd.ufrn.application.interceptors;

import br.imd.ufrn.Interceptor.Interceptor;
import br.imd.ufrn.Interceptor.InvocationContext;

public class LoggingInterceptor implements Interceptor {
    @Override
    public void before( InvocationContext context) {
        System.out.println("Do something before");
        System.out.println("Imprimindo requisicao recebida: " + context.getRequest().toString());
        context.setAttribute("startTime", System.currentTimeMillis());
    }

    @Override
    public void after(InvocationContext context) {
        System.out.println("Do something after");
        context.setAttribute("endTime", System.currentTimeMillis());
        long duration = (Long) context.getAttribute("endTime") - (Long) context.getAttribute("startTime");
        context.setAttribute("elapsedTime", duration);
        System.out.println("Duracao da requisicao em millissegundos: " + context.getAttribute("elapsedTime"));
        System.out.println("Imprimindo resposta que sera retornada: " + context.getResponse().toString());
    }
    
}
