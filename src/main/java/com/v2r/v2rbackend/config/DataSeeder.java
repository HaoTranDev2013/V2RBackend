package com.v2r.v2rbackend.config;

import com.v2r.v2rbackend.entity.Role;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.RoleRepository;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSubscriptions();
    }

    private void seedAdminUser() {
        // Check if admin user already exists
        if (userRepository.findByEmail("v2radmin@gmail.com").isPresent()) {
            System.out.println("✅ Admin user already exists: v2radmin@gmail.com");
            return;
        }

        // Get or create ADMIN role
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    System.out.println("📝 Creating ADMIN role...");
                    Role newAdminRole = new Role();
                    newAdminRole.setRoleName("ADMIN");
                    return roleRepository.save(newAdminRole);
                });

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail("v2radmin@gmail.com");
        adminUser.setPassword(passwordEncoder.encode("v2r@admin"));
        adminUser.setFullName("V2R Administrator");
        adminUser.setRole(adminRole);
        adminUser.setVerified(true); // Admin is pre-verified
        adminUser.setStatus(true); // Active status
        adminUser.setNumberOfModel(-1); // Unlimited models for admin
        
        userRepository.save(adminUser);
        
        System.out.println("✅ Admin user created successfully!");
        System.out.println("   Email: v2radmin@gmail.com");
        System.out.println("   Password: v2r@admin");
        System.out.println("   Role: ADMIN");
        System.out.println("   Status: Active & Verified");
        System.out.println("   Models: Unlimited");
    }

    private void seedSubscriptions() {
        System.out.println("\n📦 Seeding Subscription Plans...");

        // Basic Plan
        if (subscriptionRepository.findAll().stream()
                .noneMatch(s -> "Basic".equalsIgnoreCase(s.getName()))) {
            Subscription basic = new Subscription();
            basic.setName("Basic");
            basic.setPrice("199000");
            basic.setNumberOfModel(10);
            basic.setStatus(true);
            subscriptionRepository.save(basic);
            System.out.println("✅ Basic Plan created: 10 models - 199,000 VND");
        } else {
            System.out.println("✓ Basic Plan already exists");
        }

        // Pro Plan
        if (subscriptionRepository.findAll().stream()
                .noneMatch(s -> "Pro".equalsIgnoreCase(s.getName()))) {
            Subscription pro = new Subscription();
            pro.setName("Pro");
            pro.setPrice("499000");
            pro.setNumberOfModel(30);
            pro.setStatus(true);
            subscriptionRepository.save(pro);
            System.out.println("✅ Pro Plan created: 30 models - 499,000 VND");
        } else {
            System.out.println("✓ Pro Plan already exists");
        }

        // Enterprise Plan
        if (subscriptionRepository.findAll().stream()
                .noneMatch(s -> "Enterprise".equalsIgnoreCase(s.getName()))) {
            Subscription enterprise = new Subscription();
            enterprise.setName("Enterprise");
            enterprise.setPrice("4999000");
            enterprise.setNumberOfModel(-1); // Unlimited
            enterprise.setStatus(true);
            subscriptionRepository.save(enterprise);
            System.out.println("✅ Enterprise Plan created: Unlimited models - 4,999,000 VND");
        } else {
            System.out.println("✓ Enterprise Plan already exists");
        }

        System.out.println("✅ Subscription seeding completed!\n");
    }
}
