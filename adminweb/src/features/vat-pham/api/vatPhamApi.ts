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

interface DonViLiteDto {
  id: number;
  ten: string;
}

interface NhomVatPhamLiteDto {
  id: number;
  ten: string;
}

interface TepTinLiteDto {
  id: number;
  duongDan: string;
  loaiTepTin: string;
}

export interface VatPhamDto {
  id: number;
  tenVatPham: string;
  soLuong: number;
  donVi: DonViLiteDto | null;
  nhomVatPham: NhomVatPhamLiteDto | null;
  tepTin: TepTinLiteDto | null;
  trangThai: boolean;
  createdAt: string;
}

export interface VatPhamCreateWithImageRequest {
  tenVatPham: string;
  soLuong: number;
  donViId: number;
  nhomVatPhamId: number;
  anhVatPham: File;
  tepTinId?: number | null;
  trangThai?: boolean;
}

export interface VatPhamUpdateRequest {
  tenVatPham: string;
  soLuong: number;
  donViId: number;
  nhomVatPhamId: number;
  tepTinId?: number | null;
  trangThai?: boolean;
}

export class VatPhamApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "VatPhamApiError";
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

function parseDonViLite(value: unknown): DonViLiteDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
  };
}

function parseNhomVatPhamLite(value: unknown): NhomVatPhamLiteDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
  };
}

function parseTepTinLite(value: unknown): TepTinLiteDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    duongDan: typeof value.duongDan === "string" ? value.duongDan : "",
    loaiTepTin: typeof value.loaiTepTin === "string" ? value.loaiTepTin : "",
  };
}

function parseVatPhamDto(value: unknown): VatPhamDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const donViValue = value.donVi;
  const nhomVatPhamValue = value.nhomVatPham;
  const tepTinValue = value.tepTin;

  const donVi =
    donViValue === null || donViValue === undefined ? null : parseDonViLite(donViValue);
  const nhomVatPham =
    nhomVatPhamValue === null || nhomVatPhamValue === undefined
      ? null
      : parseNhomVatPhamLite(nhomVatPhamValue);
  const tepTin =
    tepTinValue === null || tepTinValue === undefined ? null : parseTepTinLite(tepTinValue);

  if (donViValue !== null && donViValue !== undefined && !donVi) {
    return null;
  }
  if (nhomVatPhamValue !== null && nhomVatPhamValue !== undefined && !nhomVatPham) {
    return null;
  }
  if (tepTinValue !== null && tepTinValue !== undefined && !tepTin) {
    return null;
  }

  return {
    id: value.id,
    tenVatPham: typeof value.tenVatPham === "string" ? value.tenVatPham : "",
    soLuong: typeof value.soLuong === "number" ? value.soLuong : 0,
    donVi,
    nhomVatPham,
    tepTin,
    trangThai: typeof value.trangThai === "boolean" ? value.trangThai : true,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new VatPhamApiError("Phien dang nhap da het han. Vui long dang nhap lai.", 401);
  }

  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new VatPhamApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new VatPhamApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new VatPhamApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new VatPhamApiError(
      envelope.message || "Yeu cau vat pham that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchVatPhamList(): Promise<VatPhamDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/vat-pham", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new VatPhamApiError("Backend khong tra ve danh sach vat pham hop le", envelope.status);
  }

  const parsedItems = envelope.data
    .map(parseVatPhamDto)
    .filter((item): item is VatPhamDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new VatPhamApiError("Du lieu vat pham tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function createVatPhamWithImage(
  request: VatPhamCreateWithImageRequest
): Promise<VatPhamDto> {
  const authorization = getAuthHeaderOrThrow();
  const formData = new FormData();
  formData.append("tenVatPham", request.tenVatPham);
  formData.append("soLuong", String(request.soLuong));
  formData.append("donViId", String(request.donViId));
  formData.append("nhomVatPhamId", String(request.nhomVatPhamId));
  formData.append("anhVatPham", request.anhVatPham);

  if (typeof request.tepTinId === "number") {
    formData.append("tepTinId", String(request.tepTinId));
  }
  if (typeof request.trangThai === "boolean") {
    formData.append("trangThai", String(request.trangThai));
  }

  const envelope = await requestEnvelope<unknown>("/vat-pham", {
    method: "POST",
    headers: {
      Authorization: authorization,
    },
    body: formData,
  });

  const parsedItem = parseVatPhamDto(envelope.data);
  if (!parsedItem) {
    throw new VatPhamApiError("Backend khong tra ve vat pham vua tao", envelope.status);
  }

  return parsedItem;
}

export async function updateVatPham(
  id: number,
  request: VatPhamUpdateRequest
): Promise<VatPhamDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/vat-pham/${id}`, {
    method: "PUT",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      tenVatPham: request.tenVatPham,
      soLuong: request.soLuong,
      donViId: request.donViId,
      nhomVatPhamId: request.nhomVatPhamId,
      tepTinId: request.tepTinId ?? null,
      trangThai: request.trangThai ?? true,
    }),
  });

  const parsedItem = parseVatPhamDto(envelope.data);
  if (!parsedItem) {
    throw new VatPhamApiError("Backend khong tra ve vat pham sau cap nhat", envelope.status);
  }

  return parsedItem;
}

export async function deleteVatPham(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/vat-pham/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}
