import { safeJsonParse } from "./json";
import type { ApiExecutionResult, ApiSettings, EndpointDefinition } from "../types";

interface ExecuteEndpointInput {
  endpoint: EndpointDefinition;
  settings: ApiSettings;
  pathValues?: Record<string, string>;
  queryValues?: Record<string, string>;
  bodyText?: string;
  sendOptionalAuth?: boolean;
}

function assertRequiredPathParams(
  endpoint: EndpointDefinition,
  pathValues: Record<string, string>,
) {
  for (const param of endpoint.pathParams ?? []) {
    const value = pathValues[param]?.trim();
    if (!value) {
      throw new Error(`Missing path parameter: ${param}`);
    }
  }
}

function resolvePathTemplate(
  endpoint: EndpointDefinition,
  pathValues: Record<string, string>,
) {
  let path = endpoint.pathTemplate;
  for (const key of endpoint.pathParams ?? []) {
    const value = pathValues[key]?.trim();
    if (value) {
      path = path.replace(`{${key}}`, encodeURIComponent(value));
    }
  }
  return path;
}

function buildQueryString(queryValues: Record<string, string>) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(queryValues)) {
    const trimmed = value.trim();
    if (trimmed) params.set(key, trimmed);
  }
  const value = params.toString();
  return value ? `?${value}` : "";
}

function normalizeBearerToken(token: string) {
  const trimmed = token.trim();
  if (!trimmed) return "";

  const bearerMatch = /^bearer\s+(.+)$/i.exec(trimmed);
  if (!bearerMatch) return `Bearer ${trimmed}`;

  const bearerValue = bearerMatch[1]?.trim();
  if (!bearerValue) return "";
  return `Bearer ${bearerValue}`;
}

function buildHeaders(input: ExecuteEndpointInput, hasBody: boolean) {
  const headers: Record<string, string> = {
    Accept: "application/json, text/plain, */*",
  };

  if (hasBody) headers["Content-Type"] = "application/json";

  const { endpoint, settings } = input;
  const token = normalizeBearerToken(settings.token);

  if (endpoint.authMode === "required") {
    if (!token) {
      throw new Error(
        "This endpoint requires an Authorization token. Create one and set it in Settings.",
      );
    }
    headers.Authorization = token;
  }

  if (endpoint.authMode === "optional" && input.sendOptionalAuth !== false && token) {
    headers.Authorization = token;
  }

  if (endpoint.requiresManagementKey) {
    const managementKey = settings.managementKey.trim();
    if (!managementKey) {
      throw new Error(
        "This endpoint requires X-Token-Management-Key. Set it in Connection Settings.",
      );
    }
    headers["X-Token-Management-Key"] = managementKey;
  }

  return headers;
}

export async function executeEndpoint(
  input: ExecuteEndpointInput,
): Promise<ApiExecutionResult> {
  const started = performance.now();
  const pathValues = input.pathValues ?? {};
  const queryValues = input.queryValues ?? {};
  const bodyText = input.bodyText?.trim() ?? "";

  assertRequiredPathParams(input.endpoint, pathValues);

  const path = resolvePathTemplate(input.endpoint, pathValues);
  const query = buildQueryString(queryValues);
  const baseUrl = input.settings.baseUrls[input.endpoint.service].trim();

  if (!baseUrl) {
    throw new Error(
      `Missing base URL for service "${input.endpoint.service}". Configure it in Settings.`,
    );
  }

  const url = `${baseUrl}${path}${query}`;
  const hasBody = !!bodyText && input.endpoint.method !== "GET" && input.endpoint.method !== "DELETE";
  const headers = buildHeaders(input, hasBody);

  const response = await fetch(url, {
    method: input.endpoint.method,
    headers,
    body: hasBody ? bodyText : undefined,
  });

  const rawBody = await response.text();
  const durationMs = performance.now() - started;

  let body: unknown = null;
  if (rawBody) {
    try {
      body = safeJsonParse(rawBody);
    } catch {
      body = rawBody;
    }
  }

  return {
    ok: response.ok,
    status: response.status,
    statusText: response.statusText,
    url,
    durationMs,
    headers: Object.fromEntries(response.headers.entries()),
    rawBody,
    body,
  };
}
