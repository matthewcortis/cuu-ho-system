import { SERVER_BASE_URL } from "@/api/serverBaseUrl";

export interface ResponseData<T> {
  status: number;
  data: T;
  error: string | null;
  message: string;
  timestamp: string;
  path: string;
}

export interface DangNhapRequest {
  tenDangNhap: string;
  matKhau: string;
}

export interface DangNhapData {
  tokenType: string;
  accessToken: string;
  expiresAt: string;
  taiKhoanId: number;
  tenDangNhap: string;
  vaiTro: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "ApiError";
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

function isDangNhapData(value: unknown): value is DangNhapData {
  if (!isObjectRecord(value)) {
    return false;
  }

  return (
    typeof value.tokenType === "string" &&
    typeof value.accessToken === "string" &&
    typeof value.expiresAt === "string" &&
    typeof value.taiKhoanId === "number" &&
    typeof value.tenDangNhap === "string" &&
    typeof value.vaiTro === "string"
  );
}

export async function dangNhap(request: DangNhapRequest): Promise<DangNhapData> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });
  } catch {
    throw new ApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new ApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<DangNhapData>(payload);
  if (!envelope) {
    throw new ApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new ApiError(envelope.message || "Dang nhap that bai", response.status, envelope.error);
  }

  if (!isDangNhapData(envelope.data)) {
    throw new ApiError("Backend khong tra ve du lieu dang nhap hop le", response.status, envelope.error);
  }

  return envelope.data;
}
