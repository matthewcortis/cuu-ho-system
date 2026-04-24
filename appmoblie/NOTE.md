quy tắc frontend như sau:
sử dụng RelativeLayout LinearLayout làm bố cục, dùng các thành phần của material3 
màu chủ đạo  <color name="color_primary">#E02323</color>
font chữ chủ đạo roboto ở mục font 

backend config (Gradle properties):
- `backendBaseUrl`: ví dụ `http://10.0.2.2:8080` (emulator) hoặc domain production.
- `backendBearerToken`: token tĩnh (ưu tiên dùng khi đã có token).
- `backendUsername` + `backendPassword`: tài khoản để app tự login lấy JWT nếu chưa có token tĩnh.

Dùng app sẽ có 3 role chính là người dân đã đăng ký tài khoản, người dân chưa có tài khoản (GUEST), và đội trưởng tình nguyện viên, những tính năng yêu cầu đăng nhập là chi tiết tài khoản tạo bài viết