import { createBrowserRouter, Navigate } from 'react-router-dom'
import { routePaths, routeSegments } from '@/app/router/route-paths'
import { DashboardPage } from '@/pages/dashboard'
import { NotFoundPage } from '@/pages/not-found'
import { ProductsPage } from '@/pages/products'
import { UsersPage } from '@/pages/users'
import { AdminShell } from '@/widgets/admin-shell'

export const router = createBrowserRouter([
  {
    path: routePaths.root,
    element: <Navigate replace to={routePaths.dashboard} />,
  },
  {
    path: routePaths.dashboard,
    element: <AdminShell />,
    children: [
      {
        index: true,
        element: <DashboardPage />,
      },
      {
        path: routeSegments.products,
        element: <ProductsPage />,
      },
      {
        path: routeSegments.users,
        element: <UsersPage />,
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
])
