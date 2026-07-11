interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-4 text-sm text-rose-800">
      <p>{message}</p>
      {onRetry ? (
        <button className="mt-3 rounded-md bg-rose-700 px-3 py-2 font-semibold text-white focus:outline-none focus:ring-2 focus:ring-rose-500" onClick={onRetry}>
          Thu lai
        </button>
      ) : null}
    </div>
  );
}
