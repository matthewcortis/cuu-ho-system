import { routePaths } from '@/app/router/route-paths'

export const primaryMenuItems = [
  { label: 'Dashboard', to: routePaths.dashboard, end: true, icon: 'dashboard' },
  { label: 'Products', to: routePaths.products, icon: 'products' },
  { label: 'Favorites', icon: 'favorites' },
  { label: 'Inbox', icon: 'inbox' },
  { label: 'Order Lists', icon: 'orders' },
  { label: 'Product Stock', icon: 'stock' },
]

export const pageMenuItems = [
  { label: 'Pricing', icon: 'pricing' },
  { label: 'Calender', icon: 'calender' },
  { label: 'To-Do', icon: 'todo' },
  { label: 'Contact', icon: 'contact' },
  { label: 'Invoice', icon: 'invoice' },
  { label: 'UI Elements', icon: 'ui' },
  { label: 'Team', icon: 'team' },
  { label: 'Table', icon: 'table' },
]

export const footerMenuItems = [
  { label: 'Settings', icon: 'settings' },
  { label: 'Logout', icon: 'logout' },
]
