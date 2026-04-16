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
  id: number;
  diaChi: string;
}

export interface DoiNhomThanhVienDto {
  tinhNguyenVienId: number;
  ten: string;
  sdt: string;
  avatarUrl: string;
  vaiTro: "truong_nhom" | "pho_nhom" | "thanh_vien";
}

export interface DoiNhomDto {
  id: number;
  tenDoiNhom: string;
  soDienThoai: string;
  viTri: ViTriLiteDto | null;
  trangThaiHoatDong: boolean;
  active: boolean;
  trangThai: string;
  createdAt: string;
  doiTruong: DoiNhomThanhVienDto | null;
  thanhViens: DoiNhomThanhVienDto[];
  soLuongThanhVien: number;
}

export interface DoiNhomCreateRequest {
  tenDoiNhom: string;
  soDienThoai: string;
  viTri: {
    diaChi: string;
    lat?: string;
    longitude?: string;
  };
  doiTruongTinhNguyenVienId: number;
}

interface NguoiDungLiteDto {
  ten: string;
  sdt: string;
}

interface TinhNguyenVienLiteDto {
  id: number;
  nguoiDung: NguoiDungLiteDto | null;
}

export interface DoiTruongOption {
  id: number;
  ten: string;
  soDienThoai: string;
}

export class DoiNhomApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "DoiNhomApiError";
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

function parseViTriLiteDto(value: unknown): ViTriLiteDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    diaChi: typeof value.diaChi === "string" ? value.diaChi : "",
  };
}

function parseDoiNhomDto(value: unknown): DoiNhomDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const viTriValue = value.viTri;
  const viTri =
    viTriValue === null || viTriValue === undefined ? null : parseViTriLiteDto(viTriValue);

  if (viTriValue !== null && viTriValue !== undefined && !viTri) {
    return null;
  }

  const doiTruongValue = value.doiTruong;
  const thanhViensValue = value.thanhViens;
  const doiTruong =
    doiTruongValue === null || doiTruongValue === undefined
      ? null
      : parseDoiNhomThanhVienDto(doiTruongValue);
  if (doiTruongValue !== null && doiTruongValue !== undefined && !doiTruong) {
    return null;
  }

  const thanhViens = Array.isArray(thanhViensValue)
    ? thanhViensValue
        .map(parseDoiNhomThanhVienDto)
        .filter((item): item is DoiNhomThanhVienDto => item !== null)
    : [];
  if (Array.isArray(thanhViensValue) && thanhViens.length === 0 && thanhViensValue.length > 0) {
    return null;
  }

  return {
    id: value.id,
    tenDoiNhom: typeof value.tenDoiNhom === "string" ? value.tenDoiNhom : "",
    soDienThoai: typeof value.soDienThoai === "string" ? value.soDienThoai : "",
    viTri,
    trangThaiHoatDong: typeof value.trangThaiHoatDong === "boolean" ? value.trangThaiHoatDong : true,
    active: typeof value.active === "boolean" ? value.active : true,
    trangThai: typeof value.trangThai === "string" ? value.trangThai : "idle",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
    doiTruong,
    thanhViens,
    soLuongThanhVien: typeof value.soLuongThanhVien === "number" ? value.soLuongThanhVien : thanhViens.length,
  };
}

function parseDoiNhomThanhVienDto(value: unknown): DoiNhomThanhVienDto | null {
  if (!isObjectRecord(value) || typeof value.tinhNguyenVienId !== "number") {
    return null;
  }

  const vaiTroRaw = typeof value.vaiTro === "string" ? value.vaiTro.trim().toLowerCase() : "";
  const vaiTro =
    vaiTroRaw === "truong_nhom" || vaiTroRaw === "pho_nhom" || vaiTroRaw === "thanh_vien"
      ? vaiTroRaw
      : "thanh_vien";

  return {
    tinhNguyenVienId: value.tinhNguyenVienId,
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
    avatarUrl: typeof value.avatarUrl === "string" ? value.avatarUrl : "",
    vaiTro,
  };
}

function parseNguoiDungLiteDto(value: unknown): NguoiDungLiteDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  return {
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
  };
}

function parseTinhNguyenVienLiteDto(value: unknown): TinhNguyenVienLiteDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const nguoiDungValue = value.nguoiDung;
  const nguoiDung =
    nguoiDungValue === null || nguoiDungValue === undefined
      ? null
      : parseNguoiDungLiteDto(nguoiDungValue);

  if (nguoiDungValue !== null && nguoiDungValue !== undefined && !nguoiDung) {
    return null;
  }

  return {
    id: value.id,
    nguoiDung,
  };
}

function trimOrFallback(value: string, fallback: string): string {
  const normalized = value.trim();
  return normalized.length > 0 ? normalized : fallback;
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new DoiNhomApiError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", 401);
  }

  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new DoiNhomApiError("Không thể kết nối đến backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new DoiNhomApiError("Phản hồi từ backend không hợp lệ", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new DoiNhomApiError("Cấu trúc response backend không đúng định dạng", response.status);
  }

  if (!response.ok) {
    throw new DoiNhomApiError(
      envelope.message || "Yêu cầu đội nhóm thất bại",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchDoiTruongOptions(): Promise<DoiTruongOption[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>(
    "/tinh-nguyen-vien?trangThaiDuyet=DUOC_DUYET&chiLayDoiTruongKhaDung=true",
    {
      method: "GET",
      headers: {
        Authorization: authorization,
      },
    }
  );

  if (!Array.isArray(envelope.data)) {
    throw new DoiNhomApiError(
      "Backend không trả về danh sách tình nguyện viên hợp lệ",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseTinhNguyenVienLiteDto)
    .filter((item): item is TinhNguyenVienLiteDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new DoiNhomApiError("Dữ liệu tình nguyện viên trả về không hợp lệ", envelope.status);
  }

  return parsedItems.map((item) => ({
    id: item.id,
    ten: trimOrFallback(item.nguoiDung?.ten ?? "", `Tình nguyện viên #${item.id}`),
    soDienThoai: trimOrFallback(item.nguoiDung?.sdt ?? "", "Chưa cập nhật"),
  }));
}

export async function fetchDoiNhomList(): Promise<DoiNhomDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/doi-nhom", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new DoiNhomApiError("Backend không trả về danh sách đội nhóm hợp lệ", envelope.status);
  }

  const parsedItems = envelope.data
    .map(parseDoiNhomDto)
    .filter((item): item is DoiNhomDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new DoiNhomApiError("Dữ liệu đội nhóm trả về không hợp lệ", envelope.status);
  }

  return parsedItems;
}

export async function createDoiNhom(request: DoiNhomCreateRequest): Promise<DoiNhomDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>("/doi-nhom", {
    method: "POST",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseDoiNhomDto(envelope.data);
  if (!parsedItem) {
    throw new DoiNhomApiError("Backend không trả về đội nhóm vừa tạo", envelope.status);
  }

  return parsedItem;
}
