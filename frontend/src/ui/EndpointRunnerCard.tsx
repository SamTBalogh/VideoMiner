import { useEffect, useMemo, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { executeEndpoint } from "../lib/http";
import { useApiSettings } from "../context/ApiSettingsContext";
import type { ApiExecutionResult, EndpointDefinition } from "../types";

function getInitialPathValues(endpoint: EndpointDefinition) {
  return Object.fromEntries((endpoint.pathParams ?? []).map((name) => [name, ""]));
}

function getInitialQueryValues(endpoint: EndpointDefinition) {
  return Object.fromEntries(
    (endpoint.queryParams ?? []).map((param) => [param.name, param.defaultValue ?? ""]),
  );
}

interface EndpointRunnerCardProps {
  endpoint: EndpointDefinition;
  title?: string;
  compact?: boolean;
  onResult?: (result: ApiExecutionResult) => void;
}

export function EndpointRunnerCard({
  endpoint,
  title,
  compact = false,
  onResult,
}: EndpointRunnerCardProps) {
  const { settings } = useApiSettings();
  const [pathValues, setPathValues] = useState<Record<string, string>>(() =>
    getInitialPathValues(endpoint),
  );
  const [queryValues, setQueryValues] = useState<Record<string, string>>(() =>
    getInitialQueryValues(endpoint),
  );
  const [bodyText, setBodyText] = useState(endpoint.bodyTemplate ?? "");
  const [sendOptionalAuth, setSendOptionalAuth] = useState(true);

  useEffect(() => {
    setPathValues(getInitialPathValues(endpoint));
    setQueryValues(getInitialQueryValues(endpoint));
    setBodyText(endpoint.bodyTemplate ?? "");
    setSendOptionalAuth(true);
  }, [endpoint]);

  const mutation = useMutation({
    mutationFn: async () => {
      const result = await executeEndpoint({
        endpoint,
        settings,
        pathValues,
        queryValues,
        bodyText,
        sendOptionalAuth,
      });
      onResult?.(result);
      return result;
    },
  });

  const canSubmit = useMemo(() => {
    if (mutation.isPending) return false;
    if (endpoint.authMode !== "required") return true;
    return settings.token.trim().length > 0;
  }, [endpoint.authMode, mutation.isPending, settings.token]);

  const response = mutation.data;
  const responseClass = response?.ok ? "status status--ok" : "status status--error";

  return (
    <section className={compact ? "panel panel--compact" : "panel"}>
      <div className="panel__header">
        <h3>{title ?? endpoint.label}</h3>
        <span className={`pill method method--${endpoint.method.toLowerCase()}`}>
          {endpoint.method}
        </span>
      </div>
      <p className="muted">{endpoint.description}</p>
      <code className="endpoint-path">{endpoint.pathTemplate}</code>

      {(endpoint.pathParams ?? []).length > 0 && (
        <div className="field-grid">
          {(endpoint.pathParams ?? []).map((param) => (
            <label key={param}>
              <span>Path: {param}</span>
              <input
                value={pathValues[param] ?? ""}
                placeholder={`{${param}}`}
                onChange={(event) =>
                  setPathValues((current) => ({
                    ...current,
                    [param]: event.target.value,
                  }))
                }
              />
            </label>
          ))}
        </div>
      )}

      {(endpoint.queryParams ?? []).length > 0 && (
        <div className="field-grid">
          {(endpoint.queryParams ?? []).map((param) => (
            <label key={param.name}>
              <span>Query: {param.name}</span>
              <input
                value={queryValues[param.name] ?? ""}
                placeholder={param.placeholder ?? ""}
                onChange={(event) =>
                  setQueryValues((current) => ({
                    ...current,
                    [param.name]: event.target.value,
                  }))
                }
              />
            </label>
          ))}
        </div>
      )}

      {(endpoint.method === "POST" || endpoint.method === "PUT") && (
        <label>
          <span>Request Body (JSON)</span>
          <textarea
            rows={10}
            value={bodyText}
            onChange={(event) => setBodyText(event.target.value)}
          />
        </label>
      )}

      {endpoint.authMode === "optional" && (
        <label className="checkbox">
          <input
            type="checkbox"
            checked={sendOptionalAuth}
            onChange={(event) => setSendOptionalAuth(event.target.checked)}
          />
          <span>Send Authorization token (if present)</span>
        </label>
      )}

      {endpoint.authMode === "required" && !settings.token.trim() && (
        <div className="warning">
          This endpoint requires a token. Set it in Connection Settings.
        </div>
      )}

      <div className="actions">
        <button type="button" className="button" onClick={() => mutation.mutate()} disabled={!canSubmit}>
          {mutation.isPending ? "Running..." : "Run Request"}
        </button>
      </div>

      {mutation.isError && (
        <pre className="response response--error">
          {mutation.error instanceof Error ? mutation.error.message : "Unknown error"}
        </pre>
      )}

      {response && (
        <div className="runner-output">
          <div className={responseClass}>
            {response.status} {response.statusText} ({Math.round(response.durationMs)} ms)
          </div>
          <pre className="response">{JSON.stringify(response.body ?? response.rawBody, null, 2)}</pre>
        </div>
      )}
    </section>
  );
}
