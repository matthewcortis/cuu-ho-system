import { useEffect, useMemo, useRef, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import PageMeta from "../../components/common/PageMeta";
import { MoreDotIcon, PaperPlaneIcon, VideoIcon } from "../../icons";

type ChatRole = "admin" | "volunteer" | "customer";

interface ChatContact {
  id: number;
  name: string;
  role: ChatRole;
  avatar: string;
  isOnline: boolean;
  lastSeen: string;
  preview: string;
}

interface ChatMessage {
  id: string;
  sender: "me" | "them";
  text: string;
  meta: string;
}

const roleLabels: Record<ChatRole, string> = {
  admin: "Admin",
  volunteer: "Volunteer",
  customer: "Customer",
};

const initialContacts: ChatContact[] = [
  {
    id: 1,
    name: "Kaiya George",
    role: "volunteer",
    avatar: "/images/user/user-01.jpg",
    isOnline: true,
    lastSeen: "15 mins",
    preview: "Can we support this area today?",
  },
  {
    id: 2,
    name: "Lindsey Curtis",
    role: "customer",
    avatar: "/images/user/user-02.jpg",
    isOnline: true,
    lastSeen: "30 mins",
    preview: "I need emergency relief support.",
  },
  {
    id: 3,
    name: "Zain Geidt",
    role: "admin",
    avatar: "/images/user/user-03.jpg",
    isOnline: true,
    lastSeen: "45 mins",
    preview: "Please update volunteer status.",
  },
  {
    id: 4,
    name: "Carla George",
    role: "volunteer",
    avatar: "/images/user/user-04.jpg",
    isOnline: false,
    lastSeen: "2 days",
    preview: "The supply truck is on the way.",
  },
  {
    id: 5,
    name: "Abram Schleifer",
    role: "customer",
    avatar: "/images/user/user-05.jpg",
    isOnline: true,
    lastSeen: "1 hour",
    preview: "Water and food are needed now.",
  },
];

const initialMessagesByChat: Record<number, ChatMessage[]> = {
  1: [
    {
      id: "1-1",
      sender: "them",
      text: "I want to make an appointment tomorrow from 2:00 to 5:00pm?",
      meta: "Kaiya George, 15 mins",
    },
    {
      id: "1-2",
      sender: "me",
      text: "If I do not like something, I will stay away from it.",
      meta: "2 hours ago",
    },
    {
      id: "1-3",
      sender: "them",
      text: "I want more detailed information.",
      meta: "Kaiya George, 2 hours ago",
    },
  ],
  2: [
    {
      id: "2-1",
      sender: "them",
      text: "My family is stuck near flooded road section 3.",
      meta: "Lindsey Curtis, 30 mins",
    },
    {
      id: "2-2",
      sender: "me",
      text: "We received your request and assigned a nearby team.",
      meta: "18 mins ago",
    },
  ],
  3: [
    {
      id: "3-1",
      sender: "them",
      text: "The approval list is ready for the next shift.",
      meta: "Zain Geidt, 45 mins",
    },
  ],
  4: [
    {
      id: "4-1",
      sender: "them",
      text: "I can join after 7pm if more people are needed.",
      meta: "Carla George, 2 days",
    },
  ],
  5: [
    {
      id: "5-1",
      sender: "them",
      text: "Please help. We need clean water and medicine.",
      meta: "Abram Schleifer, 1 hour",
    },
    {
      id: "5-2",
      sender: "me",
      text: "Relief team ETA is around 25 minutes.",
      meta: "40 mins ago",
    },
  ],
};

function SearchIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.8" />
      <path
        d="M20 20L17 17"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}

function PhoneIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path
        d="M6.6 3H4.9C4.4 3 3.9 3.2 3.6 3.6C3.2 4 3 4.5 3 5C3 9.8 5 14.4 8.5 17.9C12 21.4 16.6 23.4 21.4 23.4C21.9 23.4 22.4 23.2 22.8 22.8C23.2 22.5 23.4 22 23.4 21.5V19.8C23.4 19.1 22.9 18.5 22.2 18.3L18.4 17.3C17.8 17.1 17.2 17.3 16.8 17.8L15.9 18.9C13.3 17.7 10.9 15.3 9.7 12.7L10.8 11.8C11.3 11.4 11.5 10.8 11.3 10.2L10.3 6.4C10.1 5.7 9.5 5.2 8.8 5.2H7.1"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function EmojiIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" />
      <path
        d="M8.5 14.5C9.2 15.4 10.5 16 12 16C13.5 16 14.8 15.4 15.5 14.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <circle cx="9" cy="10" r="1" fill="currentColor" />
      <circle cx="15" cy="10" r="1" fill="currentColor" />
    </svg>
  );
}

function AttachmentIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path
        d="M8 12.5L13.5 7C14.9 5.6 17.1 5.6 18.5 7C19.9 8.4 19.9 10.6 18.5 12L11 19.5C9.1 21.4 6.1 21.4 4.2 19.5C2.3 17.6 2.3 14.6 4.2 12.7L11.2 5.7"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function MicIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <rect
        x="9"
        y="3.5"
        width="6"
        height="11"
        rx="3"
        stroke="currentColor"
        strokeWidth="1.8"
      />
      <path
        d="M6 11.5C6 14.8 8.7 17.5 12 17.5C15.3 17.5 18 14.8 18 11.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <path d="M12 17.5V21" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      <path
        d="M9.5 21H14.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function ChatPage() {
  const [contacts, setContacts] = useState<ChatContact[]>(initialContacts);
  const [activeChatId, setActiveChatId] = useState<number>(initialContacts[0].id);
  const [messagesByChat, setMessagesByChat] = useState<Record<number, ChatMessage[]>>(
    initialMessagesByChat
  );
  const [searchKeyword, setSearchKeyword] = useState("");
  const [draftMessage, setDraftMessage] = useState("");
  const messageEndRef = useRef<HTMLDivElement | null>(null);

  const filteredContacts = useMemo(() => {
    const keyword = searchKeyword.trim().toLowerCase();
    if (!keyword) return contacts;

    return contacts.filter((contact) => {
      const nameMatched = contact.name.toLowerCase().includes(keyword);
      const roleMatched = roleLabels[contact.role].toLowerCase().includes(keyword);
      return nameMatched || roleMatched;
    });
  }, [contacts, searchKeyword]);

  useEffect(() => {
    if (filteredContacts.length === 0) return;
    const stillVisible = filteredContacts.some((contact) => contact.id === activeChatId);
    if (!stillVisible) {
      setActiveChatId(filteredContacts[0].id);
    }
  }, [activeChatId, filteredContacts]);

  const activeContact =
    contacts.find((contact) => contact.id === activeChatId) ?? filteredContacts[0] ?? null;
  const activeMessages = activeContact ? messagesByChat[activeContact.id] ?? [] : [];

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [activeChatId, activeMessages.length]);

  const handleSendMessage = () => {
    const text = draftMessage.trim();
    if (!text || !activeContact) return;

    const newMessage: ChatMessage = {
      id: `new-${Date.now()}`,
      sender: "me",
      text,
      meta: "just now",
    };

    setMessagesByChat((prev) => ({
      ...prev,
      [activeContact.id]: [...(prev[activeContact.id] ?? []), newMessage],
    }));

    setContacts((prev) => {
      const updated = prev.map((contact) =>
        contact.id === activeContact.id
          ? {
              ...contact,
              preview: text,
              lastSeen: "just now",
            }
          : contact
      );
      const selected = updated.find((contact) => contact.id === activeContact.id);
      return selected
        ? [selected, ...updated.filter((contact) => contact.id !== activeContact.id)]
        : updated;
    });

    setDraftMessage("");
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <>
      <PageMeta
        title="Chat"
        description="Chat interface for admin, volunteers and customers."
      />
      <PageBreadcrumb pageTitle="Tổng đài hỗ trợ khách hàng" />

      <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex min-h-[620px] flex-col xl:h-[76vh] xl:flex-row">
          <aside className="w-full shrink-0 border-b border-gray-200 px-5 py-5 dark:border-gray-800 xl:w-[340px] xl:border-b-0 xl:border-r">
            <div className="mb-5 flex items-center justify-between">
              <h2 className="text-[30px] font-semibold leading-none text-gray-800 dark:text-white/90">
                Messages
              </h2>
              <button
                type="button"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                aria-label="More"
              >
                <MoreDotIcon className="size-5" />
              </button>
            </div>

            <div className="relative mb-5">
              <SearchIcon className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Search..."
                value={searchKeyword}
                onChange={(event) => setSearchKeyword(event.target.value)}
                className="h-12 w-full rounded-xl border border-gray-200 bg-gray-50 pl-12 pr-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>

            <div className="max-h-[300px] overflow-y-auto pr-2 custom-scrollbar xl:max-h-[calc(76vh-170px)]">
              {filteredContacts.map((contact) => {
                const isActive = contact.id === activeChatId;

                return (
                  <button
                    key={contact.id}
                    type="button"
                    onClick={() => setActiveChatId(contact.id)}
                    className={`mb-2 flex w-full items-start gap-3 rounded-xl px-3 py-3 text-left transition ${
                      isActive
                        ? "bg-brand-50 dark:bg-brand-500/10"
                        : "hover:bg-gray-100 dark:hover:bg-white/10"
                    }`}
                  >
                    <div className="relative shrink-0">
                      <img
                        src={contact.avatar}
                        alt={contact.name}
                        className="h-12 w-12 rounded-full object-cover"
                      />
                      <span
                        className={`absolute -bottom-0.5 -right-0.5 block h-3.5 w-3.5 rounded-full border-2 border-white dark:border-gray-900 ${
                          contact.isOnline ? "bg-success-500" : "bg-gray-400"
                        }`}
                      />
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-start justify-between gap-2">
                        <p className="truncate text-base font-semibold text-gray-800 dark:text-white/90">
                          {contact.name}
                        </p>
                        <span className="shrink-0 text-xs text-gray-500 dark:text-gray-400">
                          {contact.lastSeen}
                        </span>
                      </div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">
                        {roleLabels[contact.role]}
                      </p>
                      <p className="mt-0.5 truncate text-xs text-gray-500 dark:text-gray-400">
                        {contact.preview}
                      </p>
                    </div>
                  </button>
                );
              })}

              {filteredContacts.length === 0 && (
                <div className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
                  No conversation found.
                </div>
              )}
            </div>
          </aside>

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
                            <div>
                              <p className="rounded-2xl bg-gray-100 px-4 py-3 text-sm text-gray-800 dark:bg-white/10 dark:text-gray-100">
                                {message.text}
                              </p>
                              <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                                {message.meta}
                              </p>
                            </div>
                          </div>
                        </div>
                      ) : (
                        <div className="max-w-[85%] sm:max-w-[70%]">
                          <p className="rounded-2xl bg-brand-500 px-4 py-3 text-sm text-white">
                            {message.text}
                          </p>
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
                    >
                      <EmojiIcon className="size-5" />
                    </button>

                    <input
                      type="text"
                      value={draftMessage}
                      onChange={(event) => setDraftMessage(event.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="Type a message"
                      className="h-10 min-w-0 flex-1 border-none bg-transparent px-2 text-sm text-gray-800 outline-none dark:text-white/90"
                    />

                    <button
                      type="button"
                      className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                      aria-label="Attach"
                    >
                      <AttachmentIcon className="size-5" />
                    </button>
                    <button
                      type="button"
                      className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/10 dark:hover:text-white/90"
                      aria-label="Record"
                    >
                      <MicIcon className="size-5" />
                    </button>
                    <button
                      type="button"
                      onClick={handleSendMessage}
                      className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-500 text-white transition hover:bg-brand-600"
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
        </div>
      </div>
    </>
  );
}
