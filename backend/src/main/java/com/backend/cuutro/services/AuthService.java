package com.backend.cuutro.services;

import com.backend.cuutro.dto.request.DangNhapRequest;
import com.backend.cuutro.dto.request.DangKyRequest;
import com.backend.cuutro.dto.request.QuenMatKhauRequest;
import com.backend.cuutro.dto.response.entities.DangNhapResponse;
import com.backend.cuutro.dto.response.entities.DangKyResponse;

public interface AuthService {

	DangNhapResponse dangNhap(DangNhapRequest request);

	DangKyResponse dangKy(DangKyRequest request);

	void quenMatKhau(QuenMatKhauRequest request);
}
