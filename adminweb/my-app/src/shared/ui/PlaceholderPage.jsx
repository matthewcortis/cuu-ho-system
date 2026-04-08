import { Panel } from '@/shared/ui/Panel'

export function PlaceholderPage({
  badge,
  title,
  description,
  checklist,
  children,
}) {
  return (
    <Panel className="p-6 sm:p-8">
      {badge ? (
        <p className="text-xs font-semibold uppercase tracking-[0.32em] text-amber-600">
          {badge}
        </p>
      ) : null}

      <h2 className="mt-3 font-display text-2xl font-semibold tracking-[-0.04em] text-slate-950 sm:text-3xl">
        {title}
      </h2>
      <p className="mt-4 max-w-2xl text-sm leading-7 text-slate-600 sm:text-base">
        {description}
      </p>

      <ul className="mt-8 space-y-4">
        {checklist.map((item) => (
          <li
            key={item}
            className="flex items-start gap-3 rounded-[20px] border border-slate-200 bg-slate-50/70 px-4 py-4 text-sm leading-7 text-slate-700"
          >
            <span className="mt-2 h-2.5 w-2.5 flex-none rounded-full bg-amber-500" />
            <span>{item}</span>
          </li>
        ))}
      </ul>

      {children}
    </Panel>
  )
}
