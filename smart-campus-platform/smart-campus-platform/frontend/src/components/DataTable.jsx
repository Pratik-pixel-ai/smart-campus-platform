import { ChevronLeft, ChevronRight, ChevronsUpDown, Search } from 'lucide-react';
import LoadingSpinner from './LoadingSpinner.jsx';
import EmptyState from './EmptyState.jsx';
import ErrorState from './ErrorState.jsx';

/**
 * Table with an optional search box, sortable headers and pagination.
 *
 * Searching, sorting and paging are all done by the backend: this component only
 * reports what the user clicked. That keeps the browser from downloading every row
 * just to show ten of them.
 */
export default function DataTable({
  columns,
  rows = [],
  loading,
  error,
  onRetry,
  emptyTitle = 'No records found',
  emptyMessage,
  search,
  onSearchChange,
  searchPlaceholder = 'Search...',
  sort,
  onSortChange,
  page = 0,
  totalPages = 1,
  totalElements,
  onPageChange,
  toolbar,
}) {
  const toggleSort = (key) => {
    if (!onSortChange) return;
    const [currentKey, currentDirection] = (sort || '').split(',');
    const direction = currentKey === key && currentDirection === 'asc' ? 'desc' : 'asc';
    onSortChange(`${key},${direction}`);
  };

  return (
    <div className="card overflow-hidden">
      {(onSearchChange || toolbar) && (
        <div className="flex flex-col gap-3 border-b border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between">
          {onSearchChange ? (
            <div className="relative w-full sm:max-w-xs">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                className="field pl-9"
                placeholder={searchPlaceholder}
                value={search}
                onChange={(event) => onSearchChange(event.target.value)}
                aria-label="Search"
              />
            </div>
          ) : (
            <span />
          )}
          {toolbar}
        </div>
      )}

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={onRetry} />}
      {!loading && !error && rows.length === 0 && <EmptyState title={emptyTitle} message={emptyMessage} />}

      {!loading && !error && rows.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px]">
            <thead className="bg-slate-50">
              <tr>
                {columns.map((column) => (
                  <th key={column.key} className="table-head">
                    {column.sortable && onSortChange ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 hover:text-slate-700"
                        onClick={() => toggleSort(column.sortKey || column.key)}
                      >
                        {column.header}
                        <ChevronsUpDown size={13} />
                      </button>
                    ) : (
                      column.header
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {rows.map((row, index) => (
                <tr key={row.id ?? index} className="hover:bg-slate-50">
                  {columns.map((column) => (
                    <td key={column.key} className="table-cell">
                      {column.render ? column.render(row) : row[column.key]}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {onPageChange && totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-slate-200 px-4 py-3">
          <p className="text-xs text-slate-500">
            Page {page + 1} of {totalPages}
            {typeof totalElements === 'number' ? ` · ${totalElements} records` : ''}
          </p>
          <div className="flex gap-1">
            <button
              type="button"
              className="rounded-lg border border-slate-300 p-1.5 text-slate-600 disabled:opacity-40"
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
              aria-label="Previous page"
            >
              <ChevronLeft size={16} />
            </button>
            <button
              type="button"
              className="rounded-lg border border-slate-300 p-1.5 text-slate-600 disabled:opacity-40"
              onClick={() => onPageChange(page + 1)}
              disabled={page + 1 >= totalPages}
              aria-label="Next page"
            >
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
