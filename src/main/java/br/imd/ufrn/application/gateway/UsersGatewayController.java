package br.imd.ufrn.application.gateway;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Param;
import br.imd.ufrn.Annotations.Post;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Annotations.Singleton;
import br.imd.ufrn.Exceptions.RemoteException;
import br.imd.ufrn.HTTP.HTTPResponse;
import br.imd.ufrn.application.HTTP.HTTPMarshaller;
import br.imd.ufrn.application.models.Message;
import br.imd.ufrn.application.models.ServiceRecord;
import br.imd.ufrn.application.models.User;

@Singleton
@RemoteService("/users")
public class UsersGatewayController {
    // private HttpClient httpClient;
    // private ObjectMapper objectMapper;
    private static final HttpClient httpClient =
    HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private GatewayRegistry registry;
    private String protocol;

    public UsersGatewayController() {
        this.registry = new GatewayRegistry();
        this.protocol = MiddlewareConfig.getProtocol();
    }

    @Post("/heartbeat")
    public void listenHeartBeat(@Param("address") String stringAddress, @Param("port") int port)  {
        InetAddress address;

        try {
            address = InetAddress.getByName(stringAddress);
        } catch (UnknownHostException e) {
            System.out.println("Erro para inicializar o endereco no listenHearBeat do UsersGateway: " + e);
            return;
        }

        ServiceRecord service = new ServiceRecord(address, port);
        String key = address + ":" + port;
        registry.update(key, service);
    }

    @Post("/create")
    public User create(@Body User user) throws Exception {
        System.out.println("Executando o create dentro do Users gateway, com protocolo: " + this.protocol);
        // System.out.println("MessageService: de=" + senderId + " para=" + receiverId + " conteudo=" + content);
        String body;
        try {
            body = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RemoteException(
                "UserService Gateway error: Erro interno ao processar requisicao",
                500
            );
        }

        ServiceRecord service = this.registry.getNextService();

        if (service == null) {
            System.out.println("UserService Gateway error:  Nao ha servidores disponiveis!");
            throw new RemoteException(
                "UserService Gateway error:  Nao ha servidores disponiveis!",
                503
            );
        }

        System.out.println("Enviando requisicao para porta: " + service.getPort());

        if (this.protocol.equals("tcp")) {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + "127.0.0.1" + ":" + service.getPort() + "/users/create"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            try {
                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                User responseUser = objectMapper.readValue(res.body(), User.class);

                return responseUser;
            } catch (Exception e) {
                throw new RemoteException(
                    "UserService Gateway error: Erro ao comunicar com MessageService " + e,
                    502
                );
            }
        }
        else {
            /////// envio
            System.out.println("Enviando requisicao para o servidor!");
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                InetAddress serviceAddress = InetAddress.getByName("127.0.0.1");
                System.out.println("criando request");

                String request =
                    "POST /users/create HTTP/1.1\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body;

                byte[] sendData = request.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serviceAddress,
                    service.getPort()
                );
                System.out.println("enviando request pelo datagram");

                clientSocket.send(sendPacket);
                /////// envio

                /// espera resposta e responde
                System.out.println("request enviado");
                byte[] receiveData = new byte[4096];
                System.out.println("aguardando resposta do servidor");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.setSoTimeout(2000);
                clientSocket.receive(receivePacket);
                System.out.println("resposta recebida");

                /// Convertendo resposta HTTP em objeto Java da aplicação ou Exceção da aplicação
                String HTTPResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
                BufferedReader messageReader = new BufferedReader(new StringReader(HTTPResponse));
                HTTPResponse response = HTTPMarshaller.getHTTPResponse(messageReader);

                if (response == null) {
                    throw new RemoteException(
                        "UserService Gateway error: Erro receber resposta do servidor ",
                        502
                    );
                }

                System.out.println("traduzindo resposta: " + response);
                User responseUser = objectMapper.readValue(response.getBody(), User.class);

                System.out.println("retornando resposta");
                return responseUser;
                ///
            } catch (Exception e) {
                System.out.println("Erro ao enviar resposta ao serviço: " + e.getMessage());
                throw new RemoteException(
                    "UserService Gateway error: Erro enviar resposta ao cliente",
                    502
                );
            }
            
        }
    }
}
