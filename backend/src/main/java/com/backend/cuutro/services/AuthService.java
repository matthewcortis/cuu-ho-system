package com.backend.cuutro.services;

import com.backend.cuutro.dto.request.DangNhapRequest;
import com.backend.cuutro.dto.response.entities.DangNhapResponse;

public interface AuthService {

	DangNhapResponse dangNhap(DangNhapRequest request);
}

