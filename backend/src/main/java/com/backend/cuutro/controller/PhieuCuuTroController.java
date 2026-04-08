package com.backend.cuutro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.CapNhatTrangThaiPhieuRequest;
import com.backend.cuutro.dto.request.DieuPhoiPhieuRequest;
import com.backend.cuutro.dto.request.GuiTinNhanPhieuRequest;
import com.backend.cuutro.dto.request.TaoPhieuHoTroRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.PhanCongDto;
import com.backend.cuutro.dto.response.entities.PhieuCuuTroDto;
import com.backend.cuutro.dto.response.entities.TinNhanDto;
import com.backend.cuutro.dto.response.entities.TrangThaiPhieuResponse;
import com.backend.cuutro.services.PhieuCuuTroService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/phieu-cuu-tro")
@RequiredArgsConstructor
public class PhieuCuuTroController {

	private final PhieuCuuTroService phieuCuuTroService;

	@PostMapping
	public ResponseEntity<ResponseData<PhieuCuuTroDto>> taoPhieu(
			@Valid @RequestBody TaoPhieuHoTroRequest request,
			HttpServletRequest httpRequest) {
		PhieuCuuTroDto created = phieuCuuTroService.taoPhieuCongKhai(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Tao phieu ho tro thanh cong", created, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<PhieuCuuTroDto>>> getDanhSach(HttpServletRequest httpRequest) {
		List<PhieuCuuTroDto> data = phieuCuuTroService.getDanhSach();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched phieu list successfully", data, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<PhieuCuuTroDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		PhieuCuuTroDto data = phieuCuuTroService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched phieu successfully", data, httpRequest));
	}

	@GetMapping("/{id}/trang-thai")
	public ResponseEntity<ResponseData<TrangThaiPhieuResponse>> getTrangThai(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		TrangThaiPhieuResponse data = phieuCuuTroService.getTrangThai(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched phieu status successfully", data, httpRequest));
	}

	@PostMapping("/{id}/dieu-phoi")
	public ResponseEntity<ResponseData<PhanCongDto>> dieuPhoi(
			@PathVariable Long id,
			@Valid @RequestBody DieuPhoiPhieuRequest request,
			HttpServletRequest httpRequest) {
		PhanCongDto data = phieuCuuTroService.dieuPhoi(id, request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Dieu phoi doi nhom thanh cong", data, httpRequest));
	}

	@PostMapping("/{id}/nhan-nhiem-vu")
	public ResponseEntity<ResponseData<TrangThaiPhieuResponse>> nhanNhiemVu(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		TrangThaiPhieuResponse data = phieuCuuTroService.nhanNhiemVu(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Nhan nhiem vu thanh cong", data, httpRequest));
	}

	@PutMapping("/{id}/trang-thai")
	public ResponseEntity<ResponseData<TrangThaiPhieuResponse>> capNhatTrangThai(
			@PathVariable Long id,
			@Valid @RequestBody CapNhatTrangThaiPhieuRequest request,
			HttpServletRequest httpRequest) {
		TrangThaiPhieuResponse data = phieuCuuTroService.capNhatTrangThai(id, request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Cap nhat trang thai phieu thanh cong", data, httpRequest));
	}

	@PostMapping("/{id}/tin-nhan")
	public ResponseEntity<ResponseData<TinNhanDto>> guiTinNhan(
			@PathVariable Long id,
			@Valid @RequestBody GuiTinNhanPhieuRequest request,
			HttpServletRequest httpRequest) {
		TinNhanDto data = phieuCuuTroService.guiTinNhan(id, request.getNoiDung());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Gui tin nhan thanh cong", data, httpRequest));
	}

	@GetMapping("/{id}/tin-nhan")
	public ResponseEntity<ResponseData<List<TinNhanDto>>> getDanhSachTinNhan(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		List<TinNhanDto> data = phieuCuuTroService.getDanhSachTinNhan(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched tin nhan list successfully", data, httpRequest));
	}

	private <T> ResponseData<T> buildResponse(HttpStatus status, String message, T data, HttpServletRequest request) {
		return ResponseData.<T>builder()
				.status(status.value())
				.message(message)
				.error(null)
				.data(data)
				.path(request.getRequestURI())
				.build();
	}
}

