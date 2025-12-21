package com.ngabonzizacedrick.user_tracker_service.config;

import com.ngabonzizacedrick.user_tracker_service.model.User;
import com.ngabonzizacedrick.user_tracker_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            // Check if users already exist
            if (repository.count() == 0) {
                // Create test users
                User user1 = new User();
                user1.setEmail("client@example.com");
                user1.setLastSeen(0L);
                user1.setIp("0.0.0.0");
                user1.setPort(0);
                repository.save(user1);

                User user2 = new User();
                user2.setEmail("admin@example.com");
                user2.setLastSeen(0L);
                user2.setIp("0.0.0.0");
                user2.setPort(0);
                repository.save(user2);

                User user3 = new User();
                user3.setEmail("test@example.com");
                user3.setLastSeen(0L);
                user3.setIp("0.0.0.0");
                user3.setPort(0);
                repository.save(user3);

                System.out.println("✅ Sample users created: client@example.com, admin@example.com, test@example.com");
            } else {
                System.out.println("✅ Users already exist in database");
            }
        };
    }
}