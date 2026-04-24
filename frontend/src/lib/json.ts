export function safeJsonParse(value: string): unknown {
  if (!value.trim()) return null;
  return JSON.parse(value);
}

export function prettyJson(value: unknown): string {
  if (typeof value === "string") return value;
  return JSON.stringify(value, null, 2);
}
