package de.conet.srtp.playground.wsclient;

import java.net.URI;
import java.net.URISyntaxException;

public class JavaSrtcClient {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        WebSocketClient client = new WebSocketClient(new URI("ws://localhost:8080/socket"));
        while (true) {
            Thread.sleep(3000L);
        }
    }
}
