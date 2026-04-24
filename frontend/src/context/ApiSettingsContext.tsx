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
  setBaseUrl: (service: ServiceId, value: string) => void;
  resetBaseUrls: () => void;
}

const STORAGE_KEY = "videominer.frontend.settings";

const defaultSettings: ApiSettings = {
  token: "",
  baseUrls: DEFAULT_BASE_URLS,
};

const ApiSettingsContext = createContext<ApiSettingsContextValue | null>(null);

function readSettingsFromStorage(): ApiSettings {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) return defaultSettings;

  try {
    const parsed = JSON.parse(stored) as Partial<ApiSettings>;
    return {
      token: typeof parsed.token === "string" ? parsed.token : "",
      baseUrls: {
        ...DEFAULT_BASE_URLS,
        ...(parsed.baseUrls ?? {}),
      },
    };
  } catch {
    return defaultSettings;
  }
}

export function ApiSettingsProvider({ children }: PropsWithChildren) {
  const [settings, setSettings] = useState<ApiSettings>(() =>
    readSettingsFromStorage(),
  );

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
  }, [settings]);

  const value = useMemo<ApiSettingsContextValue>(
    () => ({
      settings,
      setToken: (token) =>
        setSettings((current) => ({
          ...current,
          token,
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
          baseUrls: DEFAULT_BASE_URLS,
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
