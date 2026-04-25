import { useMemo, useState } from "react";
import { ENDPOINTS } from "../config/endpoints";
import { EndpointRunnerCard } from "./EndpointRunnerCard";

const serviceLabel: Record<string, string> = {
  videominer: "VideoMiner",
  vimeominer: "VimeoMiner",
  youtubeminer: "YouTubeMiner",
};

export function EndpointStudioPage() {
  const [selectedId, setSelectedId] = useState<string>(ENDPOINTS[0]?.id ?? "");
  const [search, setSearch] = useState("");

  const selectedEndpoint = useMemo(
    () => ENDPOINTS.find((endpoint) => endpoint.id === selectedId) ?? ENDPOINTS[0] ?? null,
    [selectedId],
  );

  const visibleEndpoints = useMemo(() => {
    const needle = search.trim().toLowerCase();
    if (!needle) return ENDPOINTS;

    return ENDPOINTS.filter((endpoint) =>
      [endpoint.label, endpoint.description, endpoint.pathTemplate, endpoint.method]
        .join(" ")
        .toLowerCase()
        .includes(needle),
    );
  }, [search]);

  return (
    <div className="page page--split">
      <section className="panel panel--sticky">
        <h2>Endpoint Catalog</h2>
        <p className="muted">
          Select any public endpoint from your ecosystem and execute it with generated
          path/query/body controls.
        </p>
        <label>
          <span>Search endpoints</span>
          <input
            value={search}
            placeholder="method, path or description"
            onChange={(event) => setSearch(event.target.value)}
          />
        </label>
        <div className="endpoint-list">
          {visibleEndpoints.map((endpoint) => (
            <button
              type="button"
              key={endpoint.id}
              className={endpoint.id === selectedId ? "endpoint-item endpoint-item--active" : "endpoint-item"}
              onClick={() => setSelectedId(endpoint.id)}
            >
              <span className={`pill method method--${endpoint.method.toLowerCase()}`}>
                {endpoint.method}
              </span>
              <span className="endpoint-item__meta">
                <strong>{endpoint.label}</strong>
                <small>
                  {serviceLabel[endpoint.service]} · {endpoint.pathTemplate}
                </small>
              </span>
            </button>
          ))}
        </div>
      </section>

      <section className="page__main">
        {selectedEndpoint ? (
          <EndpointRunnerCard endpoint={selectedEndpoint} />
        ) : (
          <section className="panel">
            <h3>No endpoints configured</h3>
            <p className="muted">The endpoint catalog is empty.</p>
          </section>
        )}
      </section>
    </div>
  );
}
