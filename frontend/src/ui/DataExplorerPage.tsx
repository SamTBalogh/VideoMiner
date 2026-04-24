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

export function DataExplorerPage() {
  const { settings } = useApiSettings();
  const [entityKey, setEntityKey] = useState<EntityKey>("channels");
  const [page, setPage] = useState("0");
  const [size, setSize] = useState("10");
  const [order, setOrder] = useState("");
  const [filterField, setFilterField] = useState<string>(firstFilter("channels"));
  const [filterValue, setFilterValue] = useState("");
  const [selectedRow, setSelectedRow] = useState<unknown>(null);

  const entity = entities[entityKey];

  const query = useQuery({
    queryKey: ["explorer", entityKey, page, size, order, filterField, filterValue, settings.token],
    queryFn: async () => {
      const queryValues: Record<string, string> = { page, size };
      if (order.trim()) queryValues.order = order.trim();
      if (filterValue.trim()) queryValues[filterField] = filterValue.trim();

      const result = await executeEndpoint({
        endpoint: getEndpoint(entity.endpointId),
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
            <input value={page} onChange={(event) => setPage(event.target.value)} />
          </label>
          <label>
            <span>Size</span>
            <input value={size} onChange={(event) => setSize(event.target.value)} />
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
                  {rows.map((row, index) => (
                    <tr
                      key={index}
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
