import { NavLink, Outlet } from "react-router-dom";
import { useApiSettings } from "../context/ApiSettingsContext";

const links = [
  { to: "/", label: "Ingestion" },
  { to: "/studio", label: "Endpoint Studio" },
  { to: "/explorer", label: "Data Explorer" },
];

export function AppLayout() {
  const { settings, setToken, setManagementKey, setBaseUrl, resetBaseUrls } = useApiSettings();

  return (
    <div className="app-shell">
      <header className="hero">
        <div className="hero__meta">
          <h1>VideoMiner Console</h1>
          <p>
            One lightweight frontend to ingest data from Vimeo/YouTube and operate
            VideoMiner storage endpoints.
          </p>
        </div>
        <nav className="tabs">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === "/"}
              className={({ isActive }) =>
                isActive ? "tabs__item tabs__item--active" : "tabs__item"
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <section className="panel">
        <div className="panel__header">
          <h2>Connection Settings</h2>
          <button type="button" className="button button--ghost" onClick={resetBaseUrls}>
            Reset URLs
          </button>
        </div>
        <div className="settings-grid">
          <label>
            <span>Authorization Token</span>
            <input
              placeholder="Bearer <token>"
              value={settings.token}
              onChange={(event) => setToken(event.target.value)}
            />
          </label>
          <label>
            <span>Token Management Key</span>
            <input
              placeholder="X-Token-Management-Key"
              value={settings.managementKey}
              onChange={(event) => setManagementKey(event.target.value)}
            />
          </label>
          <label>
            <span>VideoMiner Base URL</span>
            <input
              value={settings.baseUrls.videominer}
              onChange={(event) => setBaseUrl("videominer", event.target.value)}
            />
          </label>
          <label>
            <span>VimeoMiner Base URL</span>
            <input
              value={settings.baseUrls.vimeominer}
              onChange={(event) => setBaseUrl("vimeominer", event.target.value)}
            />
          </label>
          <label>
            <span>YouTubeMiner Base URL</span>
            <input
              value={settings.baseUrls.youtubeminer}
              onChange={(event) => setBaseUrl("youtubeminer", event.target.value)}
            />
          </label>
        </div>
      </section>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
