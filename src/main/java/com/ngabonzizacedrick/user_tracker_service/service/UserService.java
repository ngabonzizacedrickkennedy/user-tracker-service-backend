package com.ngabonzizacedrick.user_tracker_service.service;

import com.ngabonzizacedrick.user_tracker_service.exception.UserNotFoundException;
import com.ngabonzizacedrick.user_tracker_service.model.User;
import com.ngabonzizacedrick.user_tracker_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UdpBroadcaster udpBroadcaster;

    @Transactional
    public void updateUserActivity(String email, String ip, int port) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Instant now = Instant.now();
        long lastSeen = now.getEpochSecond() * 1_000_000_000L + now.getNano();
        
        user.setLastSeen(lastSeen);
        user.setIp(ip);
        user.setPort(port);
        
        userRepository.save(user);
        
        udpBroadcaster.broadcast(email, lastSeen, ip, port);
    }
}