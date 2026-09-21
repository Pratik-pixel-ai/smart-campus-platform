import { useState } from 'react';

export default function Select({
  label,
  error,
  id,
  options = [],
  placeholder,
  className = '',
  onInvalid,
  onChange,
  ...props
}) {
  const selectId = id || props.name;
  // The browser's native "Please select an item in the list" bubble looks inconsistent
  // with the rest of the app, so we suppress it and fall back to the same inline
  // red-text error style Input already uses.
  const [nativeInvalid, setNativeInvalid] = useState(false);

  const handleInvalid = (event) => {
    event.preventDefault();
    setNativeInvalid(true);
    onInvalid?.(event);
  };

  const handleChange = (event) => {
    if (nativeInvalid) setNativeInvalid(false);
    onChange?.(event);
  };

  const shownError = error || (nativeInvalid ? 'Please select an option.' : '');

  return (
    <div className={className}>
      {label && (
        <label className="label" htmlFor={selectId}>
          {label}
        </label>
      )}
      <select
        id={selectId}
        className={`field ${shownError ? 'border-red-400 focus:border-red-500 focus:ring-red-500' : ''}`}
        onInvalid={handleInvalid}
        onChange={handleChange}
        {...props}
      >
        {/* disabled: the placeholder is instructional text, not a real choice the user can submit */}
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {shownError && <p className="mt-1 text-xs text-red-600">{shownError}</p>}
    </div>
  );
}
