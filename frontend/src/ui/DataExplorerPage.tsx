import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ENDPOINTS_BY_ID } from "../config/endpoints";
import { useApiSettings } from "../context/ApiSettingsContext";
import { executeEndpoint } from "../lib/http";

type EntityKey = "channels" | "videos" | "comments" | "captions" | "users";

interface EntityConfig {
  endpointId: string;
  title: string;
  filters: string[];
  columns: string[];
}

const DEFAULT_PAGE = "0";
const DEFAULT_SIZE = "10";

const entities: Record<EntityKey, EntityConfig> = {
  channels: {
    endpointId: "vm-list-channels",
    title: "Channels",
    filters: ["id", "name", "description", "createdTime"],
    columns: ["id", "name", "createdTime", "description"],
  },
  videos: {
    endpointId: "vm-list-videos",
    title: "Videos",
    filters: ["id", "name", "description", "releaseTime"],
    columns: ["id", "name", "releaseTime", "description"],
  },
  comments: {
    endpointId: "vm-list-comments",
    title: "Comments",
    filters: ["id", "text", "createdOn"],
    columns: ["id", "createdOn", "text", "author.name"],
  },
  captions: {
    endpointId: "vm-list-captions",
    title: "Captions",
    filters: ["id", "name", "language"],
    columns: ["id", "name", "language"],
  },
  users: {
    endpointId: "vm-list-users",
    title: "Users",
    filters: ["id", "name", "userLink", "pictureLink"],
    columns: ["id", "name", "user_link", "picture_link"],
  },
};

function firstFilter(entityKey: EntityKey) {
  return entities[entityKey].filters[0] ?? "id";
}

function getEndpoint(endpointId: string) {
  const endpoint = ENDPOINTS_BY_ID[endpointId];
  if (!endpoint) throw new Error(`Endpoint not found: ${endpointId}`);
  return endpoint;
}

function pickValue(record: unknown, path: string): string {
  if (record == null || typeof record !== "object") return "";
  const parts = path.split(".");
  let current: unknown = record;
  for (const part of parts) {
    if (current == null || typeof current !== "object") return "";
    current = (current as Record<string, unknown>)[part];
  }
  if (current == null) return "";
  if (typeof current === "string" || typeof current === "number" || typeof current === "boolean") {
    return String(current);
  }
  return JSON.stringify(current);
}

function buildRowKey(row: unknown, entityKey: EntityKey, columns: string[]) {
  const id = pickValue(row, "id");
  if (id) return `${entityKey}:${id}`;

  const fingerprint = columns.map((column) => pickValue(row, column)).join("|");
  return `${entityKey}:${fingerprint || JSON.stringify(row)}`;
}

function normalizeIntegerInput(value: string, minimum: number, fallback: number) {
  const parsed = Number.parseInt(value, 10);
  if (Number.isNaN(parsed)) return String(fallback);
  return String(Math.max(minimum, parsed));
}

