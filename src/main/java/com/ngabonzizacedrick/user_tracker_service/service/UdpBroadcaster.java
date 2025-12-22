package com.ngabonzizacedrick.user_tracker_service.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Component
@Slf4j
public class UdpBroadcaster {

    private static final int SOURCE_PORT = 6666;
    private static final int DESTINATION_PORT = 6667;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    
    private DatagramSocket socket;

    @PostConstruct
    public void init() throws SocketException {
        socket = new DatagramSocket(SOURCE_PORT);
        socket.setBroadcast(true);
        log.info("UDP Broadcaster initialized on port {}", SOURCE_PORT);
    }

    public void broadcast(String email, long lastSeen, String ip, int port) {
        try {
            byte[] data = serializeToBytes(email, lastSeen, ip, port);
            
            InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddr, DESTINATION_PORT);
            
            socket.send(packet);
            log.info("Broadcasted user activity: email={}, lastSeen={}, ip={}, port={}", 
                    email, lastSeen, ip, port);
        } catch (IOException e) {
            log.error("Failed to broadcast UDP message", e);
        }
    }

    private byte[] serializeToBytes(String email, long lastSeen, String ip, int port) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeUTF(email);
        dos.writeLong(lastSeen);
        dos.writeUTF(ip);
        dos.writeInt(port);
        
        dos.flush();
        return baos.toByteArray();
    }

    @PreDestroy
    public void cleanup() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            log.info("UDP Broadcaster socket closed");
        }
    }
}