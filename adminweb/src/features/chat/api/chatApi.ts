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

export interface ChatNguoiGuiDto {
  type: "NGUOI_DUNG" | "VANG_LAI";
  userId: string | null;
  ten: string;
  sdt: string;
}

export interface ChatPhieuCuuTroDto {
  id: number;
  createdAt: string;
  ghiChu: string;
  trangThai: string;
  nguoiGui: ChatNguoiGuiDto | null;
}

export interface ChatTinNhanTaiKhoanDto {
  id: number;
  tenDangNhap: string;
  vaiTro: string;
}

export interface ChatTinNhanNguoiDungDto {
  id: string;
  ten: string;
  avatarUrl: string;
  taiKhoan: ChatTinNhanTaiKhoanDto | null;
}

export interface ChatTinNhanViTriDto {
  id: number;
  lat: string;
  longitude: string;
  diaChi: string;
}

export interface ChatTinNhanDto {
  id: number;
  sender: ChatTinNhanNguoiDungDto | null;
  viTri: ChatTinNhanViTriDto | null;
  noiDung: string;
  createdAt: string;
  loaiTinNhan: string;
  mediaUrl: string;
  mediaType: string;
}

export interface GuiTinNhanPhieuPayload {
  noiDung?: string;
  tepTinId?: number;
  viTriId?: number;
  viTri?: {
    diaChi: string;
    lat?: string;
    longitude?: string;
  };
}

interface TepTinDto {
  id: number;
  duongDan: string;
  loaiTepTin: string;
}

export class ChatApiError extends Error {
  readonly status: number;
  readonly error: string | null;

  constructor(message: string, status: number, error: string | null = null) {
    super(message);
    this.name = "ChatApiError";
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

function parseChatNguoiGuiDto(value: unknown): ChatNguoiGuiDto | null {
  if (!isObjectRecord(value) || typeof value.type !== "string") {
    return null;
  }

  const normalizedType = value.type.trim().toUpperCase();
  if (normalizedType !== "NGUOI_DUNG" && normalizedType !== "VANG_LAI") {
    return null;
  }

  return {
    type: normalizedType,
    userId: typeof value.userId === "string" ? value.userId : null,
    ten: typeof value.ten === "string" ? value.ten : "",
    sdt: typeof value.sdt === "string" ? value.sdt : "",
  };
}

function parseChatPhieuCuuTroDto(value: unknown): ChatPhieuCuuTroDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const nguoiGuiValue = value.nguoiGui;
  const nguoiGui =
    nguoiGuiValue === null || nguoiGuiValue === undefined
      ? null
      : parseChatNguoiGuiDto(nguoiGuiValue);
  if (nguoiGuiValue !== null && nguoiGuiValue !== undefined && !nguoiGui) {
    return null;
  }

  return {
    id: value.id,
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
    ghiChu: typeof value.ghiChu === "string" ? value.ghiChu : "",
    trangThai: typeof value.trangThai === "string" ? value.trangThai : "",
    nguoiGui,
  };
}

function parseChatTinNhanTaiKhoanDto(value: unknown): ChatTinNhanTaiKhoanDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    tenDangNhap: typeof value.tenDangNhap === "string" ? value.tenDangNhap : "",
    vaiTro: typeof value.vaiTro === "string" ? value.vaiTro : "",
  };
}

function parseChatTinNhanNguoiDungDto(value: unknown): ChatTinNhanNguoiDungDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "string") {
    return null;
  }

  const taiKhoanValue = value.taiKhoan;
  const taiKhoan =
    taiKhoanValue === null || taiKhoanValue === undefined
      ? null
      : parseChatTinNhanTaiKhoanDto(taiKhoanValue);
  if (taiKhoanValue !== null && taiKhoanValue !== undefined && !taiKhoan) {
    return null;
  }

  return {
    id: value.id,
    ten: typeof value.ten === "string" ? value.ten : "",
    avatarUrl: typeof value.avatarUrl === "string" ? value.avatarUrl : "",
    taiKhoan,
  };
}

function parseChatTinNhanViTriDto(value: unknown): ChatTinNhanViTriDto | null {
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

function parseChatTinNhanDto(value: unknown): ChatTinNhanDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  const senderValue = value.sender;
  const sender =
    senderValue === null || senderValue === undefined
      ? null
      : parseChatTinNhanNguoiDungDto(senderValue);
  if (senderValue !== null && senderValue !== undefined && !sender) {
    return null;
  }

  const viTriValue = value.viTri;
  const viTri =
    viTriValue === null || viTriValue === undefined
      ? null
      : parseChatTinNhanViTriDto(viTriValue);
  if (viTriValue !== null && viTriValue !== undefined && !viTri) {
    return null;
  }

  return {
    id: value.id,
    sender,
    viTri,
    noiDung: typeof value.noiDung === "string" ? value.noiDung : "",
    createdAt: typeof value.createdAt === "string" ? value.createdAt : "",
    loaiTinNhan: typeof value.loaiTinNhan === "string" ? value.loaiTinNhan : "",
    mediaUrl: typeof value.mediaUrl === "string" ? value.mediaUrl : "",
    mediaType: typeof value.mediaType === "string" ? value.mediaType : "",
  };
}

