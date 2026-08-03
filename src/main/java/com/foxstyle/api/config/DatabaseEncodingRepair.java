package com.foxstyle.api.config;

import com.foxstyle.api.entity.*;
import com.foxstyle.api.repository.*;
import com.foxstyle.api.util.VietnameseTextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseEncodingRepair implements CommandLineRunner {

    @Value("${app.repair-database-encoding:true}")
    private boolean repairOnStartup;

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final DistrictRepository districtRepository;
    private final SettingRepository settingRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!repairOnStartup) {
            return;
        }
        System.out.println("[REPAIR] Starting database UTF-8 encoding repair on startup...");
        repairDatabase();
    }

    @Transactional
    public void repairDatabase() {
        try {
            alterColumnsToNvarchar();
            repairDistricts();
            repairSettings();
            repairCategories();
            repairProducts();
            repairOrders();
            repairUsers();
            repairUserAddresses();
            System.out.println("[REPAIR] Database UTF-8 encoding repair completed successfully!");
        } catch (Exception e) {
            System.err.println("[REPAIR] Database repair encountered an error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void alterColumnsToNvarchar() {
        System.out.println("[REPAIR] Altering database columns to NVARCHAR to support Unicode...");
        String[] alterStatements = {
            "ALTER TABLE districts ALTER COLUMN district_name NVARCHAR(100) NOT NULL",
            "ALTER TABLE districts ALTER COLUMN province NVARCHAR(100) NOT NULL",
            
            "ALTER TABLE settings ALTER COLUMN setting_value NVARCHAR(MAX) NULL",
            "ALTER TABLE settings ALTER COLUMN description NVARCHAR(255) NULL",
            
            "ALTER TABLE categories ALTER COLUMN category_name NVARCHAR(100) NOT NULL",
            "ALTER TABLE categories ALTER COLUMN description NVARCHAR(MAX) NULL",
            
            "ALTER TABLE products ALTER COLUMN product_name NVARCHAR(150) NOT NULL",
            "ALTER TABLE products ALTER COLUMN material NVARCHAR(100) NULL",
            "ALTER TABLE products ALTER COLUMN origin NVARCHAR(100) NULL",
            
            "ALTER TABLE orders ALTER COLUMN recipient_name NVARCHAR(100) NOT NULL",
            "ALTER TABLE orders ALTER COLUMN shipping_address NVARCHAR(255) NOT NULL",
            
            "ALTER TABLE users ALTER COLUMN full_name NVARCHAR(100) NULL",
            
            "ALTER TABLE user_addresses ALTER COLUMN recipient_name NVARCHAR(100) NOT NULL",
            "ALTER TABLE user_addresses ALTER COLUMN detail_address NVARCHAR(255) NOT NULL",
            "ALTER TABLE user_addresses ALTER COLUMN district NVARCHAR(100) NOT NULL",
            "ALTER TABLE user_addresses ALTER COLUMN ward NVARCHAR(100) NOT NULL",
            "ALTER TABLE user_addresses ALTER COLUMN province NVARCHAR(100) NOT NULL",

            "ALTER TABLE chat_messages ALTER COLUMN customer_name NVARCHAR(150) NULL",
            "ALTER TABLE chat_messages ALTER COLUMN sender_name NVARCHAR(150) NOT NULL",
            "ALTER TABLE chat_messages ALTER COLUMN content NVARCHAR(MAX) NOT NULL"
        };

        for (String sql : alterStatements) {
            try {
                jdbcTemplate.execute(sql);
                System.out.println("[REPAIR] Executed: " + sql);
            } catch (Exception e) {
                System.err.println("[REPAIR] Alter column warning (ignored): " + e.getMessage());
            }
        }

        try {
            jdbcTemplate.update("UPDATE chat_messages SET customer_name = N'🔒 Nhóm nội bộ (Nhân viên & Quản trị viên)' WHERE channel_id = 'staff_admin_group'");
            jdbcTemplate.update("UPDATE chat_messages SET customer_name = N'💬 Nhóm thông báo chung' WHERE channel_id = 'general_group'");
        } catch (Exception e) {
            System.err.println("[REPAIR] Could not normalize chat group names: " + e.getMessage());
        }
    }

    private void repairDistricts() {
        List<District> list = districtRepository.findAll();
        int count = 0;
        for (District item : list) {
            String newName = fixMojibake(item.getDistrictName());
            String newProv = fixMojibake(item.getProvince());
            if (!newName.equals(item.getDistrictName()) || !newProv.equals(item.getProvince())) {
                item.setDistrictName(newName);
                item.setProvince(newProv);
                districtRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " districts.");
        }
    }

    private void repairSettings() {
        List<Setting> list = settingRepository.findAll();
        int count = 0;
        for (Setting item : list) {
            String newVal = fixMojibake(item.getSettingValue());
            String newDesc = fixMojibake(item.getDescription());
            if ((newVal != null && !newVal.equals(item.getSettingValue())) || (newDesc != null && !newDesc.equals(item.getDescription()))) {
                item.setSettingValue(newVal);
                item.setDescription(newDesc);
                settingRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " settings.");
        }
    }

    private void repairCategories() {
        List<Category> list = categoryRepository.findAll();
        int count = 0;
        for (Category item : list) {
            String newName = fixMojibake(item.getCategoryName());
            String newDesc = fixMojibake(item.getDescription());
            if ((newName != null && !newName.equals(item.getCategoryName())) || (newDesc != null && !newDesc.equals(item.getDescription()))) {
                item.setCategoryName(newName);
                item.setDescription(newDesc);
                categoryRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " categories.");
        }
    }

    private void repairProducts() {
        List<Product> list = productRepository.findAll();
        int count = 0;
        for (Product item : list) {
            String newName = fixMojibake(item.getProductName());
            String newDesc = fixMojibake(item.getDescription());
            String newMat = fixMojibake(item.getMaterial());
            String newOrig = fixMojibake(item.getOrigin());
            if (!newName.equals(item.getProductName()) || 
                (newDesc != null && !newDesc.equals(item.getDescription())) ||
                (newMat != null && !newMat.equals(item.getMaterial())) ||
                (newOrig != null && !newOrig.equals(item.getOrigin()))) {
                
                item.setProductName(newName);
                item.setDescription(newDesc);
                item.setMaterial(newMat);
                item.setOrigin(newOrig);
                productRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " products.");
        }
    }

    private void repairOrders() {
        List<Order> list = orderRepository.findAll();
        int count = 0;
        for (Order item : list) {
            String newName = fixMojibake(item.getRecipientName());
            String newAddr = fixMojibake(item.getShippingAddress());
            if (!newName.equals(item.getRecipientName()) || !newAddr.equals(item.getShippingAddress())) {
                item.setRecipientName(newName);
                item.setShippingAddress(newAddr);
                orderRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " orders.");
        }
    }

    private void repairUsers() {
        List<User> list = userRepository.findAll();
        int count = 0;
        for (User item : list) {
            String newName = fixMojibake(item.getFullName());
            if (newName != null && !newName.equals(item.getFullName())) {
                item.setFullName(newName);
                userRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " users.");
        }
    }

    private void repairUserAddresses() {
        List<UserAddress> list = userAddressRepository.findAll();
        int count = 0;
        for (UserAddress item : list) {
            String newName = fixMojibake(item.getRecipientName());
            String newAddr = fixMojibake(item.getDetailAddress());
            String newDist = fixMojibake(item.getDistrict());
            String newWard = fixMojibake(item.getWard());
            String newProv = fixMojibake(item.getProvince());
            if ((newName != null && !newName.equals(item.getRecipientName())) ||
                (newAddr != null && !newAddr.equals(item.getDetailAddress())) ||
                (newDist != null && !newDist.equals(item.getDistrict())) ||
                (newWard != null && !newWard.equals(item.getWard())) ||
                (newProv != null && !newProv.equals(item.getProvince()))) {
                
                item.setRecipientName(newName);
                item.setDetailAddress(newAddr);
                item.setDistrict(newDist);
                item.setWard(newWard);
                item.setProvince(newProv);
                userAddressRepository.save(item);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("[REPAIR] Repaired " + count + " user addresses.");
        }
    }

    private String fixMojibake(String input) {
        return VietnameseTextNormalizer.normalize(input);
    }
}
