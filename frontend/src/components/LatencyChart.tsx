import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

interface Props {
  data: Array<{ time: number; latency: number }>
}

export default function LatencyChart({ data }: Props) {
  const chartData = data.map(d => ({
    time: new Date(d.time).toLocaleTimeString(),
    latency: Math.round(d.latency * 100) / 100
  }))

  return (
    <div className="bg-slate-800 rounded-lg p-6 border border-slate-700">
      <h2 className="text-xl font-bold mb-4 text-white">Latency Monitor</h2>
      
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis 
            dataKey="time" 
            stroke="#94a3b8"
            style={{ fontSize: '12px' }}
          />
          <YAxis 
            stroke="#94a3b8"
            style={{ fontSize: '12px' }}
            label={{ value: 'Latency (µs)', angle: -90, position: 'insideLeft', fill: '#94a3b8' }}
          />
          <Tooltip 
            contentStyle={{ 
              backgroundColor: '#1e293b', 
              border: '1px solid #334155',
              borderRadius: '8px',
              color: '#e2e8f0'
            }}
          />
          <Line 
            type="monotone" 
            dataKey="latency" 
            stroke="#8b5cf6" 
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>

      <div className="mt-4 text-center text-sm text-slate-400">
        Real-time order matching latency (last 30 trades)
      </div>
    </div>
  )
}
