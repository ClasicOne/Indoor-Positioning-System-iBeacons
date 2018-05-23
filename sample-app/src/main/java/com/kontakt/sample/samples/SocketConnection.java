package com.kontakt.sample.samples;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketConnection {
    private Socket socket;
    {
        try {
            socket = IO.socket("https://websocket-server-2018.herokuapp.com/");
        }catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
