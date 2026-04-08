package com.backend.cuutro.dto.request;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NguoiDungFilterRequest {

	private String ten;
	private String sdt;
	private Long taiKhoanId;
	private Long viTriId;
	private Instant createdFrom;
	private Instant createdTo;
}
