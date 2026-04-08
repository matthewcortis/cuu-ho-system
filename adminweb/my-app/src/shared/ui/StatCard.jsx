import { cn } from '@/shared/lib/cn'
import { Panel } from '@/shared/ui/Panel'

const toneStyles = {
  positive: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  neutral: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
  warning: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
}

export function StatCard({ label, value, change, hint, tone = 'neutral' }) {
  return (
    <Panel className="p-5 sm:p-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-3 font-display text-3xl font-semibold tracking-[-0.05em] text-slate-950">
            {value}
          </p>
        </div>

        {change ? (
          <span
            className={cn(
              'inline-flex rounded-full px-3 py-1 text-xs font-semibold',
              toneStyles[tone],
            )}
          >
            {change}
          </span>
        ) : null}
      </div>

      {hint ? <p className="mt-6 text-sm leading-7 text-slate-600">{hint}</p> : null}
    </Panel>
  )
}
