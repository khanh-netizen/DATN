package com.foxstyle.api.config;

import com.foxstyle.api.entity.*;
import com.foxstyle.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CouponRepository couponRepository;
    private final BannerRepository bannerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!bannerRepository.existsByBannerType("MARQUEE")) {
            bannerRepository.save(Banner.builder()
                    .title("FoxStyle Fashion • Miễn phí vận chuyển cho đơn hàng từ 499.000đ")
                    .imageUrl("marquee://text")
                    .bannerType("MARQUEE")
                    .linkUrl("/products")
                    .position(1)
                    .status((byte) 1)
                    .build());
        }

        // Xóa hoàn toàn các tài khoản Shipper và vai trò ROLE_SHIPPER nếu có trong cơ sở dữ liệu
        userRepository.findByUsername("shipper").ifPresent(u -> {
            try { userRepository.delete(u); } catch (Exception ignored) {}
        });
        userRepository.findByUsername("shipper_demo").ifPresent(u -> {
            try { userRepository.delete(u); } catch (Exception ignored) {}
        });
        userRepository.findByUsername("shipper_1").ifPresent(u -> {
            try { userRepository.delete(u); } catch (Exception ignored) {}
        });
        roleRepository.findByRoleName("ROLE_SHIPPER").ifPresent(r -> {
            try { roleRepository.delete(r); } catch (Exception ignored) {}
        });

        // 1. Khởi tạo các vai trò (Roles) gốc chuẩn
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_ADMIN")
                        .description("Quản trị viên hệ thống tối cao")
                        .build()));

        Role staffRole = roleRepository.findByRoleName("ROLE_STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_STAFF")
                        .description("Nhân viên vận hành, quản lý kho và đơn hàng")
                        .build()));

        Role customerRole = roleRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_CUSTOMER")
                        .description("Khách hàng mua sắm trực tuyến")
                        .build()));

        String adminHashedPassword = passwordEncoder.encode("Admin123@");
        String staffHashedPassword = passwordEncoder.encode("Staff123@");
        String customerHashedPassword = passwordEncoder.encode("Chien123@");

        // 2. Khởi tạo/Cập nhật Tài khoản Quản trị (Admin)
        User adminUser = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.findByUsername("admin_fox").orElse(null));

        if (adminUser == null) {
            adminUser = userRepository.save(User.builder()
                    .role(adminRole)
                    .username("admin")
                    .password(adminHashedPassword)
                    .fullName("Nguyễn Quản Trị")
                    .email("admin@foxstyle.vn")
                    .phone("0912345678")
                    .status((byte) 1)
                    .build());
        } else {
            adminUser.setUsername("admin");
            adminUser.setPassword(adminHashedPassword);
            adminUser.setRole(adminRole);
            adminUser.setStatus((byte) 1);
            userRepository.save(adminUser);
        }

        // 3. Khởi tạo/Cập nhật Tài khoản Nhân viên (Staff)
        User staffUser = userRepository.findByUsername("staff")
                .orElseGet(() -> userRepository.findByUsername("staff_chien").orElse(null));

        if (staffUser == null) {
            userRepository.save(User.builder()
                    .role(staffRole)
                    .username("staff")
                    .password(staffHashedPassword)
                    .fullName("Lê Văn Nhân Viên")
                    .email("staff@foxstyle.vn")
                    .phone("0987654321")
                    .status((byte) 1)
                    .build());
        } else {
            staffUser.setUsername("staff");
            staffUser.setPassword(staffHashedPassword);
            staffUser.setRole(staffRole);
            staffUser.setStatus((byte) 1);
            userRepository.save(staffUser);
        }

        // 4. Khởi tạo nhiều khách hàng mẫu với mật khẩu riêng cho vai trò CUSTOMER.
        String[][] customersData = {
            {"customer1", "Nguyễn Văn An", "customer1@gmail.com", "0901111222"},
            {"customer2", "Trần Thị Bình", "customer2@gmail.com", "0902222333"},
            {"customer3", "Lê Hoàng Cường", "customer3@gmail.com", "0903333444"},
            {"customer4", "Phạm Minh Duy", "customer4@gmail.com", "0904444555"},
            {"customer5", "Hoàng Ngọc Em", "customer5@gmail.com", "0905555666"},
            {"customer6", "Phan Thanh Hải", "customer6@gmail.com", "0906666777"},
            {"customer7", "Vũ Minh Khánh", "customer7@gmail.com", "0907777888"},
            {"customer8", "Đặng Quốc Bảo", "customer8@gmail.com", "0908888999"},
            {"customer9", "Bùi Thị Linh", "customer9@gmail.com", "0909999000"},
            {"customer10", "Đỗ Anh Tuấn", "customer10@gmail.com", "0911222333"}
        };

        for (String[] cust : customersData) {
            User customer = userRepository.findByUsername(cust[0]).orElse(null);
            if (customer == null) {
                userRepository.save(User.builder()
                        .role(customerRole)
                        .username(cust[0])
                        .password(customerHashedPassword)
                        .fullName(cust[1])
                        .email(cust[2])
                        .phone(cust[3])
                        .status((byte) 1)
                        .build());
            } else {
                customer.setPassword(customerHashedPassword);
                customer.setRole(customerRole);
                customer.setStatus((byte) 1);
                userRepository.save(customer);
            }
        }

        // Chỉ lấy danh sách sau khi đã tạo đủ tài khoản mẫu; không đổi mật khẩu tài khoản thật.
        List<User> existingUsers = userRepository.findAll();

        // 5. KHỞI TẠO NGẪU NHIÊN NHIỀU ĐỊA CHỈ GIAO HÀNG CHO TẤT CẢ TÀI KHOẢN
        String[][] sampleAddresses = {
            {"TP Hồ Chí Minh", "Quận 1", "Phường Bến Nghé", "123 Đường Nguyễn Huệ"},
            {"TP Hồ Chí Minh", "Quận 3", "Phường Võ Thị Sáu", "456 Đường Điện Biên Phủ (Nhà riêng)"},
            {"Hà Nội", "Quận Cầu Giấy", "Phường Dịch Vọng", "78 Đường Trần Thái Tông (Văn phòng)"},
            {"Hà Nội", "Quận Hoàn Kiếm", "Phường Hàng Đào", "12 Phố Hàng Đào"},
            {"Đà Nẵng", "Quận Hải Châu", "Phường Thạch Thang", "89 Đường Bạch Đằng"},
            {"Cần Thơ", "Quận Ninh Kiều", "Phường An Khánh", "34 Đường Nguyễn Văn Cừ"},
            {"Hải Phòng", "Quận Hồng Bàng", "Phường Minh Khai", "56 Đường Điện Biên Phủ"}
        };

        int createdAddressesCount = 0;
        for (User user : existingUsers) {
            List<UserAddress> userAddresses = userAddressRepository.findByUserUserIdOrderByIsDefaultDesc(user.getUserId());
            if (userAddresses.isEmpty()) {
                int idx1 = (user.getUserId() != null ? user.getUserId() : 1) % sampleAddresses.length;
                String[] a1 = sampleAddresses[idx1];
                userAddressRepository.save(UserAddress.builder()
                        .user(user)
                        .recipientName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                        .phone(user.getPhone() != null ? user.getPhone() : "0901234567")
                        .province(a1[0])
                        .district(a1[1])
                        .ward(a1[2])
                        .detailAddress(a1[3])
                        .isDefault(true)
                        .build());
                createdAddressesCount++;

                int idx2 = (idx1 + 1) % sampleAddresses.length;
                String[] a2 = sampleAddresses[idx2];
                userAddressRepository.save(UserAddress.builder()
                        .user(user)
                        .recipientName(user.getFullName() != null ? user.getFullName() + " (Cơ quan)" : user.getUsername())
                        .phone(user.getPhone() != null ? user.getPhone() : "0909876543")
                        .province(a2[0])
                        .district(a2[1])
                        .ward(a2[2])
                        .detailAddress(a2[3])
                        .isDefault(false)
                        .build());
                createdAddressesCount++;
            }
        }

        // 6. Khởi tạo Danh mục thời trang riêng biệt (Bao gồm danh mục Set Combo Tiết Kiệm)
        String[][] categoriesSeed = {
            {"Áo Thun Nam", "Áo thun ngắn tay, dài tay, cổ tròn, cổ bẻ phom dáng thể thao thoải mái"},
            {"Áo Sơ Mi Nam", "Sơ mi tay ngắn, dài tay, vải lụa, vải kate chống nhăn cao cấp"},
            {"Áo Khoác & Blazer", "Áo gió, áo khoác bomber, áo phao giữ nhiệt, blazer nam nữ"},
            {"Quần Jeans & Denim", "Các sản phẩm quần jeans denim dáng ôm, suông, bền bỉ phong cách"},
            {"Quần Tây & Kaki", "Quần tây công sở, quần kaki phom slimfit sang trọng"},
            {"Váy & Đầm Nữ", "Đầm xòe, váy suông, chân váy chữ A phong cách Hàn Quốc"},
            {"Áo Nữ & Áo Kiểu", "Áo kiểu nữ, áo croptop, áo sơ mi thiết kế thanh lịch"},
            {"Đồ Thể Thao", "Bộ quần áo thể thao co giãn 4 chiều thấm hút mồ hôi"},
            {"Phụ Kiện Thời Trang", "Mũ nón, kính mát, thắt lưng da, ví da thời trang"},
            {"Giày & Dép", "Giày thể thao sneaker, giày tây, dép thời trang cao cấp"},
            {"Set Combo Tiết Kiệm", "Danh mục các gói Combo trọn bộ phối sẵn ưu đãi độc quyền tiết kiệm lên tới 30%"}
        };

        for (String[] catData : categoriesSeed) {
            if (!categoryRepository.existsByCategoryNameIgnoreCase(catData[0])) {
                categoryRepository.save(Category.builder()
                        .categoryName(catData[0])
                        .description(catData[1])
                        .status((byte) 1)
                        .build());
            }
        }

        // Khởi tạo sản phẩm Combo riêng biệt trong danh mục "Set Combo Tiết Kiệm"
        Category comboCategory = categoryRepository.findByCategoryNameIgnoreCase("Set Combo Tiết Kiệm").orElse(null);
        if (comboCategory != null) {
            String[][] combosSeed = {
                {"[SET COMBO] Combo 2 Áo Thun Basic FoxStyle [COMBO:1,3]", "420000", "598000", "[COMBO:1,3] Bộ đôi áo thun basic cao cấp co giãn 4 chiều phom dáng trẻ trung.", "/image_san_pham/photo-1521572163474-6864f9cf17ab.jpg"},
                {"[SET COMBO] Set Phối Đồ Công Sở Lịch Lẫm [COMBO:2,5]", "580000", "830000", "[COMBO:2,5] Set áo sơ mi lụa và quần tây baggy chuẩn phom quý ông công sở.", "/image_san_pham/photo-1596755094514-f87e34085b2c.jpg"},
                {"[SET COMBO] Set Dạo Phố Streetwear (Áo Polo + Quần Jean) [COMBO:3,4]", "599000", "879000", "[COMBO:3,4] Set đồ dạo phố năng động gồm áo Polo pique và quần jean skinny co giãn.", "/image_san_pham/photo-1521572163474-6864f9cf17ab.jpg"},
                {"[SET COMBO] Set Phụ Kiện Quý Ông FoxStyle Luxury [COMBO:12,13]", "399000", "670000", "[COMBO:12,13] Bộ phụ kiện sang trọng gồm mũ lưỡi trai thêu logo và túi xách da đeo chéo.", "/image_san_pham/photo-1548036328-c9fa89d128fa.jpg"}
            };

            for (String[] cb : combosSeed) {
                if (!productRepository.existsByProductNameIgnoreCase(cb[0])) {
                    productRepository.save(Product.builder()
                            .category(comboCategory)
                            .productName(cb[0])
                            .price(new BigDecimal(cb[1]))
                            .originalPrice(new BigDecimal(cb[2]))
                            .description(cb[3])
                            .imageUrl(cb[4])
                            .isCombo(true)
                            .material("Thun Cotton / Silk Blend")
                            .origin("Việt Nam")
                            .status((byte) 1)
                            .build());
                }
            }
        }

        // 7. Thêm Nhiều Mã giảm giá (Coupons / Vouchers)
        String[][] couponsSeed = {
            {"FOXSTYLE10", "2", "10", "200000", "50000"},
            {"FOXSTYLE20", "2", "20", "500000", "100000"},
            {"WELCOME50", "1", "50000", "300000", "50000"},
            {"SUMMERVIBES", "2", "15", "250000", "75000"},
            {"FLASHSALE30", "2", "30", "600000", "200000"},
            {"VIPDISCOUNT", "2", "25", "1000000", "300000"}
        };

        for (String[] cData : couponsSeed) {
            if (!couponRepository.existsByCouponCode(cData[0])) {
                couponRepository.save(Coupon.builder()
                        .couponCode(cData[0])
                        .discountType(Byte.valueOf(cData[1]))
                        .discountValue(new BigDecimal(cData[2]))
                        .minOrderValue(new BigDecimal(cData[3]))
                        .maxDiscountValue(new BigDecimal(cData[4]))
                        .startDate(LocalDateTime.now().minusDays(10))
                        .endDate(LocalDateTime.now().plusMonths(6))
                        .usageLimit(500)
                        .usedCount(12)
                        .status((byte) 1)
                        .build());
            }
        }

        // 8. Tự động sinh ĐẦY ĐỦ SIZE & MÀU SẮC CHO TẤT CẢ SẢN PHẨM VÀ CẢ CÁC GÓI COMBO
        List<Product> allProducts = productRepository.findAll();

        String[] sampleColors = {"Đen", "Trắng", "Xám Ghi", "Xanh Navy", "Kem Be", "Đỏ Đô", "Xanh Rêu", "Nâu Cafe"};
        String[] comboColors = {"Chuẩn Set", "Set Đen", "Set Trắng", "Set Xanh Navy"};

        String[] clothingSizes = {"XS", "S", "M", "L", "XL", "XXL", "3XL"};
        String[] pantsSizes = {"28", "29", "30", "31", "32", "33", "34", "35", "36"};
        String[] shoeSizes = {"37", "38", "39", "40", "41", "42", "43", "44"};
        String[] comboSizes = {"Freesize", "Set Size S", "Set Size M", "Set Size L", "Set Size XL", "Set Size XXL"};
        String[] accessorySizes = {"Freesize", "Size S", "Size M", "Size L"};

        String[] galleryImages = {
            "/image_san_pham/photo-1521572163474-6864f9cf17ab.jpg",
            "/image_san_pham/photo-1618354691373-d851c5c3a990.jpg",
            "/image_san_pham/photo-1602810318383-e386cc2a3ccf.jpg",
            "/image_san_pham/photo-1596755094514-f87e34085b2c.jpg",
            "/image_san_pham/photo-1542272604-787c3835535d.jpg"
        };

        int createdVariantsCount = 0;
        int createdImagesCount = 0;

        for (Product product : allProducts) {
            String categoryName = (product.getCategory() != null ? product.getCategory().getCategoryName() : "").toLowerCase();
            boolean isCombo = categoryName.contains("combo") || (product.getProductName() != null && product.getProductName().contains("[SET COMBO]"));

            String[] targetSizes = clothingSizes;
            String[] targetColors = sampleColors;

            if (isCombo) {
                targetSizes = comboSizes;
                targetColors = comboColors;
            } else if (categoryName.contains("quần") || categoryName.contains("jean")) {
                targetSizes = pantsSizes;
            } else if (categoryName.contains("giày") || categoryName.contains("dép")) {
                targetSizes = shoeSizes;
            } else if (categoryName.contains("phụ kiện")) {
                targetSizes = accessorySizes;
            }

            for (String color : targetColors) {
                for (String size : targetSizes) {
                    if (productVariantRepository.findByProductProductIdAndColorAndSize(product.getProductId(), color, size).isEmpty()) {
                        productVariantRepository.save(ProductVariant.builder()
                                .product(product)
                                .color(color)
                                .size(size)
                                .quantity(100)
                                .sku("FOX-" + product.getProductId() + "-" + (color.length() > 0 ? color.substring(0, 1).toUpperCase() : "C") + "-" + size.replaceAll("\\s+", ""))
                                .price(product.getPrice())
                                .build());
                        createdVariantsCount++;
                    }
                }
            }

            List<ProductImage> existingImages = productImageRepository.findByProductProductIdOrderByDisplayOrderAsc(product.getProductId());
            if (existingImages.isEmpty()) {
                productImageRepository.save(ProductImage.builder()
                        .product(product)
                        .imageUrl(product.getImageUrl() != null ? product.getImageUrl() : galleryImages[0])
                        .isPrimary(true)
                        .displayOrder(1)
                        .build());
                createdImagesCount++;

                for (int i = 0; i < galleryImages.length; i++) {
                    productImageRepository.save(ProductImage.builder()
                            .product(product)
                            .imageUrl(galleryImages[i])
                            .isPrimary(false)
                            .displayOrder(i + 2)
                            .build());
                    createdImagesCount++;
                }
            }
        }

        System.out.println("✅ DataInitializer: Đã nạp thành công 2 địa chỉ giao hàng riêng cho từng tài khoản (Tạo " + createdAddressesCount + " địa chỉ giao hàng)!");
    }
}
