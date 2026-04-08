import { Link } from 'react-router-dom'
import { routePaths } from '@/app/router/route-paths'
import { PageIntro } from '@/shared/ui/PageIntro'
import { Panel } from '@/shared/ui/Panel'

export function NotFoundPage() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-3xl items-center px-4 py-10 sm:px-6">
      <Panel className="w-full p-8 sm:p-10">
        <PageIntro
          eyebrow="404"
          title="Page not found"
          description="The requested route does not exist in the current admin workspace."
        />

        <div className="mt-8">
          <Link
            to={routePaths.dashboard}
            className="inline-flex items-center rounded-full bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            Back to dashboard
          </Link>
        </div>
      </Panel>
    </main>
  )
}
