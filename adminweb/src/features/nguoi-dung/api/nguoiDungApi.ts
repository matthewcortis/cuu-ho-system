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

export interface ViTriDto {
  id: number;
  lat: string;
  longitude: string;
  diaChi: string;
}

export interface TaiKhoanDto {
  id: number;
  email: string;
  tenDangNhap: string;
  trangThai: boolean;
  vaiTro: string;
  createdAt: string;
}

export interface NguoiDungDto {
  id: string;
  ten: string;
  sdt: string;
  avatarUrl: string;
  createdAt: string;
  taiKhoan: TaiKhoanDto | null;
  viTri: ViTriDto | null;
}

export interface NguoiGuiDto {
  type: "NGUOI_DUNG" | "VANG_LAI";
  userId: string | null;
  ten: string;
  sdt: string;
}

export interface PhieuCuuTroChiTietDto {
  id: number;
  vatPhamId: number | null;
  tenVatPham: string;
  soLuong: number | null;
  ghiChu: string;
}

export interface PhieuCuuTroDto {
  id: number;
  viTri: ViTriDto | null;
  nguoiGui: NguoiGuiDto | null;
  ghiChu: string;
  trangThai: string;
  chiTietCuuTro: PhieuCuuTroChiTietDto[];
  createdAt: string;
}

export class NguoiDungApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "NguoiDungApiError";
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

  if (typeof value.status !== "number" || typeof value.message !== "string") {
    return null;
  }

  const error = value.error;
  if (!(typeof error === "string" || error === null || error === undefined)) {
    return null;
  }

  return {
    status: value.status,
    data: value.data as T,
    error: (error as string | null | undefined) ?? null,
    message: value.message,
    timestamp: typeof value.timestamp === "string" ? value.timestamp : "",
    path: typeof value.path === "string" ? value.path : "",
  };
}

function parseViTriDto(value: unknown): ViTriDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    lat: typeof value.lat === "string" ? value.lat : "",
    longitude: typeof value.longitude === "string" ? value.longitude : "",
    diaChi: typeof value.diaChi === "string" ? value.diaChi : "",
  };
}

function parseTaiKhoanDto(value: unknown): TaiKhoanDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    email: typeof value.email === "string" ? value.email : "",
    tenDangNhap: typeof value.tenDangNhap === "string" ? value.tenDangNhap : "",
    trangThai: typeof value.trangThai === "boolean" ? value.trangThai : false,
    vaiTro: typeof value.vaiTro === "string" ? value.vaiTro : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function parseNguoiDungDto(value: unknown): NguoiDungDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "string") {
    return null;
  }

  const taiKhoanValue = value.taiKhoan;
  const viTriValue = value.viTri;

  const taiKhoan =
    taiKhoanValue === null || taiKhoanValue === undefined
      ? null
      : parseTaiKhoanDto(taiKhoanValue);
  if (taiKhoanValue !== null && taiKhoanValue !== undefined && !taiKhoan) {
    return null;
  }

  const viTri =
    viTriValue === null || viTriValue === undefined ? null : parseViTriDto(viTriValue);
  if (viTriValue !== null && viTriValue !== undefined && !viTri) {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
    avatarUrl: typeof value.avatarUrl === "string" ? value.avatarUrl : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
    taiKhoan,
    viTri,
  };
}

function parseNguoiGuiDto(value: unknown): NguoiGuiDto | null {
  if (!isObjectRecord(value) || typeof value.type !== "string") {
    return null;
  }

  const type = value.type.trim().toUpperCase();
  if (type !== "NGUOI_DUNG" && type !== "VANG_LAI") {
    return null;
  }

  return {
    type,
    userId: typeof value.userId === "string" ? value.userId : null,
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
  };
}

function parsePhieuCuuTroChiTietDto(value: unknown): PhieuCuuTroChiTietDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    vatPhamId: typeof value.vatPhamId === "number" ? value.vatPhamId : null,
    tenVatPham: typeof value.tenVatPham === "string" ? value.tenVatPham : "",
    soLuong: typeof value.soLuong === "number" ? value.soLuong : null,
    ghiChu: typeof value.ghiChu === "string" ? value.ghiChu : "",
  };
}

function parsePhieuCuuTroDto(value: unknown): PhieuCuuTroDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const viTriValue = value.viTri;
  const nguoiGuiValue = value.nguoiGui;
  const chiTietValue = value.chiTietCuuTro;

  const viTri =
    viTriValue === null || viTriValue === undefined ? null : parseViTriDto(viTriValue);
  if (viTriValue !== null && viTriValue !== undefined && !viTri) {
    return null;
  }

  const nguoiGui =
    nguoiGuiValue === null || nguoiGuiValue === undefined
      ? null
      : parseNguoiGuiDto(nguoiGuiValue);
  if (nguoiGuiValue !== null && nguoiGuiValue !== undefined && !nguoiGui) {
    return null;
  }

  const chiTietCuuTro = Array.isArray(chiTietValue)
    ? chiTietValue
        .map(parsePhieuCuuTroChiTietDto)
        .filter((item): item is PhieuCuuTroChiTietDto => item !== null)
    : [];
  if (Array.isArray(chiTietValue) && chiTietCuuTro.length === 0 && chiTietValue.length > 0) {
    return null;
  }

  return {
    id: value.id,
    viTri,
    nguoiGui,
    ghiChu: typeof value.ghiChu === "string" ? value.ghiChu : "",
    trangThai: typeof value.trangThai === "string" ? value.trangThai : "",
    chiTietCuuTro,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function getAuthorizationHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new NguoiDungApiError("Phien dang nhap da het han. Vui long dang nhap lai.", 401);
  }
  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;

  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new NguoiDungApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new NguoiDungApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new NguoiDungApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new NguoiDungApiError(
      envelope.message || "Yeu cau nguoi dung that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchNguoiDungList(): Promise<NguoiDungDto[]> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/nguoi-dung", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new NguoiDungApiError(
      "Backend khong tra ve danh sach nguoi dung hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseNguoiDungDto)
    .filter((item): item is NguoiDungDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new NguoiDungApiError("Du lieu nguoi dung tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function fetchPhieuCuuTroList(): Promise<PhieuCuuTroDto[]> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/phieu-cuu-tro", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new NguoiDungApiError(
      "Backend khong tra ve danh sach phieu cuu tro hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parsePhieuCuuTroDto)
    .filter((item): item is PhieuCuuTroDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new NguoiDungApiError("Du lieu phieu cuu tro tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function updateNguoiDungTaiKhoanTrangThai(
  nguoiDungId: string,
  trangThai: boolean
): Promise<NguoiDungDto> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(
    `/nguoi-dung/${encodeURIComponent(nguoiDungId)}/trang-thai-tai-khoan`,
    {
      method: "PUT",
      headers: {
        Authorization: authorization,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ trangThai }),
    }
  );

  const parsedItem = parseNguoiDungDto(envelope.data);
  if (!parsedItem) {
    throw new NguoiDungApiError(
      "Backend khong tra ve nguoi dung sau cap nhat trang thai tai khoan",
      envelope.status
    );
  }

  return parsedItem;
}
