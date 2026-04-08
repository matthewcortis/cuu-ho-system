function BellIcon() {
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
      <path d="M15 17H5l1.4-1.7a2 2 0 0 0 .6-1.4V10a5 5 0 1 1 10 0v3.9a2 2 0 0 0 .6 1.4L19 17h-4Z" />
      <path d="M10 19a2 2 0 0 0 4 0" />
    </svg>
  )
}

function ChevronDownIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4 text-[#7e8398]"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}

export function TopbarActions() {
  return (
    <div className="flex items-center gap-4">
      <button
        aria-label="Notifications"
        className="relative inline-flex h-10 w-10 items-center justify-center rounded-full text-[#5f6680] transition hover:bg-[#f3f4f8]"
        type="button"
      >
        <BellIcon />
        <span className="absolute -right-0.5 -top-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-[#ff5460] px-1 text-[10px] font-bold text-white">
          6
        </span>
        <span className="absolute bottom-1.5 right-1.5 h-1.5 w-1.5 rounded-full bg-[#ff8f8f]" />
      </button>

      <button
        className="hidden items-center gap-3 rounded-full px-2 py-2 text-sm font-semibold text-[#5f6680] transition hover:bg-[#f3f4f8] sm:inline-flex"
        type="button"
      >
        <span className="inline-flex h-6 w-9 overflow-hidden rounded-[4px] border border-[#d6d9e5]">
          <span className="w-[36%] bg-[#153e90]" />
          <span className="w-[28%] bg-white" />
          <span className="w-[36%] bg-[#d62839]" />
        </span>
        <span>English</span>
        <ChevronDownIcon />
      </button>

      <button
        className="inline-flex items-center gap-3 rounded-full px-2 py-2 transition hover:bg-[#f3f4f8]"
        type="button"
      >
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-full bg-[linear-gradient(135deg,#f7accf,#845ef7)] text-sm font-bold text-white">
          MR
        </span>
        <span className="hidden text-left sm:block">
          <span className="block text-sm font-bold leading-4 text-[#202224]">Moni Roy</span>
          <span className="mt-1 block text-xs font-semibold text-[#7e8398]">Admin</span>
        </span>
      </button>

      <button
        aria-label="Open user menu"
        className="inline-flex h-8 w-8 items-center justify-center rounded-full border border-[#d9dce6] transition hover:bg-[#f3f4f8]"
        type="button"
      >
        <ChevronDownIcon />
      </button>
    </div>
  )
}
