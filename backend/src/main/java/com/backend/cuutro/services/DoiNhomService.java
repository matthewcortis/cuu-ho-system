package com.backend.cuutro.services;

import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;

public interface DoiNhomService {

	DoiNhomDto taoDoiNhom(DoiNhomTaoRequest request);
}

