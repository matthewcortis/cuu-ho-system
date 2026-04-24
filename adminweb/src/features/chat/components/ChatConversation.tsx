import { MoreDotIcon, PaperPlaneIcon, VideoIcon } from "@/icons";
import { useRef, type KeyboardEvent, type RefObject } from "react";
import { AttachmentIcon, EmojiIcon, MicIcon, PhoneIcon } from "./ChatIcons";
import type { ChatContact, ChatMessage, RoleLabels } from "../types/chatTypes";

interface ChatConversationProps {
  activeContact: ChatContact | null;
  activeMessages: ChatMessage[];
  draftMessage: string;
  roleLabels: RoleLabels;
  messageEndRef: RefObject<HTMLDivElement | null>;
  isSending: boolean;
  isUploadingMedia: boolean;
  isSendingLocation: boolean;
  onDraftMessageChange: (value: string) => void;
  onDraftKeyDown: (event: KeyboardEvent<HTMLInputElement>) => void;
  onSendMessage: () => void;
  onUploadMedia: (file: File) => void;
  onSendCurrentLocation: () => void;
}

function renderMedia(message: ChatMessage) {
  if (!message.mediaUrl) {
    return null;
  }

  if (message.mediaType.startsWith("image/")) {
    return (
      <img
        src={message.mediaUrl}
        alt="media"
        className="mt-2 max-h-[280px] w-full rounded-xl object-cover"
      />
    );
  }

  if (message.mediaType.startsWith("video/")) {
    return (
      <video controls className="mt-2 max-h-[320px] w-full rounded-xl">
        <source src={message.mediaUrl} type={message.mediaType} />
        Trình duyệt không hỗ trợ video.
      </video>
    );
  }

  if (message.mediaType.startsWith("audio/")) {
    return (
      <audio controls className="mt-2 w-full">
        <source src={message.mediaUrl} type={message.mediaType} />
        Trình duyệt không hỗ trợ audio.
      </audio>
    );
  }

  return (
    <a
      href={message.mediaUrl}
      target="_blank"
      rel="noreferrer"
      className="mt-2 inline-flex text-xs font-medium text-brand-500 hover:underline"
    >
      Mở tệp đính kèm
    </a>
  );
}

function renderLocation(message: ChatMessage) {
  if (!message.location) {
    return null;
  }

  const { diaChi, lat, longitude } = message.location;
  const hasLatLong = lat.length > 0 && longitude.length > 0;
  const googleMapsHref = hasLatLong
    ? `https://www.google.com/maps?q=${encodeURIComponent(`${lat},${longitude}`)}`
    : null;

  return (
    <div className="mt-2 rounded-xl border border-gray-200 bg-white/80 px-3 py-2 text-xs text-gray-700 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200">
      <p className="font-medium">Vị trí</p>
      <p className="mt-1">{diaChi || `${lat}, ${longitude}`}</p>
      {googleMapsHref ? (
        <a
          href={googleMapsHref}
          target="_blank"
          rel="noreferrer"
          className="mt-1 inline-flex text-brand-500 hover:underline"
        >
          Xem trên bản đồ
        </a>
      ) : null}
    </div>
  );
}

