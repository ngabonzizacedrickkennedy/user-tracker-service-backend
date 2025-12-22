package com.ngabonzizacedrick.user_tracker_service.config;

import com.ngabonzizacedrick.user_tracker_service.model.User;
import com.ngabonzizacedrick.user_tracker_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        userRepository.deleteAll();
        System.out.println("Cleared existing users");

        User user1 = new User();
        user1.setEmail("cedrickngabo03@gmail.com");
        user1.setLastSeen(0L);
        user1.setIp("0.0.0.0");
        user1.setPort(0);

        User user2 = new User();
        user2.setEmail("ngabocedkennedy03@gmail.com");
        user2.setLastSeen(0L);
        user2.setIp("0.0.0.0");
        user2.setPort(0);

        User user3 = new User();
        user3.setEmail("novemba42@gmail.com");
        user3.setLastSeen(0L);
        user3.setIp("0.0.0.0");
        user3.setPort(0);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        System.out.println("Initialized database with 3 users");
    }
}