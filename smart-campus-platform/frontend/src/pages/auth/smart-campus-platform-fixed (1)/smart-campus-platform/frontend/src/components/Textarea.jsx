export default function Textarea({ label, error, id, className = '', ...props }) {
  const areaId = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label className="label" htmlFor={areaId}>
          {label}
        </label>
      )}
      <textarea id={areaId} rows={4} className="field" {...props} />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
