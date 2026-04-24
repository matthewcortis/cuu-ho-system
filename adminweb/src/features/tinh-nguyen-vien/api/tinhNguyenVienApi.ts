import { SERVER_BASE_URL } from "@/api/serverBaseUrl";
import { getAuthorizationHeader } from "@/features/auth/utils/authSession";

interface ResponseData<T> {
  status: number;
  data: T;
  error: string | null;
  message: string;
  timestamp: string;
  path: string;
}

interface ViTriLiteDto {
  diaChi: string;
}

interface TaiKhoanLiteDto {
  email: string;
}

interface NguoiDungLiteDto {
  ten: string;
  sdt: string;
  avatarUrl: string;
  viTri: ViTriLiteDto | null;
  taiKhoan: TaiKhoanLiteDto | null;
}

interface TinhNguyenVienDto {
  id: number;
  nguoiDung: NguoiDungLiteDto | null;
  ghiChu: string;
  coTheGiup: string;
  trangThaiDuyet: string;
  thoiGianDuyet: string;
  createdAt: string;
}

export interface TinhNguyenVienChoDuyetItem {
  id: number;
  ten: string;
  soDienThoai: string;
  email: string;
  diaChi: string;
  avatarUrl: string;
  kyNang: string;
  ghiChu: string;
  createdAt: string;
}

export interface TinhNguyenVienDaDuyetItem extends TinhNguyenVienChoDuyetItem {
  trangThaiDuyet: string;
  thoiGianDuyet: string;
}

export type VaiTroDoiNhom = "truong_nhom" | "pho_nhom" | "thanh_vien";

export interface GanDoiNhomTinhNguyenVienRequest {
  tinhNguyenVienId: number;
  doiNhomId: number;
  vaiTro?: VaiTroDoiNhom;
}

export class TinhNguyenVienApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "TinhNguyenVienApiError";
    this.status = status;
    this.error = error;
  }
}

function isObjectRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function parseEnvelope<T>(value: unknown): ResponseData<T> | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  const status = value.status;
  const message = value.message;
  const error = value.error;

  if (typeof status !== "number" || typeof message !== "string") {
    return null;
  }

  if (!(typeof error === "string" || error === null || error === undefined)) {
    return null;
  }

  return {
    status,
    data: value.data as T,
    error: (error as string | null | undefined) ?? null,
    message,
    timestamp: typeof value.timestamp === "string" ? value.timestamp : "",
    path: typeof value.path === "string" ? value.path : "",
  };
}

function parseViTriLite(value: unknown): ViTriLiteDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  return {
    diaChi: typeof value.diaChi === "string" ? value.diaChi : "",
  };
}

function parseTaiKhoanLite(value: unknown): TaiKhoanLiteDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  return {
    email: typeof value.email === "string" ? value.email : "",
  };
}

function parseNguoiDungLite(value: unknown): NguoiDungLiteDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  const viTriValue = value.viTri;
  const taiKhoanValue = value.taiKhoan;

  const viTri =
    viTriValue === null || viTriValue === undefined ? null : parseViTriLite(viTriValue);
  const taiKhoan =
    taiKhoanValue === null || taiKhoanValue === undefined
      ? null
      : parseTaiKhoanLite(taiKhoanValue);

  if (viTriValue !== null && viTriValue !== undefined && !viTri) {
    return null;
  }
  if (taiKhoanValue !== null && taiKhoanValue !== undefined && !taiKhoan) {
    return null;
  }

  return {
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
    avatarUrl: typeof value.avatarUrl === "string" ? value.avatarUrl : "",
    viTri,
    taiKhoan,
  };
}

