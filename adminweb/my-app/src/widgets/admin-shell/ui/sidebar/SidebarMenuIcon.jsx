import { cn } from '@/shared/lib/cn'

function IconBase({ children, className }) {
  return (
    <svg
      aria-hidden="true"
      className={cn('h-4 w-4', className)}
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      {children}
    </svg>
  )
}

const icons = {
  dashboard: (
    <>
      <path d="M4 12h7V4H4v8Z" />
      <path d="M13 20h7v-6h-7v6Z" />
      <path d="M13 10h7V4h-7v6Z" />
      <path d="M4 20h7v-6H4v6Z" />
    </>
  ),
  products: (
    <>
      <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
      <path d="m4 7.5 8 4.5 8-4.5" />
      <path d="M12 12v9" />
    </>
  ),
  favorites: (
    <>
      <path d="m12 17.5-5 3 1.4-5.8L4 10.8l5.9-.5L12 5l2.1 5.3 5.9.5-4.4 3.9 1.4 5.8-5-3Z" />
    </>
  ),
  inbox: (
    <>
      <path d="M4 5h16v12H4z" />
      <path d="M4 14h5l2 3h2l2-3h5" />
    </>
  ),
  orders: (
    <>
      <path d="M8 6h11" />
      <path d="M8 12h11" />
      <path d="M8 18h11" />
      <path d="M4 6h.01" />
      <path d="M4 12h.01" />
      <path d="M4 18h.01" />
    </>
  ),
  stock: (
    <>
      <path d="M4 7h16v10H4z" />
      <path d="M9 7V4h6v3" />
      <path d="M4 12h16" />
    </>
  ),
  pricing: (
    <>
      <path d="M7 8c0-2 2.2-3 5-3s5 1 5 3-2.2 3-5 3-5 1-5 3 2.2 3 5 3 5-1 5-3" />
    </>
  ),
  calender: (
    <>
      <path d="M8 3v3" />
      <path d="M16 3v3" />
      <path d="M4 8h16" />
      <path d="M5 5h14v16H5z" />
    </>
  ),
  todo: (
    <>
      <path d="m6 12 3 3 8-8" />
      <path d="M4 4h16v16H4z" />
    </>
  ),
  contact: (
    <>
      <path d="M7 5h10v14H7z" />
      <path d="M9 9h6" />
      <path d="M9 13h6" />
      <path d="M9 17h4" />
    </>
  ),
  invoice: (
    <>
      <path d="M7 4h10v16l-2-1-2 1-2-1-2 1-2-1V4Z" />
      <path d="M9 9h6" />
      <path d="M9 13h6" />
    </>
  ),
  ui: (
    <>
      <rect height="6" rx="1" width="6" x="4" y="4" />
      <rect height="6" rx="1" width="6" x="14" y="4" />
      <rect height="6" rx="1" width="6" x="4" y="14" />
      <rect height="6" rx="1" width="6" x="14" y="14" />
    </>
  ),
  team: (
    <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" />
      <path d="M9.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </>
  ),
  table: (
    <>
      <path d="M4 4h16v16H4z" />
      <path d="M4 10h16" />
      <path d="M10 4v16" />
    </>
  ),
  settings: (
    <>
      <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
      <path d="m19.4 15-.3.8 1.5 2.7-2.1 2.1-2.7-1.5-.8.3-1 2.9H10l-1-2.9-.8-.3-2.7 1.5-2.1-2.1 1.5-2.7-.3-.8L1 14v-4l2.9-1 .3-.8L2.7 5.5l2.1-2.1 2.7 1.5.8-.3 1-2.9h4l1 2.9.8.3 2.7-1.5 2.1 2.1-1.5 2.7.3.8L23 10v4l-2.9 1Z" />
    </>
  ),
  logout: (
    <>
      <path d="M15 17v2a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h7a2 2 0 0 1 2 2v2" />
      <path d="M10 12h11" />
      <path d="m18 8 4 4-4 4" />
    </>
  ),
}

export function SidebarMenuIcon({ name, className }) {
  return <IconBase className={className}>{icons[name] ?? icons.dashboard}</IconBase>
}
