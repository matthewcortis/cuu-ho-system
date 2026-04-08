export const routeSegments = {
  admin: 'admin',
  products: 'products',
  users: 'users',
}

export const routePaths = {
  root: '/',
  dashboard: `/${routeSegments.admin}`,
  products: `/${routeSegments.admin}/${routeSegments.products}`,
  users: `/${routeSegments.admin}/${routeSegments.users}`,
}
