package br.imd.ufrn.application.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import br.imd.ufrn.Middleware;
import br.imd.ufrn.HTTP.HTTPRequest;

public class UserServiceApplication {
    public static void main( String[] args ) {

        if (args.length == 0) {
            System.out.println("Erro! Nenhum argumento fornecido");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String protocol = args[1];

        Middleware middleware = new Middleware();
        middleware.register(UserService.class);

        new Thread( () -> sendHeartBeat(port, protocol)).start();

        middleware.start(port, protocol);
    }

    public static void sendHeartBeat(int port, String protocol) {
        switch (protocol) {
            case "tcp":
                sendTCPHeartBeat(port);
                break;
            case "udp":
                sendUDPHeartBeat(port);
                break;
        }
    }

    public static void sendTCPHeartBeat(int port) {
        HttpClient  httpClient;

        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        while(true) {
            System.out.println("Enviando Hearbeat TCP");
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + "127.0.0.1" + ":" + 8080 + "/users/heartbeat?address=127.0.0.1&port=" + port))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            try {
                httpClient.send(req, HttpResponse.BodyHandlers.discarding());
                System.out.println("heartBeat enviado!");
            } catch (IOException e) {
                System.out.println("IOException: erro ao enviar HeartBeat do User Service");
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: erro ao enviar HeartBeat  do User Service");
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: ao chamar sleep no UserService");
            }
        }
    }

    public static void sendUDPHeartBeat(int port) {
        try {
			//pode ser que mude depois
			String gatewayAdressString = "127.0.0.1";
			InetAddress gatewayAddress = InetAddress.getByName(gatewayAdressString);
            DatagramSocket serverSocket = new DatagramSocket();

			while (true) {
				///// assembling request
				HTTPRequest request = new HTTPRequest("POST " + "/messages/heartbeat?address=127.0.0.1&port=" + port + " HTTP/1.1");
				request.setHeader("Host: localhost");
				request.setHeader("Content-Type: application/json");
				int length = 0;
				request.setHeader("Content-Length: " + length);
				request.setContentLength(length);
				/////

				String msg = request.toString();
				// System.out.println("Enviando heartbeat, tum tum: ");
				// System.out.println(msg);

				byte[] heartBeatMessage = msg.getBytes();
				DatagramPacket heartBeatPacket = new DatagramPacket(heartBeatMessage, heartBeatMessage.length, gatewayAddress, 8080);
                System.out.println("heartBeat enviado!");

				serverSocket.send(heartBeatPacket);
				// Interval for sending heartbeat (every 3s)
				Thread.sleep(3000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException nfe) {
			System.out.println("NumberFormatException: Erro ao converter numero: " + nfe.getMessage());

		} catch (Exception e) {
			System.out.println("Erro inesperado: " + e.getMessage());
		}
    }
}
