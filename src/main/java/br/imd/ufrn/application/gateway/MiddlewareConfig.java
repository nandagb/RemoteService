package br.imd.ufrn.application.gateway;

public class MiddlewareConfig {
    private static String protocol = "tcp"; // Padrão TCP

    public static void setProtocol(String p) { 
        protocol = p;
    } 
    
    public static String getProtocol() { 
        return protocol;
    }
}
