import type { FieldErrorItem, ProblemBody } from "@/types/api";

/** A failed API call. `message` comes from the server and is written to be shown to staff as-is. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly fieldErrors: FieldErrorItem[];
  readonly requestId?: string;
  readonly details: Record<string, unknown>;

  constructor(problem: ProblemBody) {
    super(problem.message);
    this.name = "ApiError";
    this.status = problem.status;
    this.code = problem.code;
    this.fieldErrors = problem.fieldErrors ?? [];
    this.requestId = problem.requestId;
    this.details = problem.details ?? {};
  }

  static network(): ApiError {
    return new ApiError({
      status: 0,
      code: "NETWORK_ERROR",
      message: "Can't reach the Folio server. Check your connection and try again.",
    });
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function errorMessage(error: unknown): string {
  return isApiError(error) ? error.message : "Something went wrong. Try again in a moment.";
}

function isProblemBody(value: unknown): value is ProblemBody {
  return typeof value === "object" && value !== null && typeof (value as ProblemBody).code === "string";
}

export async function toApiError(response: Response): Promise<ApiError> {
  try {
    const body: unknown = await response.json();
    if (isProblemBody(body)) {
      return new ApiError({ ...body, status: response.status });
    }
  } catch {
    // Non-JSON body, e.g. a proxy error page while the API is waking up.
  }
  const unavailable = response.status === 502 || response.status === 503 || response.status === 504;
  return new ApiError({
    status: response.status,
    code: unavailable ? "SERVICE_UNAVAILABLE" : response.status >= 500 ? "INTERNAL_ERROR" : `HTTP_${response.status}`,
    message: unavailable
      ? "The server is starting up. Give it a few seconds and try again."
      : response.status >= 500
        ? "The server had a problem. Try again in a moment."
        : "The request could not be completed.",
    requestId: response.headers.get("X-Request-Id") ?? undefined,
  });
}
