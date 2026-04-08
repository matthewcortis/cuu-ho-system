import { cn } from '@/shared/lib/cn'
import {
  footerMenuItems,
  pageMenuItems,
  primaryMenuItems,
} from '@/widgets/admin-shell/model/sidebar-menu'
import { SidebarMenuItem } from '@/widgets/admin-shell/ui/sidebar/SidebarMenuItem'
import { SidebarMenuSection } from '@/widgets/admin-shell/ui/sidebar/SidebarMenuSection'

export function AdminSidebar({ isOpen }) {
  const collapsed = !isOpen

  return (
    <>
      <aside className="border-b border-[#e7e9f0] bg-white px-4 py-4 lg:hidden">
        <h1 className="text-3xl font-extrabold leading-none tracking-[-0.02em] text-[#202224]">
          <span className="text-[#4d7cfe]">Dash</span>Stack
        </h1>
        <div className="mt-4 flex flex-wrap gap-2 pb-1">
          {primaryMenuItems.map((item) => (
            <SidebarMenuItem compact item={item} key={item.label} />
          ))}
        </div>
      </aside>

      <aside
        className={cn(
          'hidden h-screen shrink-0 border-r border-[#e7e9f0] bg-[#fbfbfc] transition-[width] duration-300 ease-in-out lg:flex lg:flex-col',
          isOpen ? 'w-[238px]' : 'w-[86px]',
        )}
      >
        <div className={cn('pb-8 pt-9 text-center', isOpen ? 'px-10' : 'px-2')}>
          <h1
            className={cn(
              'font-extrabold leading-none tracking-[-0.02em] text-[#202224] transition-all duration-300',
              isOpen ? 'text-[37px]' : 'text-3xl',
            )}
          >
            {isOpen ? (
              <span>
                <span className="text-[#4d7cfe]">Cứu </span>trợ
              </span>
            ) : (
              <span className="text-[#4d7cfe]">CT</span>
            )}
          </h1>
        </div>

        <div className="no-scrollbar flex flex-1 flex-col overflow-hidden">
          <SidebarMenuSection collapsed={collapsed} items={primaryMenuItems} />

          <div className="my-5 border-t border-[#e7e9f0] pt-5">
            <SidebarMenuSection
              collapsed={collapsed}
              items={pageMenuItems}
              title="Pages"
            />
          </div>

          <div className="mt-auto border-t border-[#e7e9f0] py-6">
            <SidebarMenuSection collapsed={collapsed} items={footerMenuItems} />
          </div>
        </div>
      </aside>
    </>
  )
}
