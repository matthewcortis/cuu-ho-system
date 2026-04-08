import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { AdminHeader } from '@/widgets/admin-shell/ui/AdminHeader'
import { AdminSidebar } from '@/widgets/admin-shell/ui/AdminSidebar'

export function AdminShell() {
  const [isSidebarOpen, setIsSidebarOpen] = useState(true)

  return (
    <div className="min-h-screen bg-[#f5f6fa]">
      <div className="mx-auto min-h-screen max-w-[1600px] lg:flex">
        <AdminSidebar isOpen={isSidebarOpen} />

        <div className="flex min-h-screen min-w-0 flex-1 flex-col">
          <AdminHeader
            isSidebarOpen={isSidebarOpen}
            onToggleSidebar={() => setIsSidebarOpen((prev) => !prev)}
          />
          <main className="flex-1 bg-[#f3f5fa]">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}
