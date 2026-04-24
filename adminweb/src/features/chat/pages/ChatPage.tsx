import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import PageMeta from "@/components/common/PageMeta";
import ChatConversation from "../components/ChatConversation";
import ChatSidebar from "../components/ChatSidebar";
import { useChatState } from "../hooks/useChatState";

export default function ChatPage() {
  const {
    roleLabels,
    filteredContacts,
    activeChatId,
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
    reloadChatData,
  } = useChatState();

  return (
    <>
      <PageMeta
        title="Chat"
        description="Chat interface for admin, volunteers and customers."
      />
      <PageBreadcrumb pageTitle="Tổng đài hỗ trợ khách hàng" />

      {errorMessage ? (
        <div className="mb-4 flex items-center justify-between rounded-xl border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-300">
          <span>{errorMessage}</span>
          <button
            type="button"
            onClick={() => void reloadChatData()}
            className="rounded-md border border-error-300 px-2.5 py-1 text-xs font-semibold hover:bg-error-100 dark:border-error-400/40 dark:hover:bg-error-500/20"
          >
            Tải lại
          </button>
        </div>
      ) : null}

      <div className="mb-4 flex items-center justify-between rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-600 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-300">
        <span>
          Realtime: {isRealtimeConnected ? "Đã kết nối" : "Đang kết nối lại..."}
        </span>
        {isLoading ? <span className="text-xs text-gray-500">Đang tải dữ liệu...</span> : null}
      </div>

      <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex min-h-[620px] flex-col xl:h-[76vh] xl:flex-row">
          <ChatSidebar
            contacts={filteredContacts}
            activeChatId={activeChatId}
            searchKeyword={searchKeyword}
            roleLabels={roleLabels}
            onSearchKeywordChange={setSearchKeyword}
            onSelectContact={setActiveChatId}
          />

          <ChatConversation
            activeContact={activeContact}
            activeMessages={activeMessages}
            draftMessage={draftMessage}
            roleLabels={roleLabels}
            messageEndRef={messageEndRef}
            isSending={isSending}
            isUploadingMedia={isUploadingMedia}
            isSendingLocation={isSendingLocation}
            onDraftMessageChange={setDraftMessage}
            onDraftKeyDown={handleDraftKeyDown}
            onSendMessage={handleSendMessage}
            onUploadMedia={handleUploadMedia}
            onSendCurrentLocation={handleSendCurrentLocation}
          />
        </div>
      </div>
    </>
  );
}
