export type ChatRole = "admin" | "volunteer" | "customer";

export interface ChatContact {
  id: number;
  name: string;
  role: ChatRole;
  avatar: string;
  isOnline: boolean;
  lastSeen: string;
  preview: string;
  unreadCount: number;
}

export interface ChatMessageLocation {
  diaChi: string;
  lat: string;
  longitude: string;
}

export interface ChatMessage {
  id: string;
  rawId: number | null;
  sender: "me" | "them";
  text: string;
  meta: string;
  mediaUrl: string;
  mediaType: string;
  location: ChatMessageLocation | null;
  createdAt: string;
}

export type RoleLabels = Record<ChatRole, string>;
