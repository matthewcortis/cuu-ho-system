import { getAuthorizationHeader } from "@/features/auth/utils/authSession";
import { SERVER_BASE_URL } from "@/api/serverBaseUrl";

interface ResponseData<T> {
  status: number;
  data: T;
  error: string | null;
  message: string;
  timestamp: string;
  path: string;
}

export interface DonViDto {
  id: number;
  ten: string;
  maDonVi: string;
  createdAt: string;
}

export interface DonViUpsertRequest {
  ten: string;
  maDonVi: string;
}

export class DonViApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "DonViApiError";
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

function parseDonViDto(value: unknown): DonViDto | null {
  if (!isObjectRecord(value)) {
    return null;
  }

  if (typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
    maDonVi: typeof value.maDonVi === "string" ? value.maDonVi : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
  };
}

function getAuthHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new DonViApiError("Phien dang nhap da het han. Vui long dang nhap lai.", 401);
  }

  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new DonViApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new DonViApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new DonViApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new DonViApiError(
      envelope.message || "Yeu cau don vi that bai",
      response.status,
      envelope.error
    );
  }

  return envelope;
}

export async function fetchDonViList(): Promise<DonViDto[]> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/don-vi", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new DonViApiError("Backend khong tra ve danh sach don vi hop le", envelope.status);
  }

  const parsedItems = envelope.data.map(parseDonViDto);
  if (parsedItems.some((item) => item === null)) {
    throw new DonViApiError("Du lieu don vi tra ve khong hop le", envelope.status);
  }

  return parsedItems as DonViDto[];
}

export async function createDonVi(request: DonViUpsertRequest): Promise<DonViDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>("/don-vi", {
    method: "POST",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseDonViDto(envelope.data);
  if (!parsedItem) {
    throw new DonViApiError("Backend khong tra ve don vi vua tao", envelope.status);
  }

  return parsedItem;
}

export async function updateDonVi(id: number, request: DonViUpsertRequest): Promise<DonViDto> {
  const authorization = getAuthHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/don-vi/${id}`, {
    method: "PUT",
    headers: {
      Authorization: authorization,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  const parsedItem = parseDonViDto(envelope.data);
  if (!parsedItem) {
    throw new DonViApiError("Backend khong tra ve don vi sau cap nhat", envelope.status);
  }

  return parsedItem;
}

export async function deleteDonVi(id: number): Promise<void> {
  const authorization = getAuthHeaderOrThrow();
  await requestEnvelope<unknown>(`/don-vi/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: authorization,
    },
  });
}
