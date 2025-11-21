package com.v2r.v2rbackend.config;

import com.v2r.v2rbackend.entity.Order;
import com.v2r.v2rbackend.entity.OrderDetail;
import com.v2r.v2rbackend.entity.Role;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.OrderRepository;
import com.v2r.v2rbackend.repository.RoleRepository;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedSubscriptions();
        seedRegularUsers();
        seedOrders();
    }

    private void seedRoles() {
        System.out.println("\n👥 Seeding Roles...");

        // Seed ADMIN role (should be ID 1)
        if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            roleRepository.save(adminRole);
            System.out.println("✅ ADMIN role created (ID: " + adminRole.getRoleID() + ")");
        } else {
            Role existingAdmin = roleRepository.findByRoleName("ADMIN").get();
            System.out.println("✓ ADMIN role already exists (ID: " + existingAdmin.getRoleID() + ")");
        }

        // Seed USER role (should be ID 2)
        if (roleRepository.findByRoleName("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setRoleName("USER");
            roleRepository.save(userRole);
            System.out.println("✅ USER role created (ID: " + userRole.getRoleID() + ")");
        } else {
            Role existingUser = roleRepository.findByRoleName("USER").get();
            System.out.println("✓ USER role already exists (ID: " + existingUser.getRoleID() + ")");
        }

        // Seed BUYER role (should be ID 3)
        if (roleRepository.findByRoleName("BUYER").isEmpty()) {
            Role buyerRole = new Role();
            buyerRole.setRoleName("BUYER");
            roleRepository.save(buyerRole);
            System.out.println("✅ BUYER role created (ID: " + buyerRole.getRoleID() + ")");
        } else {
            Role existingBuyer = roleRepository.findByRoleName("BUYER").get();
            System.out.println("✓ BUYER role already exists (ID: " + existingBuyer.getRoleID() + ")");
        }

        System.out.println("✅ Role seeding completed!\n");
    }

    private void seedAdminUser() {
        System.out.println("\n👑 Seeding Admin Users...");

        // Get or create ADMIN role
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    System.out.println("📝 Creating ADMIN role...");
                    Role newAdminRole = new Role();
                    newAdminRole.setRoleName("ADMIN");
                    return roleRepository.save(newAdminRole);
                });

        // Admin 1: v2radmin@gmail.com
        if (userRepository.findByEmail("v2radmin@gmail.com").isEmpty()) {
            User adminUser1 = new User();
            adminUser1.setEmail("v2radmin@gmail.com");
            adminUser1.setPassword(passwordEncoder.encode("v2r@admin"));
            adminUser1.setFullName("V2R Administrator");
            adminUser1.setRole(adminRole);
            adminUser1.setVerified(true);
            adminUser1.setStatus(true);
            adminUser1.setNumberOfModel(-1);
            
            userRepository.save(adminUser1);
            
            System.out.println("✅ Admin user created successfully!");
            System.out.println("   Email: v2radmin@gmail.com");
            System.out.println("   Password: v2r@admin");
        } else {
            System.out.println("✓ Admin user already exists: v2radmin@gmail.com");
        }

        // Admin 2: nhatanh011204@outlook.com
        if (userRepository.findByEmail("nhatanh011204@outlook.com").isEmpty()) {
            User adminUser2 = new User();
            adminUser2.setEmail("nhatanh011204@outlook.com");
            adminUser2.setPassword(passwordEncoder.encode("v2radmin@"));
            adminUser2.setFullName("Nhat Anh Administrator");
            adminUser2.setRole(adminRole);
            adminUser2.setVerified(true);
            adminUser2.setStatus(true);
            adminUser2.setNumberOfModel(-1);
            
            userRepository.save(adminUser2);
            
            System.out.println("✅ Admin user created successfully!");
            System.out.println("   Email: nhatanh011204@outlook.com");
            System.out.println("   Password: v2radmin@");
        } else {
            System.out.println("✓ Admin user already exists: nhatanh011204@outlook.com");
        }

        System.out.println("   Role: ADMIN");
        System.out.println("   Status: Active & Verified");
        System.out.println("   Models: Unlimited");
        System.out.println("✅ Admin users seeding completed!\n");
    }

    private void seedSubscriptions() {
        System.out.println("\n📦 Seeding Subscription Plans...");

        // Basic Plan
        if (subscriptionRepository.findAll().stream()
                .noneMatch(s -> "Basic".equalsIgnoreCase(s.getName()))) {
            Subscription basic = new Subscription();
            basic.setName("Basic");
            basic.setPrice("190000");
            basic.setNumberOfModel(10);
            basic.setStatus(true);
            subscriptionRepository.save(basic);
            System.out.println("✅ Basic Plan created: 10 models - 190,000 VND");
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

    private void seedRegularUsers() {
        System.out.println("\n👤 Seeding Regular Users...");

        // Get or create USER role
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    System.out.println("📝 Creating USER role...");
                    Role newUserRole = new Role();
                    newUserRole.setRoleName("USER");
                    return roleRepository.save(newUserRole);
                });

        // List of user emails to seed
        String[] userEmails = {
            "phillipvothien306@gmail.com",
            "nhungthyse184902@fpt.edu.vn",
            "notnagisapham1103@gmail.com",
            "datkhoi224@gmail.com",
            "quannmse182197@fpt.edu.vn",
            "huyhnse182014@fpt.edu.vn",
            "hatanphong8@gmail.com",
            "Ckhoa269@gmail.com",
            "huynhthuchadoan99@gmail.com",
            "anhtuanphanminh206@gmail.com",
            "anhlam2103@gmail.com",
            "nguyenthithuydung.nbk@gmail.com",
            "nguyentriminhgc@gmail.com",
            "ledinhduy09042005@gmail.com",
            "huynhgiahuydao1717@gmail.com",
            "greeenma052@gmail.com",
            "dangminhtuanan9999@gmail.com",
            "thinhkg644@gmail.com",
            "vuquangphihoang124@gmail.com",
            "nghi050609@gmail.com",
            "tranphuongnamrg2004@gmail.com",
            "giahienpk123@gmail.com"
        };

        String defaultPassword = "123456@";
        int createdCount = 0;
        int existingCount = 0;

        for (String email : userEmails) {
            // Check if user already exists
            if (userRepository.findByEmail(email).isPresent()) {
                existingCount++;
                continue;
            }

            // Create new user
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(defaultPassword));
            
            // Extract name from email (part before @)
            String emailPrefix = email.split("@")[0];
            // Convert to a nicer display name (capitalize first letter)
            String displayName = emailPrefix.substring(0, 1).toUpperCase() + emailPrefix.substring(1);
            user.setFullName(displayName);
            
            user.setRole(userRole);
            user.setVerified(true); // Pre-verified
            user.setStatus(true); // Active status
            user.setNumberOfModel(0); // Start with 0 models
            
            userRepository.save(user);
            createdCount++;
            System.out.println("   ✅ Created user: " + email);
        }

        System.out.println("\n📊 Regular Users Seeding Summary:");
        System.out.println("   ✅ Created: " + createdCount + " users");
        System.out.println("   ✓ Already existed: " + existingCount + " users");
        System.out.println("   📧 Total: " + userEmails.length + " users");
        System.out.println("   🔑 Default password: " + defaultPassword);
        System.out.println("   👤 Role: USER");
        System.out.println("   ✔ Status: Active & Verified");
        System.out.println("✅ Regular users seeding completed!\n");
    }

    private void seedOrders() {
        System.out.println("\n🛒 Seeding Orders...");

        // Use the correct date format matching the data
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        dateFormat.setLenient(false); // Strict parsing

        // Order data from the image
        Object[][] orderData = {
            {"12:01:28 20-11-2025", "FT25324055245745", "phillipvothien306@gmail.com", 2, 499000.0},
            {"11:59:41 20-11-2025", "FT25324453487440", "nhungthyse184902@fpt.edu.vn", 2, 499000.0},
            {"22:36:17 17-11-2025", "FT25322313880389", "notnagisapham1103@gmail.com", 2, 499000.0},
            {"22:34:52 17-11-2025", "FT25322346000561", "datkhoi224@gmail.com", 2, 499000.0},
            {"20:04:25 17-11-2025", "FT25321240939704", "quannmse182197@fpt.edu.vn", 1, 199000.0},
            {"20:02:20 17-11-2025", "FT25321888795387", "huyhnse182014@fpt.edu.vn", 1, 199000.0},
            {"11:15:37 16-11-2025", "FT25321554489213", "hatanphong8@gmail.com", 1, 199000.0},
            {"11:14:57 16-11-2025", "FT25321431914306", "Ckhoa269@gmail.com", 1, 199000.0},
            {"11:13:21 16-11-2025", "FT25321282060392", "huynhthuchadoan99@gmail.com", 2, 499000.0},
            {"11:12:19 16-11-2025", "FT25321094300301", "anhtuanphanminh206@gmail.com", 1, 199000.0},
            {"12:31:25 15-11-2025", "FT25319703413381", "anhlam2103@gmail.com", 2, 499000.0},
            {"12:29:50 15-11-2025", "FT25319404639403", "nguyenthithuydung.nbk@gmail.com", 2, 499000.0},
            {"11:51:37 15-11-2025", "FT25319269019284", "nguyentriminhgc@gmail.com", 1, 199000.0},
            {"11:50:24 15-11-2025", "FT25319232725701", "ledinhduy09042005@gmail.com", 1, 199000.0},
            {"11:43:53 15-11-2025", "FT25319992002114", "huynhgiahuydao1717@gmail.com", 1, 199000.0},
            {"11:16:33 15-11-2025", "FT25319247201007", "greeenma052@gmail.com", 1, 199000.0},
            {"10:17:28 15-11-2025", "FT25319037239910", "dangminhtuanan9999@gmail.com", 1, 199000.0},
            {"20:30:42 14-11-2025", "FT25318009643290", "thinhkg644@gmail.com", 2, 499000.0},
            {"20:29:12 14-11-2025", "FT25318000471046", "vuquangphihoang124@gmail.com", 2, 499000.0},
            {"16:38:35 14-11-2025", "FT25318831156970", "nghi050609@gmail.com", 1, 199000.0},
            {"16:36:13 14-11-2025", "FT25318023961115", "tranphuongnamrg2004@gmail.com", 1, 199000.0},
            {"16:26:41 14-11-2025", "FT25318035728653", "giahienpk123@gmail.com", 2, 499000.0}
        };

        int createdCount = 0;
        int skippedCount = 0;

        for (Object[] data : orderData) {
            try {
                String dateStr = (String) data[0];
                String checkCode = (String) data[1];
                String email = (String) data[2];
                int subscriptionId = (int) data[3];
                double totalPrice = (double) data[4];

                // Check if order with this check code already exists
                if (orderRepository.findAll().stream()
                        .anyMatch(o -> checkCode.equals(o.getCheckCode()))) {
                    skippedCount++;
                    continue;
                }

                // Find user by email
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    System.out.println("   ⚠️ User not found: " + email + " - Skipping order");
                    skippedCount++;
                    continue;
                }

                // Find subscription
                Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
                if (subscription == null) {
                    System.out.println("   ⚠️ Subscription not found: ID " + subscriptionId + " - Skipping order");
                    skippedCount++;
                    continue;
                }

                // Parse date
                Date orderDate = dateFormat.parse(dateStr);

                // Create order
                Order order = new Order();
                order.setUser(user);
                order.setOrder_date(orderDate);
                order.setTotalPrice(totalPrice);
                order.setCheckCode(checkCode);
                order.setStatus(1); // Default status: Đã thanh toán

                // Create order detail
                List<OrderDetail> orderDetails = new ArrayList<>();
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setSubscription(subscription);
                orderDetail.setQuantity(1);
                orderDetail.setPricePerUnit(totalPrice);
                orderDetail.setTotalPrice(totalPrice);
                orderDetails.add(orderDetail);

                order.setOrderDetails(orderDetails);

                // Save order (cascade will save order details)
                orderRepository.save(order);
                createdCount++;
                System.out.println("   ✅ Created order: " + checkCode + " for " + email);

            } catch (Exception e) {
                System.out.println("   ❌ Error creating order: " + e.getMessage());
                skippedCount++;
            }
        }

        System.out.println("\n📊 Orders Seeding Summary:");
        System.out.println("   ✅ Created: " + createdCount + " orders");
        System.out.println("   ⚠️ Skipped: " + skippedCount + " orders");
        System.out.println("   📧 Total: " + orderData.length + " orders");
        System.out.println("✅ Orders seeding completed!\n");
    }
}