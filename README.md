# HƯỚNG DẪN CHẠY BACKEND SPRING BOOT (DATN-BE)

Tài liệu này hướng dẫn chi tiết cách thiết lập môi trường, cấu hình cơ sở dữ liệu và khởi chạy dự án Backend Spring Boot của hệ thống FoxStyle.

---

## 1. Yêu cầu hệ thống (Prerequisites)
Trước khi chạy, hãy đảm bảo máy tính của bạn đã cài đặt:
- **Java Development Kit (JDK):** Phiên bản **17** hoặc **21**.
- **Apache Maven:** Trình quản lý thư viện và build project Java.
- **SQL Server:** Hệ quản trị cơ sở dữ liệu MS SQL Server (phiên bản 2019 trở lên) & công cụ quản lý SSMS.

---

## 2. Di chuyển vào thư mục dự án Backend
Mở Terminal (Command Prompt, PowerShell hoặc Git Bash).

Nếu bạn đang đứng ở **thư mục gốc của dự án** (thư mục tổng chứa cả hai thư mục `DATN-BE` và `DATN-FE`), chạy lệnh sau để vào thư mục backend:
```bash
# Di chuyển vào thư mục Backend từ thư mục gốc
cd DATN-BE
```

Hoặc bạn có thể dùng đường dẫn tuyệt đối tương ứng với vị trí lưu thư mục trên máy của bạn:
```bash
# Di chuyển bằng đường dẫn tuyệt đối
cd <đường_dẫn_thư_mục_dự_án>\DATN-BE
# Ví dụ: cd C:\du-an\DATN\DATN-BE
```

---

## 3. Thiết lập Cơ sở dữ liệu
Ứng dụng Backend kết nối tới hệ quản trị cơ sở dữ liệu SQL Server.
1. Mở **SQL Server Management Studio (SSMS)** và kết nối tới server của bạn.
2. Tạo mới một database rỗng tên là: **`foxstyle_db`**:
   ```sql
   CREATE DATABASE foxstyle_db;
   ```
3. Mở file script SQL khởi tạo dữ liệu có sẵn tại thư mục: `docs/foxstyle_db.sql` (ở thư mục gốc của dự án).
4. Copy toàn bộ nội dung file script đó, dán vào một tab query mới trong SSMS (đảm bảo đang chọn cơ sở dữ liệu `foxstyle_db`) và nhấn **Execute (F5)** để khởi tạo các bảng và dữ liệu mẫu.

---

## 4. Cấu hình ứng dụng (`application.properties`)
Mở file cấu hình dự án tại đường dẫn:
`src/main/resources/application.properties` (hoặc tạo mới file này nếu chưa có).

Kiểm tra và cập nhật các thông số kết nối Database sao cho trùng khớp với tài khoản SQL Server của bạn:

```properties
# Cấu hình Cổng chạy ứng dụng Backend
server.port=8080

# Cấu hình kết nối SQL Server (Thay username 'sa' và password của bạn)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=foxstyle_db;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_sql_password
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Đồng bộ cấu hình Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

---

## 5. Biên dịch và Khởi chạy Backend

### Cách 1: Sử dụng dòng lệnh (Terminal/CMD)
Tại thư mục `DATN-BE`, chạy lệnh Maven sau để tải các thư viện dependency và chạy server:

```bash
# Dọn dẹp bản build cũ và chạy Spring Boot
mvn clean spring-boot:run
```

### Cách 2: Sử dụng IDE (IntelliJ IDEA / VS Code)
1. Mở thư mục `DATN-BE` bằng **IntelliJ IDEA** hoặc **VS Code (với Java Extension Pack)**.
2. Đợi IDE tải xong các thư viện cấu hình trong file `pom.xml`.
3. Tìm đến file chạy chính: `src/main/java/com/foxstyle/api/ApiApplication.java`
4. Click chuột phải chọn **Run 'ApiApplication'** hoặc nhấn nút **Run/Debug** trên IDE.

---

## 6. Kiểm tra hoạt động của API
Sau khi ứng dụng chạy thành công trên cổng `8080`, bạn có thể mở trình duyệt hoặc sử dụng Postman kiểm tra các api cơ bản:
- Link kiểm tra danh sách sản phẩm: [http://localhost:8080/api/v1/products](http://localhost:8080/api/v1/products)
- API Tài liệu hướng dẫn chi tiết: [rest_api_guide.md](../docs/rest_api_guide.md)
