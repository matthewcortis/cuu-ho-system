type EnvLike = Record<string, unknown>;

function normalizeUrl(value: string): string {
  return value.replace(/\/+$/, "");
}

function pickServerBaseUrl(): string {
  const env = import.meta.env as EnvLike;
  const candidates = [
    env.SERVER_BASE_URL,
    env.VITE_SERVER_BASE_URL,
    env.VITE_API_BASE_URL,
  ];

  const matched = candidates.find(
    (item): item is string => typeof item === "string" && item.trim().length > 0
  );

  return normalizeUrl(matched ?? "http://localhost:8080");
}

export const SERVER_BASE_URL = pickServerBaseUrl();
