package com.exemplo.services;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CallbackServer {

    private HttpServer server;
    private final CompletableFuture<String> authCodeFuture = new CompletableFuture<>();

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(9999), 0);
        server.createContext("/callback", httpExchange -> {
            String query = httpExchange.getRequestURI().getQuery();
            String authCode = null;
            if (query != null && query.contains("code=")) {
                authCode = query.split("code=")[1].split("&")[0];

                // Primeiro, completa o Future para "liberar" a thread que está esperando.
                authCodeFuture.complete(authCode);
            }
            // Envia
            // uma resposta visual para o usu&aacute;rio no navegador.




            String response = """
    <html>
    <head>
        <style>
            body {
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                margin: 0;
                background-color: #f0f0f0;
                font-family: Arial, sans-serif;
            }
            .message-box {
                background-color: #ffffff;
                padding: 30px;
                border-radius: 10px;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                text-align: center;
            }
            h1 {
                color: #4CAF50;
                margin-bottom: 10px;
            }
            p {
                color: #333;
            }
        </style>
    </head>
    <body>
        <div class="message-box">
            <h1>Login bem-sucedido!</h1>
            <p>Pode fechar esta janela e voltar para o aplicativo.</p>
        </div>
    </body>
    </html>
    """;

            httpExchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = httpExchange.getResponseBody()) {
               os.write(response.getBytes());
            }

            // A MUDANÇA CRÍTICA: Desliga o servidor em uma nova thread com um pequeno delay.
            // Isso dá tempo para a thread principal receber o valor do Future.
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Meio segundo de pausa.
                    stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor de callback iniciado na porta 9999...");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Servidor de callback finalizado.");
        }
    }

    public CompletableFuture<String> getAuthCodeFuture() {
        return authCodeFuture;
    }
}