function parseTepTinDto(value: unknown): TepTinDto | null {
  if (!isObjectRecord(value) || typeof value.id !== "number") {
    return null;
  }

  return {
    id: value.id,
    duongDan: typeof value.duongDan === "string" ? value.duongDan : "",
    loaiTepTin: typeof value.loaiTepTin === "string" ? value.loaiTepTin : "",
  };
}

function getAuthorizationHeaderOrThrow(): string {
  const authorization = getAuthorizationHeader();
  if (!authorization) {
    throw new ChatApiError("Phien dang nhap da het han. Vui long dang nhap lai.", 401);
  }
  return authorization;
}

async function requestEnvelope<T>(path: string, init: RequestInit): Promise<ResponseData<T>> {
  let response: Response;

  try {
    response = await fetch(`${SERVER_BASE_URL}${path}`, init);
  } catch {
    throw new ChatApiError("Khong the ket noi den backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new ChatApiError("Phan hoi tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<T>(payload);
  if (!envelope) {
    throw new ChatApiError("Cau truc response backend khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new ChatApiError(envelope.message || "Yeu cau chat that bai", response.status, envelope.error);
  }

  return envelope;
}

export async function fetchChatPhieuList(): Promise<ChatPhieuCuuTroDto[]> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>("/phieu-cuu-tro", {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new ChatApiError("Backend khong tra ve danh sach phieu cuu tro hop le", envelope.status);
  }

  const parsedItems = envelope.data
    .map(parseChatPhieuCuuTroDto)
    .filter((item): item is ChatPhieuCuuTroDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new ChatApiError("Du lieu phieu cuu tro tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function fetchChatTinNhanByPhieuId(phieuId: number): Promise<ChatTinNhanDto[]> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown[]>(`/phieu-cuu-tro/${phieuId}/tin-nhan`, {
    method: "GET",
    headers: {
      Authorization: authorization,
    },
  });

  if (!Array.isArray(envelope.data)) {
    throw new ChatApiError("Backend khong tra ve danh sach tin nhan hop le", envelope.status);
  }

  const parsedItems = envelope.data
    .map(parseChatTinNhanDto)
    .filter((item): item is ChatTinNhanDto => item !== null);

  if (parsedItems.length === 0 && envelope.data.length > 0) {
    throw new ChatApiError("Du lieu tin nhan tra ve khong hop le", envelope.status);
  }

  return parsedItems;
}

export async function guiTinNhanPhieu(
  phieuId: number,
  payload: GuiTinNhanPhieuPayload
): Promise<ChatTinNhanDto> {
  const authorization = getAuthorizationHeaderOrThrow();
  const envelope = await requestEnvelope<unknown>(`/phieu-cuu-tro/${phieuId}/tin-nhan`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: authorization,
    },
    body: JSON.stringify(payload),
  });

  const tinNhan = parseChatTinNhanDto(envelope.data);
  if (!tinNhan) {
    throw new ChatApiError("Backend khong tra ve du lieu tin nhan hop le", envelope.status);
  }

  return tinNhan;
}

export async function capNhatDaXemTinNhan(phieuId: number, lastSeenMessageId: number): Promise<void> {
  const authorization = getAuthorizationHeaderOrThrow();
  await requestEnvelope<unknown>(`/phieu-cuu-tro/${phieuId}/tin-nhan/da-xem`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: authorization,
    },
    body: JSON.stringify({ lastSeenMessageId }),
  });
}

export async function uploadChatMedia(file: File): Promise<TepTinDto> {
  const authorization = getAuthorizationHeader();

  const formData = new FormData();
  formData.append("tepTin", file);
  formData.append("thuMuc", "chat");
  formData.append("tenTep", file.name);

  let response: Response;
  try {
    response = await fetch(`${SERVER_BASE_URL}/tep-tin/upload`, {
      method: "POST",
      headers: authorization
        ? {
            Authorization: authorization,
          }
        : undefined,
      body: formData,
    });
  } catch {
    throw new ChatApiError("Khong the tai tep len backend", 0);
  }

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    throw new ChatApiError("Phan hoi upload tep tu backend khong hop le", response.status);
  }

  const envelope = parseEnvelope<unknown>(payload);
  if (!envelope) {
    throw new ChatApiError("Cau truc response upload tep khong dung dinh dang", response.status);
  }

  if (!response.ok) {
    throw new ChatApiError(envelope.message || "Tai tep that bai", response.status, envelope.error);
  }

  const tepTin = parseTepTinDto(envelope.data);
  if (!tepTin) {
    throw new ChatApiError("Backend khong tra ve du lieu tep tin hop le", response.status);
  }

  return tepTin;
}
