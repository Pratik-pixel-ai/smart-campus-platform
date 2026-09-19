export default function Select({ label, error, id, options = [], placeholder, className = '', ...props }) {
  const selectId = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label className="label" htmlFor={selectId}>
          {label}
        </label>
      )}
      <select id={selectId} className="field" {...props}>
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
