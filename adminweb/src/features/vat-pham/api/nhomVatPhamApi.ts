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

interface LoaiSuCoDto {
  id: number;
  tenLoaiSuCo: string;
  moTa: string;
  createdAt: string;
}

export interface NhomVatPhamDto {
  id: number;
  ten: string;
  moTa: string;
  loaiSuCo: LoaiSuCoDto | null;
  createdAt: string;
}

export interface NhomVatPhamUpsertRequest {
  ten: string;
  moTa: string | null;
  loaiSuCoId: number | null;
}

export class NhomVatPhamApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "NhomVatPhamApiError";
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

function parseLoaiSuCo(value: unknown): LoaiSuCoDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  if (typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    tenLoaiSuCo: typeof value.tenLoaiSuCo === "string" ? value.tenLoaiSuCo : "",
    moTa: typeof value.moTa === "string" ? value.moTa : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function parseNhomVatPhamDto(value: unknown): NhomVatPhamDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  if (typeof value.id !== "number") {
    return null;
  }

  const loaiSuCoValue = value.loaiSuCo;
  const loaiSuCo =
    loaiSuCoValue === null || loaiSuCoValue === undefined
      ? null
      : parseLoaiSuCo(loaiSuCoValue);

  if (loaiSuCoValue !== null && loaiSuCoValue !== undefined && !loaiSuCo) {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
    moTa: typeof value.moTa === "string" ? value.moTa : "",
    loaiSuCo,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new NhomVatPhamApiError(
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
    throw new NhomVatPhamApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new NhomVatPhamApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new NhomVatPhamApiError(
      "Cau truc response backend khong dung dinh dang",
      response.status
    );
  }

  if (!response.ok) {
    throw new NhomVatPhamApiError(
      envelope.message || "Yeu cau nhom vat pham that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchNhomVatPhamList(): Promise<NhomVatPhamDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/nhom-vat-pham", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new NhomVatPhamApiError(
      "Backend khong tra ve danh sach nhom vat pham hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseNhomVatPhamDto)
    .filter((item): item is NhomVatPhamDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new NhomVatPhamApiError("Du lieu nhom vat pham tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function createNhomVatPham(
  request: NhomVatPhamUpsertRequest
): Promise<NhomVatPhamDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>("/nhom-vat-pham", {
    method: "POST",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseNhomVatPhamDto(envelope.data);
  if (!parsedItem) {
    throw new NhomVatPhamApiError("Backend khong tra ve nhom vat pham vua tao", envelope.status);
  }

  return parsedItem;
}

export async function updateNhomVatPham(
  id: number,
  request: NhomVatPhamUpsertRequest
): Promise<NhomVatPhamDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/nhom-vat-pham/${id}`, {
    method: "PUT",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseNhomVatPhamDto(envelope.data);
  if (!parsedItem) {
    throw new NhomVatPhamApiError(
      "Backend khong tra ve nhom vat pham sau cap nhat",
      envelope.status
    );
  }

  return parsedItem;
}

export async function deleteNhomVatPham(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/nhom-vat-pham/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}