export function DataExplorerPage() {
  const { settings } = useApiSettings();
  const [entityKey, setEntityKey] = useState<EntityKey>("channels");
  const [page, setPage] = useState(DEFAULT_PAGE);
  const [size, setSize] = useState(DEFAULT_SIZE);
  const [order, setOrder] = useState("");
  const [filterField, setFilterField] = useState<string>(firstFilter("channels"));
  const [filterValue, setFilterValue] = useState("");
  const [selectedRow, setSelectedRow] = useState<unknown>(null);

  const entity = entities[entityKey];
  const endpoint = getEndpoint(entity.endpointId);
  const endpointBaseUrl = settings.baseUrls[endpoint.service];
  const normalizedPage = useMemo(() => normalizeIntegerInput(page, 0, 0), [page]);
  const normalizedSize = useMemo(() => normalizeIntegerInput(size, 1, 10), [size]);

  const query = useQuery({
    queryKey: [
      "explorer",
      entityKey,
      endpoint.service,
      endpointBaseUrl,
      normalizedPage,
      normalizedSize,
      order,
      filterField,
      filterValue,
      settings.token,
    ],
    queryFn: async () => {
      const queryValues: Record<string, string> = {
        page: normalizedPage,
        size: normalizedSize,
      };
      if (order.trim()) queryValues.order = order.trim();
      if (filterValue.trim()) queryValues[filterField] = filterValue.trim();

      const result = await executeEndpoint({
        endpoint,
        settings,
        queryValues,
      });

      if (!result.ok) {
        throw new Error(
          `Request failed (${result.status} ${result.statusText})\n${result.rawBody || "No response body"}`,
        );
      }

      if (!Array.isArray(result.body)) {
        throw new Error("Expected an array in the response body.");
      }

      return result.body as unknown[];
    },
  });

  const rows = query.data ?? [];

  const columnHeaders = useMemo(
    () => entity.columns.map((column) => column.replaceAll(".", " · ")),
    [entity.columns],
  );

  return (
    <div className="page">
      <section className="panel">
        <h2>Storage Data Explorer</h2>
        <p className="muted">
          Browse stored entities with pagination, sorting, and one-field filtering (matching
          VideoMiner API behavior).
        </p>
        <div className="field-grid field-grid--tight">
          <label>
            <span>Entity</span>
            <select
              value={entityKey}
              onChange={(event) => {
                const nextKey = event.target.value as EntityKey;
                setEntityKey(nextKey);
                setFilterField(firstFilter(nextKey));
                setFilterValue("");
                setSelectedRow(null);
              }}
            >
              {Object.entries(entities).map(([key, config]) => (
                <option key={key} value={key}>
                  {config.title}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>Page</span>
            <input
              type="number"
              min={0}
              step={1}
              inputMode="numeric"
              value={page}
              onChange={(event) => setPage(event.target.value)}
              onBlur={() => setPage(normalizedPage)}
            />
          </label>
          <label>
            <span>Size</span>
            <input
              type="number"
              min={1}
              step={1}
              inputMode="numeric"
              value={size}
              onChange={(event) => setSize(event.target.value)}
              onBlur={() => setSize(normalizedSize)}
            />
          </label>
          <label>
            <span>Order</span>
            <input
              value={order}
              placeholder="-createdTime"
              onChange={(event) => setOrder(event.target.value)}
            />
          </label>
          <label>
            <span>Filter Field</span>
            <select value={filterField} onChange={(event) => setFilterField(event.target.value)}>
              {entity.filters.map((field) => (
                <option key={field} value={field}>
                  {field}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>Filter Value</span>
            <input
              value={filterValue}
              placeholder="Optional"
              onChange={(event) => setFilterValue(event.target.value)}
            />
          </label>
        </div>
      </section>

      <section className="panel">
        {query.isLoading && <div className="muted">Loading...</div>}
        {query.isError && (
          <pre className="response response--error">
            {query.error instanceof Error ? query.error.message : "Unknown error"}
          </pre>
        )}
        {!query.isLoading && !query.isError && (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    {columnHeaders.map((column) => (
                      <th key={column}>{column}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {rows.length === 0 && (
                    <tr>
                      <td colSpan={columnHeaders.length}>No rows found.</td>
                    </tr>
                  )}
                  {rows.map((row) => (
                    <tr
                      key={buildRowKey(row, entityKey, entity.columns)}
                      onClick={() => setSelectedRow(row)}
                      className={selectedRow === row ? "table-row--active" : ""}
                    >
                      {entity.columns.map((column) => (
                        <td key={column}>{pickValue(row, column)}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="muted">Rows: {rows.length}</p>
          </>
        )}
      </section>

      <section className="panel panel--compact">
        <h3>Selected Row</h3>
        <pre className="response">{JSON.stringify(selectedRow, null, 2)}</pre>
      </section>
    </div>
  );
}
