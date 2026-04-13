# Source Structure

## Main folders

- `app/providers`: global providers (`ThemeContext`, `SidebarContext`)
- `routes`: centralized route config
- `features`: domain modules (`auth`, `nguoi-dung`, `tinh-nguyen-vien`, `vat-pham`, `phieu-cuu-tro`)
- `components`: shared UI/layout components
- `pages`: generic pages not tied to a specific feature
- `api`: cross-feature APIs
- `data`: shared mock data
- `hooks`, `utils`: shared hooks and utilities

## Feature convention

Each feature can contain:

- `api`: API clients for the feature
- `components`: UI used only by that feature
- `pages`: route-level pages
- `data` or `utils`: feature-specific data/helpers

## Import convention

- Use alias `@/*` (configured in `tsconfig.app.json` and `vite.config.ts`) to avoid deep relative imports.
