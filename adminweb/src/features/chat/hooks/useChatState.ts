import { SERVER_BASE_URL } from "@/api/serverBaseUrl";
import {
  getAuthSession,
  getAuthorizationHeader,
} from "@/features/auth/utils/authSession";
import {
  capNhatDaXemTinNhan,
  ChatApiError,
  fetchChatPhieuList,
  fetchChatTinNhanByPhieuId,
  guiTinNhanPhieu,
  uploadChatMedia,
  type ChatPhieuCuuTroDto,
  type ChatTinNhanDto,
} from "../api/chatApi";
import { StompClient, type StompFrame } from "../api/stompClient";
import { DEFAULT_CHAT_AVATAR, roleLabels } from "../data/chatData";
import type { ChatContact, ChatMessage, ChatRole } from "../types/chatTypes";
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type KeyboardEvent,
} from "react";

function resolveChatRole(phieu: ChatPhieuCuuTroDto): ChatRole {
  if (phieu.nguoiGui?.type === "NGUOI_DUNG") {
    return "customer";
  }
  return "volunteer";
}

function trimOrEmpty(value: string | null | undefined): string {
  if (typeof value !== "string") {
    return "";
  }
  return value.trim();
}

function formatLastSeen(value: string): string {
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return "vừa xong";
  }

  const diffMs = Date.now() - parsedDate.getTime();
  if (diffMs < 60_000) {
    return "vừa xong";
  }

  const diffMinutes = Math.floor(diffMs / 60_000);
  if (diffMinutes < 60) {
    return `${diffMinutes}p`;
  }

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours}h`;
  }

  return parsedDate.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
  });
}

function formatMessageMeta(createdAt: string, senderName: string, sender: "me" | "them"): string {
  const parsedDate = new Date(createdAt);
  const timeText = Number.isNaN(parsedDate.getTime())
    ? "vừa xong"
    : parsedDate.toLocaleTimeString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
      });

  if (sender === "them" && senderName.length > 0) {
    return `${senderName}, ${timeText}`;
  }

  return timeText;
}

function buildMessagePreview(message: ChatMessage): string {
  const normalizedText = trimOrEmpty(message.text);
  if (normalizedText.length > 0) {
    return normalizedText;
  }

  if (trimOrEmpty(message.mediaUrl).length > 0) {
    return "[Media]";
  }

  if (message.location) {
    if (trimOrEmpty(message.location.diaChi).length > 0) {
      return `Vị trí: ${message.location.diaChi}`;
    }
    return "[Vị trí]";
  }

  return "Tin nhắn mới";
}

function mapPhieuToContact(phieu: ChatPhieuCuuTroDto): ChatContact {
  const nguoiGuiTen = trimOrEmpty(phieu.nguoiGui?.ten);
  const title = nguoiGuiTen.length > 0 ? nguoiGuiTen : `Phiếu #${phieu.id}`;

  return {
    id: phieu.id,
    name: title,
    role: resolveChatRole(phieu),
    avatar: DEFAULT_CHAT_AVATAR,
    isOnline: false,
    lastSeen: formatLastSeen(phieu.createdAt),
    preview: trimOrEmpty(phieu.ghiChu) || "Chưa có tin nhắn",
    unreadCount: 0,
  };
}

function mapTinNhanToChatMessage(
  tinNhan: ChatTinNhanDto,
  currentTaiKhoanId: number | null
): ChatMessage {
  const senderTaiKhoanId = tinNhan.sender?.taiKhoan?.id ?? null;
  const sender: "me" | "them" =
    senderTaiKhoanId !== null && currentTaiKhoanId !== null && senderTaiKhoanId === currentTaiKhoanId
      ? "me"
      : "them";

  const senderName = trimOrEmpty(tinNhan.sender?.ten);

  return {
    id: String(tinNhan.id),
    rawId: tinNhan.id,
    sender,
    text: trimOrEmpty(tinNhan.noiDung),
    meta: formatMessageMeta(tinNhan.createdAt, senderName, sender),
    mediaUrl: trimOrEmpty(tinNhan.mediaUrl),
    mediaType: trimOrEmpty(tinNhan.mediaType),
    location: tinNhan.viTri
      ? {
          diaChi: trimOrEmpty(tinNhan.viTri.diaChi),
          lat: trimOrEmpty(tinNhan.viTri.lat),
          longitude: trimOrEmpty(tinNhan.viTri.longitude),
        }
      : null,
    createdAt: tinNhan.createdAt,
  };
}

