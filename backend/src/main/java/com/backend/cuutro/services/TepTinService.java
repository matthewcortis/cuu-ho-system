package com.backend.cuutro.services;

import com.backend.cuutro.dto.request.TepTinUploadRequest;
import com.backend.cuutro.dto.response.entities.TepTinDto;

public interface TepTinService {

	TepTinDto upload(TepTinUploadRequest request);
}
