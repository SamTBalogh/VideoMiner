export function safeJsonParse(value: string): unknown | undefined {
  if (!value.trim()) return null;
  try {
    return JSON.parse(value);
  } catch {
    return undefined;
  }
}

export function prettyJson(value: unknown): string {
  if (typeof value === "string") return value;
  return JSON.stringify(value, null, 2);
}