export default function ChatConversation({
  activeContact,
  activeMessages,
  draftMessage,
  roleLabels,
  messageEndRef,
  isSending,
  isUploadingMedia,
  isSendingLocation,
  onDraftMessageChange,
  onDraftKeyDown,
  onSendMessage,
  onUploadMedia,
  onSendCurrentLocation,
}: ChatConversationProps) {
  const mediaInputRef = useRef<HTMLInputElement | null>(null);

  const isBusy = isSending || isUploadingMedia || isSendingLocation;

  return (
    <section className="flex min-h-[420px] flex-1 flex-col">
      {activeContact ? (
        <>
          <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-800">
            <div className="flex items-center gap-3">
              <div className="relative">
                <img
                  src={activeContact.avatar}
                  alt={activeContact.name}
                  className="h-12 w-12 rounded-full object-cover"
                />
                <span
                  className={`absolute -bottom-0.5 -right-0.5 block h-3.5 w-3.5 rounded-full border-2 border-white dark:border-gray-900 ${
                    activeContact.isOnline ? "bg-success-500" : "bg-gray-400"
                  }`}
                />
              </div>
              <div>
                <p className="text-lg font-semibold text-gray-800 dark:text-white/90">
                  {activeContact.name}
                </p>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {roleLabels[activeContact.role]}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1.5">
              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="Call"
              >
                <PhoneIcon className="size-5" />
              </button>
              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="Video call"
              >
                <VideoIcon className="size-5" />
              </button>
              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="More"
              >
                <MoreDotIcon className="size-5" />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto bg-gray-50/70 px-5 py-6 custom-scrollbar dark:bg-gray-900/30">
            {activeMessages.map((message) => (
              <div
                key={message.id}
                className={`mb-6 flex ${
                  message.sender === "me" ? "justify-end" : "justify-start"
                }`}
              >
                {message.sender === "them" ? (
                  <div className="max-w-[85%] sm:max-w-[70%]">
                    <div className="flex items-start gap-3">
                      <img
                        src={activeContact.avatar}
                        alt={activeContact.name}
                        className="mt-0.5 h-10 w-10 rounded-full object-cover"
                      />
                      <div className="min-w-0">
                        <div className="rounded-2xl bg-gray-100 px-4 py-3 text-sm text-gray-800 dark:bg-white/10 dark:text-gray-100">
                          {message.text ? <p>{message.text}</p> : null}
                          {renderMedia(message)}
                          {renderLocation(message)}
                        </div>
                        <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                          {message.meta}
                        </p>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="max-w-[85%] sm:max-w-[70%]">
                    <div className="rounded-2xl bg-brand-500 px-4 py-3 text-sm text-white">
                      {message.text ? <p>{message.text}</p> : null}
                      {renderMedia(message)}
                      {renderLocation(message)}
                    </div>
                    <p className="mt-2 text-right text-xs text-gray-500 dark:text-gray-400">
                      {message.meta}
                    </p>
                  </div>
                )}
              </div>
            ))}
            <div ref={messageEndRef} />
          </div>

          <div className="border-t border-gray-200 px-3 py-3 dark:border-gray-800 sm:px-5">
            <div className="flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-2 py-2 dark:border-gray-700 dark:bg-gray-900">
              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="Emoji"
                disabled={isBusy}
              >
                <EmojiIcon className="size-5" />
              </button>

              <input
                type="text"
                value={draftMessage}
                onChange={(event) => onDraftMessageChange(event.target.value)}
                onKeyDown={onDraftKeyDown}
                placeholder="Type a message"
                disabled={isBusy}
                className="h-10 min-w-0 flex-1 border-none bg-transparent px-2 text-sm text-gray-800 outline-none disabled:cursor-not-allowed dark:text-white/90"
              />

              <input
                ref={mediaInputRef}
                type="file"
                className="hidden"
                accept="image/*,video/*,audio/*"
                onChange={(event) => {
                  const file = event.target.files?.[0] ?? null;
                  if (!file) {
                    return;
                  }
                  onUploadMedia(file);
                  event.currentTarget.value = "";
                }}
              />

              <button
                type="button"
                onClick={() => mediaInputRef.current?.click()}
                disabled={isBusy}
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 disabled:cursor-not-allowed disabled:opacity-60 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="Attach"
              >
                <AttachmentIcon className="size-5" />
              </button>

              <button
                type="button"
                onClick={onSendCurrentLocation}
                disabled={isBusy}
                className="inline-flex h-9 items-center justify-center rounded-lg px-2 text-xs font-semibold text-gray-600 hover:bg-gray-100 hover:text-gray-800 disabled:cursor-not-allowed disabled:opacity-60 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                aria-label="Send location"
              >
                GPS
              </button>

              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 disabled:cursor-not-allowed disabled:opacity-60 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="Record"
                disabled
              >
                <MicIcon className="size-5" />
              </button>
              <button
                type="button"
                onClick={onSendMessage}
                disabled={isBusy || draftMessage.trim().length === 0}
                className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-500 text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                aria-label="Send message"
              >
                <PaperPlaneIcon className="size-5" />
              </button>
            </div>
          </div>
        </>
      ) : (
        <div className="flex h-full items-center justify-center px-6 text-sm text-gray-500 dark:text-gray-400">
          No active conversation.
        </div>
      )}
    </section>
  );
}
