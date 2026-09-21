export default function Input({ label, error, id, className = '', ...props }) {
  const inputId = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label className="label" htmlFor={inputId}>
          {label}
        </label>
      )}
      <input id={inputId} className="field" {...props} />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
