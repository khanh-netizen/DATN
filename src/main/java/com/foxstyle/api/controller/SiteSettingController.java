package com.foxstyle.api.controller;

import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.entity.Setting;
import com.foxstyle.api.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/site-settings")
@RequiredArgsConstructor
public class SiteSettingController {
    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    private static final Set<String> ALLOWED_KEYS;

    static {
        DEFAULTS.put("site_name", "FoxStyle");
        DEFAULTS.put("site_logo", "/image_quan_tri/logo.jpg");
        DEFAULTS.put("site_phone", "0123 456 789");
        DEFAULTS.put("site_email", "support@foxstyle.vn");
        DEFAULTS.put("site_address", "Hà Nội & TP. Hồ Chí Minh");
        DEFAULTS.put("policy_returns", "FoxStyle hỗ trợ đổi trả sản phẩm trong vòng 7 ngày kể từ ngày nhận hàng.\n\nĐiều kiện đổi trả:\n- Sản phẩm còn nguyên tem, nhãn và chưa qua sử dụng hoặc giặt ủi.\n- Sản phẩm không bị bẩn, hư hỏng hoặc biến dạng.\n- Khách hàng cung cấp mã đơn hàng hoặc hóa đơn mua hàng.\n\nChi phí vận chuyển sẽ được thông báo trước khi xử lý.");
        DEFAULTS.put("policy_warranty", "FoxStyle tiếp nhận bảo hành sản phẩm có lỗi kỹ thuật từ nhà sản xuất, gồm lỗi đường may, phom dáng và chất liệu.\n\nThời gian xử lý dự kiến từ 3–7 ngày làm việc. Không áp dụng với hư hỏng do sử dụng sai hướng dẫn, tác động ngoại lực hoặc tự ý sửa chữa.");
        DEFAULTS.put("policy_tax", "Giá sản phẩm niêm yết đã bao gồm VAT theo mức thuế hiện hành.\n\nKhách hàng có thể yêu cầu xuất hóa đơn GTGT khi đặt hàng. Hóa đơn điện tử được gửi đến email khách hàng đã đăng ký.");
        DEFAULTS.put("policy_privacy", "FoxStyle cam kết bảo mật thông tin cá nhân và chỉ sử dụng để xử lý giao dịch, giao hàng và chăm sóc khách hàng.\n\nFoxStyle không bán hoặc chia sẻ dữ liệu cá nhân cho bên thứ ba vì mục đích thương mại.");
        ALLOWED_KEYS = Set.copyOf(DEFAULTS.keySet());
    }

    private final SettingRepository settingRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicSettings() {
        Map<String, String> values = new LinkedHashMap<>(DEFAULTS);
        ALLOWED_KEYS.forEach(key -> settingRepository.findBySettingKey(key)
                .ifPresent(setting -> values.put(key, setting.getSettingValue())));
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .status("success").message("Lấy cấu hình website thành công")
                .data(values).timestamp(LocalDateTime.now()).build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSiteSettings(
            @RequestBody Map<String, String> request) {
        request.forEach((key, value) -> {
            if (!ALLOWED_KEYS.contains(key)) return;
            Setting setting = settingRepository.findBySettingKey(key)
                    .orElseGet(() -> Setting.builder().settingKey(key).description("Cấu hình website").build());
            setting.setSettingValue(value == null ? "" : value.trim());
            settingRepository.save(setting);
        });
        return getPublicSettings();
    }
}
