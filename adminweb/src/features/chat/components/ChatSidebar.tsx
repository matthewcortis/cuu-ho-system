import { MoreDotIcon } from "@/icons";
import { SearchIcon } from "./ChatIcons";
import type { ChatContact, RoleLabels } from "../types/chatTypes";

interface ChatSidebarProps {
  contacts: ChatContact[];
  activeChatId: number | null;
  searchKeyword: string;
  roleLabels: RoleLabels;
  onSearchKeywordChange: (value: string) => void;
  onSelectContact: (chatId: number) => void;
}

export default function ChatSidebar({
  contacts,
  activeChatId,
  searchKeyword,
  roleLabels,
  onSearchKeywordChange,
  onSelectContact,
}: ChatSidebarProps) {
  return (
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
          onChange={(event) => onSearchKeywordChange(event.target.value)}
          className="h-12 w-full rounded-xl border border-gray-200 bg-gray-50 pl-12 pr-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
        />
      </div>

      <div className="max-h-[300px] overflow-y-auto pr-2 custom-scrollbar xl:max-h-[calc(76vh-170px)]">
        {contacts.map((contact) => {
          const isActive = contact.id === activeChatId;

          return (
            <button
              key={contact.id}
              type="button"
              onClick={() => onSelectContact(contact.id)}
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
                  <div className="flex shrink-0 items-center gap-1.5">
                    {contact.unreadCount > 0 ? (
                      <span className="inline-flex min-w-5 items-center justify-center rounded-full bg-brand-500 px-1.5 py-0.5 text-[10px] font-semibold text-white">
                        {contact.unreadCount > 99 ? "99+" : contact.unreadCount}
                      </span>
                    ) : null}
                    <span className="text-xs text-gray-500 dark:text-gray-400">
                      {contact.lastSeen}
                    </span>
                  </div>
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

        {contacts.length === 0 && (
          <div className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500 dark:border-gray-700 dark:text-gray-400">
            No conversation found.
          </div>
        )}
      </div>
    </aside>
  );
}
