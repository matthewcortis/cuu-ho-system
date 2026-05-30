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

export interface BangTinTepTinDto {
  id: number;
  duongDan: string;
  loaiTepTin: string;
}

export interface BangTinNguoiDungDto {
  id: string;
  ten: string;
  sdt: string;
  avatarUrl: string;
}

export interface BangTinDto {
  id: number;
  tieuDe: string;
  noiDung: string;
  tepTin: BangTinTepTinDto | null;
  nguoiDung: BangTinNguoiDungDto | null;
  trangThai: boolean;
  createdAt: string;
}

export class BangTinApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "BangTinApiError";
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

function parseTepTin(value: unknown): BangTinTepTinDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    duongDan: typeof value.duongDan === "string" ? value.duongDan : "",
    loaiTepTin: typeof value.loaiTepTin === "string" ? value.loaiTepTin : "",
  };
}

function parseNguoiDung(value: unknown): BangTinNguoiDungDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  const rawId = value.id;
  let id = "";
  if (typeof rawId === "string") {
    id = rawId;
  } else if (typeof rawId === "number") {
    id = String(rawId);
  } else {
    return null;
  }

  return {
    id,
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
    avatarUrl: typeof value.avatarUrl === "string" ? value.avatarUrl : "",
  };
}

function parseBangTin(value: unknown): BangTinDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const tepTinValue = value.tepTin;
  const nguoiDungValue = value.nguoiDung;
  const tepTin =
    tepTinValue === null || tepTinValue === undefined ? null : parseTepTin(tepTinValue);
  const nguoiDung =
    nguoiDungValue === null || nguoiDungValue === undefined
      ? null
      : parseNguoiDung(nguoiDungValue);

  if (tepTinValue !== null && tepTinValue !== undefined && !tepTin) {
    return null;
  }
  if (nguoiDungValue !== null && nguoiDungValue !== undefined && !nguoiDung) {
    return null;
  }

  return {
    id: value.id,
    tieuDe: typeof value.tieuDe === "string" ? value.tieuDe : "",
    noiDung: typeof value.noiDung === "string" ? value.noiDung : "",
    tepTin,
    nguoiDung,
    trangThai: typeof value.trangThai === "boolean" ? value.trangThai : true,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new BangTinApiError("Phien dang nhap da het han. Vui long dang nhap lai.", 401);
  }
  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new BangTinApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new BangTinApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new BangTinApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new BangTinApiError(
      envelope.message || "Yeu cau bang tin that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchBangTinAdminList(): Promise<BangTinDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/bang-tin/quan-ly", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new BangTinApiError(
      "Backend khong tra ve danh sach bang tin hop le",
      envelope.status
    );
  }

  const parsedItems = envelope.data
    .map(parseBangTin)
    .filter((item): item is BangTinDto => item !== null);
  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new BangTinApiError("Du lieu bang tin tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function deleteBangTin(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/bang-tin/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}