function parseRealtimeTinNhan(value: unknown): ChatTinNhanDto | null {
  if (typeof value !== "object" || value === null) {
    return null;
  }

  const raw = value as Record<string, unknown>;
  if (typeof raw.id !== "number") {
    return null;
  }

  const senderValue = raw.sender;
  let sender: ChatTinNhanDto["sender"] = null;
  if (typeof senderValue === "object" && senderValue !== null) {
    const senderRecord = senderValue as Record<string, unknown>;
    const taiKhoanValue = senderRecord.taiKhoan;
    let taiKhoan: NonNullable<ChatTinNhanDto["sender"]>["taiKhoan"] = null;

    if (typeof taiKhoanValue === "object" && taiKhoanValue !== null) {
      const taiKhoanRecord = taiKhoanValue as Record<string, unknown>;
      if (typeof taiKhoanRecord.id === "number") {
        taiKhoan = {
          id: taiKhoanRecord.id,
          tenDangNhap:
            typeof taiKhoanRecord.tenDangNhap === "string" ? taiKhoanRecord.tenDangNhap : "",
          vaiTro: typeof taiKhoanRecord.vaiTro === "string" ? taiKhoanRecord.vaiTro : "",
        };
      }
    }

    if (typeof senderRecord.id === "string") {
      sender = {
        id: senderRecord.id,
        ten: typeof senderRecord.ten === "string" ? senderRecord.ten : "",
        avatarUrl: typeof senderRecord.avatarUrl === "string" ? senderRecord.avatarUrl : "",
        taiKhoan,
      };
    }
  }

  const viTriValue = raw.viTri;
  let viTri: ChatTinNhanDto["viTri"] = null;
  if (typeof viTriValue === "object" && viTriValue !== null) {
    const viTriRecord = viTriValue as Record<string, unknown>;
    if (typeof viTriRecord.id === "number") {
      viTri = {
        id: viTriRecord.id,
        lat: typeof viTriRecord.lat === "string" ? viTriRecord.lat : "",
        longitude: typeof viTriRecord.longitude === "string" ? viTriRecord.longitude : "",
        diaChi: typeof viTriRecord.diaChi === "string" ? viTriRecord.diaChi : "",
      };
    }
  }

  return {
    id: raw.id,
    sender,
    viTri,
    noiDung: typeof raw.noiDung === "string" ? raw.noiDung : "",
    createdAt: typeof raw.createdAt === "string" ? raw.createdAt : "",
    loaiTinNhan: typeof raw.loaiTinNhan === "string" ? raw.loaiTinNhan : "",
    mediaUrl: typeof raw.mediaUrl === "string" ? raw.mediaUrl : "",
    mediaType: typeof raw.mediaType === "string" ? raw.mediaType : "",
  };
}

function resolveErrorMessage(error: unknown): string {
  if (error instanceof ChatApiError) {
    return error.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return "Không thể xử lý dữ liệu chat.";
}

function buildWebSocketUrl(): string {
  const parsed = new URL(SERVER_BASE_URL);
  parsed.protocol = parsed.protocol === "https:" ? "wss:" : "ws:";
  parsed.pathname = "/ws";
  parsed.search = "";
  parsed.hash = "";
  return parsed.toString();
}

function buildChatDestination(phieuId: number): string {
  return `/topic/phieu-cuu-tro/${phieuId}/tin-nhan`;
}

function parsePhieuIdFromDestination(destination: string): number | null {
  const matched = destination.match(/^\/topic\/phieu-cuu-tro\/(\d+)\/tin-nhan$/);
  if (!matched) {
    return null;
  }

  const parsedId = Number.parseInt(matched[1] ?? "", 10);
  return Number.isFinite(parsedId) ? parsedId : null;
}

function getCurrentPosition(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Trình duyệt không hỗ trợ định vị."));
      return;
    }

    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout: 10_000,
      maximumAge: 0,
    });
  });
}

