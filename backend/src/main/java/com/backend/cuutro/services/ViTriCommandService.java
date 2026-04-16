package com.backend.cuutro.services;

import com.backend.cuutro.dto.request.ViTriInputRequest;
import com.backend.cuutro.entities.ViTriEntity;

public interface ViTriCommandService {

	ViTriEntity taoViTriMoi(ViTriInputRequest request);
}