function parseTinhNguyenVienDto(value: unknown): TinhNguyenVienDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const nguoiDungValue = value.nguoiDung;
  const nguoiDung =
    nguoiDungValue === null || nguoiDungValue === undefined
      ? null
      : parseNguoiDungLite(nguoiDungValue);

  if (nguoiDungValue !== null && nguoiDungValue !== undefined && !nguoiDung) {
    return null;
  }

  return {
    id: value.id,
    nguoiDung,
    ghiChu: typeof value.ghiChu === "string" ? value.ghiChu : "",
    coTheGiup: typeof value.coTheGiup === "string" ? value.coTheGiup : "",
    trangThaiDuyet: typeof value.trangThaiDuyet === "string" ? value.trangThaiDuyet : "",
    thoiGianDuyet: typeof value.thoiGianDuyet === "string" ? value.thoiGianDuyet : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function trimOrFallback(value: string, fallback: string): string {
  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function mapDtoToChoDuyetItem(dto: TinhNguyenVienDto): TinhNguyenVienChoDuyetItem {
  const nguoiDung = dto.nguoiDung;

  return {
    id: dto.id,
    ten: trimOrFallback(nguoiDung?.ten ?? "", `Tinh nguyen vien #${dto.id}`),
    soDienThoai: trimOrFallback(nguoiDung?.sdt ?? "", "Chua cap nhat"),
    email: trimOrFallback(nguoiDung?.taiKhoan?.email ?? "", "Chua cap nhat"),
    diaChi: trimOrFallback(nguoiDung?.viTri?.diaChi ?? "", "Chua cap nhat"),
    avatarUrl: trimOrFallback(nguoiDung?.avatarUrl ?? "", "/images/user/user-01.jpg"),
    kyNang: trimOrFallback(dto.coTheGiup, "Chua cap nhat"),
    ghiChu: trimOrFallback(dto.ghiChu, "Khong co ghi chu"),
    createdAt: dto.createdAt,
  };
}

function mapDtoToDaDuyetItem(dto: TinhNguyenVienDto): TinhNguyenVienDaDuyetItem {
  return {
    ...mapDtoToChoDuyetItem(dto),
    trangThaiDuyet: trimOrFallback(dto.trangThaiDuyet, "DUOC_DUYET"),
    thoiGianDuyet: dto.thoiGianDuyet,
  };
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new TinhNguyenVienApiError(
      "Phien dang nhap da het han. Vui long dang nhap lai.",
      401
    );
  }

  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new TinhNguyenVienApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new TinhNguyenVienApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new TinhNguyenVienApiError(
      "Cau truc response backend khong dung dinh dang",
      response.status
    );
  }

  if (!response.ok) {
    throw new TinhNguyenVienApiError(
      envelope.message || "Yeu cau tinh nguyen vien that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchChoXetDuyetTinhNguyenVien(): Promise<TinhNguyenVienChoDuyetItem[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/tinh-nguyen-vien/cho-xet-duyet", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new TinhNguyenVienApiError(
      "Backend khong tra ve danh sach tinh nguyen vien hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseTinhNguyenVienDto)
    .filter((item): item is TinhNguyenVienDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new TinhNguyenVienApiError(
      "Du lieu tinh nguyen vien tra ve khong hop le",
      envelope.status
    );
  }

  return parsedItems.map(mapDtoToChoDuyetItem);
}

export async function fetchDaDuyetTinhNguyenVien(): Promise<TinhNguyenVienDaDuyetItem[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>(
    "/tinh-nguyen-vien?trangThaiDuyet=DUOC_DUYET",
    {
      method: "GET",
      headers: {
        Authorization: authorization,
      },
    }
  );

  if (!Array.isArray(envelope.data)) {
    throw new TinhNguyenVienApiError(
      "Backend khong tra ve danh sach tinh nguyen vien da duyet hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseTinhNguyenVienDto)
    .filter((item): item is TinhNguyenVienDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new TinhNguyenVienApiError(
      "Du lieu tinh nguyen vien da duyet tra ve khong hop le",
      envelope.status
    );
  }

  return parsedItems.map(mapDtoToDaDuyetItem);
}

export async function duyetTinhNguyenVien(id: number): Promise<TinhNguyenVienChoDuyetItem> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/tinh-nguyen-vien/${id}/duyet`, {
    method: "PUT",
    headers: {
      Authorization: authorization,
    },
  });

  const parsedItem = parseTinhNguyenVienDto(envelope.data);
  if (!parsedItem) {
    throw new TinhNguyenVienApiError(
      "Backend khong tra ve tinh nguyen vien sau khi duyet",
      envelope.status
    );
  }

  return mapDtoToChoDuyetItem(parsedItem);
}

export async function xoaTinhNguyenVien(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/tinh-nguyen-vien/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}

export async function ganDoiNhomChoTinhNguyenVien(
  request: GanDoiNhomTinhNguyenVienRequest
): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>("/tinh-nguyen-vien/gan-doi-nhom", {
    method: "POST",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });
}