export function useChatState() {
  const session = getAuthSession();
  const currentTaiKhoanId = session?.taiKhoanId ?? null;

  const [contacts, setContacts] = useState<ChatContact[]>([]);
  const [activeChatIdState, setActiveChatIdState] = useState<number | null>(null);
  const [messagesByChat, setMessagesByChat] = useState<Record<number, ChatMessage[]>>({});
  const [searchKeyword, setSearchKeyword] = useState("");
  const [draftMessage, setDraftMessage] = useState("");
  const [isLoadingContacts, setIsLoadingContacts] = useState<boolean>(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState<boolean>(false);
  const [isSending, setIsSending] = useState<boolean>(false);
  const [isUploadingMedia, setIsUploadingMedia] = useState<boolean>(false);
  const [isSendingLocation, setIsSendingLocation] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isRealtimeConnected, setIsRealtimeConnected] = useState<boolean>(false);

  const messageEndRef = useRef<HTMLDivElement | null>(null);
  const loadedChatIdsRef = useRef<Set<number>>(new Set());
  const markAsReadInProgressRef = useRef<Set<number>>(new Set());
  const stompClientRef = useRef<StompClient | null>(null);
  const subscribedIdsRef = useRef<Set<number>>(new Set());
  const unsubscribeByChatIdRef = useRef<Map<number, () => void>>(new Map());
  const activeChatIdRef = useRef<number | null>(null);
  const messagesByChatRef = useRef<Record<number, ChatMessage[]>>({});

  useEffect(() => {
    activeChatIdRef.current = activeChatIdState;
  }, [activeChatIdState]);

  useEffect(() => {
    messagesByChatRef.current = messagesByChat;
  }, [messagesByChat]);

  const filteredContacts = useMemo(() => {
    const keyword = searchKeyword.trim().toLowerCase();
    if (!keyword) {
      return contacts;
    }

    return contacts.filter((contact) => {
      const nameMatched = contact.name.toLowerCase().includes(keyword);
      const roleMatched = roleLabels[contact.role].toLowerCase().includes(keyword);
      const previewMatched = contact.preview.toLowerCase().includes(keyword);
      return nameMatched || roleMatched || previewMatched;
    });
  }, [contacts, searchKeyword]);

  useEffect(() => {
    if (filteredContacts.length === 0) {
      if (activeChatIdState !== null) {
        setActiveChatIdState(null);
      }
      return;
    }

    const stillVisible =
      activeChatIdState !== null &&
      filteredContacts.some((contact) => contact.id === activeChatIdState);

    if (!stillVisible) {
      setActiveChatIdState(filteredContacts[0]?.id ?? null);
    }
  }, [activeChatIdState, filteredContacts]);

  const activeContact =
    activeChatIdState === null
      ? null
      : contacts.find((contact) => contact.id === activeChatIdState) ?? null;

  const activeMessages = activeContact ? messagesByChat[activeContact.id] ?? [] : [];

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [activeChatIdState, activeMessages.length]);

  const updateContactAfterMessage = useCallback(
    (chatId: number, message: ChatMessage, shouldIncreaseUnread: boolean) => {
      setContacts((prev) => {
        const updated = prev.map((contact) => {
          if (contact.id !== chatId) {
            return contact;
          }

          return {
            ...contact,
            preview: buildMessagePreview(message),
            lastSeen: formatLastSeen(message.createdAt),
            unreadCount: shouldIncreaseUnread ? contact.unreadCount + 1 : contact.unreadCount,
            isOnline: true,
          };
        });

        const selected = updated.find((contact) => contact.id === chatId);
        return selected
          ? [selected, ...updated.filter((contact) => contact.id !== chatId)]
          : updated;
      });
    },
    []
  );

  const appendMessage = useCallback(
    (chatId: number, message: ChatMessage, shouldIncreaseUnread: boolean) => {
      const existingMessages = messagesByChatRef.current[chatId] ?? [];
      const isDuplicated =
        message.rawId !== null
          ? existingMessages.some((item) => item.rawId === message.rawId)
          : existingMessages.some((item) => item.id === message.id);

      if (isDuplicated) {
        return;
      }

      setMessagesByChat((prev) => ({
        ...prev,
        [chatId]: [...(prev[chatId] ?? []), message],
      }));

      updateContactAfterMessage(chatId, message, shouldIncreaseUnread);
    },
    [updateContactAfterMessage]
  );

  const markChatAsRead = useCallback(async (chatId: number, messages: ChatMessage[]) => {
    const lastMessage = messages[messages.length - 1];
    const lastSeenMessageId = lastMessage?.rawId ?? null;

    if (!lastSeenMessageId) {
      return;
    }

    if (markAsReadInProgressRef.current.has(chatId)) {
      return;
    }

    markAsReadInProgressRef.current.add(chatId);
    try {
      await capNhatDaXemTinNhan(chatId, lastSeenMessageId);
      setContacts((prev) =>
        prev.map((contact) =>
          contact.id === chatId
            ? {
                ...contact,
                unreadCount: 0,
              }
            : contact
        )
      );
    } catch {
      // Silent failure for read receipt to avoid breaking chat flow.
    } finally {
      markAsReadInProgressRef.current.delete(chatId);
    }
  }, []);

  const loadMessagesForChat = useCallback(
    async (chatId: number, forceReload = false) => {
      if (!forceReload && loadedChatIdsRef.current.has(chatId)) {
        return;
      }

      setIsLoadingMessages(true);
      try {
        const tinNhanList = await fetchChatTinNhanByPhieuId(chatId);
        const mappedMessages = tinNhanList.map((tinNhan) =>
          mapTinNhanToChatMessage(tinNhan, currentTaiKhoanId)
        );

        loadedChatIdsRef.current.add(chatId);
        setMessagesByChat((prev) => ({
          ...prev,
          [chatId]: mappedMessages,
        }));

        if (mappedMessages.length > 0) {
          const latestMessage = mappedMessages[mappedMessages.length - 1];
          setContacts((prev) =>
            prev.map((contact) =>
              contact.id === chatId
                ? {
                    ...contact,
                    preview: buildMessagePreview(latestMessage),
                    lastSeen: formatLastSeen(latestMessage.createdAt),
                  }
                : contact
            )
          );
        }
      } catch (error) {
        setErrorMessage(resolveErrorMessage(error));
      } finally {
        setIsLoadingMessages(false);
      }
    },
    [currentTaiKhoanId]
  );

  const loadContacts = useCallback(async () => {
    setIsLoadingContacts(true);
    setErrorMessage(null);

    try {
      const phieuList = await fetchChatPhieuList();
      const mappedContacts = phieuList
        .map(mapPhieuToContact)
        .sort((left, right) => right.id - left.id);

      setContacts(mappedContacts);
      setActiveChatIdState((prev) => {
        if (prev !== null && mappedContacts.some((contact) => contact.id === prev)) {
          return prev;
        }
        return mappedContacts[0]?.id ?? null;
      });
    } catch (error) {
      setErrorMessage(resolveErrorMessage(error));
      setContacts([]);
      setActiveChatIdState(null);
    } finally {
      setIsLoadingContacts(false);
    }
  }, []);

  useEffect(() => {
    void loadContacts();
  }, [loadContacts]);

  useEffect(() => {
    if (activeChatIdState === null) {
      return;
    }

    void loadMessagesForChat(activeChatIdState);

    setContacts((prev) =>
      prev.map((contact) =>
        contact.id === activeChatIdState
          ? {
              ...contact,
              unreadCount: 0,
            }
          : contact
      )
    );
  }, [activeChatIdState, loadMessagesForChat]);

  useEffect(() => {
    if (activeChatIdState === null || activeMessages.length === 0) {
      return;
    }

    void markChatAsRead(activeChatIdState, activeMessages);
  }, [activeChatIdState, activeMessages, markChatAsRead]);

  const handleIncomingFrame = useCallback(
    (frame: StompFrame) => {
      const destination = frame.headers.destination;
      if (!destination) {
        return;
      }

      const phieuId = parsePhieuIdFromDestination(destination);
      if (!phieuId) {
        return;
      }

      let payload: unknown = null;
      try {
        payload = JSON.parse(frame.body);
      } catch {
        return;
      }

      const tinNhan = parseRealtimeTinNhan(payload);
      if (!tinNhan) {
        return;
      }

      const mappedMessage = mapTinNhanToChatMessage(tinNhan, currentTaiKhoanId);
      const shouldIncreaseUnread =
        activeChatIdRef.current !== phieuId && mappedMessage.sender === "them";
      appendMessage(phieuId, mappedMessage, shouldIncreaseUnread);

      if (activeChatIdRef.current === phieuId && mappedMessage.rawId !== null) {
        void capNhatDaXemTinNhan(phieuId, mappedMessage.rawId).catch(() => {
          // Silent failure for realtime read receipt.
        });
      }
    },
    [appendMessage, currentTaiKhoanId]
  );

  useEffect(() => {
    const authorization = getAuthorizationHeader();
    if (!authorization) {
      setIsRealtimeConnected(false);
      return;
    }

    const stompClient = new StompClient({
      url: buildWebSocketUrl(),
      connectHeaders: {
        Authorization: authorization,
      },
      onConnectionStateChange: (state) => {
        setIsRealtimeConnected(state === "connected");
      },
      onError: (message) => {
        setErrorMessage(message);
      },
    });

    stompClientRef.current = stompClient;
    stompClient.connect();

    return () => {
      unsubscribeByChatIdRef.current.forEach((unsubscribe) => unsubscribe());
      unsubscribeByChatIdRef.current.clear();
      subscribedIdsRef.current.clear();

      stompClient.disconnect();
      stompClientRef.current = null;
      setIsRealtimeConnected(false);
    };
  }, []);

  const contactIdsKey = useMemo(
    () => contacts.map((contact) => contact.id).sort((left, right) => left - right).join(","),
    [contacts]
  );

  useEffect(() => {
    const stompClient = stompClientRef.current;
    if (!stompClient) {
      return;
    }

    const availableIds = new Set(contacts.map((contact) => contact.id));

    availableIds.forEach((chatId) => {
      if (subscribedIdsRef.current.has(chatId)) {
        return;
      }

      const destination = buildChatDestination(chatId);
      const unsubscribe = stompClient.subscribe(destination, handleIncomingFrame);
      unsubscribeByChatIdRef.current.set(chatId, unsubscribe);
      subscribedIdsRef.current.add(chatId);
    });

    const subscribedIds = Array.from(subscribedIdsRef.current);
    subscribedIds.forEach((chatId) => {
      if (availableIds.has(chatId)) {
        return;
      }

      const unsubscribe = unsubscribeByChatIdRef.current.get(chatId);
      unsubscribe?.();
      unsubscribeByChatIdRef.current.delete(chatId);
      subscribedIdsRef.current.delete(chatId);
    });
  }, [contactIdsKey, contacts, handleIncomingFrame]);

  const setActiveChatId = useCallback((chatId: number) => {
    setActiveChatIdState(chatId);
    setContacts((prev) =>
      prev.map((contact) =>
        contact.id === chatId
          ? {
              ...contact,
              unreadCount: 0,
            }
          : contact
      )
    );
  }, []);

  const handleSendMessage = useCallback(async () => {
    const activeChatId = activeChatIdRef.current;
    const text = draftMessage.trim();

    if (activeChatId === null || text.length === 0) {
      return;
    }

    setIsSending(true);
    setErrorMessage(null);

    try {
      const tinNhan = await guiTinNhanPhieu(activeChatId, {
        noiDung: text,
      });
      const mappedMessage = mapTinNhanToChatMessage(tinNhan, currentTaiKhoanId);
      appendMessage(activeChatId, mappedMessage, false);
      setDraftMessage("");
    } catch (error) {
      setErrorMessage(resolveErrorMessage(error));
    } finally {
      setIsSending(false);
    }
  }, [appendMessage, currentTaiKhoanId, draftMessage]);

  const handleUploadMedia = useCallback(
    async (file: File) => {
      const activeChatId = activeChatIdRef.current;
      if (activeChatId === null) {
        return;
      }

      setIsUploadingMedia(true);
      setErrorMessage(null);

      try {
        const tepTin = await uploadChatMedia(file);
        const text = draftMessage.trim();
        const tinNhan = await guiTinNhanPhieu(activeChatId, {
          tepTinId: tepTin.id,
          noiDung: text.length > 0 ? text : undefined,
        });

        const mappedMessage = mapTinNhanToChatMessage(tinNhan, currentTaiKhoanId);
        appendMessage(activeChatId, mappedMessage, false);
        setDraftMessage("");
      } catch (error) {
        setErrorMessage(resolveErrorMessage(error));
      } finally {
        setIsUploadingMedia(false);
      }
    },
    [appendMessage, currentTaiKhoanId, draftMessage]
  );

  const handleSendCurrentLocation = useCallback(async () => {
    const activeChatId = activeChatIdRef.current;
    if (activeChatId === null) {
      return;
    }

    setIsSendingLocation(true);
    setErrorMessage(null);

    try {
      const position = await getCurrentPosition();
      const latitude = position.coords.latitude.toFixed(6);
      const longitude = position.coords.longitude.toFixed(6);

      const tinNhan = await guiTinNhanPhieu(activeChatId, {
        viTri: {
          diaChi: `Vị trí hiện tại (${latitude}, ${longitude})`,
          lat: latitude,
          longitude,
        },
      });

      const mappedMessage = mapTinNhanToChatMessage(tinNhan, currentTaiKhoanId);
      appendMessage(activeChatId, mappedMessage, false);
    } catch (error) {
      setErrorMessage(resolveErrorMessage(error));
    } finally {
      setIsSendingLocation(false);
    }
  }, [appendMessage, currentTaiKhoanId]);

  const handleDraftKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      void handleSendMessage();
    }
  };

  const isLoading = isLoadingContacts || (activeChatIdState !== null && isLoadingMessages);

  return {
    roleLabels,
    filteredContacts,
    activeChatId: activeChatIdState,
    activeContact,
    activeMessages,
    searchKeyword,
    draftMessage,
    messageEndRef,
    isLoading,
    isSending,
    isUploadingMedia,
    isSendingLocation,
    isRealtimeConnected,
    errorMessage,
    setActiveChatId,
    setSearchKeyword,
    setDraftMessage,
    handleSendMessage,
    handleDraftKeyDown,
    handleUploadMedia,
    handleSendCurrentLocation,
    reloadChatData: loadContacts,
  };
}
