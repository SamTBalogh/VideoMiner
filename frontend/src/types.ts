export type ServiceId = "videominer" | "vimeominer" | "youtubeminer";

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

export type AuthMode = "none" | "optional" | "required";

export interface EndpointQueryParam {
  name: string;
  defaultValue?: string;
  placeholder?: string;
}

export interface EndpointDefinition {
  id: string;
  service: ServiceId;
  section: "ingestion" | "storage";
  method: HttpMethod;
  label: string;
  pathTemplate: string;
  description: string;
  pathParams?: string[];
  queryParams?: EndpointQueryParam[];
  authMode: AuthMode;
  requiresManagementKey?: boolean;
  bodyTemplate?: string;
}

export interface ApiSettings {
  token: string;
  managementKey: string;
  baseUrls: Record<ServiceId, string>;
}

export interface ApiExecutionResult {
  ok: boolean;
  status: number;
  statusText: string;
  url: string;
  durationMs: number;
  headers: Record<string, string>;
  rawBody: string;
  body: unknown;
}

export interface VideoMinerUser {
  id?: number;
  name?: string;
  user_link?: string;
  picture_link?: string;
}

export interface VideoMinerComment {
  id: string;
  text?: string;
  createdOn?: string;
  author?: VideoMinerUser;
}

export interface VideoMinerCaption {
  id: string;
  name?: string;
  language?: string;
}

export interface VideoMinerVideo {
  id: string;
  name?: string;
  description?: string;
  releaseTime?: string;
  comments?: VideoMinerComment[];
  captions?: VideoMinerCaption[];
}

export interface VideoMinerChannel {
  id: string;
  name?: string;
  description?: string;
  createdTime?: string;
  videos?: VideoMinerVideo[];
}
