import { TopbarActions } from '@/widgets/admin-shell/ui/topbar/TopbarActions'
import { TopbarSearch } from '@/widgets/admin-shell/ui/topbar/TopbarSearch'

function ToggleIcon({ isOpen }) {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5 text-[#5f6680]"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      {isOpen ? <path d="m15 18-6-6 6-6" /> : <path d="m9 18 6-6-6-6" />}
    </svg>
  )
}

export function AdminHeader({ isSidebarOpen, onToggleSidebar }) {
  return (
    <header className="sticky top-0 z-10 border-b border-[#e7e9f0] bg-white">
      <div className="flex flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-10">
        <div className="flex items-center gap-3">
          <button
            aria-label="Toggle sidebar"
            className="hidden h-10 w-10 items-center justify-center rounded-full border border-[#d9dce6] transition hover:bg-[#f3f4f8] lg:inline-flex"
            onClick={onToggleSidebar}
            type="button"
          >
            <ToggleIcon isOpen={isSidebarOpen} />
          </button>
          <TopbarSearch />
        </div>
        <TopbarActions />
      </div>
    </header>
  )
}
