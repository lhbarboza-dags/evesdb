/** Thin fetch wrapper: same-origin session cookie + Spring Security CSRF cookie (XSRF-TOKEN → X-XSRF-TOKEN). */

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public fields?: Record<string, string> | null,
  ) {
    super(message);
  }
}

let unauthorizedHandler: (() => void) | null = null;
export function onUnauthorized(fn: () => void) {
  unauthorizedHandler = fn;
}

function csrfToken(): string | undefined {
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : undefined;
}

async function ensureCsrf() {
  if (!csrfToken()) await fetch('/api/auth/me', { credentials: 'same-origin' });
}

export async function request<T>(method: string, url: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (method !== 'GET') {
    await ensureCsrf();
    const t = csrfToken();
    if (t) headers['X-XSRF-TOKEN'] = t;
  }
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const res = await fetch(url, {
    method,
    headers,
    credentials: 'same-origin',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) {
    let msg = res.statusText || 'Request failed';
    let fields: Record<string, string> | null = null;
    try {
      const j = await res.json();
      msg = j.message ?? msg;
      fields = j.fields ?? null;
    } catch {
      /* not JSON */
    }
    if (res.status === 401 && !url.startsWith('/api/auth/')) unauthorizedHandler?.();
    if (res.status === 403) msg = msg || 'You are not allowed to do this.';
    throw new ApiError(res.status, msg, fields);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  get: <T>(url: string) => request<T>('GET', url),
  post: <T>(url: string, body?: unknown) => request<T>('POST', url, body ?? {}),
  put: <T>(url: string, body?: unknown) => request<T>('PUT', url, body ?? {}),
  del: <T>(url: string) => request<T>('DELETE', url),
};

/** POST that returns a file (XLSX export); triggers a browser download. */
export async function download(url: string, body: unknown, fallbackName: string) {
  await ensureCsrf();
  const res = await fetch(url, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken() ?? '' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let msg = 'Download failed';
    try {
      msg = (await res.json()).message ?? msg;
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, msg);
  }
  const cd = res.headers.get('Content-Disposition') ?? '';
  const name = /filename="?([^";]+)"?/.exec(cd)?.[1] ?? fallbackName;
  const blob = await res.blob();
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}
