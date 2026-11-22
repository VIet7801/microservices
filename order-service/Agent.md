2. Code đơn giản nhất có thể - hoặc code như 1 người mới học, sao cho đọc luồng dễ hiểu và không giống AI code,không comment hay code thừa thãi.Mọi test,kiểm tra đều phải test trực tiếp chứ không được tạo thêm file test thừa.
3. Viết nghiệp vụ Inventory	Tạo dữ liệu tồn kho mẫu. 
Viết hai thao tác giữ hàng và trả hàng theo orderId. 
Test riêng ( Không tạo file test, test trực tiếp), chưa nối Kafka.
Tồn kho từ 6 → 4 khi giữ 2 sản phẩm, rồi về 6 khi trả.
Không đủ hàng thì không trừ. Gửi lại cùng lệnh không làm trừ hoặc cộng lần nữa.

3. Viết nghiệp vụ Payment	Giả lập hai kết quả thành công và bị từ chối.
Lưu kết quả theo orderId, không kết nối cổng thanh toán thật.	
Chạy được cả hai kịch bản. Xử lý lại cùng lệnh không tạo thêm giao dịch.
4. Thêm Transactional Outbox và Kafka ( Có thể thêm file config ở package config)
Bắt đầu ở Order. Lưu đơn và lệnh ReserveStock trong cùng transaction PostgreSQL. 
Viết job gửi bản ghi Outbox lên Kafka. 
Sau đó nối Inventory và áp dụng Outbox cho kết quả trả về.	
Nếu bước ghi Outbox lỗi, đơn cũng rollback.
Nếu Kafka tắt, bản ghi Outbox vẫn còn và được gửi khi broker hoạt động lại.
5. Ghép Saga Orchestration	
Đặt bộ điều phối trong order-service. 
Nhận kết quả giữ hàng để quyết định có gửi lệnh thanh toán không. 
Nếu thanh toán bị từ chối, gửi lệnh trả hàng.	
Chạy được ba kịch bản bên dưới và xem trạng thái cuối bằng API.
7. Nối Eureka và viết Gateway	
Dùng Eureka Server đã có. 
Gateway dùng Spring Boot để tra địa chỉ order-service, nhưng tự viết routing HTTP,
không dùng Spring Cloud Gateway.	
Client gọi được POST /api/orders và GET /api/orders/{id} qua Gateway 
mà không cần biết cổng của Order Service.
8. update tại thời điểm viết dòng này: đang dùng RESTful API, cần chuyển đổi sang grbc và có 1 package riêng cho dễ hình dung, quản lý luồng
9. Outbox( ghi đơn và message trong cùng 1 transaction) chưa có bảng outbox, không có publisher
10. kafka producer/consumer: Mới chỉ khai báo dependency+cấu hình nhưng chưa thấy sử dụng.
11. saga orchestration ( điều phôối + bù trừ): chưa có, đơn luôn ở 'pending'
12. inventory-service,payment-service Chỉ có class main
13. Redis: có trong compose +pom nhưng chưa dùng
14. eureka không có lời gọi HTTP nao giữa các service: chua dùng tới
15. 8 đến 14 là các yêu cầu đã được kieerm tra: Hãy tạo cho tôi 1 ví dụ mẫu ( code có triển khai các ý 8-14)
về luồng bài toán dặt hàng microservice. tiện làm luôn xử lý lỗi tập trung + kiểm tra groupId đã thống nhất chưa