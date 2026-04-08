function SearchIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4 text-[#9aa0af]"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      <circle cx="11" cy="11" r="7" />
      <path d="m21 21-4.3-4.3" />
    </svg>
  )
}

export function TopbarSearch() {
  return (
    <div className="relative w-full max-w-[460px] lg:max-w-[540px]">
      <span className="pointer-events-none absolute inset-y-0 left-4 inline-flex items-center">
        <SearchIcon />
      </span>
      <input
        className="h-11 w-full rounded-full border border-[#d9dce6] bg-[#f3f4f8] pl-11 pr-4 text-sm text-[#202224] outline-none transition focus:border-[#4d7cfe] focus:bg-white"
        placeholder="Search"
        type="search"
      />
    </div>
  )
}
