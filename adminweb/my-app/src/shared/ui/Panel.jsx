import { cn } from '@/shared/lib/cn'

export function Panel({ className, children }) {
  return <section className={cn('panel-surface', className)}>{children}</section>
}
