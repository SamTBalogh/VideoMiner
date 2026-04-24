import { ENDPOINTS_BY_ID } from "../config/endpoints";
import { EndpointRunnerCard } from "./EndpointRunnerCard";

const publishEndpointIds = [
  "vimeo-post-channel",
  "youtube-v1-post-channel",
  "youtube-v1-post-channels",
  "youtube-v2-post-channel",
  "youtube-v2-post-channels",
] as const;

const previewEndpointIds = [
  "vimeo-get-channel",
  "youtube-v1-get-channel",
  "youtube-v1-get-channels",
  "youtube-v2-get-channel",
  "youtube-v2-get-channels",
] as const;

function getEndpoint(endpointId: string) {
  const endpoint = ENDPOINTS_BY_ID[endpointId];
  if (!endpoint) {
    throw new Error(`Endpoint not found: ${endpointId}`);
  }
  return endpoint;
}

export function IngestionPage() {
  return (
    <div className="page">
      <section className="panel">
        <h2>Fetch and Store Data</h2>
        <p className="muted">
          These operations call Vimeo/YouTube APIs through their microservices and publish
          the resulting channel data to VideoMiner.
        </p>
      </section>

      <section className="card-grid">
        {publishEndpointIds.map((endpointId) => (
          <EndpointRunnerCard key={endpointId} endpoint={getEndpoint(endpointId)} />
        ))}
      </section>

      <section className="panel">
        <h2>Preview Ingestion Data</h2>
        <p className="muted">
          These operations assemble data from Vimeo/YouTube but do not store it. Use them to
          validate IDs, limits, and payload shape before publishing.
        </p>
      </section>

      <section className="card-grid">
        {previewEndpointIds.map((endpointId) => (
          <EndpointRunnerCard key={endpointId} endpoint={getEndpoint(endpointId)} compact />
        ))}
      </section>
    </div>
  );
}
