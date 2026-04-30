import { API_BASE_URL, TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from "../config/apiConfig";
import type { ApiError, PageResponse } from "../types/apiTypes";

type QueryValue = string | number | boolean | null | undefined;
type QueryParams = Record<string, QueryValue>;

export class ApiClientError extends Error {
  status: number;
  apiError?: ApiError;
  validationErrors: Record<string, string>;

  constructor(message: string, status: number, apiError?: ApiError) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.apiError = apiError;
    this.validationErrors = apiError?.validationErrors ?? {};
  }
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  query?: QueryParams;
  token?: string | null;
  headers?: Record<string, string>;
};

function buildUrl(path: string, query?: QueryParams): string {
  const base = API_BASE_URL.replace(/\/$/, "");
  const url = new URL(`${base}${path}`);

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  return url.toString();
}

async function parseError(response: Response): Promise<ApiClientError> {
  try {
    const payload = (await response.json()) as ApiError;
    return new ApiClientError(payload.message || response.statusText, response.status, payload);
  } catch {
    return new ApiClientError(response.statusText || "Request failed", response.status);
  }
}

export function clearStoredSession(): void {
  sessionStorage.removeItem(TOKEN_STORAGE_KEY);
  sessionStorage.removeItem(USER_STORAGE_KEY);
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = options.token ?? sessionStorage.getItem(TOKEN_STORAGE_KEY);
  const isFormData = options.body instanceof FormData;
  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers ?? {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const body: BodyInit | undefined = isFormData
    ? (options.body as FormData)
    : options.body === undefined
      ? undefined
      : JSON.stringify(options.body);

  const response = await fetch(buildUrl(path, options.query), {
    method: options.method ?? "GET",
    headers,
    body
  });

  if (!response.ok) {
    if (response.status === 401) {
      clearStoredSession();
      window.dispatchEvent(new CustomEvent("ultron:unauthorized"));
    }
    throw await parseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function pageQuery(page = 0, size = 20, extra: QueryParams = {}): QueryParams {
  return { page, size, ...extra };
}

export type { PageResponse };
