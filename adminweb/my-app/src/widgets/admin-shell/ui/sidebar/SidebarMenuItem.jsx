import { NavLink } from 'react-router-dom'
import { cn } from '@/shared/lib/cn'
import { SidebarMenuIcon } from '@/widgets/admin-shell/ui/sidebar/SidebarMenuIcon'

export function SidebarMenuItem({ item, compact = false, collapsed = false }) {
  const chipClassName =
    'inline-flex whitespace-nowrap rounded-full border border-[#dbe0ef] px-4 py-2 text-sm font-semibold text-[#202224] transition'

  const desktopClassName = cn(
    'flex items-center rounded-[10px] py-3 text-[15px] font-semibold leading-[1.25] text-[#202224] transition',
    collapsed
      ? 'mx-auto w-[52px] justify-center px-2'
      : 'mx-auto w-[186px] justify-start gap-3 px-4 text-left',
  )

  if (compact && item.to) {
    return (
      <NavLink
        className={({ isActive }) =>
          cn(
            chipClassName,
            isActive ? 'border-[#4d7cfe] bg-[#4d7cfe] text-white' : 'hover:bg-[#eef2ff]',
          )
        }
        end={item.end}
        to={item.to}
      >
        {item.label}
      </NavLink>
    )
  }

  if (compact) {
    return (
      <button
        aria-disabled="true"
        className={cn(chipClassName, 'hover:bg-[#f1f3f8]')}
        type="button"
      >
        {item.label}
      </button>
    )
  }

  if (item.to) {
    return (
      <NavLink
        aria-label={item.label}
        className={({ isActive }) =>
          cn(
            desktopClassName,
            isActive ? 'bg-[#4d7cfe] text-white' : 'hover:bg-[#eef2ff]',
          )
        }
        end={item.end}
        title={collapsed ? item.label : undefined}
        to={item.to}
      >
        <SidebarMenuIcon className="shrink-0" name={item.icon} />
        {collapsed ? null : <span className="block flex-1">{item.label}</span>}
      </NavLink>
    )
  }

  return (
    <button
      aria-disabled="true"
      aria-label={item.label}
      className={cn(desktopClassName, 'cursor-default hover:bg-[#f1f3f8]')}
      title={collapsed ? item.label : undefined}
      type="button"
    >
      <SidebarMenuIcon className="shrink-0" name={item.icon} />
      {collapsed ? null : <span className="block flex-1">{item.label}</span>}
    </button>
  )
}
