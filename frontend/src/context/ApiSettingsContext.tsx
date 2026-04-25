import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";
import { DEFAULT_BASE_URLS } from "../config/endpoints";
import type { ApiSettings, ServiceId } from "../types";

interface ApiSettingsContextValue {
  settings: ApiSettings;
  setToken: (token: string) => void;
  setManagementKey: (managementKey: string) => void;
  setBaseUrl: (service: ServiceId, value: string) => void;
  resetBaseUrls: () => void;
}

const LEGACY_SETTINGS_STORAGE_KEY = "videominer.frontend.settings";
const BASE_URLS_STORAGE_KEY = "videominer.frontend.baseUrls";
const TOKEN_SESSION_STORAGE_KEY = "videominer.frontend.token";
const MANAGEMENT_KEY_SESSION_STORAGE_KEY = "videominer.frontend.managementKey";

const defaultSettings: ApiSettings = {
  token: "",
  managementKey: "",
  baseUrls: { ...DEFAULT_BASE_URLS },
};

const ApiSettingsContext = createContext<ApiSettingsContextValue | null>(null);

function readSessionValue(key: string) {
  const value = sessionStorage.getItem(key);
  return typeof value === "string" ? value : "";
}

function readBaseUrlsFromStorage() {
  const storedBaseUrls = localStorage.getItem(BASE_URLS_STORAGE_KEY);
  if (storedBaseUrls) {
    try {
      const parsed = JSON.parse(storedBaseUrls) as Partial<Record<ServiceId, string>>;
      return {
        ...DEFAULT_BASE_URLS,
        ...parsed,
      };
    } catch {
      return { ...DEFAULT_BASE_URLS };
    }
  }

  const legacyStored = localStorage.getItem(LEGACY_SETTINGS_STORAGE_KEY);
  if (!legacyStored) {
    return { ...DEFAULT_BASE_URLS };
  }

  try {
    const parsed = JSON.parse(legacyStored) as Partial<ApiSettings>;
    return {
      ...DEFAULT_BASE_URLS,
      ...(parsed.baseUrls ?? {}),
    };
  } catch {
    return { ...DEFAULT_BASE_URLS };
  }
}

function readSettingsFromStorage(): ApiSettings {
  return {
    token: readSessionValue(TOKEN_SESSION_STORAGE_KEY),
    managementKey: readSessionValue(MANAGEMENT_KEY_SESSION_STORAGE_KEY),
    baseUrls: readBaseUrlsFromStorage(),
  };
}

function writeSessionValue(key: string, value: string) {
  if (value.trim()) {
    sessionStorage.setItem(key, value);
    return;
  }
  sessionStorage.removeItem(key);
}

export function ApiSettingsProvider({ children }: PropsWithChildren) {
  const [settings, setSettings] = useState<ApiSettings>(() =>
    readSettingsFromStorage(),
  );

  useEffect(() => {
    localStorage.setItem(BASE_URLS_STORAGE_KEY, JSON.stringify(settings.baseUrls));
  }, [settings.baseUrls]);

  useEffect(() => {
    writeSessionValue(TOKEN_SESSION_STORAGE_KEY, settings.token);
  }, [settings.token]);

  useEffect(() => {
    writeSessionValue(MANAGEMENT_KEY_SESSION_STORAGE_KEY, settings.managementKey);
  }, [settings.managementKey]);

  useEffect(() => {
    localStorage.removeItem(LEGACY_SETTINGS_STORAGE_KEY);
  }, []);

  const value = useMemo<ApiSettingsContextValue>(
    () => ({
      settings,
      setToken: (token) =>
        setSettings((current) => ({
          ...current,
          token,
        })),
      setManagementKey: (managementKey) =>
        setSettings((current) => ({
          ...current,
          managementKey,
        })),
      setBaseUrl: (service, value) =>
        setSettings((current) => ({
          ...current,
          baseUrls: {
            ...current.baseUrls,
            [service]: value.trim(),
          },
        })),
      resetBaseUrls: () =>
        setSettings((current) => ({
          ...current,
          baseUrls: { ...DEFAULT_BASE_URLS },
        })),
    }),
    [settings],
  );

  return (
    <ApiSettingsContext.Provider value={value}>
      {children}
    </ApiSettingsContext.Provider>
  );
}

export function useApiSettings() {
  const context = useContext(ApiSettingsContext);
  if (!context) {
    throw new Error("useApiSettings must be used inside ApiSettingsProvider");
  }
  return context;
}
