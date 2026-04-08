import { SidebarMenuItem } from '@/widgets/admin-shell/ui/sidebar/SidebarMenuItem'
import { cn } from '@/shared/lib/cn'

export function SidebarMenuSection({ title, items, collapsed = false }) {
  return (
    <section>
      {title && !collapsed ? (
        <p className="mx-auto w-[186px] pb-4 pl-4 text-left text-xs font-bold uppercase tracking-[0.08em] text-[#8f8f9f]">
          {title}
        </p>
      ) : null}
      <div className={cn('grid gap-1 px-2', collapsed ? 'justify-items-center' : '')}>
        {items.map((item) => (
          <SidebarMenuItem collapsed={collapsed} item={item} key={item.label} />
        ))}
      </div>
    </section>
  )
}
