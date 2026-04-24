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

export interface LoaiSuCoDto {
  id: number;
  ten: string;
  iconUrl: string;
  trangThai: boolean;
  createdAt: string;
}

export interface TepTinLiteDto {
  id: number;
  duongDan: string;
  loaiTepTin: string;
}

export interface LoaiSuCoUpsertRequest {
  ten: string;
  iconUrl: string | null;
  trangThai: boolean;
}

export interface LoaiSuCoIconUploadRequest {
  iconFile: File;
  tenLoaiSuCo: string;
}

export class LoaiSuCoApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "LoaiSuCoApiError";
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

function parseLoaiSuCoDto(value: unknown): LoaiSuCoDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  if (typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
    iconUrl: typeof value.iconUrl === "string" ? value.iconUrl : "",
    trangThai: typeof value.trangThai === "boolean" ? value.trangThai : true,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
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

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new LoaiSuCoApiError(
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
    throw new LoaiSuCoApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new LoaiSuCoApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new LoaiSuCoApiError(
      "Cau truc response backend khong dung dinh dang",
      response.status
    );
  }

  if (!response.ok) {
    throw new LoaiSuCoApiError(
      envelope.message || "Yeu cau loai su co that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchLoaiSuCoList(): Promise<LoaiSuCoDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/loai-su-co", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new LoaiSuCoApiError(
      "Backend khong tra ve danh sach loai su co hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseLoaiSuCoDto)
    .filter((item): item is LoaiSuCoDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new LoaiSuCoApiError("Du lieu loai su co tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function createLoaiSuCo(
  request: LoaiSuCoUpsertRequest
): Promise<LoaiSuCoDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>("/loai-su-co", {
    method: "POST",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseLoaiSuCoDto(envelope.data);
  if (!parsedItem) {
    throw new LoaiSuCoApiError("Backend khong tra ve loai su co vua tao", envelope.status);
  }

  return parsedItem;
}

export async function updateLoaiSuCo(
  id: number,
  request: LoaiSuCoUpsertRequest
): Promise<LoaiSuCoDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/loai-su-co/${id}`, {
    method: "PUT",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseLoaiSuCoDto(envelope.data);
  if (!parsedItem) {
    throw new LoaiSuCoApiError(
      "Backend khong tra ve loai su co sau cap nhat",
      envelope.status
    );
  }

  return parsedItem;
}

export async function deleteLoaiSuCo(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/loai-su-co/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}

export async function uploadLoaiSuCoIcon(
  request: LoaiSuCoIconUploadRequest
): Promise<TepTinLiteDto> {
  const authorization = getAuthHeaderOrThrow();
  const formData = new FormData();
  const tenLoaiSuCo = request.tenLoaiSuCo.trim();

  formData.append("tepTin", request.iconFile);
  formData.append("thuMuc", "iconloaisuco");
  formData.append("tenTep", tenLoaiSuCo || `icon-loai-su-co-${Date.now()}`);

  const envelope = await requestEnvelope<unknown>("/tep-tin/upload", {
    method: "POST",
    headers: {
      Authorization: authorization,
    },
    body: formData,
  });

  const parsedItem = parseTepTinLite(envelope.data);
  if (!parsedItem) {
    throw new LoaiSuCoApiError("Backend khong tra ve icon vua tai len", envelope.status);
  }

  return parsedItem;
}
