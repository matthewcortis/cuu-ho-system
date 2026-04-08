# Scalable React Admin Starter

This project has been reorganized into a React structure that is easier to extend as the admin area grows.

## Tech stack

- React 19
- Vite
- React Router
- Tailwind CSS 4

## Source structure

```text
src
|-- app
|   |-- providers
|   `-- router
|-- pages
|   |-- dashboard
|   |-- products
|   |-- users
|   `-- not-found
|-- shared
|   |-- config
|   |-- icons
|   |-- lib
|   `-- ui
`-- widgets
    `-- admin-shell
```

## Folder rules

- `app`: application bootstrap, global providers, router setup.
- `pages`: route-level screens only.
- `widgets`: large composed UI blocks such as shells, headers, sidebars.
- `shared`: reusable UI primitives and helper functions.

## How to add a new module

1. Create a route page under `src/pages/<module>`.
2. Register the path in `src/app/router/route-paths.js`.
3. Add the page to `src/app/router/router.jsx`.
4. Add sidebar metadata in `src/widgets/admin-shell/model/sidebar-menu.js`.
5. Extract reusable logic into feature or entity folders when the module starts growing.

## Scripts

- `npm run dev`: start local development.
- `npm run lint`: run ESLint.
- `npm run build`: create a production build.
- `npm run check`: run lint and build together.

## Import alias

Use `@/` to import from `src`, for example:

```js
import { AdminShell } from '@/widgets/admin-shell'
```
