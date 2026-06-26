package org.stepan1411.pvp_bot.stats.local;

import com.sun.net.httpserver.HttpExchange;
import java.util.concurrent.CopyOnWriteArrayList;

public class SseBroadcaster {

    private final CopyOnWriteArrayList<HttpExchange> clients = new CopyOnWriteArrayList<>();

    public void addClient(HttpExchange exchange) {
        clients.add(exchange);
    }

    public void removeClient(HttpExchange exchange) {
        clients.remove(exchange);
    }

    public void broadcast(String data) {
        for (HttpExchange client : clients) {
            try {
                synchronized (client) {
                    client.getResponseBody().write(("data: " + data + "\n\n").getBytes());
                    client.getResponseBody().flush();
                }
            } catch (Exception e) {
                clients.remove(client);
            }
        }
    }
}
