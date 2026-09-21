import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import EmptyState from './EmptyState.jsx';

const COLORS = ['#059669', '#0ea5e9', '#f59e0b', '#ef4444', '#8b5cf6', '#14b8a6'];

/**
 * Thin wrapper over recharts so every chart on the platform gets the same card,
 * title, height and colour palette.
 */
export default function ChartCard({ title, subtitle, type = 'bar', data = [], xKey, yKey, height = 260 }) {
  const hasData = Array.isArray(data) && data.length > 0;

  return (
    <div className="card p-5">
      <div className="mb-4">
        <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
        {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
      </div>

      {!hasData ? (
        <EmptyState title="No data to chart yet" message="Records will appear here once activity starts." />
      ) : (
        <ResponsiveContainer width="100%" height={height}>
          {type === 'line' ? (
            <LineChart data={data} margin={{ top: 5, right: 10, bottom: 5, left: -20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey={xKey} tick={{ fontSize: 12, fill: '#64748b' }} />
              <YAxis tick={{ fontSize: 12, fill: '#64748b' }} />
              <Tooltip />
              <Line type="monotone" dataKey={yKey} stroke="#059669" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          ) : type === 'pie' ? (
            <PieChart>
              <Pie data={data} dataKey={yKey} nameKey={xKey} innerRadius={50} outerRadius={85} paddingAngle={2}>
                {data.map((entry, index) => (
                  <Cell key={entry[xKey]} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend wrapperStyle={{ fontSize: 12 }} />
            </PieChart>
          ) : (
            <BarChart data={data} margin={{ top: 5, right: 10, bottom: 5, left: -20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis dataKey={xKey} tick={{ fontSize: 12, fill: '#64748b' }} />
              <YAxis tick={{ fontSize: 12, fill: '#64748b' }} />
              <Tooltip cursor={{ fill: '#f1f5f9' }} />
              <Bar dataKey={yKey} fill="#059669" radius={[4, 4, 0, 0]} maxBarSize={48} />
            </BarChart>
          )}
        </ResponsiveContainer>
      )}
    </div>
  );
}
