package com.backend.cuutro.services;

import java.util.List;

import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;

public interface DoiNhomService {

	List<DoiNhomDto> getDanhSach();

	DoiNhomDto taoDoiNhom(DoiNhomTaoRequest request);
}
