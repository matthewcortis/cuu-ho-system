package com.backend.cuutro.services;

import java.util.List;

import com.backend.cuutro.dto.request.GanDoiNhomTinhNguyenVienRequest;
import com.backend.cuutro.dto.request.TinhNguyenVienDangKyRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomTinhNguyenVienDto;
import com.backend.cuutro.dto.response.entities.TinhNguyenVienDto;

public interface TinhNguyenVienService {

	TinhNguyenVienDto dangKy(TinhNguyenVienDangKyRequest request);

	List<TinhNguyenVienDto> getDanhSach(String trangThaiDuyet);

	List<TinhNguyenVienDto> getChoXetDuyet();

	TinhNguyenVienDto getById(Long id);

	TinhNguyenVienDto duyet(Long id);

	void xoa(Long id);

	DoiNhomTinhNguyenVienDto ganDoiNhom(GanDoiNhomTinhNguyenVienRequest request);
}

